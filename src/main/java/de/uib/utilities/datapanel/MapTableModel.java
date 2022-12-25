/*
 * (c) uib, www.uib.de, 2009-2013, 2022
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.Function;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.DataChangedSubject;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.ListCellOptions;

public class MapTableModel extends javax.swing.table.AbstractTableModel implements DataChangedSubject {

	protected Vector<DataChangedObserver> observers;

	protected Collection updateCollection;
	protected Collection<Map<String, Object>> storeData;
	protected boolean datachanged;

	private List<Object> showOnlyValues; // values set cannot be set for any key
	private java.util.Set<String> keysOfReadOnlyEntries; // keys which identify readonly entries

	private Function<String, Boolean> editDenier;

	public static final List nullLIST = new ArrayList<>();
	static {
		nullLIST.add(null);
	}

	protected Map<String, ListCellOptions> optionsMap;

	private SortedMap<String, Object> data; // shall be sorted
	private Map<String, Object> oridata; // we keep the original data for writing back changed values
	private Map<String, Object> defaultData; // the shadow default values of all data
	private Vector<String> keys;
	private String modifiedKey;
	private int rowModiTime;

	private ListModelProducerForVisualDatamap modelProducer;

	private boolean writeData = true;

	public MapTableModel() {
		observers = new Vector<>();

		/*
		 * editDenier
		 * =
		 * key -> {
		 * logging.info(this, "key denied ?" + key );
		 * Boolean result = key.endsWith( "modified");
		 * if (result)
		 * JOptionPane.showMessageDialog(Globals.mainFrame,
		 * "this entry cannot be edited manually",
		 * "information", JOptionPane.INFORMATION_MESSAGE
		 * );
		 * return result;
		 * }
		 * ;
		 */
	}

	public void setModelProducer(ListModelProducerForVisualDatamap modelProducer) {

		// at 0,0 " + modelProducer.getClass(0,0));
		this.modelProducer = modelProducer;
	}

	public void setOptions(Map<String, ListCellOptions> optionsMap, Map<String, Object> defaultData) {
		this.optionsMap = optionsMap;
		this.defaultData = defaultData;
		defaultData = new HashMap<>();

	}

	public Map<String, ListCellOptions> getOptionsMap() {
		return optionsMap;
	}

	public void setMap(Map<String, Object> data) {

		this.data = null;
		resetModifiedKey();

		if (data != null) {
			Collator myCollator = Collator.getInstance();
			myCollator.setStrength(Collator.PRIMARY);
			this.data = Collections.synchronizedSortedMap(new TreeMap(myCollator));
			this.data.putAll(data);
			keys = new Vector<>(this.data.keySet());

		}
		oridata = data;
	}

	public Map<String, Object> getData() {
		return data;
	}

	private void resetModifiedKey() {
		logging.debug(this, "resetModifiedKey");
		modifiedKey = null;
	}

	private String getModifiedKey() {
		if (modifiedKey != null)
			return modifiedKey;

		for (String key : keys) {
			if (key.startsWith(de.uib.opsidatamodel.permission.UserConfig.CONFIGKEY_STR_USER)
					&& key.endsWith(de.uib.opsidatamodel.permission.UserConfig.MODIFICATION_INFO_KEY)) {
				modifiedKey = key;
				break;
			}
		}
		rowModiTime = keys.indexOf(modifiedKey);

		return modifiedKey;
	}

	/*
	 * private Map<String, Object> getDefaultData()
	 * {
	 * return defaultData;
	 * }
	 */

	@Override
	public String toString() {
		return getClass().getName() + ": " + data;
	}

	private void setNew() {
		datachanged = false; // starting with a new set of data
	}

	public Vector<String> getKeys() {
		return keys;
	}

	/**
	 * set collection (e.g. of clients) where each member stores the changed
	 * data; we assume that it is a collection of maps
	 * 
	 * @param Collection data
	 */
	public void setStoreData(Collection<Map<String, Object>> data) {
		if (data == null)
			logging.debug(this, "setStoreData null ");
		else {

		}

		setNew();
		storeData = data;
		resetModifiedKey();
	}

	public Collection<Map<String, Object>> getStoreData() {
		return storeData;
	}

	/**
	 * take a reference to a collection of maps that we will have to use for
	 * updating the data base
	 * 
	 * @param Collection updateCollection
	 */
	public void setUpdateCollection(Collection updateCollection) {
		this.updateCollection = updateCollection;
	}

	public Collection getUpdateCollection() {
		return updateCollection;
	}

	public void setReadOnlyEntries(java.util.Set<String> keys) {
		keysOfReadOnlyEntries = keys;
	}

	public java.util.Set<String> getReadOnlyEntries(java.util.Set<String> keys) {
		return keysOfReadOnlyEntries;
	}

	public void setEditDenier(Function<String, Boolean> disallow) {
		editDenier = disallow;
	}

	public void setShowOnlyValues(List<Object> showOnly) {
		showOnlyValues = showOnly;
	}

	public List<Object> getShowOnlyValues() {
		return showOnlyValues;
	}

	public void addEntry(String key, Object newval, boolean toStore) {
		data.put(key, newval);
		oridata.put(key, newval);
		logging.debug(this, " keys " + keys);
		keys = new Vector<>(data.keySet());
		logging.debug(this, " new keys  " + keys);
		if (toStore)
			putEntryIntoStoredMaps(key, newval, toStore);
		fireTableDataChanged();
	}

	public void addEntry(String key, Object newval) {
		addEntry(key, newval, true);
	}

	public void addEntry(String key) {
		ArrayList<Object> newval = new ArrayList<>();
		data.put(key, newval);
		oridata.put(key, newval);

		keys = new Vector<>(data.keySet());

		putEntryIntoStoredMaps(key, newval);
		fireTableDataChanged();
	}

	public void removeEntry(String key) {
		data.remove(key);
		oridata.remove(key);

		keys = new Vector<>(data.keySet());

		removeEntryFromStoredMaps(key);
		fireTableDataChanged();

	}

	public boolean hasEntryFor(String key) {
		return data.containsKey(key);
	}

	/*
	 * public void setDataChanged (boolean b)
	 * {
	 * dataChanged = b;
	 * }
	 * 
	 * public boolean isDataChanged ()
	 * {
	 * return dataChanged;
	 * }
	 */

	// table model
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		int result = 0;
		if (data != null)
			result = keys.size();
		return result;
	}

	@Override
	public String getColumnName(int col) {
		String result = "";
		switch (col) {
		case 0:
			result = configed.getResourceValue("EditMapPanel.ColumnHeaderName");
			break;
		case 1:
			result = configed.getResourceValue("EditMapPanel.ColumnHeaderValue");
			break;
		};

		return result;

	}

	@Override
	public Object getValueAt(int row, int col) {
		if (data == null)
			return "";

		String key = null;
		Object result = null;
		try {
			key = keys.get(row);
		} catch (Exception ex) {
			result = "keys " + keys + " row " + row + " : " + ex.toString();
		}

		if (result != null)
			return result;

		switch (col) {
		case 0:
			result = key;
			break;
		case 1:
			result = data.get(key);
			/*
			 * if (key.equals ("test456"))
			 * {
			 * logging.info(this, " result for " + key + ": " + result + " is null  " +
			 * (result == null)
			 * + " class " + result.getClass() + " equals null " + result.equals( null )
			 * + " String value equals \"null\" " + (result.toString()).equals("null"));
			 * if (result != null && result instanceof List)
			 * {
			 * logging.info(this, " result size for " + key + " " +
			 * ((List)(result)).size());
			 * if ( ((List)(result)).size() > 0)
			 * logging.info(this, " result.get(0) for " + key + " is null " +
			 * (((List)(result)).get(0) == null));
			 * }
			 * 
			 * logging.info(this, " optionsMap for " + key + ": " + optionsMap.get(key) );
			 * logging.info(this, " optionsMap for " + key + ": " +
			 * optionsMap.get(key).getDefaultValues() );
			 * }
			 */

			// deliver the default value

			if (result != null && result instanceof List) {
				List li = (List) result;
				if (!li.isEmpty() && li.get(0) == null && optionsMap != null) {
					result = defaultData.get(key);
					logging.info(this,
							"getValueAt " + row + ", " + col + " result corrected for key  " + key + ": " + result);
				}
			}

			break;

		}

		if (result == null)
			return "";

		return result;
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.
	 */
	@Override
	public Class getColumnClass(int c) {
		switch (c) {
		case 0:
			return "".getClass();
		case 1:
			return List.class;
		}
		return Object.class;

	}

	/*
	 * We implement this method since the table is partially
	 * editable.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		// The data/cell address is constant,
		// no matter where the cell appears onscreen.

		if (data == null)
			return false;

		if (col < 1) {
			return false;
		} else {
			if (keysOfReadOnlyEntries != null && keysOfReadOnlyEntries.contains(keys.get(row)) ||

					editDenier != null && editDenier.apply(keys.get(row)))
				return false;
			else
				return true;
		}
	}

	void weHaveChangedStoredMaps() {
		if (!datachanged || updateCollection.isEmpty() // updateCollection has been emptied since last change
		) {
			datachanged = true;
			// tell it to all registered DataChangedObservers
			notifyChange();

			// we add the reference to the changed backend data only once to the
			// updateCollection

			if (updateCollection == null)
				logging.debug(this, "updateCollection null - should not be");
			else
				updateCollection.addAll(storeData);

			logging.debug(this,
					" ---  updateCollection: " + updateCollection + "  has size " + updateCollection.size());
		}
	}

	public void removeEntryFromStoredMaps(String myKey) {
		if (storeData != null) {
			for (Map<String, Object> aStoreMap : storeData) {

				aStoreMap.put(myKey, nullLIST);
			}

			logging.debug(this,
					"remove entry --  updateCollection: " + updateCollection + "  has size " + updateCollection.size());
			weHaveChangedStoredMaps();
		}
	}

	// we put a new entry into each map in the given collection and preserve the
	// change data
	void putEntryIntoStoredMaps(String myKey, Object value) {
		putEntryIntoStoredMaps(myKey, value, true);
	}

	// we put a new entry into each map in the given collection
	void putEntryIntoStoredMaps(String myKey, Object value, boolean toStore) {
		logging.debug(this, "putEntryIntoStoredMaps myKey, value: " + myKey + ", " + value);
		logging.debug(this, "putEntryIntoStoredMaps storeData  counting " + storeData.size());
		if (storeData != null) {
			Iterator it = storeData.iterator();
			while (it.hasNext()) {
				Object aStoreMap = it.next();

				if (!(aStoreMap instanceof Map)) {
					if (aStoreMap == null) {
						logging.info(this, "EditMapPanel.setValueAt: we have some data null ");
					} else {
						logging.error(this,
								"EditMapPanel.setValueAt: backendData " + aStoreMap + " is not null and not a Map ");
					}
				} else {
					((Map) aStoreMap).put(myKey, value);
				}
			}

			// " + updateCollection.size());

			if (toStore)
				weHaveChangedStoredMaps();
		}
	}

	public void setWrite() {
		writeData = true;
	}

	public void unsetWrite() {
		writeData = false;
	}

	/*
	 * We need to implement this method since the table's
	 * data can change.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		if (value == null) {
			logging.debug(this, "call set value in table at " + row + "," + col + " to null");
			return;
		}

		logging.info(this, "Setting value in table at " + row + "," + col + " to " + value + " (an instance of "
				+ value.getClass() + ")");

		if (getValueAt(row, col).equals(value) || getValueAt(row, col).toString().equals(value.toString())) {

		} else {

			if (col == 1)
			// check not necessary since, by virtue of the method isCellEditable (int,int),
			// we can only have come to here in this case
			{
				if (keys == null) // perhaps everything has changed to null in the meantime
				{

				} else {
					String myKey = (String) keys.get(row);
					// StringvaluedObject o = new StringvaluedObject ( "" + value );
					Object o = value;

					// data.get(myKey).getClass().getName()

					// the internal view data:
					data.put(myKey, o);
					// the external view data
					oridata.put(myKey, o);
					logging.debug(this, "put into oridata for myKey o " + myKey + ": " + o);
					// the data sources:

					modelProducer.updateData(oridata);

					if (writeData) {
						logging.debug(this, " -------  storeData " + value + " (class : " + value.getClass());
						putEntryIntoStoredMaps(myKey, value);

						modifiedKey = getModifiedKey();
						// produces as well rowModiTime

						if (rowModiTime > -1 && row != rowModiTime) {
							setValueAt(Globals.getNowTimeListValue(), rowModiTime, 1);
						}

					}
				}
			}
		}
		fireTableCellUpdated(row, col);
	}

	/**
	 * writing a new value into the row with a certain key errors occur if the
	 * key is not among the given property names, or if a list of allowed values
	 * is given and the value is not among them <br />
	 * 
	 * @param String key
	 * @param Object value
	 */
	public void setValue(String key, Object value) {

		int row = keys.indexOf(key);

		if (row < 0) {
			logging.error("key not valid: " + key);
			return;
		}

		if (optionsMap.get(key) != null && (optionsMap.get(key)) instanceof List) {
			List valuelist = (List) optionsMap.get(key);

			if (!valuelist.isEmpty() && valuelist.indexOf(value) == -1) {

				logging.error("EditMapPanel: value not allowed: " + value);
				return;
			}
		}

		setValueAt(value, row, 1);
	}

	// implementation of DataChangedSubject
	@Override
	public void registerDataChangedObserver(DataChangedObserver o) {
		observers.addElement(o);
	}

	// for transport between a class family
	Vector<DataChangedObserver> getObservers() {
		return observers;
	}

	void setObservers(Vector<DataChangedObserver> observers) {
		this.observers = observers;
	}

	public void notifyChange() {

		logging.debug(this, "notifyChange, notify observers " + observers.size());
		for (int i = 0; i < observers.size(); i++) {
			(observers.elementAt(i)).dataHaveChanged(this);
		}

	}
}
