/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.uib.configed.core.infrastructure.webdav.WebDAVClient;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.logging.Logging;

public class WinProductsRetriever extends SwingWorker<List<String>, Void> {
	@SuppressWarnings("java:S1104")
	public static class Context {
		public JDialog owner;
		public WebDAVClient webDAVClient;
		public JLabel msg;
		public JComboBox<String> options;
		public Runnable onDone;
	}

	private final Context ctx;

	public WinProductsRetriever(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public List<String> doInBackground() {
		ctx.msg.setVisible(true);
		ctx.options.setEnabled(false);
		setWaitCursor(true);
		ConfigedMain.getMainFrame().activateLoadingCursor();
		return WinProductUtils.getWinProductsPD(ctx.webDAVClient, "depot/");
	}

	@Override
	public void done() {
		try {
			List<String> winProducts = get();
			ctx.options.setModel(new DefaultComboBoxModel<>(winProducts.toArray(new String[0])));
			ctx.onDone.run();

			ConfigedMain.getMainFrame().deactivateLoadingCursor();
			setWaitCursor(false);
			ctx.options.setEnabled(true);
			ctx.msg.setVisible(false);
			resizeDialog();
		} catch (InterruptedException e) {
			Logging.warning(this, "Thread was interrupted", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			Logging.warning(this, "Failed to retrieve win products ", e);
			setWaitCursor(false);
			ctx.msg.setVisible(false);
			ctx.options.setEnabled(true);
			showErrorDialog(Configed.getResourceValue("WinProductsRetriever.failedToRetrieveWinProducts"));
		}
	}

	private void setWaitCursor(boolean wait) {
		if (ctx.owner != null) {
			ctx.owner.setCursor(wait ? Globals.WAIT_CURSOR : null);
		}
	}

	private void resizeDialog() {
		if (ctx.owner != null) {
			ctx.owner.revalidate();
			ctx.owner.pack();
			ctx.owner.repaint();
		}
	}

	private void showErrorDialog(String message) {
		SwingUtilities.invokeLater(
				() -> JOptionPane.showMessageDialog(ctx.owner, message, "Error", JOptionPane.ERROR_MESSAGE));
	}
}
