package de.uib.utilities.table;

import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

public class DefaultListModelProducer implements ListModelProducer {
	public ListModel getListModel(int row, int column) {
		return null;
	}

	public int getSelectionMode(int row, int column) {
		return ListSelectionModel.SINGLE_SELECTION;
	}

	public boolean getNullable(int row, int column) {
		return true;
	}

	public boolean getEditable(int row, int column) {
		return false;
	}

	public java.util.List getSelectedValues(int row, int column) {
		return new ArrayList();
	}

	public void setSelectedValues(java.util.List newValues, int row, int column) {
	}

	public String getCaption(int row, int column) {
		return "";
	}

	public Class getClass(int row, int column) {
		return Object.class;
	}

	public java.util.List toList(Object value) {
		if (value == null)
			return null;

		if (value instanceof java.util.List)
			return (java.util.List) value;

		ArrayList list = new ArrayList();
		list.add(value);

		return list;
	}

}
