/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.datapanel;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.utils.DataChangedObserver;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.ListCellOptions;

public class MapTableModel extends AbstractTableModel {
	public static final List<?> nullLIST = Collections.singletonList(null);

	private List<DataChangedObserver> observers;

	private Collection updateCollection;
	private Collection<Map<String, Object>> storeData;
	private boolean datachanged;

	// keys which identify readonly entries
	private Set<String> keysOfReadOnlyEntries;

	private Function<String, Boolean> isEditable;

	private Map<String, ListCellOptions> optionsMap;

	// shall be sorted
	private Map<String, Object> data;

	// we keep the original data for writing back changed values
	private Map<String, Object> oridata;

	// the shadow default values of all data
	private Map<String, Object> defaultData;
	private List<String> keys;
	private String modifiedKey;
	private int rowModiTime;

	private ListModelProducerForVisualDatamap<String> modelProducer;

	private boolean writeData = true;

	public MapTableModel() {
		observers = new ArrayList<>();
	}

	public void setModelProducer(ListModelProducerForVisualDatamap<String> modelProducer) {
		this.modelProducer = modelProducer;
	}

	public void setOptions(Map<String, ListCellOptions> optionsMap, Map<String, Object> defaultData) {
		this.optionsMap = optionsMap;
		this.defaultData = defaultData;
	}

	public void setMap(Map<String, Object> data) {
		this.data = null;
		resetModifiedKey();

		if (data != null) {
			Collator myCollator = Collator.getInstance();
			myCollator.setStrength(Collator.PRIMARY);
			this.data = Collections.synchronizedSortedMap(new TreeMap<>(myCollator));
			this.data.putAll(data);
			keys = new ArrayList<>(this.data.keySet());
		}
		oridata = data;
	}

	public Map<String, Object> getData() {
		return data;
	}

	private void resetModifiedKey() {
		Logging.debug(this, "resetModifiedKey");
		modifiedKey = null;
	}

	private String getModifiedKey() {
		if (modifiedKey != null) {
			return modifiedKey;
		}

		for (String key : keys) {
			if (key.startsWith(UserConfig.CONFIGKEY_STR_USER) && key.endsWith(UserConfig.MODIFICATION_INFO_KEY)) {
				modifiedKey = key;
				break;
			}
		}
		rowModiTime = keys.indexOf(modifiedKey);

		return modifiedKey;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + data;
	}

	private void setNew() {
		// starting with a new set of data
		datachanged = false;
	}

	public List<String> getKeys() {
		return keys;
	}

	/**
	 * set collection (e.g. of clients) where each member stores the changed
	 * data; we assume that it is a collection of maps
	 * 
	 * @param Collection data
	 */
	public void setStoreData(Collection<Map<String, Object>> data) {
		if (data == null) {
			Logging.debug(this, "setStoreData, data is null ");
		}

		setNew();
		storeData = data;
		resetModifiedKey();
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

	public void setReadOnlyEntries(Set<String> keys) {
		keysOfReadOnlyEntries = keys;
	}

	public void setIsEditable(Function<String, Boolean> isEditable) {
		this.isEditable = isEditable;
	}

	public void addEntry(String key, Object newval, boolean toStore) {
		data.put(key, newval);
		oridata.put(key, newval);
		Logging.debug(this, " keys " + keys);
		keys = new ArrayList<>(data.keySet());
		Logging.debug(this, " new keys  " + keys);
		if (toStore) {
			putEntryIntoStoredMaps(key, newval, toStore);
		}
		fireTableDataChanged();
	}

	public void addEntry(String key, Object newval) {
		addEntry(key, newval, true);
	}

	public void removeEntry(String key) {
		data.remove(key);
		oridata.remove(key);

		keys = new ArrayList<>(data.keySet());

		removeEntryFromStoredMaps(key);
		fireTableDataChanged();
	}

	// table model
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		int result = 0;
		if (data != null) {
			result = keys.size();
		}

		return result;
	}

	@Override
	public String getColumnName(int col) {
		return switch (col) {
		case 0 -> Configed.getResourceValue("EditMapPanel.ColumnHeaderName");
		case 1 -> Configed.getResourceValue("EditMapPanel.ColumnHeaderValue");
		default -> "";
		};
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (data == null) {
			return "";
		}

		String key = keys.get(row);

		Object result = null;

		if (col == 0) {
			result = key;
		} else if (col == 1) {
			result = data.get(key);

			// deliver the default value
			if (result instanceof List) {
				List<?> li = (List<?>) result;
				if (!li.isEmpty() && li.get(0) == null && optionsMap != null) {
					result = defaultData.get(key);
					Logging.info(this,
							"getValueAt " + row + ", " + col + " result corrected for key  " + key + ": " + result);
				}
			}
		} else {
			Logging.warning(this, "col is neither 0 or 1, but " + col + ", so we don't know what to do...");
		}

		if (result == null) {
			return "";
		}

		return result;
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return switch (c) {
		case 0 -> String.class;
		case 1 -> List.class;
		default -> Object.class;
		};
	}

