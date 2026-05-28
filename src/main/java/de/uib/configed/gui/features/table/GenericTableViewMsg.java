/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SortOrder;

public sealed interface GenericTableViewMsg permits GenericTableViewMsg.CellEdited, GenericTableViewMsg.CommitChanges,
		GenericTableViewMsg.CancelChanges, GenericTableViewMsg.ToggleColumn, GenericTableViewMsg.ChangeSelection,
		GenericTableViewMsg.AddRow, GenericTableViewMsg.DeleteRow, GenericTableViewMsg.ChangeOriginalSnapshot,
		GenericTableViewMsg.ChangeSortOrder, GenericTableViewMsg.ResizeColumns {
	record CellEdited(int rowIdx, int colIdx, Object newValue) implements GenericTableViewMsg {

	}

	record CommitChanges() implements GenericTableViewMsg {

	}

	record CancelChanges() implements GenericTableViewMsg {

	}

	record ToggleColumn(String columnKey) implements GenericTableViewMsg {

	}

	record ChangeSelection(Set<String> selectedRows) implements GenericTableViewMsg {

	}

	record AddRow(Map<String, Object> data) implements GenericTableViewMsg {

	}

	record DeleteRow(int rowIdx) implements GenericTableViewMsg {

	}

	record ChangeSortOrder(Map<String, SortOrder> sortKeys) implements GenericTableViewMsg {
	}

	record ResizeColumns(Map<String, Integer> columnWidths) implements GenericTableViewMsg {
	}

	record ChangeOriginalSnapshot(List<Map<String, Object>> originalSnapshot) implements GenericTableViewMsg {

	}
}
