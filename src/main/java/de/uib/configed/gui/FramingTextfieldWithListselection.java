package de.uib.configed.gui;

import java.util.List;

public interface FramingTextfieldWithListselection {

	public String getTitle();

	public String getTextfieldLabel();

	public String getListLabel();

	public String getListLabelToolTip();

	public List<String> getListData();

	public void setListData(List<String> v);
}
