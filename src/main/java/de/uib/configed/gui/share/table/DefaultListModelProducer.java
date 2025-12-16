/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table;

import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import de.uib.configed.gui.share.table.gui.ListModelProducer;
import de.uib.configed.gui.type.ConfigOption;

public class DefaultListModelProducer implements ListModelProducer {
	private static ConfigOption defaultConfigOption = new ConfigOption();

	public ListModel<String> getListModel(int row) {
		return null;
	}

	public ComboBoxModel<String> getComboBoxModel(int row) {
		return null;
	}

	public int getSelectionMode(int row) {
		return ListSelectionModel.SINGLE_SELECTION;
	}

	public boolean isEditable(int row) {
		return false;
	}

	public ConfigOption getListCellOptions(String key) {
		return defaultConfigOption;
	}
}
