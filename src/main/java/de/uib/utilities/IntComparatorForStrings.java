package de.uib.utilities;

import java.util.Comparator;

import de.uib.utilities.logging.Logging;

public class IntComparatorForStrings implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		int result = 0;
		int i1 = Integer.MAX_VALUE;
		int i2 = Integer.MIN_VALUE;

		try {
			i1 = Integer.parseInt(o1);
		}

		catch (NumberFormatException ex) {
			Logging.debug("o1 no number " + o1);
		}

		try {
			i2 = Integer.parseInt(o2);
		}

		catch (NumberFormatException ex) {
			Logging.debug("o2 no number " + o2);
		}

		if (i1 < i2) {
			result = -1;
		} else if (i1 > i2) {
			result = +1;
		}

		return result;
	}

}
