/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.logging.Logging;

public class SearchQueryExecutor extends SwingWorker<List<String>, Void> {
	private Callable<List<String>> runnable;
	private String searchQuery;

	public SearchQueryExecutor(Callable<List<String>> runnable, String searchQuery) {
		this.runnable = runnable;
		this.searchQuery = searchQuery;
	}

	@Override
	public List<String> doInBackground() {
		ConfigedMain.getMainFrame().activateLoadingPane(
				Configed.getResourceValue("ClientSelectionDialog.executingSearchQuery") + " " + searchQuery);
		ConfigedMain.getMainFrame().activateLoadingCursor();
		List<String> result = new ArrayList<>();
		try {
			result = runnable.call();
		} catch (Exception e) {
			Logging.error(this, "encountered error on executing search query", e);
		}
		return result;
	}

	@Override
	public void done() {
		ConfigedMain.getMainFrame().deactivateLoadingPane();
		ConfigedMain.getMainFrame().deactivateLoadingCursor();

		// because of potential memory problems we switch to
		// client view
		ConfigedMain.getMainFrame().getClientConfiguration().setSelectedIndex(0);

		try {
			List<String> clients = get();
			Logging.debug(this, "", clients);
			ConfigedMain.getMainFrame().getClientTablePanel().setSelectedValues(clients);
		} catch (InterruptedException e) {
			Logging.error(this, "error occured while retrieving results from search query", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			Logging.error(this, "error occured while retrieving results from search query", e);
		}
	}
}
