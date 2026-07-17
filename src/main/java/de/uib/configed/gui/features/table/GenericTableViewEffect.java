/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.List;
import java.util.Map;

public sealed interface GenericTableViewEffect
		permits GenericTableViewEffect.Selection, GenericTableViewEffect.StoreVisibleColulmns,
		GenericTableViewEffect.CellEdited, GenericTableViewEffect.DeleteRows, GenericTableViewEffect.AddRow {
	record Selection() implements GenericTableViewEffect {
	}

	record StoreVisibleColulmns(List<String> visibleColumns) implements GenericTableViewEffect {
	}

	record CellEdited(int rowIdx, int colIdx, Object newValue) implements GenericTableViewEffect {
	}

	record DeleteRows(List<RowData> deletedRows) implements GenericTableViewEffect {
	}

	record AddRow(Map<String, Object> data) implements GenericTableViewEffect {
	}
}
