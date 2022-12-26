package de.uib.utilities.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class SeparatedDocument extends CheckedDocument {
	int partsLength;
	String separator;

	public SeparatedDocument(char[] allowedChars, int realSize, char separatingChar, int partsLength,
			boolean checkMask) {
		super();
		this.allowedChars = allowedChars;
		this.separator = "" + separatingChar;
		this.partsLength = partsLength;
		this.checkMask = checkMask;
		this.size = realSize + (int) (realSize / partsLength - 1);
	}

	@Override
	public String giveAllowedCharacters(String s, int offset) {
		if (s == null)
			return "";

		char[] startchars = s.toCharArray();
		StringBuffer textBuf = new StringBuffer();

		for (int i = 0; i < startchars.length; i++) {
			if (appendCharIfAllowed(textBuf, startchars[i])) {
				try {
					if (checkMask && (getText(offset, 1).equals(separator)))
						remove(offset, 1); // remove old separators

					remove(offset, 1); // remove one char of existing chars
				} catch (BadLocationException ex) {
				}
			}
		}

		return textBuf.toString();
		//
	}

	@Override
	protected void applyMask(AttributeSet a) throws BadLocationException {
		int oriLength = getLength();
		int insertOffs = 0;
		for (int i = 0; i < oriLength - 1; i++) {
			if ((i % partsLength) == 0) {
				insertOffs = insertOffs + partsLength;
				if (!getText(insertOffs, 1).equals(separator) && insertOffs < size)
					insertStringPlain(insertOffs, separator, a);
				insertOffs++;
			}
		}
	}

}
