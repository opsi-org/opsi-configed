/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities;

import java.util.Comparator;

import de.uib.utilities.logging.Logging;

public class IntComparatorForStrings implements Comparator<String> {
	@Override
	public int compare(String o1, String o2) {
		Integer i1 = o1.isBlank() ? -1 : convStrToInt(o1);
		Integer i2 = o2.isBlank() ? -1 : convStrToInt(o2);
		return i1.compareTo(i2);
	}

	private static Integer convStrToInt(String str) {
		Integer result = 0;
		try {
			result = Integer.parseInt(str);
		} catch (NumberFormatException ex) {
			Logging.debug("not a number " + str);
		}
		return result;
	}
}
