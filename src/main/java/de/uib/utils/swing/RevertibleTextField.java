/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.text.Document;

public class RevertibleTextField extends JTextField {
	private String previousText;

	public RevertibleTextField() {
		super();
		addCustomKeyListener();
	}

	public RevertibleTextField(String s) {
		super(s);
		addCustomKeyListener();
	}

	public RevertibleTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		addCustomKeyListener();
	}

	private void addCustomKeyListener() {
		super.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setText(previousText);
				}
			}
		});
	}

	@Override
	public void setText(String s) {
		previousText = s != null ? s : "";
		super.setText(s);
	}

	public boolean isTextChanged() {
		if (previousText == null && getText() == null) {
			return false;
		}

		if (previousText == null && getText() != null) {
			return true;
		}

		return !previousText.equals(getText());
	}
}
