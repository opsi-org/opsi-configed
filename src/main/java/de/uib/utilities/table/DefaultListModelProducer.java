/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import de.uib.utilities.logging.Logging;

public class DefaultListModelProducer<O> implements ListModelProducer<O> {
	@Override
	public ListModel<O> getListModel(int row, int column) {
		return null;
	}

	@Override
	public int getSelectionMode(int row, int column) {
		return ListSelectionModel.SINGLE_SELECTION;
	}

	@Override
	public boolean isNullable(int row, int column) {
		return true;
	}

	@Override
	public boolean isEditable(int row, int column) {
		return false;
	}

	@Override
	public List<O> getSelectedValues(int row, int column) {
		return new ArrayList<>();
	}

	@Override
	public String getCaption(int row, int column) {
		return "";
	}

	@Override
	public Class<?> getClass(int row) {
		return Object.class;
	}

	@Override
	public List<O> toList(Object value) {
		if (value == null) {
			Logging.warning(this, "value is null");
			return new ArrayList<>();
		}

		if (value instanceof List) {
			return (List<O>) value;
		}

		Logging.info(this, "value is not instance of List<O>, create List with element value");
		return Collections.singletonList((O) value);
	}

}
