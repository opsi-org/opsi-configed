/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import de.uib.utilities.logging.Logging;

public class ExtendedDate {
	private static final String INFINITE_IMPORT = "never";
	private static final String STRING_INFINITE = "INFINITE";

	public static final ExtendedDate INFINITE = new ExtendedDate(STRING_INFINITE);
	public static final ExtendedDate ZERO = new ExtendedDate("1900-01-01 00:00:0");

	private LocalDateTime date;
	private String sDate;

	// TODO where will this be needed?
	public ExtendedDate(Object value) {
		interpretAsTimestamp(value);
	}

	private void setFromDate(LocalDateTime d) {
		date = d;
		sDate = date.toString();
		// remove time
		sDate = sDate.substring(0, sDate.indexOf('T'));
	}

	private void interpretAsTimestamp(Object ob) {
		date = null;
		sDate = null;

		if (ob == null) {
			return;
		}

		if (ob instanceof String) {
			String value = ((String) ob).trim();
			if (value.equalsIgnoreCase(INFINITE_IMPORT) || value.equalsIgnoreCase(STRING_INFINITE)) {
				sDate = STRING_INFINITE;
			} else {

				// extend

				if (!value.contains(" ")) {
					// append time for reading timestamp
					value = value + "T00:00:0";
				} else {
					value = value.replace(' ', 'T');
				}

				value = value + '0';

				try {
					setFromDate(LocalDateTime.parse(value));
				} catch (DateTimeParseException e) {
					Logging.warning(this, "Cannot parse value to get Timestamp of " + value, e);
				}
			}

			return;
		}

		if (ob instanceof LocalDateTime) {
			setFromDate((LocalDateTime) ob);
		}
	}

	@Override
	public String toString() {
		return sDate;
	}

	@Override
	public boolean equals(Object ob) {
		return this == ob || (ob instanceof ExtendedDate && toString().equals(ob.toString()));
	}

	@Override
	public int hashCode() {
		return sDate.hashCode();
	}

	public LocalDateTime getDate() {
		return date;
	}
}
