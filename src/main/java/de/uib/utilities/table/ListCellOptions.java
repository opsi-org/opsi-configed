/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.util.List;

// may represent an Opsi 4.0 config
// TODO make this class generic?
public interface ListCellOptions {

	List<Object> getPossibleValues();

	List<Object> getDefaultValues();

	void setDefaultValues(List<Object> values);

	int getSelectionMode();

	boolean isEditable();

	boolean isNullable();

	String getDescription();

	ListCellOptions deepCopy();
}
