/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

public class StringBlock {
	private String s;
	private int iEnd;
	private int contentStart;
	private int contentEnd;
	private boolean found;

	private char startC;
	private char endC;

	public StringBlock() {
		super();
	}

	public void setString(String s) {
		this.s = s;
	}

	public String getContent() {
		return s.substring(contentStart, contentEnd).trim();
	}

	public boolean hasFound() {

		return found;
	}

	public int getIEnd() {
		return iEnd;
	}

	private int findContentEnd() {
		int i = contentStart;
		int result = -1;
		int counterStartC = 0;
		while (result == -1 && i < s.length()) {
			char c = s.charAt(i);

			if (c == startC) {
				counterStartC++;
			} else if (c == endC) {
				if (counterStartC > 0) {
					counterStartC--;
				} else {
					result = i;
				}
			} else {
				// Do nothing when c is not at start or the end
			}

			i++;
		}

		return result;
	}

	public void forward(int iStart, char startC, char endC) {

		iEnd = iStart;
		found = false;
		this.startC = startC;
		this.endC = endC;

		contentStart = s.indexOf(startC, iStart);
		if (contentStart < 0) {
			// not found
			return;
		}

		if (s.substring(iStart, contentStart).trim().length() > 0) {
			// other chars than white space before contentStart
			return;
		}

		contentStart++;

		contentEnd = findContentEnd();

		if (contentEnd > -1) {
			found = true;
		} else {
			return;
		}

		iEnd = contentEnd;
		iEnd++;
	}
}
