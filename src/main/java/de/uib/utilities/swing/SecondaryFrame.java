/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.GlassPane;
import de.uib.utilities.logging.Logging;

public class SecondaryFrame extends JFrame implements WindowListener {

	protected Container masterFrame;

	private GlassPane glassPane;

	public SecondaryFrame() {
		this.masterFrame = ConfigedMain.getMainFrame();
		if (masterFrame == null) {
			Logging.warning(this.getClass(), "masterFrame yet null");
		}

		super.addWindowListener(this);

		glassPane = new GlassPane();
		super.setGlassPane(glassPane);
	}

	public void start() {
		setExtendedState(Frame.NORMAL);
		centerOnParent();
		setVisible(true);
		Logging.info(this, "started");
	}

	public void centerOnParent() {
		setLocationRelativeTo(masterFrame);
	}

	// for overriding
	private void callExit() {
		setVisible(false);
	}

	public void activateLoadingCursor() {
		glassPane.activateLoadingCursor();
	}

	public void disactivateLoadingCursor() {
		glassPane.disactivateLoadingCursor();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			callExit();
		}

	}

	/* WindowListener implementation */
	@Override
	public void windowClosing(WindowEvent e) {
		callExit();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowClosed(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowActivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeactivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowIconified(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeiconified(WindowEvent e) {
		/* Not needed */}
}
