/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.datapanel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.ListModel;

import de.uib.utils.logging.Logging;
import de.uib.utils.table.DefaultListCellOptions;
import de.uib.utils.table.DefaultListModelProducer;
import de.uib.utils.table.ListCellOptions;

/*
	private ListModelProducerForDatamap 
	produces list models based on keys (given in column 0 of a table) 
	private for which data exist (to be placed in column 1 of the table)
*/

public class ListModelProducerForVisualDatamap<O> extends DefaultListModelProducer<O> {
	private Map<Integer, ListModel> listmodels = new HashMap<>();

	private Map<String, ListCellOptions> optionsMap;
	private Map<String, List<O>> currentData;
	private Map<String, Class<?>> originalTypes;
	private JTable table;

	public ListModelProducerForVisualDatamap(JTable tableVisualizingMap, Map<String, ListCellOptions> optionsMap,
			Map<String, Object> currentData) {
		this.table = tableVisualizingMap;
		setData(optionsMap, currentData);
	}

	public final void setData(Map<String, ListCellOptions> optionsMap, Map<String, Object> currentData) {
		this.optionsMap = optionsMap;

		mapTypes(currentData);
	}

	public void updateData(Map<String, Object> data) {
		mapTypes(data);
	}

	private ListCellOptions getListCellOptions(String key) {
		return optionsMap.computeIfAbsent(key, arg -> new DefaultListCellOptions());
	}

	private void mapTypes(final Map<String, Object> data) {
		currentData = new HashMap<>();
		Logging.debug(this, "mapTypes  " + data);
		originalTypes = new HashMap<>();
		for (Entry<String, Object> dataEntry : data.entrySet()) {
			originalTypes.put(dataEntry.getKey(), dataEntry.getValue().getClass());
			currentData.put(dataEntry.getKey(), toList(dataEntry.getValue()));
		}
	}

	@Override
	public ListModel getListModel(int row, int column) {
		// column can be assumed to be 1

		if (listmodels.get(row) != null) {
			// we already built a model
			return listmodels.get(row);
		}

		Logging.info(this, "getListModel, row " + row + ", column " + column);

		// build listmodel

		String key = (String) table.getValueAt(row, 0);

		ListCellOptions options = getListCellOptions(key);

		List<Object> values = options.getPossibleValues();
		Logging.info(this, "getListModel key " + key + " the option values " + values);
		Logging.info(this, "getListModel key " + key + " options  " + options);

		DefaultListModel<Object> model = new DefaultListModel<>();
		Iterator<? extends Object> iter = values.iterator();
		while (iter.hasNext()) {
			model.addElement(iter.next());
		}
		if (currentData.get(key) instanceof List) {
			iter = currentData.get(key).iterator();

			while (iter.hasNext()) {
				Object entry = iter.next();
				if (!model.contains(entry) && entry != null) {
					model.addElement(entry);
				}
			}
		}
		listmodels.put(row, model);

		return model;
	}

	@Override
	public List<O> getSelectedValues(int row, int column) {
		String key = (String) table.getValueAt(row, 0);
		return currentData.get(key);
	}

	@Override
	public int getSelectionMode(int row, int column) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).getSelectionMode();
	}

	@Override
	public boolean isEditable(int row, int column) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).isEditable();
	}

	@Override
	public boolean isNullable(int row, int column) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).isNullable();
	}

	@Override
	public String getCaption(int row, int column) {
		return (String) table.getValueAt(row, 0);
	}

	@Override
	public Class<?> getClass(int row) {
		String key = (String) table.getValueAt(row, 0);
		return originalTypes.get(key);
	}
}
