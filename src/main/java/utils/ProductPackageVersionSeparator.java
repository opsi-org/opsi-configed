/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package utils;

public final class ProductPackageVersionSeparator {
	public static final String FOR_DISPLAY = "-";
	public static final String FOR_KEY = ";";

	private ProductPackageVersionSeparator() {
	}

	public static String formatKeyForDisplay(String key) {
		if (key == null) {
			return null;
		}

		int i = key.indexOf(FOR_KEY);
		if (i == -1) {
			return key;
		}

		String result = key.substring(0, i);
		if (i < key.length()) {
			result = result + FOR_DISPLAY + key.substring(i + 1);
		}

		return result;
	}
}
