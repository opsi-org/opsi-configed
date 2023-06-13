/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licences;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.util.Map;

import de.uib.configed.ConfigedMain.LicencesTabStatus;
import de.uib.configed.gui.GlassPane;
import de.uib.utilities.swing.SecondaryFrame;
import de.uib.utilities.swing.tabbedpane.TabController;
import de.uib.utilities.swing.tabbedpane.TabbedPaneX;

public class LicencesFrame extends SecondaryFrame {

	private TabbedPaneX panel;

	private GlassPane glassPane;

	public LicencesFrame(TabController controller) {
		super();
		panel = new TabbedPaneX(controller);
		init();
	}

	@Override
	public void setGlobals(Map<String, Object> globals) {
		panel.setGlobals(globals);
		setIconImage((Image) globals.get("mainIcon"));
		setTitle((String) globals.get("APPNAME"));
	}

	private void init() {
		add(panel);

		glassPane = new GlassPane();
		setGlassPane(glassPane);

		pack();
	}

	public void activateLoadingPane() {
		glassPane.activate(true);
	}

	public void disactivateLoadingPane() {
		glassPane.activate(false);
	}

	@Override
	public void start() {
		setVisible(true);
		setExtendedState(Frame.NORMAL);
	}

	public TabbedPaneX getMainPanel() {
		return panel;
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, using an extra title
	 */
	public void addTab(LicencesTabStatus s, String title, Component c) {
		panel.addTab(s, title, c);
	}

	/**
	 * removes a tab
	 */
	public void removeTab(LicencesTabStatus s) {
		panel.removeTab(s);
	}
}
