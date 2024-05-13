/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

public final class ErrorCode {
	public static final int NO_ERROR = 0;
	public static final int INITIALIZATION_ERROR = 1;
	public static final int CONNECTION_ERROR = 2;
	public static final int CLIENTNAMES_FILENAME_MISSING = 11;

	private ErrorCode() {
	}

	public static String tell(int n) {
		String result = "";
		if (n > 0) {
			result = "problem type " + n + ": ";
		}

		return switch (n) {
		case NO_ERROR -> result + "no error occured";
		case INITIALIZATION_ERROR -> result + "inititalization error";
		case CONNECTION_ERROR -> result + "connection error";
		case CLIENTNAMES_FILENAME_MISSING -> result + "REQUIRED: name of file with clientnames";
		default -> result + "_";
		};
	}
}
