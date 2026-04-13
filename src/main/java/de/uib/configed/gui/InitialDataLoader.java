/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.uib.configed.share.SplitPaneStateManager;
import de.uib.configed.share.logging.Logging;

public class InitialDataLoader extends SwingWorker<Void, Void> {
	private ConfigedMain configedMain;
	private boolean isDataLoaded;

	public InitialDataLoader(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	@Override
	protected Void doInBackground() throws Exception {
		isDataLoaded = false;
		configedMain.preloadData();
		return null;
	}

	@Override
	public void done() {
		configedMain.initGui();

		isDataLoaded = true;

		Logging.checkErrorList();
		ConfigedMain.getLoginDialog().setVisible(false);

		Logging.info("setting mainframe visible");

		ConfigedMain.getMainFrame().setVisible(true);

		// The client selection JSplitPane is registered using SwingUtilities.invokeLater because this ensures
		// that its preferred size is correctly calculated. Although it might seem logical to register it in
		// ClientConfiguration, doing so earlier causes the preferred size to be incorrect, leading to an
		// an improper divider location. This here points to potential issues with the timing of GUI
		// initialization and suggests that the initialization order may not be fully synchronized for proper
		// layout calculation.
		SwingUtilities.invokeLater(() -> SplitPaneStateManager.registerSplitPane(
				ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().getPanelClientSelection(),
				SplitPaneStateManager.CLIENT_INFO_SPLIT, ClientConfiguration.DIVIDER_LOCATION));

		ConfigedMain.getMainFrame().toFront();
	}

	public boolean isDataLoaded() {
		return isDataLoaded;
	}
}
