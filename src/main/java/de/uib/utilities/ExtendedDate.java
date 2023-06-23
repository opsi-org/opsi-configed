/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

import java.util.Date;

public class ExtendedDate {
	private static final String INFINITE_IMPORT = "never";
	private static final String STRING_INFINITE = "INFINITE";

	public static final ExtendedDate INFINITE = new ExtendedDate(STRING_INFINITE);
	public static final ExtendedDate ZERO = new ExtendedDate("1900-01-01 00:00:0");

	private Date date;
	private String sDate;

	public ExtendedDate(Object value) {
		interpretAsTimestamp(value);
	}

	private void setFromDate(Date d) {
		date = d;
		sDate = date.toString();
		// remove time
		sDate = sDate.substring(0, sDate.indexOf(' '));
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

				if (value.indexOf(' ') == -1) {
					// append time for reading timestamp
					value = value + " 00:00:0";
				}
				setFromDate(java.sql.Timestamp.valueOf(value));
			}

			return;

		}

		if (ob instanceof Date) {
			setFromDate((Date) ob);
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

	public Date getDate() {
		return date;
	}
}
