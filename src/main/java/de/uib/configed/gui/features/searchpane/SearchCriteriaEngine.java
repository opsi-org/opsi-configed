/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import java.util.Locale;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.uib.configed.share.logging.Logging;

public class SearchCriteriaEngine {
	public Pattern getPattern(boolean useRegex, boolean caseSensitive, String query) {
		if (!useRegex) {
			return null;
		}

		Pattern pattern = null;

		try {
			pattern = compilePattern(query, caseSensitive);
		} catch (PatternSyntaxException e) {
			Logging.warning("Failed regex pattern" + e);
			return null;
		}

		return pattern;
	}

	private static Pattern compilePattern(String query, boolean caseSensitive) throws PatternSyntaxException {
		if (query == null || query.isEmpty()) {
			return null;
		}
		return caseSensitive ? Pattern.compile(query) : Pattern.compile(query, Pattern.CASE_INSENSITIVE);
	}

	public boolean matchCell(Object valueObj, String query, Pattern pattern, boolean useRegex, boolean caseSensitive) {
		if (query == null || query.trim().isEmpty()) {
			return true;
		}

		if (valueObj == null) {
			return false;
		}

		String cellValue = valueObj.toString();
		String targetQuery = caseSensitive ? query : query.toLowerCase(Locale.ROOT);
		String normalizedCell = caseSensitive ? cellValue : cellValue.toLowerCase(Locale.ROOT);

		boolean result = normalizedCell.contains(targetQuery);

		if (useRegex && pattern != null) {
			result = pattern.matcher(cellValue).find();
		}

		return result;
	}

	public boolean matchAcrossColumns(IntFunction<Object> provider, int startCol, int endCol, String query,
			Pattern pattern, boolean useRegex, boolean caseSensitive) {
		for (int i = startCol; i < endCol; i++) {
			Object val = provider.apply(i);
			if (val == null) {
				continue;
			}

			if (matchCell(val, query, pattern, useRegex, caseSensitive)) {
				return true;
			}
		}
		return false;
	}
}
