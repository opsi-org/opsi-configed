package de.uib.configed.gui;

import java.util.List;

public interface FramingTextfieldWithListselection {

	String getTitle();

	String getTextfieldLabel();

	String getListLabel();

	String getListLabelToolTip();

	List<String> getListData();

	void setListData(List<String> v);
}
