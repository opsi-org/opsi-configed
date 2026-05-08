/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import de.uib.configed.gui.features.table.RowData.RowState;

/**
 * The Strategy Interface for calculating cell styles (DRIFT, EDIT, etc.).
 * Implemented by specific table types (e.g., HostParameterTable).
 */
public interface RowDiffStrategy {
	/**
	 * Calculates the visual style token for a specific cell.
	 * 
	 * @param rowId         The row ID.
	 * @param colKey        The column key.
	 * @param currentValue  The current value in the UI.
	 * @param originalValue The value from the last clean state (or server
	 *                      default).
	 * @return A style token (e.g., "NORMAL", "MODIFIED", "EDIT").
	 */
	RowState getRowStyle(String rowId, String colKey, Object currentValue, Object originalValue);
}
