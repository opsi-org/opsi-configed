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
}
