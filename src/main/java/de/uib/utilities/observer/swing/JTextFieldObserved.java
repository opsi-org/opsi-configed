/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.observer.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public class JTextFieldObserved extends JTextField implements KeyListener {
	private String startText = "";

	public JTextFieldObserved() {
		super("");
		super.addKeyListener(this);
	}

	@Override
	public void setText(String s) {
		super.setText(s);
		startText = s;
		setCaretPosition(0);
	}

	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setText(startText);
			setCaretPosition(startText.length());
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			transferFocus();
		} else {
			// Do nothing on other keys
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

}
