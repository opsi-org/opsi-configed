/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import de.uib.configed.share.logging.Logging;

public class SeparatedDocument extends CheckedDocument {
	private int partsLength;
	private String separator;

	public SeparatedDocument(char[] allowedChars, int realSize, char separatingChar, int partsLength,
			boolean checkMask) {
		this.allowedChars = allowedChars;
		this.separator = "" + separatingChar;
		this.partsLength = partsLength;
		this.checkMask = checkMask;
		this.size = realSize + (int) ((double) realSize / partsLength - 1);
	}

	@Override
	public String giveAllowedCharacters(String s, int offset) {
		if (s == null) {
			return "";
		}

		StringBuilder textBuf = new StringBuilder();

		for (char startchar : s.toCharArray()) {
			try {
				if (appendCharIfAllowed(textBuf, startchar) && checkMask && getText(offset, 1).equals(separator)) {
					// remove old separators
					remove(offset, 1);
				}
			} catch (BadLocationException ex) {
				Logging.warning(this, ex, "Exception with location in giveAllowedCharacters");
			}
		}

		return textBuf.toString();
	}

	@Override
	protected void applyMask(AttributeSet a) throws BadLocationException {
		int expectedOffset = partsLength;
		while (expectedOffset <= getLength() && expectedOffset < size) {
			if (expectedOffset == getLength()) {
				insertStringPlain(expectedOffset, separator, a);
			} else {
				String existing;
				try {
					existing = getText(expectedOffset, 1);
				} catch (BadLocationException e) {
					Logging.debug(this, "Failed to retrieve text", e);
					break;
				}
				if (!separator.equals(existing)) {
					insertStringPlain(expectedOffset, separator, a);
				}
			}
			expectedOffset = expectedOffset + partsLength + 1;
		}
	}
}
