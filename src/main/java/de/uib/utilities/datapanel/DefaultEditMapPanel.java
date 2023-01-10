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
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ListCellOptions;

public class DefaultEditMapPanel extends AbstractEditMapPanel
// works on a map of pairs of type String - List
{
	TableCellEditor theCellEditor;
	JComboBox editorfield;
	TableCellEditor defaultCellEditor;

	List<String> names;
	Map<String, ListCellOptions> optionsMap;
	Map<String, String> descriptionsMap;
	Map<String, Object> defaultsMap;

	TableCellRenderer tableCellRenderer;

	protected class DefaultPropertyHandler extends PropertyHandler {
		@Override
		public void removeValue(String key) {
			Logging.debug(this, "removing value for key " + key);
			mapTableModel.removeEntry(key);
		}

		@Override
		public String getRemovalMenuText() {
			super.getRemovalMenuText();
			return Configed.getResourceValue("EditMapPanel.PopupMenu.RemoveEntry");
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

	public DefaultEditMapPanel(TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean keylistEditable,
			boolean reloadable) {
		super(keylistExtendible, keylistEditable, reloadable);
		this.tableCellRenderer = tableCellRenderer;
		Logging.debug(this, "DefaultEditMapPanel " + keylistExtendible + ",  " + keylistEditable + ",  " + reloadable);

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
	 * setting all data for displaying and editing <br />
	 * 
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */
	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap) {

		mapTableModel.setMap(visualdata);
		mapTableModel.fireTableDataChanged();

		// from mapTableModel, we get back the sorted List of property names:
		names = mapTableModel.getKeys();

		if (optionsMap == null) {
			this.optionsMap = new HashMap<>();
			// we introduce an empty Map since otherwise we use two cell editors, and they
			// dont come always when they should
		} else {
			this.optionsMap = optionsMap;
		}

		// derive from optionsMap, opsi 4.0
		{
			descriptionsMap = new HashMap<>();
			defaultsMap = new HashMap<>();

			if (optionsMap != null) {
				for (String key : optionsMap.keySet()) {

					String description = optionsMap.get(key).getDescription();
					Object defaultvalue = optionsMap.get(key).getDefaultValues();

					descriptionsMap.put(key, description);
					defaultsMap.put(key, defaultvalue);

				}
			}
		}

		mapTableModel.setOptions(optionsMap,
				// for convenience we deliver defaultsMap
				defaultsMap);

		cancelOldCellEditing();

	}

	@Override
	public void setLabel(String s) {
	}

	public void cancelOldCellEditing() {

		if (theCellEditor != null) {
			theCellEditor.cancelCellEditing(); // don't shift the old editing state to a new product

		}

	}

	public void setValues(Map data) {

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
		for (String key : names) {
			mapTableModel.removeEntryFromStoredMaps(key);

		}

		mapTableModel.unsetWrite();
		setValues(defaultsMap);
		mapTableModel.setWrite();

	}
}
