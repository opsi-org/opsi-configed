/* 
 *
 * (c) uib, www.uib.de, 2009-2013
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.ListCellOptions;

public class DefaultEditMapPanel extends AbstractEditMapPanel
// works on a map of pairs of type String - List
{
	TableCellEditor theCellEditor;
	JComboBox editorfield;
	TableCellEditor defaultCellEditor;

	Vector<String> names;
	Map<String, ListCellOptions> optionsMap;
	Map<String, String> descriptionsMap;
	Map<String, Object> defaultsMap;

	TableCellRenderer tableCellRenderer;

	protected class DefaultPropertyHandler extends PropertyHandler {
		@Override
		public void removeValue(String key) {
			logging.debug(this, "removing value for key " + key);
			mapTableModel.removeEntry(key);
		}

		@Override
		public String getRemovalMenuText() {
			super.getRemovalMenuText();
			return configed.getResourceValue("EditMapPanel.PopupMenu.RemoveEntry");
		}

	}

	protected PropertyHandler propertyHandler;

	protected final PropertyHandler defaultPropertyHandler;

	public DefaultEditMapPanel() {
		this(null);
	}

	public DefaultEditMapPanel(TableCellRenderer tableCellRenderer) {
		this(tableCellRenderer, false);
	}

	public DefaultEditMapPanel(TableCellRenderer tableCellRenderer, boolean keylistExtendible) {
		this(tableCellRenderer, keylistExtendible, true);
	}

	public DefaultEditMapPanel(TableCellRenderer tableCellRenderer, boolean keylistExtendible,
			boolean keylistEditable) {
		this(tableCellRenderer, keylistExtendible, keylistEditable, false);
	}

	public DefaultEditMapPanel(TableCellRenderer tableCellRenderer,
			boolean keylistExtendible,
			boolean keylistEditable,
			boolean reloadable) {
		super(keylistExtendible, keylistEditable, reloadable);
		this.tableCellRenderer = tableCellRenderer;
		logging.debug(this, "DefaultEditMapPanel " + keylistExtendible + ",  " + keylistEditable + ",  " + reloadable);

		defaultPropertyHandler = new DefaultPropertyHandler();
		defaultPropertyHandler.setMapTableModel(mapTableModel);
		propertyHandler = defaultPropertyHandler;

	}

	@Override
	protected void buildPanel() {
		setLayout(new BorderLayout());
	}

	@Override
	public void init() {
		setEditableMap(null, null);
	}

	/**
	 * setting all data for displaying and editing
	 * <br />
	 * 
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */
	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap) {

		mapTableModel.setMap(visualdata);
		mapTableModel.fireTableDataChanged();

		// logging.info(this, " setEditableMap: visualdata " + visualdata);
		// logging.info(this, " setEditableMap: optionsMap " + optionsMap);

		// from mapTableModel, we get back the sorted vector of property names:
		names = mapTableModel.getKeys();

		// logging.debug(this, " +++++++ visualdata " + visualdata);
		// logging.debugMap(this, visualdata);
		// logging.debug(this, " +++++++ optionsMap " + optionsMap);
		// logging.debugMap(this, optionsMap);
		// logging.debug(this, " +++++++ descriptionsMap started " + descriptionsMap);

		if (optionsMap == null) {
			this.optionsMap = new HashMap<String, ListCellOptions>();
			// we introduce an empty Map since otherwise we use two cell editors, and they
			// dont come always when they should
		} else {
			this.optionsMap = optionsMap;
		}

		// derive from optionsMap, opsi 4.0
		{
			descriptionsMap = new HashMap<String, String>();
			defaultsMap = new HashMap<String, Object>();

			if (optionsMap != null) {
				for (String key : optionsMap.keySet()) {
					// logging.debug(this, "optionsMap.get(key) is " + optionsMap.get(key));
					String description = optionsMap.get(key).getDescription();
					Object defaultvalue = optionsMap.get(key).getDefaultValues();

					descriptionsMap.put(key, description);
					defaultsMap.put(key, defaultvalue);

					/*
					 * logging.info(this, "key  " + key + " optionsMap.get(key)  "
					 * + " class " + optionsMap.get(key).getClass()
					 * + " optionsMap.get(key) instanceof Map " + ((optionsMap.get(key)) instanceof
					 * Map)
					 * + " optionsMap.get(key) instanceof ListCellOptions " + ((optionsMap.get(key))
					 * instanceof ListCellOptions)
					 * + " optionsMap.get(key) instanceof DefaultListCellOptions " +
					 * ((optionsMap.get(key)) instanceof DefaultListCellOptions)
					 * + " value " + optionsMap.get(key));
					 * System.exit(0);
					 */

				}
			}
		}

		mapTableModel.setOptions(optionsMap,
				// for convenience we deliver defaultsMap
				defaultsMap);

		cancelOldCellEditing();

		// setNew();
		// mapTableModel.fireTableDataChanged();
	}

	public void setLabel(String s) {
	}

	public void cancelOldCellEditing() {

		if (theCellEditor != null) // && data != null)
		{
			theCellEditor.cancelCellEditing(); // don't shift the old editing state to a new product
			// theCellEditor.stopCellEditing(); //here we get null value errors since the
			// state "hangs"
		}

	}

	private boolean checkKey(String s) {
		boolean ok = false;

		if (s != null && !s.equals("")) {
			ok = true;

			if (names.indexOf(s) > -1) {
				ok =

						(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
								de.uib.configed.Globals.mainContainer,
								"Ein Eintrag mit diesem Namen existiert bereits. Überschreiben des bisherigen Eintrags?",
								de.uib.utilities.Globals.APPNAME,
								JOptionPane.OK_CANCEL_OPTION));
			}

		}

		return ok;
	}

	public void setValues(Map data) {
		// logging.debug(this, "setData: " + data);
		if (data == null)
			return;

		Iterator iter = data.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			mapTableModel.setValue(key, data.get(key));
		}
	}

	@Override
	public void resetDefaults() {
		setValues(defaultsMap);
	}

	@Override
	public void setVoid() {
		for (Object key : names) {
			mapTableModel.removeEntryFromStoredMaps((String) key);
			// mapTableModel.removeEntry((String) key);
		}

		mapTableModel.unsetWrite();
		setValues(defaultsMap);
		mapTableModel.setWrite();

	}
}
