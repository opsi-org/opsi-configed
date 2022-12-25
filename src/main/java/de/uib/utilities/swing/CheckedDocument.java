package de.uib.utilities.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class CheckedDocument extends PlainDocument {
	char[] allowedChars;
	int size;
	boolean checkMask = false;

	public CheckedDocument() {

	}

	public CheckedDocument(char[] allowedChars, int realSize) {
		this.allowedChars = allowedChars;
		this.size = realSize;
	}

	public boolean appendCharIfAllowed(StringBuffer s, char c) {
		char cCorrected = c;

		if (allowedChars == null)
			return false;

		boolean result = false;

		for (int j = 0; j < allowedChars.length; j++) {
			if (Character.toLowerCase(allowedChars[j]) == Character.toLowerCase(c)) {
				if (Character.isLowerCase(allowedChars[j]))
					cCorrected = Character.toLowerCase(c);
				else if (Character.isUpperCase(allowedChars[j]))
					cCorrected = Character.toUpperCase(c);
				s.append(cCorrected);
				result = true;
				break;

			}
		}

		return result;
	}

	protected String giveAllowedCharacters(String s, int offset) {

		if (s == null)
			return "";

		char[] startchars = s.toCharArray();
		StringBuffer textBuf = new StringBuffer();

		for (int i = 0; i < startchars.length; i++) {
			appendCharIfAllowed(textBuf, startchars[i]);
		}

		return textBuf.toString();
	}

	protected void applyMask(AttributeSet a) throws BadLocationException {
	}

	protected void insertStringPlain(int offs, String s, AttributeSet a) throws BadLocationException {

		super.insertString(offs, s, a);
	}

	@Override
	public void insertString(int offs, String s, AttributeSet a) throws BadLocationException {

		if (s == null)
			return;

		if (size > -1 && offs >= size)
			return;

		String corrected = giveAllowedCharacters(s, offs);

		
		if (size > -1 && offs + corrected.length() > size)
			corrected = corrected.substring(0, size - offs);

		

		insertStringPlain(offs, corrected, a);
		if (checkMask)
			applyMask(a);
	}

}