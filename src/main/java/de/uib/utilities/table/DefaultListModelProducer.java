package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import de.uib.utilities.logging.Logging;

public class DefaultListModelProducer<O> implements ListModelProducer<O> {
	@Override
	public ListModel<O> getListModel(int row, int column) {
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
	public List<O> getSelectedValues(int row, int column) {
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
	public List<O> toList(Object value) {
		if (value == null) {
			Logging.warning(this, "value is null");
			return new ArrayList<>();
		}

		if (value instanceof List) {
			return (List<O>) value;
		}

		Logging.warning(this, "value is not instance of List<O>");
		return new ArrayList<>();

	}

}
