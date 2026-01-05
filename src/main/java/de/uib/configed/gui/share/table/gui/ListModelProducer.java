/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import javax.swing.ComboBoxModel;
import javax.swing.ListModel;

import de.uib.configed.gui.type.ConfigOption;

public interface ListModelProducer {
	ListModel<String> getListModel(int row);

	ComboBoxModel<String> getComboBoxModel(int row);

	int getSelectionMode(int row);

	boolean isEditable(int row);

	ConfigOption getListCellOptions(String key);
}
