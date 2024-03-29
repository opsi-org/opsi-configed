/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.gui.FramingTextfieldWithListselection;

public class FramingNewUser implements FramingTextfieldWithListselection {
	private List<String> list;

	@Override
	public String getTitle() {
		return Configed.getResourceValue("FramingNewUser.title");
	}

	@Override
	public String getTextfieldLabel() {
		return Configed.getResourceValue("FramingNewUser.textfieldLabel");
	}

	@Override
	public String getListLabel() {
		return Configed.getResourceValue("FramingNewUser.listLabel");
	}

	@Override
	public String getListLabelToolTip() {
		return Configed.getResourceValue("FramingNewUser.listLabel.ToolTip");
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
