package de.uib.configed.gui;

import java.util.Vector;

public interface FramingTextfieldWithListselection {

	public String getTitle();

	public String getTextfieldLabel();

	public String getListLabel();

	public String getListLabelToolTip();

	public Vector<String> getListData();

	public void setListData(Vector<String> v);
}
