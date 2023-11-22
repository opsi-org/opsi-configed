/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Frame;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;

import utils.Utils;

public class GeneralFrame extends JDialog {

	public GeneralFrame(Frame owner, String title, boolean modal) {
		super(owner, modal);
		super.setTitle(title);
		super.setIconImage(Utils.getMainIcon());
	}

	public void addPanel(JPanel pane) {
		getContentPane().add(pane);
	}

	private void leave() {
		setVisible(false);
		dispose();
	}

	// Events
	// window

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			leave();
		}
		super.processWindowEvent(e);
	}
}
