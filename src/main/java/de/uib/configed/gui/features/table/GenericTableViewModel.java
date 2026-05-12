/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromTable;
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
	private final List<RowData> rows;
	private final List<TableColumnConfig> columns;
	private final TableConfig tableConfig;
	private final Set<Integer> selectedRows;
	private final boolean isDirty;
	private final boolean allowMultipleSelection;
	private final boolean showSearchPane;
	private final FilterKey filterKey;
	private final List<Map<String, Object>> originalSnapshot;
	private final RowDiffStrategy diffStrategy;
	private final SearchTargetModelFromTable searchTargetModelFromTable;
}
