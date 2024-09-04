/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Dimension;

import de.uib.configed.gui.ClientSelectionDialog;
import de.uib.configed.gui.NewClientDialog;
import de.uib.configed.gui.SavedSearchesDialog;
import de.uib.utils.logging.Logging;

/**
 * This class should contain control all these control frames that we have in
 * the configed that we can open and use. This class should therefore never be
 * instantiated because we want to access the methods directly to get access to
 * the extra features and frames
 */
public final class ExtraFrameController {
	private static SavedSearchesDialog savedSearchesDialog;
	private static ClientSelectionDialog clientSelectionDialog;
	private static NewClientDialog newClientDialog;

	// We have a private empty constructor to prevent instantiation
	private ExtraFrameController() {
	}

	public static void callNewClientSelectionDialog(ConfigedMain configedMain) {
		if (clientSelectionDialog != null) {
			clientSelectionDialog.leave();
			clientSelectionDialog = null;
		}
		callClientSelectionDialog(configedMain);
	}

	public static void callClientSelectionDialog(ConfigedMain configedMain) {
		initSavedSearchesDialog(configedMain);

		if (clientSelectionDialog == null) {
			clientSelectionDialog = new ClientSelectionDialog(configedMain, configedMain.getClientTable(),
					savedSearchesDialog);
		}

		clientSelectionDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		clientSelectionDialog.setVisible(true);
	}

	public static void editClientSearch(ConfigedMain configedMain, String name) {
		callClientSelectionDialog(configedMain);
		clientSelectionDialog.loadSearch(name);
	}

	private static void initSavedSearchesDialog(ConfigedMain configedMain) {
		if (savedSearchesDialog == null) {
			Logging.debug("create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog(configedMain.getClientTable(), configedMain);
			savedSearchesDialog.setPreferredScrollPaneSize(new Dimension(300, 400));
			savedSearchesDialog.init();
		}
	}

	public static void clientSelectionGetSavedSearch(ConfigedMain configedMain) {
		Logging.debug("clientSelectionGetSavedSearch");
		initSavedSearchesDialog(configedMain);

		savedSearchesDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		savedSearchesDialog.setVisible(true);
	}

	public static void reloadDialogs() {
		if (savedSearchesDialog != null) {
			savedSearchesDialog.resetModel();
		}
	}

	public static void callNewClientDialog(ConfigedMain configedMain) {
		if (newClientDialog == null) {
			newClientDialog = new NewClientDialog(configedMain);
		}

		newClientDialog.setDefaultValues();
		newClientDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		newClientDialog.setVisible(true);
	}
}
