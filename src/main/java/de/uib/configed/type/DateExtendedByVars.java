/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.sql.Date;
import java.util.Calendar;

import de.uib.utils.logging.Logging;

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

	public static String interpretVar(final String s) {
		Logging.debug("OpsiDataDateMatcher interpretVar in " + s);

		int i = s.indexOf(CHAR_DELIMITER);

		if (i == -1) {
			return s;
		}

		i++;

		if (i > s.length()) {
			Logging.info("OpsiDataDateMatcher interpretVar \"" + CHAR_DELIMITER + "\" found at end of string");
			return s;
		}

		String replaceContent = s.substring(i);
		i = replaceContent.indexOf(CHAR_DELIMITER);

		replaceContent = replaceContent.substring(0, i);

		Logging.debug("OpsiDataDateMatcher interpretVar replaceContent " + replaceContent);

		if (!replaceContent.startsWith(MINUS)) {
			Logging.info("OpsiDataDateMatcher interpretVar expected: \"" + MINUS + "\"");
			return s;
		}

		String subtrahendS = replaceContent.substring(MINUS.length());

		Integer subtrahend = null;

		try {
			subtrahend = Integer.valueOf(subtrahendS);
		} catch (NumberFormatException ex) {
			Logging.info("OpsiDataDateMatcher interpretVar not a number: " + subtrahendS + ", error: " + ex);
			return s;
		}

		Calendar cal = new java.util.GregorianCalendar();

		cal.add(Calendar.DAY_OF_MONTH, -subtrahend);

		java.util.Date myTime = new java.sql.Timestamp(cal.getTimeInMillis());

		String timeS = stripTimeFromDay(myTime.toString());

		Logging.debug("OpsiDataDateMatcher interpretVar produced time " + timeS);

		String toReplace = CHAR_DELIMITER + replaceContent + CHAR_DELIMITER;

		return s.replace(toReplace, timeS);
	}
}
