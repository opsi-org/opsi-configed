/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.ListModel;

import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.share.table.gui.ListModelProducer;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.share.logging.Logging;

/*
	private ListModelProducerForDatamap 
	produces list models based on keys (given in column 0 of a table) 
	private for which data exist (to be placed in column 1 of the table)
*/

public class ListModelProducerForVisualDatamap implements ListModelProducer {
	private Map<Integer, ListModel<String>> listmodels = new HashMap<>();
	private Map<Integer, ComboBoxModel<String>> comboboxmodels = new HashMap<>();

	private Map<String, ConfigOption> optionsMap;
	private Map<String, Object> currentData;
	private JTable table;

	public ListModelProducerForVisualDatamap(JTable tableVisualizingMap, Map<String, ConfigOption> optionsMap,
			Map<String, Object> currentData) {
		this.table = tableVisualizingMap;
		this.optionsMap = optionsMap;
		this.currentData = currentData;
	}

	@Override
	public ConfigOption getListCellOptions(String key) {
		return optionsMap.get(key);
	}

	@Override
	public ListModel<String> getListModel(int row) {
		// column can be assumed to be 1

		if (listmodels.containsKey(row)) {
			// we already built a model
			return listmodels.get(row);
		}

		Logging.info(this, "getListModel, row ", row);

		// build listmodel

		String key = (String) table.getValueAt(row, 0);

		ConfigOption options = getListCellOptions(key);

		List<Object> values = options.getPossibleValues();
		Logging.info(this, "getListModel key ", key, " the option values ", values);
		Logging.info(this, "getListModel key ", key, " options  ", options);

		DefaultListModel<String> model = new DefaultListModel<>();
		Iterator<? extends Object> iter = values.iterator();
		while (iter.hasNext()) {
			model.addElement(POJOReMapper.remap(iter.next()));
		}
		if (currentData.get(key) instanceof List) {
			iter = ((List<?>) currentData.get(key)).iterator();

			while (iter.hasNext()) {
				String entry = (String) iter.next();
				if (!model.contains(entry) && entry != null) {
					model.addElement(entry);
				}
			}
		}
		listmodels.put(row, model);

		return model;
	}

	@Override
	public ComboBoxModel<String> getComboBoxModel(int row) {
		// column can be assumed to be 1

		if (comboboxmodels.containsKey(row)) {
			// we already built a model
			return comboboxmodels.get(row);
		}

		Logging.info(this, "getComboBoxModel, row ", row);

		// build comboboxmodel

		String key = (String) table.getValueAt(row, 0);

		ConfigOption options = getListCellOptions(key);

		List<Object> values = options.getPossibleValues();
		Logging.info(this, "getComboBoxModel key ", key, " the option values ", values);
		Logging.info(this, "getComboBoxModel key ", key, " options  ", options);

		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		Iterator<? extends Object> iter = values.iterator();
		while (iter.hasNext()) {
			model.addElement(POJOReMapper.remap(iter.next()));
		}
		if (currentData.get(key) instanceof List) {
			iter = ((List<?>) currentData.get(key)).iterator();

			while (iter.hasNext()) {
				String entry = (String) iter.next();
				if (model.getIndexOf(entry) == -1 && entry != null) {
					model.addElement(entry);
				}
			}
		}
		comboboxmodels.put(row, model);

		return model;
	}

	@Override
	public int getSelectionMode(int row) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).getSelectionMode();
	}

	@Override
	public boolean isEditable(int row) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).isEditable();
	}
}
