package de.uib.configed.gui.hostconfigs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Copyright:     Copyright (c) 2022
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.Configed;
import de.uib.configed.gui.FramingTextfieldWithListselection;

public class FramingNewRole implements FramingTextfieldWithListselection {

	List<String> list;
	String title;
	String textfieldLabel;
	String listLabel;
	String listLabelToolTip;

	@Override
	public String getTitle() {
		title = Configed.getResourceValue("FramingNewRole.title");

		return title;

	}

	@Override
	public String getTextfieldLabel() {

		textfieldLabel = Configed.getResourceValue("FramingNewRole.textfieldLabel");

		return textfieldLabel;

	}

	@Override
	public String getListLabel() {
		listLabel = Configed.getResourceValue("FramingNewRole.listLabel");

		return listLabel;
	}

	@Override
	public String getListLabelToolTip() {
		listLabelToolTip = Configed.getResourceValue("FramingNewRole.listLabel.ToolTip");

		return listLabelToolTip;
	}

	@Override
	public void setListData(List<String> v) {
		list = v;
	}

	@Override
	public List<String> getListData() {
		if (list == null) {
			list = new ArrayList<>();
			list.add("B1");
			list.add("B2");
			list.add("B1");
			list.add("ABC");
		}

		return list;

	}

}
