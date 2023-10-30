/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

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