	/*
	 * We implement this method since the table is partially
	 * editable.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		// The data/cell address is constant,
		// no matter where the cell appears onscreen.

		if (data == null) {
			return false;
		}

		if (col < 1) {
			return false;
		} else {
			return (keysOfReadOnlyEntries == null || !keysOfReadOnlyEntries.contains(keys.get(row)))
					&& (isEditable == null || isEditable.apply(keys.get(row)));
		}
	}

	private void weHaveChangedStoredMaps() {
		// updateCollection has been emptied since last change
		if (!datachanged || updateCollection.isEmpty()) {
			datachanged = true;
			// tell it to all registered DataChangedObservers
			notifyChange();

			// we add the reference to the changed backend data only once to the
			// updateCollection

			if (updateCollection == null) {
				Logging.debug(this, "updateCollection null - should not be");
			} else {
				updateCollection.addAll(storeData);
			}

			Logging.debug(this,
					" ---  updateCollection: " + updateCollection + "  has size " + updateCollection.size());
		}
	}

	public void removeEntryFromStoredMaps(String myKey) {
		if (storeData != null) {
			for (Map<String, Object> aStoreMap : storeData) {
				aStoreMap.put(myKey, nullLIST);
			}

			Logging.debug(this,
					"remove entry --  updateCollection: " + updateCollection + "  has size " + updateCollection.size());
			weHaveChangedStoredMaps();
		}
	}

	// we put a new entry into each map in the given collection and preserve the
	// change data
	private void putEntryIntoStoredMaps(String myKey, Object value) {
		putEntryIntoStoredMaps(myKey, value, true);
	}

	// we put a new entry into each map in the given collection
	private void putEntryIntoStoredMaps(String myKey, Object value, boolean toStore) {
		Logging.debug(this, "putEntryIntoStoredMaps myKey, value: " + myKey + ", " + value);
		Logging.debug(this, "putEntryIntoStoredMaps storeData  counting " + storeData.size());
		if (storeData != null) {
			Iterator<Map<String, Object>> it = storeData.iterator();
			while (it.hasNext()) {
				Map<String, Object> aStoreMap = it.next();

				if (aStoreMap != null) {
					aStoreMap.put(myKey, value);
				} else {
					Logging.info(this, "EditMapPanel.setValueAt: we have some data null ");
				}
			}

			if (toStore) {
				weHaveChangedStoredMaps();
			}
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
			Logging.debug(this, "call set value in table at " + row + "," + col + " to null");
			return;
		}

		Logging.info(this, "Setting value in table at " + row + "," + col + " to " + value + " (an instance of "
				+ value.getClass() + ")");

		// check not necessary since, by virtue of the method isCellEditable (int,int),
		// we can only have come to here in this case
		if (!getValueAt(row, col).equals(value) && !getValueAt(row, col).toString().equals(value.toString()) && col == 1
				&& keys != null) {
			String myKey = keys.get(row);
			Object o = value;

			// the internal view data:
			data.put(myKey, o);
			// the external view data
			oridata.put(myKey, o);
			Logging.debug(this, "put into oridata for myKey o " + myKey + ": " + o);
			// the data sources:

			modelProducer.updateData(oridata);

			if (writeData) {
				Logging.debug(this, " -------  storeData " + value + " (class : " + value.getClass());
				putEntryIntoStoredMaps(myKey, value);

				modifiedKey = getModifiedKey();
				// produces as well rowModiTime

				if (rowModiTime > -1 && row != rowModiTime) {
					setValueAt(Utils.getNowTimeListValue(), rowModiTime, 1);
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
			Logging.error("key not valid: " + key);
			return;
		}

		if (optionsMap.get(key) != null && (optionsMap.get(key)) instanceof List) {
			List<?> valuelist = (List<?>) optionsMap.get(key);

			if (!valuelist.isEmpty() && valuelist.indexOf(value) == -1) {
				Logging.error("EditMapPanel: value not allowed: " + value);
				return;
			}
		}

		setValueAt(value, row, 1);
	}

	public void registerDataChangedObserver(DataChangedObserver o) {
		observers.add(o);
	}

	// for transport between a class family
	public List<DataChangedObserver> getObservers() {
		return observers;
	}

	public void setObservers(List<DataChangedObserver> observers) {
		this.observers = observers;
	}

	private void notifyChange() {
		Logging.debug(this, "notifyChange, notify observers " + observers.size());
		for (int i = 0; i < observers.size(); i++) {
			(observers.get(i)).dataHaveChanged(this);
		}
	}
}
