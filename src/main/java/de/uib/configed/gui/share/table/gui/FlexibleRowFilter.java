/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Color;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.logging.Logging;

public class FlexibleRowFilter extends RowFilter<TableModel, Integer> {
	private final String query;
	private final int columnIndex;
	private final boolean useRegex;
	private final boolean caseSensitive;

	public FlexibleRowFilter(String query, int columnIndex, boolean useRegex, boolean caseSensitive) {
		this.query = query;
		this.columnIndex = columnIndex;
		this.useRegex = useRegex;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
		Pattern pattern = null;
		if (useRegex) {
			try {
				pattern = caseSensitive ? Pattern.compile(query) : Pattern.compile(query, Pattern.CASE_INSENSITIVE);
			} catch (PatternSyntaxException e) {
				Logging.warning(this, "Invalid regex ", e);
				return false;
			}
		}

		return searchAndFilter(pattern, entry);
	}

	@SuppressWarnings({ "java:S4968", "java:S3776", "java:S1541" })
	private boolean searchAndFilter(Pattern pattern, Entry<? extends TableModel, ? extends Integer> entry) {
		int columnCount = entry.getValueCount();
		int start = (columnIndex == -1) ? 0 : columnIndex;
		int end = (columnIndex == -1) ? columnCount : (columnIndex + 1);
		String targetQuery = caseSensitive ? query : query.toLowerCase(Locale.ROOT);

		for (int i = start; i < end; i++) {
			Object value = entry.getValue(i);
			Color color = InstallationStatus.getLabel2TextColor().get(value);
			String cellValue = entry.getStringValue(i);
			if (cellValue == null || (color != null && color.equals(Globals.INVISIBLE))) {
				continue;
			}

			if (useRegex) {
				if (pattern.matcher(cellValue).find()) {
					return true;
				}
			} else {
				String content = caseSensitive ? cellValue : cellValue.toLowerCase(Locale.ROOT);
				if (content.contains(targetQuery)) {
					return true;
				}
			}
		}

		return false;
	}
}
