package de.uib.utilities.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;

public class LowerCaseTextField extends javax.swing.JTextField
{
	//from apidoc

	public LowerCaseTextField()
	{
		super();
	}
	
	public LowerCaseTextField(String s)
	{
		super(s);
	}

	@Override
	protected Document createDefaultModel() {
		return new LowerCaseDocument();
	}


	private static class LowerCaseDocument extends PlainDocument {

		public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {

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


