/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import de.uib.configed.type.ConfigOption;
import de.uib.utils.logging.Logging;

public class DefaultListModelProducer {
	private static ConfigOption defaultConfigOption = new ConfigOption();

	public ListModel<String> getListModel(int row) {
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

	// TODO maybe this can go away, value should always be list
	public List<Object> toList(Object value) {
		if (value == null) {
			Logging.warning(this, "value is null");
			return new ArrayList<>();
		}

		if (value instanceof List) {
			return (List<Object>) value;
		}

		Logging.info(this, "value is not instance of List<O>, create List with element value");
		return Collections.singletonList(value);
	}
}
