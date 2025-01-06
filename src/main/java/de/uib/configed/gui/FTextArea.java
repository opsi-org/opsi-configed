/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import de.uib.configed.Globals;

public class FTextArea extends FGeneralDialog {
	private JTextPane jTextPane = new JTextPane();

	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList) {
		super(owner, title, modal, buttonList);
		initFTextArea(Globals.DEFAULT_FTEXTAREA_WIDTH, Globals.DEFAULT_FTEXTAREA_HEIGHT);
	}

	public void setMessage(String message) {
		jTextPane.setText(message);
		jTextPane.setCaretPosition(0);
	}

	private void initFTextArea(int preferredWidth, int preferredHeight) {
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		jTextPane.setAlignmentX(SwingConstants.CENTER);
		jTextPane.setText("          ");
		jTextPane.setEditable(false);

		scrollpane.getViewport().add(jTextPane, null);
	}
}
