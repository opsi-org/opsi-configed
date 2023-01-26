package de.uib.utilities.table;

import java.util.List;

import javax.swing.ListModel;

public interface ListModelProducer {
	public ListModel getListModel(int row, int column);

	public int getSelectionMode(int row, int column);

	public boolean getNullable(int row, int column);

	public boolean getEditable(int row, int column);

	public List getSelectedValues(int row, int column);

	public String getCaption(int row, int column);

	public List toList(Object value);

	public Class<?> getClass(int row, int column);

}
