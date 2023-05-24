/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class LowerCaseTextField extends JTextField {
	// from apidoc

	public LowerCaseTextField() {
		super();
	}

	public LowerCaseTextField(String s) {
		super(s);
	}

	@Override
	protected Document createDefaultModel() {
		return new LowerCaseDocument();
	}

	private static class LowerCaseDocument extends PlainDocument {

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

			if (str == null) {
				return;
			}
			char[] lower = str.toCharArray();
			for (int i = 0; i < lower.length; i++) {
				lower[i] = Character.toLowerCase(lower[i]);
			}
			super.insertString(offs, new String(lower), a);
		}
	}
}
