/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class GeneralFrame extends JDialog implements ActionListener {

	private JButton jButton1 = new JButton();

	public GeneralFrame(Frame owner, String title, boolean modal) {
		super(owner, modal);
		super.setTitle(title);
		if (!Main.FONT) {
			super.setFont(Globals.defaultFont);
		}
		super.setIconImage(Globals.mainIcon);
	}

	public void setup() {

		pack();
	}

	public void addPanel(JPanel pane) {

		getContentPane().add(pane);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		jButton1.requestFocus();
	}

	private void doAction1() {
		Logging.debug(this, "doAction1");
		leave();
	}

	public void leave() {
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

	// ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jButton1) {

			doAction1();
		}
	}
}
