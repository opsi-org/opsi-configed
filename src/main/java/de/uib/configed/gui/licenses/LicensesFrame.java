/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licenses;

import java.awt.Component;
import java.awt.Frame;

import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.LicensesTabStatus;
import de.uib.utilities.swing.SecondaryFrame;
import de.uib.utilities.swing.tabbedpane.TabbedPaneX;

public class LicensesFrame extends SecondaryFrame {
	private TabbedPaneX panel;

	public LicensesFrame(ConfigedMain configedMain) {
		super();
		panel = new TabbedPaneX(configedMain);
		super.add(panel);
	}

	@Override
	public void start() {
		setVisible(true);
		setExtendedState(Frame.NORMAL);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, using an extra title
	 */
	public void addTab(LicensesTabStatus s, String title, Component c) {
		panel.addTab(s, title, c);
	}
}
