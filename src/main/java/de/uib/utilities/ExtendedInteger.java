/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

import de.uib.utilities.logging.Logging;

public class ExtendedInteger implements Comparable<Integer> {
	private static final String INFINITE_IMPORT = "infinite";
	private static final String S_INFINITE = "INFINITE";
	private static final String DISPLAY_INFINITE = "\u221E";

	public static final ExtendedInteger INFINITE = new ExtendedInteger(S_INFINITE);
	public static final ExtendedInteger ZERO = new ExtendedInteger(0);

	private Integer number;
	private String value;

	private ExtendedInteger(Integer number, String value) {
		this.number = number;
		this.value = value;
	}

	public ExtendedInteger(int intVal) {
		number = Integer.valueOf(intVal);
		value = "" + intVal;
	}

	public ExtendedInteger(ExtendedInteger ei) {
		value = ei.getString();
		number = ei.getNumber();
	}

	public ExtendedInteger(String s) {

		number = null;
		value = null;

		if (s.equals(S_INFINITE) || s.equalsIgnoreCase(INFINITE_IMPORT) || s.equals(DISPLAY_INFINITE)) {
			value = S_INFINITE;
		} else {
			try {
				number = Integer.decode(s);
				// no exception:
				value = s;
			} catch (NumberFormatException ex) {
				Logging.error("possible values are numbers  or \"" + INFINITE_IMPORT + "\" resp. \"" + DISPLAY_INFINITE
						+ "\"", ex);
			}
		}
	}

	public Integer getNumber() {
		return number;
	}

	private String getString() {
		return value;
	}

	public String getDisplay() {
		if (value.equals(S_INFINITE)) {
			return DISPLAY_INFINITE;
		} else {
			return value;
		}
	}

	@Override
	public boolean equals(Object x) {
		if (x == null || !(x.getClass().equals(ExtendedInteger.class))) {
			return false;
		}

		ExtendedInteger ei = (ExtendedInteger) x;
		return ei.getString().equals(getString());
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	public ExtendedInteger add(ExtendedInteger ei) {
		ExtendedInteger result;

		if (ei.equals(INFINITE) || this.equals(INFINITE)) {
			result = new ExtendedInteger(INFINITE);
		} else {

			int sum = this.getNumber() + ei.getNumber();
			result = new ExtendedInteger(Integer.valueOf(sum), "" + sum);
		}

		return result;
	}

	public ExtendedInteger add(Integer z) {
		ExtendedInteger result;

		if (this.equals(INFINITE)) {
			result = new ExtendedInteger(INFINITE);
		} else {
			int sum = this.getNumber() + z;
			result = new ExtendedInteger(sum, "" + sum);
		}
		return result;
	}

	// Interface Comparable
	@Override
	public int compareTo(Integer integer) {

		if (this.equals(INFINITE)) {
			return -1;
		} else {
			return getNumber() - integer;
		}
	}

	@Override
	public String toString() {
		return getString();
	}
}
