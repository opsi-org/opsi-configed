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
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

import de.uib.configed.Globals;

public class FTextArea extends FGeneralDialog {
	private JTextPane jTextPane = new JTextPane();

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
		jTextPane.setText(message);
		jTextPane.setCaretPosition(0);
	}

	public JTextComponent getTextComponent() {
		return jTextPane;
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

		jTextPane.setAlignmentX(SwingConstants.CENTER);
		jTextPane.setText("          ");
		jTextPane.setEditable(false);

		scrollpane.getViewport().add(jTextPane, null);

		jTextPane.addKeyListener(this);
	}

	// KeyListener

	@Override
	public void keyReleased(KeyEvent e) {

		if (!e.isShiftDown() && e.getSource() == jTextPane && e.getKeyCode() == KeyEvent.VK_TAB) {
			jButton1.requestFocus();
		}

		if (e.isShiftDown() && e.getSource() == jButton1 && e.getKeyCode() == KeyEvent.VK_TAB) {
			jTextPane.requestFocus();
		}
	}
}
