/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import javax.swing.ListModel;

import de.uib.configed.type.ConfigOption;

public interface ListModelProducer {
	ListModel<String> getListModel(int row);

	int getSelectionMode(int row);

	boolean isEditable(int row);

	ConfigOption getListCellOptions(String key);
}
