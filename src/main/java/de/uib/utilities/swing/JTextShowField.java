/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import javax.swing.JTextField;

public class JTextShowField extends JTextField {
	public JTextShowField(boolean editable) {
		this("", editable);
	}

	public JTextShowField(String s) {
		this(s, false);
	}

	public JTextShowField(String s, boolean editable) {
		super(s);
		super.setEditable(editable);
	}

	public JTextShowField() {
		this("");
	}

	@Override
	public void setText(String s) {
		super.setText(s);
		setCaretPosition(0);
		setToolTipText(s);
	}
}
