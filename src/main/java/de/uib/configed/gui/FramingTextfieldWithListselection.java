package de.uib.configed.gui;

import java.util.ArrayList;

public interface FramingTextfieldWithListselection {

	public String getTitle();

	public String getTextfieldLabel();

	public String getListLabel();

	public String getListLabelToolTip();

	public ArrayList<String> getListData();

	public void setListData(ArrayList<String> v);
}
