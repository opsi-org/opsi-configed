/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import de.uib.utils.logging.Logging;

public class ExtendedDate {
	private static final String INFINITE_IMPORT = "never";
	private static final String STRING_INFINITE = "INFINITE";

	public static final ExtendedDate INFINITE = new ExtendedDate(STRING_INFINITE);
	public static final ExtendedDate ZERO = new ExtendedDate("1900-01-01 00:00:0");

	private LocalDateTime date;
	private String sDate;

	public ExtendedDate(Object value) {
		interpretAsTimestamp(value);
	}

	private void setFromDate(LocalDateTime d) {
		date = d;
		sDate = date.toString();
		// remove time
		sDate = sDate.substring(0, sDate.indexOf('T'));
	}

	private void interpretAsTimestamp(Object object) {
		date = null;
		sDate = null;

		if (object == null) {
			return;
		}

		if (object instanceof String string) {
			string = string.trim();
			if (string.equalsIgnoreCase(INFINITE_IMPORT) || string.equalsIgnoreCase(STRING_INFINITE)) {
				sDate = STRING_INFINITE;
			} else {
				// extend

				if (!string.contains(" ")) {
					// append time for reading timestamp
					string = string + "T00:00:0";
				} else {
					string = string.replace(' ', 'T');
				}

				string = string + '0';

				try {
					setFromDate(LocalDateTime.parse(string));
				} catch (DateTimeParseException e) {
					Logging.warning(this, e, "Cannot parse value to get Timestamp of ", string);
				}
			}

			return;
		}

		if (object instanceof LocalDateTime localDateTime) {
			setFromDate(localDateTime);
		}
	}

	@Override
	public String toString() {
		return sDate;
	}

	@Override
	public boolean equals(Object ob) {
		return this == ob
				|| ((ob != null && ob.getClass().equals(ExtendedDate.class)) && toString().equals(ob.toString()));
	}

	@Override
	public int hashCode() {
		return sDate.hashCode();
	}

	public LocalDateTime getDate() {
		return date;
	}
}
