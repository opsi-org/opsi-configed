/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.type;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import de.uib.configed.share.logging.Logging;

public final class DateExtendedByVars extends Date {
	public static final String MINUS = "minus";
	public static final char CHAR_DELIMITER = '%';

	private DateExtendedByVars() {
		super(0);
	}

	private static String stripTimeFromDay(String datetime) {
		int idx = datetime.indexOf(" ");
		if (idx < 0) {
			return datetime;
		}

		return datetime.substring(0, idx);
	}

	public static String interpretVar(final String input) {
		Logging.debug("OpsiDataDateMatcher interpretVar in ", input);

		String result = input;
		Optional<String> maybeToken = extractToken(input);
		Optional<String> maybeReplaced = maybeToken.filter(token -> {
			if (!token.startsWith(MINUS)) {
				Logging.info("OpsiDataDateMatcher interpretVar expected: \"", MINUS, "\"");
				return false;
			}
			return true;
		}).flatMap(token -> parseSubtrahend(token.substring(MINUS.length())).map(days -> {
			String dateString = calculateDateString(days);
			Logging.debug("OpsiDataDateMatcher interpretVar produced time ", dateString);
			String tokenFull = CHAR_DELIMITER + token + CHAR_DELIMITER;
			return input.replace(tokenFull, dateString);
		}));

		if (maybeReplaced.isPresent()) {
			result = maybeReplaced.get();
		}

		return result;
	}

	private static Optional<String> extractToken(String s) {
		int first = s.indexOf(CHAR_DELIMITER);
		if (first == -1 || first + 1 >= s.length()) {
			if (first == s.length() - 1) {
				Logging.info("OpsiDataDateMatcher interpretVar \"", CHAR_DELIMITER, "\" found at end of string");
			}
			return Optional.empty();
		}
		int second = s.indexOf(CHAR_DELIMITER, first + 1);
		if (second == -1)
			return Optional.empty();
		return Optional.of(s.substring(first + 1, second));
	}

	private static Optional<Integer> parseSubtrahend(String s) {
		try {
			return Optional.of(Integer.parseInt(s));
		} catch (NumberFormatException ex) {
			Logging.info("OpsiDataDateMatcher interpretVar not a number: ", s, ", error: ", ex);
			return Optional.empty();
		}
	}

	private static String calculateDateString(int daysToSubtract) {
		LocalDate date = LocalDate.now().minusDays(daysToSubtract);
		return stripTimeFromDay(date.toString());
	}
}
