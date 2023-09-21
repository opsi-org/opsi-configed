/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.datapanel;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.table.TableCellRenderer;

import de.uib.configed.Configed;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ListCellOptions;

// works on a map of pairs of type String - List
public class DefaultEditMapPanel extends JPanel {
	public MapTableModel mapTableModel;

	protected boolean reloadable;
	protected boolean showToolTip = true;

	protected boolean keylistExtendible;
	protected boolean keylistEditable = true;

	protected Actor actor;

	protected JPopupMenu popupmenuAtRow;
	protected JPopupMenu popupEditOptions;
	protected JPopupMenu popupNoEditOptions;

	protected List<String> names;
	protected Map<String, ListCellOptions> optionsMap;
	protected Map<String, String> descriptionsMap;
	protected Map<String, Object> defaultsMap;

	protected TableCellRenderer tableCellRenderer;

	private static class DefaultPropertyHandler extends AbstractPropertyHandler {
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

	public static class Actor {
		public void reloadData() {
			Logging.info(this, "DefaultEditMapPanel: reloadData");
		}

		public void saveData() {
			Logging.info(this, "DefaultEditMapPanel: saveData");
		}

		public void deleteData() {
			Logging.info(this, "DefaultEditMapPanel: deleteData");
		}
	}

	protected abstract static class AbstractPropertyHandler {
		MapTableModel mapTableModel;

		public void setMapTableModel(MapTableModel mapTableModel) {
			this.mapTableModel = mapTableModel;
		}

		public abstract void removeValue(String key);

		public String getRemovalMenuText() {
			String s = "";
			Logging.debug(this, "getRemovalMenuText " + s);
			return s;
		}

	}

	protected AbstractPropertyHandler propertyHandler;

	protected final AbstractPropertyHandler defaultPropertyHandler;

	public DefaultEditMapPanel(TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean keylistEditable,
			boolean reloadable) {

		actor = new Actor();
		mapTableModel = new MapTableModel();
		this.keylistExtendible = keylistExtendible;
		this.keylistEditable = keylistEditable;
		this.reloadable = reloadable;

		this.tableCellRenderer = tableCellRenderer;
		Logging.debug(this.getClass(),
				"DefaultEditMapPanel " + keylistExtendible + ",  " + keylistEditable + ",  " + reloadable);

		defaultPropertyHandler = new DefaultPropertyHandler();
		defaultPropertyHandler.setMapTableModel(mapTableModel);
		propertyHandler = defaultPropertyHandler;

	}

	protected void buildPanel() {
		setLayout(new BorderLayout());
	}

	public void init() {
		setEditableMap(null, null);
	}

	/**
	 * setting all data for displaying and editing <br />
	 * 
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */
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

		descriptionsMap = new HashMap<>();
		defaultsMap = new HashMap<>();

		if (optionsMap != null) {
			for (Entry<String, ListCellOptions> option : optionsMap.entrySet()) {
				String description = option.getValue().getDescription();
				Object defaultvalue = option.getValue().getDefaultValues();

				descriptionsMap.put(option.getKey(), description);
				defaultsMap.put(option.getKey(), defaultvalue);

			}
		}

		mapTableModel.setOptions(optionsMap,
				// for convenience we deliver defaultsMap
				defaultsMap);
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	public void setLabel(String s) {
		/* Not needed */}

	public void setValues(Map<String, Object> data) {
		if (data == null) {
			return;
		}

		Iterator<String> iter = data.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			mapTableModel.setValue(key, data.get(key));
		}
	}

	public void resetDefaults() {
		setValues(defaultsMap);
	}

	public void setVoid() {
		for (String key : names) {
			mapTableModel.removeEntryFromStoredMaps(key);
		}

		mapTableModel.unsetWrite();
		setValues(defaultsMap);
		mapTableModel.setWrite();
	}

	public List<String> getNames() {
		return mapTableModel.getKeys();
	}

	public void setShowToolTip(boolean b) {
		showToolTip = b;
	}

	public void registerDataChangedObserver(DataChangedObserver o) {
		mapTableModel.registerDataChangedObserver(o);
	}

	/**
	 * set collection (e.g. of clients) where each member stores the changed
	 * data; we assume that it is a collection of maps
	 * 
	 * @param Collection data
	 */
	public void setStoreData(Collection<Map<String, Object>> data) {
		mapTableModel.setStoreData(data);
	}

	/**
	 * take a reference to a collection of maps that we will have to use for
	 * updating the data base
	 * 
	 * @param Collection updateCollection
	 */
	public void setUpdateCollection(Collection updateCollection) {
		mapTableModel.setUpdateCollection(updateCollection);
	}

	public void setReadOnlyEntries(Set<String> keys) {
		mapTableModel.setReadOnlyEntries(keys);
	}

	public void setEditableFunction(Function<String, Boolean> isEditable) {
		mapTableModel.setIsEditable(isEditable);
	}

	protected void logPopupElements() {
		MenuElement[] popupElements = popupmenuAtRow.getSubElements();
		int size = popupElements.length;
		Logging.debug(this, "logPopupElements " + size);

	}

	public void setOptionsEditable(boolean b) {
		Logging.debug(this, "DefaultEditMapPanel.setOptionsEditable " + b);

		if (b) {
			popupmenuAtRow = popupEditOptions;
		} else {
			popupmenuAtRow = popupNoEditOptions;
		}
	}
}
