/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.util.List;

import javax.swing.ListModel;

public interface ListModelProducer<O> {
	ListModel<O> getListModel(int row, int column);

	int getSelectionMode(int row, int column);

	boolean isNullable(int row, int column);

	boolean isEditable(int row, int column);

	List<O> getSelectedValues(int row, int column);

	String getCaption(int row, int column);

	List<O> toList(Object value);

	Class<?> getClass(int row, int column);
}
