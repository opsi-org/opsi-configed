/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import de.uib.Main;
import de.uib.configed.Globals;

public class FTextArea extends FGeneralDialog {

	private JTextArea jTextArea1 = new JTextArea();

	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList) {
		super(owner, title, modal, buttonList);
		initFTextArea();
	}

	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner, title, modal, buttonList, preferredWidth, preferredHeight);
		initFTextArea(preferredWidth, preferredHeight);
	}

	public FTextArea(JFrame owner, String title, String message, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner, title, modal, buttonList, preferredWidth, preferredHeight);
		initFTextArea(preferredWidth, preferredHeight);
		setMessage(message);
	}

	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons, int preferredWidth,
			int preferredHeight) {

		super(owner, title, modal, buttonList, icons, buttonList.length, preferredWidth, preferredHeight);
		initFTextArea(preferredWidth, preferredHeight);
	}

	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons, int preferredWidth,
			int preferredHeight, JPanel addPane) {
		super(owner, title, modal, buttonList, icons, buttonList.length, preferredWidth, preferredHeight, false,
				addPane);
		super.checkAdditionalPane();
		initFTextArea(preferredWidth, preferredHeight);

	}

	public void setMessage(String message) {
		jTextArea1.setText(message);
		jTextArea1.setCaretPosition(0);
	}

	public JTextComponent getTextComponent() {
		return jTextArea1;
	}

	private void initFTextArea() {
		initFTextArea(Globals.DEFAULT_FTEXTAREA_WIDTH, Globals.DEFAULT_FTEXTAREA_HEIGHT);
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return false;
	}

	private void initFTextArea(int preferredWidth, int preferredHeight) {
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		jTextArea1.setLineWrap(true);
		jTextArea1.setWrapStyleWord(true);
		jTextArea1.setOpaque(true);
		if (!Main.THEMES) {
			jTextArea1.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
		jTextArea1.setText("          ");
		jTextArea1.setEditable(false);
		if (!Main.FONT) {
			jTextArea1.setFont(Globals.DEFAULT_FONT_BIG);
		}

		scrollpane.getViewport().add(jTextArea1, null);

		jTextArea1.addKeyListener(this);

		pack();
	}

	// KeyListener

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
		}

		if (!shiftPressed && e.getSource() == jTextArea1 && e.getKeyCode() == KeyEvent.VK_TAB) {
			jButton1.requestFocus();
		}

		if (shiftPressed && e.getSource() == jButton1 && e.getKeyCode() == KeyEvent.VK_TAB) {
			jTextArea1.requestFocus();
		}
	}
}
