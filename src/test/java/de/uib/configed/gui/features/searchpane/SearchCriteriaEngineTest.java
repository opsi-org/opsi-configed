/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.IntFunction;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SearchCriteriaEngineTest {
	private final SearchCriteriaEngine engine = new SearchCriteriaEngine();

	// --- Pattern Compilation Tests ---

	@Test
	void shouldReturnNullPattern_whenRegexIsDisabled() {
		Pattern pattern = engine.getPattern(false, true, ".*");
		assertNull(pattern);
	}

	@Test
	void shouldReturnNullPattern_whenQueryIsNull() {
		Pattern pattern = engine.getPattern(true, true, null);
		assertNull(pattern);
	}

	@Test
	void shouldReturnNullPattern_whenQueryIsEmpty() {
		Pattern pattern = engine.getPattern(true, true, "");
		assertNull(pattern);
	}

	@Test
	void shouldReturnValidPattern_whenRegexAndValidQueryProvided() {
		Pattern pattern = engine.getPattern(true, false, "test");
		assertNotNull(pattern);
		assertTrue(pattern.matcher("testing").find());
	}

	@Test
	void shouldReturnNullPattern_whenInvalidRegexProvided() {
		// Catching the log warning internally in the class, but returning null as per logic
		Pattern pattern = engine.getPattern(true, false, "[invalid(regex");
		assertNull(pattern);
	}

	// --- Cell Matching Tests (Non-Regex) ---

	@ParameterizedTest
	@CsvSource({ "hello, world, true, true, false", // Case sensitive match
			"Hello, hello, false, false, true", // Case insensitive match
			"HELLO, hello, false, false, true", // Case insensitive match full upper
			"hello, hello, true, true, true", // Different value, case sensitive
			"world, hello, false, false, false" // No match case insensitive
	})
	void shouldMatchCellCorrectly(String cellValue, String query, boolean caseSensitive, boolean useRegex,
			boolean expected) {
		boolean result = engine.matchCell(cellValue, query, null, useRegex, caseSensitive);
		assertEquals(expected, result);
	}

	@Test
	void shouldReturnTrue_whenQueryIsNullOrEmpty() {
		assertTrue(engine.matchCell("anything", null, null, false, false));
		assertTrue(engine.matchCell("anything", "", null, false, false));
		assertTrue(engine.matchCell("anything", "   ", null, false, false));
	}

	@Test
	void shouldReturnFalse_whenCellValueIsNull() {
		assertFalse(engine.matchCell(null, "query", null, false, false));
	}

	// --- Cell Matching Tests (Regex) ---

	@ParameterizedTest
	@CsvSource({ "^test$, test, true, true, true", // Exact match regex
			"^te.*t$, testing, true, true, false", // Starts with ends with fail
			"^te.*t$, test, true, true, true", // Starts with ends with pass
			"^TEST$, testing, true, true, false", // Case sensitive fail
			"^TEST$, test, false, true, true", // Case insensitive pass
			"\\d+, 123, true, true, true", // Digits match
			"[a-z]+, 123, true, true, false" // Letters fail on digits
	})
	void shouldMatchCellWithRegex(String pattern, String cellValue, boolean caseSensitive, boolean useRegex,
			boolean expected) {
		Pattern p = engine.getPattern(useRegex, caseSensitive, pattern);
		boolean result = engine.matchCell(cellValue, pattern, p, useRegex, caseSensitive);
		assertEquals(expected, result);
	}

	@Test
	void shouldReturnFalse_whenRegexMatchesNothing() {
		Pattern p = engine.getPattern(true, false, "xyz");
		assertFalse(engine.matchCell("abc def", "xyz", p, true, false));
	}

	// --- Multi-Column Matching Tests ---

	@Test
	void shouldMatchAcrossColumns_whenAnyColumnMatches() {
		IntFunction<Object> provider = col -> {
			if (col == 0) {
				return "first";
			}
			if (col == 1) {
				return "second";
			}
			if (col == 2) {
				return "third";
			}
			return null;
		};

		// Searching for "sec" across all columns
		Pattern p = engine.getPattern(false, false, "sec");
		boolean result = engine.matchAcrossColumns(provider, 0, 3, "sec", p, false, false);
		assertTrue(result);
	}

	@Test
	void shouldNotMatchAcrossColumns_whenNoColumnMatches() {
		IntFunction<Object> provider = col -> "value";

		Pattern p = engine.getPattern(false, false, "nomatch");
		boolean result = engine.matchAcrossColumns(provider, 0, 2, "nomatch", p, false, false);
		assertFalse(result);
	}

	@Test
	void shouldSkipNullValuesAcrossColumns() {
		IntFunction<Object> provider = col -> {
			if (col == 0) {
				return "value1";
			}
			if (col == 1) {
				return null;
			}
			return "value2";
		};

		// Should find "value2" despite null at index 1
		Pattern p = engine.getPattern(false, false, "value2");
		boolean result = engine.matchAcrossColumns(provider, 0, 3, "value2", p, false, false);
		assertTrue(result);
	}

	@Test
	void shouldRespectColumnRange() {
		IntFunction<Object> provider = col -> "col" + col;

		// Search for "col1" but range is 2 to 4
		Pattern p = engine.getPattern(false, false, "col1");
		boolean result = engine.matchAcrossColumns(provider, 2, 4, "col1", p, false, false);
		assertFalse(result);
	}

	@Test
	void shouldUseStartEndLogicForSpecificColumn() {
		IntFunction<Object> provider = col -> col == 1 ? "target" : "other";

		// Match only column 1
		Pattern p = engine.getPattern(false, false, "target");
		boolean result = engine.matchAcrossColumns(provider, 1, 2, "target", p, false, false);
		assertTrue(result);
	}
}
