package de.uib.utilities;

import java.util.Comparator;

import de.uib.utilities.logging.Logging;

public class IntComparatorForObjects implements Comparator<Object> {

	@Override
	public int compare(Object o1, Object o2) {

		int result = 0;
		int i1 = Integer.MAX_VALUE;
		int i2 = Integer.MIN_VALUE;

		String s1 = ("" + o1).trim();
		String s2 = ("" + o2).trim();

		try {
			if ((s1.length() > 1) && (s1.charAt(0) == '(' && (s1.charAt(s1.length() - 1)) == ')')) {
				s1 = s1.substring(1, s1.length() - 1);
			}

			i1 = Integer.parseInt(s1);
		}

		catch (Exception ex) {
			Logging.info("o1 no number " + o1 + " s1: " + s1);
		}

		try {
			if ((s2.length() > 1) && (s2.charAt(0) == '(' && (s2.charAt(s2.length() - 1)) == ')')) {
				s2 = s2.substring(1, s2.length() - 1);
			}

			i2 = Integer.parseInt(s2);
		}

		catch (Exception ex) {
			Logging.info(this, "exception " + ex);
			Logging.info("o2 no number " + o2 + " s2: " + s2);
		}

		if (i1 < i2) {
			result = -1;
		} else if (i1 > i2) {
			result = +1;
		}

		return result;
	}

	@Override
	public String toString() {
		return getClass().toString();
	}

}
