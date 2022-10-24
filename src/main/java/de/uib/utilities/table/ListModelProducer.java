package de.uib.utilities.table;

import javax.swing.*;

public interface ListModelProducer
{
	public ListModel getListModel(int row, int column);
	
	public int getSelectionMode(int row, int column);
	
	public boolean getNullable(int row, int column);
	
	public boolean getEditable(int row, int column);
	
	public java.util.List getSelectedValues(int row, int column);
	
	public void setSelectedValues( java.util.List newValues, int row, int column);
	
	public String getCaption(int row, int column);
	
	public java.util.List toList(Object value);
	
	public Class getClass(int row, int column);
	
}

