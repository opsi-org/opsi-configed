/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.provider;

import java.util.List;

public interface TableProvider {

	List<String> getColumnNames();

	List<String> getClassNames();

	// should deliver a working copy of the data
	List<List<Object>> getRows();

	// should set the working copy as new original data
	void setWorkingCopyAsNewOriginalRows();

	// should initiate reloading the original data
	void requestReturnToOriginal();

	// should initiate reloading the original data
	void requestReloadRows();

	// should initiate reloading the metadata
	void structureChanged();

	// yields a column as ordered List
	List<String> getOrderedColumn(int col, boolean emptyAllowed);
}
