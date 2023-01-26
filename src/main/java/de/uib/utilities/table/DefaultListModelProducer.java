package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

public class DefaultListModelProducer implements ListModelProducer {
	@Override
	public ListModel getListModel(int row, int column) {
		return null;
	}

	@Override
	public int getSelectionMode(int row, int column) {
		return ListSelectionModel.SINGLE_SELECTION;
	}

	@Override
	public boolean getNullable(int row, int column) {
		return true;
	}

	@Override
	public boolean getEditable(int row, int column) {
		return false;
	}

	@Override
	public List getSelectedValues(int row, int column) {
		return new ArrayList<>();
	}

	@Override
	public String getCaption(int row, int column) {
		return "";
	}

	@Override
	public Class<?> getClass(int row, int column) {
		return Object.class;
	}

	@Override
	public List toList(Object value) {
		if (value == null)
			return null;

		if (value instanceof List)
			return (List) value;

		List list = new ArrayList<>();
		list.add(value);

		return list;
	}

}
