/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

import java.io.File;

public final class FileX {

	// private constructor to hide the implicit public one
	private FileX() {
	}

	public static String getPath(String[] parts, boolean absolute, String separator) {
		if (parts == null || parts.length == 0) {
			return "";
		}

		StringBuilder result = new StringBuilder(parts[0]);

		if (absolute) {
			result.insert(0, separator);
		}

		for (int i = 1; i < parts.length; i++) {
			result.append(separator + parts[i]);
		}

		return result.toString();
	}

	public static String getLocalsystemPath(String[] parts) {
		return getPath(parts, false, File.separator);
	}

	// share, String[] parts)
	public static String getRemotePath(String server, String share, String[] parts) {
		if (parts == null || parts.length == 0) {
			return "";
		}

		StringBuilder result = new StringBuilder(File.separator + File.separator + server + File.separator + share);

		for (int i = 0; i < parts.length; i++) {
			result.append(File.separator + parts[i]);
		}

		// mount

		return result.toString();
	}

}
