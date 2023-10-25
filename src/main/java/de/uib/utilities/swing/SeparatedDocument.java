/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import de.uib.utilities.logging.Logging;

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
			if (appendCharIfAllowed(textBuf, startchar)) {
				try {
					if (checkMask && getText(offset, 1).equals(separator)) {

						// remove old separators
						remove(offset, 1);
					}

				} catch (BadLocationException ex) {
					Logging.warning(this, "Exception with location in giveAllowedCharacters", ex);
				}
			}
		}

		return textBuf.toString();
	}

	@Override
	protected void applyMask(AttributeSet a) throws BadLocationException {
		int oriLength = getLength();
		int insertOffs = 0;
		for (int i = 0; i < oriLength - 1; i++) {
			if (i % partsLength == 0) {
				insertOffs = insertOffs + partsLength;
				if (!getText(insertOffs, 1).equals(separator) && insertOffs < size) {
					insertStringPlain(insertOffs, separator, a);
				}

				insertOffs++;
			}
		}
	}
}
