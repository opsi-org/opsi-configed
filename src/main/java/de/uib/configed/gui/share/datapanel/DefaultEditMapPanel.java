/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import de.uib.configed.core.domain.datachanges.UpdateCollection;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.share.logging.Logging;

// works on a map of pairs of type String - List
public class DefaultEditMapPanel extends JPanel {
	protected MapTableModel mapTableModel;

	protected boolean showToolTip = true;

	protected Actor actor;

	protected JPopupMenu popupMenu;

	protected List<String> names;
	protected Map<String, ConfigOption> optionsMap;
	protected Map<String, String> descriptionsMap;
	protected Map<String, Object> defaultsMap;

	private static class DefaultPropertyHandler extends AbstractPropertyHandler {
		@Override
		public void removeValue(String key) {
			Logging.debug(this, "removing value for key ", key);
			mapTableModel.removeConfigEntry(key);
		}

		@Override
		public String getRemovalMenuText() {
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
	}

	protected abstract static class AbstractPropertyHandler {
		MapTableModel mapTableModel;

		public void setMapTableModel(MapTableModel mapTableModel) {
			this.mapTableModel = mapTableModel;
		}

		public abstract void removeValue(String key);

		public abstract String getRemovalMenuText();
	}

	protected AbstractPropertyHandler propertyHandler;

	protected final AbstractPropertyHandler defaultPropertyHandler;

	public DefaultEditMapPanel() {
		actor = new Actor();
		mapTableModel = new MapTableModel();

		defaultPropertyHandler = new DefaultPropertyHandler();
		defaultPropertyHandler.setMapTableModel(mapTableModel);
		propertyHandler = defaultPropertyHandler;
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
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ConfigOption> optionsMap) {
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

		for (Entry<String, ConfigOption> option : this.optionsMap.entrySet()) {
			String description = option.getValue().getDescription();
			Object defaultvalue = option.getValue().getDefaultValues();

			descriptionsMap.put(option.getKey(), description);
			defaultsMap.put(option.getKey(), defaultvalue);
		}

		mapTableModel.setOptions(optionsMap, defaultsMap);
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	public void setValues(Map<String, Object> data) {
		if (data == null) {
			return;
		}

		for (Entry<String, Object> dataEntry : data.entrySet()) {
			mapTableModel.setValue(dataEntry.getKey(), dataEntry.getValue());
		}
	}

	public void resetDefaults() {
		for (String key : names) {
			mapTableModel.addEntry(key, defaultsMap.get(key), true);
		}

		mapTableModel.unsetWrite();
		setValues(defaultsMap);
		mapTableModel.setWrite();
	}

	public void setVoid() {
		for (String key : names) {
			mapTableModel.removeEntryFromStoredMaps(key);
		}

		mapTableModel.unsetWrite();
		setValues(defaultsMap);
		mapTableModel.setWrite();
	}

	public void setShowToolTip(boolean showToolTip) {
		this.showToolTip = showToolTip;
	}

	/**
	 * set collection (e.g. of clients) where each member stores the changed
	 * data; we assume that it is a collection of maps; also take a reference to
	 * a collection of maps that we will have to use for updating the data base
	 * 
	 * @param Collection updateCollection
	 * @param Collection data
	 */
	public void updateData(UpdateCollection updateCollection, Collection<Map<String, Object>> data) {
		mapTableModel.setUpdateCollection(updateCollection);
		mapTableModel.setStoreData(data);
	}

	public MapTableModel getMapTableModel() {
		return mapTableModel;
	}
}
