/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import java.util.List;

import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Immutable state for the TableSearchPane TEA component.
 */
@Value
@With
@Builder(toBuilder = true)
public class SearchPaneModel {
	// Search Configuration
	@Builder.Default
	String searchText = "";
	int searchColumnIndex;
	boolean isRegexActive;
	boolean isRespectCase;
	@Builder.Default
	boolean selectMode = true;
	boolean showFilterMark;
	List<Integer> searchColumns;

	// Navigation State
	@Builder.Default
	int foundRow = -1;
	boolean showNavPanel;
	boolean extraOptionsVisible;
	boolean isNarrow;

	// Filter State
	boolean isFiltered;
	FilterKey filterKey;

	// UI State (derived or transient)
	boolean isDirty;
}
