/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
public class TableConfig {
	private final boolean fillViewportHeight;
	private final boolean showTableHeader;
	private final boolean dragEnabled;
	private final boolean autoCreateRowSorter;
	private final boolean reorderingAllowed;
	private final boolean columnSelectionAllowed;
	private final boolean enableHeaderContextMenu;
	private final int selectionMode;
	private final TableCellRenderer defauTableCellRenderer;
	private final String sortColumnKey;
	private final SortOrder sortOrder;
}
