/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * The Immutable View Model. Contains the data, schema, and state.
 */
@Value
@With
@Builder(toBuilder = true)
public class GenericTableViewModel {
	@Builder.Default
	private final List<RowData> rows = new ArrayList<>();
	@Builder.Default
	private final List<TableColumnConfig> columns = new ArrayList<>();
	private final TableConfig tableConfig;
	@Builder.Default
	private final Set<String> selectedRows = new HashSet<>();
	private final boolean isDirty;
	private final boolean allowMultipleSelection;
	@Builder.Default
	private final boolean rebuildTableModel = true;
	@Builder.Default
	private final List<Map<String, Object>> originalSnapshot = new ArrayList<>();
	private final RowDiffStrategy diffStrategy;
	private final boolean keyValueTable;

	/**
	 * Gets a ColumnConfig directly by its MODEL INDEX (position in the list).
	 * This is O(1) access without needing to look up by key first.
	 * 
	 * @param modelIndex The index in the underlying columns list (0-based)
	 * @return The TableColumnConfig, or null if index is out of bounds
	 */
	public TableColumnConfig getColumnByModelIndex(int modelIndex) {
		if (modelIndex < 0 || modelIndex >= columns.size()) {
			return null;
		}

		List<TableColumnConfig> visibleColumns = getVisibleColumns();
		if (modelIndex >= 0 && modelIndex < visibleColumns.size()) {
			return visibleColumns.get(modelIndex);
		}

		return null;
	}

	public List<TableColumnConfig> getVisibleColumns() {
		return columns.stream().filter(TableColumnConfig::isVisible).toList();
	}
}
