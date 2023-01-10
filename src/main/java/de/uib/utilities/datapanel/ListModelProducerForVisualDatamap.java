package de.uib.utilities.datapanel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.ListModel;

import de.uib.utilities.logging.logging;
import de.uib.utilities.table.DefaultListCellOptions;
import de.uib.utilities.table.DefaultListModelProducer;
import de.uib.utilities.table.ListCellOptions;

/*
	ListModelProducerForDatamap 
	produces list models based on keys (given in column 0 of a table) 
	for which data exist (to be placed in column 1 of the table)
*/

public class ListModelProducerForVisualDatamap extends DefaultListModelProducer {
	Map<Integer, ListModel> listmodels = new HashMap<>();

	Map<String, ListCellOptions> optionsMap;
	Map<String, List> currentData;
	Map<String, Class> originalTypes;
	JTable table;

	public ListModelProducerForVisualDatamap(JTable tableVisualizingMap, Map<String, ListCellOptions> optionsMap,
			Map currentData) {
		this.table = tableVisualizingMap;
		setData(optionsMap, currentData);
	}

	public void setData(Map<String, ListCellOptions> optionsMap, Map currentData) {
		this.optionsMap = optionsMap;

		mapTypes(currentData);
	}

	public void updateData(Map currentData) {
		mapTypes(currentData);
	}

	private ListCellOptions getListCellOptions(String key) {
		ListCellOptions options = optionsMap.get(key);
		if (options == null) {
			options = new DefaultListCellOptions();
			optionsMap.put(key, options);
		}
		return options;
	}

	private void mapTypes(final Map currentData) {
		this.currentData = new HashMap<>();
		logging.debug(this, "mapTypes  " + currentData);
		originalTypes = new HashMap<>();
		for (Object key : currentData.keySet()) {
			Object value = currentData.get(key);

			originalTypes.put((String) key, value.getClass());
			this.currentData.put((String) key, toList(value));
		}

	}

	@Override
	public ListModel getListModel(int row, int column) {
		// column can be assumed to be 1

		if (listmodels.get(row) != null) {
			// we already built a model
			return listmodels.get(row);
		}

		logging.info(this, "getListModel, row " + row + ", column " + column);

		// build listmodel

		String key = (String) table.getValueAt(row, 0);

		ListCellOptions options = getListCellOptions(key);

		List values = options.getPossibleValues();
		logging.info(this, "getListModel key " + key + " the option values " + values);
		logging.info(this, "getListModel key " + key + " options  " + options);

		DefaultListModel model = new DefaultListModel();
		Iterator iter = ((List) values).iterator();
		while (iter.hasNext()) {
			model.addElement(iter.next());
		}
		if (currentData.get(key) instanceof List) {
			iter = ((List) currentData.get(key)).iterator();

			while (iter.hasNext()) {
				Object entry = iter.next();
				if (!model.contains(entry))
					model.addElement(entry);
			}
		}
		listmodels.put(row, model);

		return model;
	}

	@Override
	public List getSelectedValues(int row, int column) {

		String key = (String) table.getValueAt(row, 0);
		return (List) currentData.get(key);
	}

	@Override
	public void setSelectedValues(List newValues, int row, int column) {

		String key = (String) table.getValueAt(row, 0);
		currentData.put(key, newValues);
		table.setValueAt(newValues, row, 1);
	}

	@Override
	public int getSelectionMode(int row, int column) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).getSelectionMode();
	}

	@Override
	public boolean getEditable(int row, int column) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).isEditable();
	}

	@Override
	public boolean getNullable(int row, int column) {
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).isNullable();
	}

	@Override
	public String getCaption(int row, int column) {
		return (String) table.getValueAt(row, 0);
	}

	@Override
	public Class getClass(int row, int column) {

		String key = (String) table.getValueAt(row, 0);

		return originalTypes.get(key);
	}

}
