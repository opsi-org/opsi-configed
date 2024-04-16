/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.GlassPane;
import de.uib.utils.logging.Logging;

public class SecondaryFrame extends JFrame {
	protected Container masterFrame;

	private GlassPane glassPane;

	public SecondaryFrame() {
		this.masterFrame = ConfigedMain.getMainFrame();
		if (masterFrame == null) {
			Logging.warning(this.getClass(), "masterFrame yet null");
		}

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

	public void activateLoadingCursor() {
		glassPane.activateLoadingCursor();
	}

	public void deactivateLoadingCursor() {
		glassPane.deactivateLoadingCursor();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			setVisible(false);
		}
	}
}
