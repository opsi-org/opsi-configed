/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import de.uib.configed.share.logging.Logging;

public class FlexibleRowFilter extends RowFilter<TableModel, Integer> {
	private final String query;
	private final int columnIndex;
	private final boolean useRegex;
	private final boolean caseSensitive;
	private final SearchCriteriaEngine searchCriteriaEngine;

	public FlexibleRowFilter(String query, int columnIndex, boolean useRegex, boolean caseSensitive) {
		this.query = query;
		this.columnIndex = columnIndex;
		this.useRegex = useRegex;
		this.caseSensitive = caseSensitive;
		this.searchCriteriaEngine = new SearchCriteriaEngine();
	}

	@Override
	public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
		Pattern pattern = null;
		try {
			pattern = searchCriteriaEngine.getPattern(useRegex, caseSensitive, query);
		} catch (PatternSyntaxException e) {
			Logging.warning(this, "Invalid regex ", e);
			return false;
		}

		int columnCount = entry.getValueCount();
		int start = (columnIndex == -1) ? 0 : columnIndex;
		int end = (columnIndex == -1) ? columnCount : (columnIndex + 1);

		return searchCriteriaEngine.matchAcrossColumns(entry::getStringValue, start, end, query, pattern, useRegex,
				caseSensitive);
	}
}
