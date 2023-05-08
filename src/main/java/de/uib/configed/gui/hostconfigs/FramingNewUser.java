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

public class FramingNewUser implements FramingTextfieldWithListselection {

	private List<String> list;
	private String title;
	private String textfieldLabel;
	private String listLabel;
	private String listLabelToolTip;

	@Override
	public String getTitle() {
		title = Configed.getResourceValue("FramingNewUser.title");

		return title;
	}

	@Override
	public String getTextfieldLabel() {

		textfieldLabel = Configed.getResourceValue("FramingNewUser.textfieldLabel");

		return textfieldLabel;

	}

	@Override
	public String getListLabel() {
		listLabel = Configed.getResourceValue("FramingNewUser.listLabel");

		return listLabel;
	}

	@Override
	public String getListLabelToolTip() {
		listLabelToolTip = Configed.getResourceValue("FramingNewUser.listLabel.ToolTip");

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
