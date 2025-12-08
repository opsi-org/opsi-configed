/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.WebDAVClient;
import de.uib.configed.share.WinProductUtils;
import de.uib.configed.share.logging.Logging;

public class PanelDriverUploadWorker extends SwingWorker<Void, Void> {
	@SuppressWarnings("java:S1104")
	public static class Context {
		public JDialog owner;
		public WebDAVClient webDAVClient;
		public JButton executeButton;
		public File targetPath;
		public File driverPath;
		public JCheckBox serverPathChecked;
	}

	private final Context ctx;

	public PanelDriverUploadWorker(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public Void doInBackground() {
		setWaitCursor(true);
		ctx.executeButton.setEnabled(false);
		Logging.info(this, "Copy ", ctx.driverPath, " to ", ctx.targetPath);

		boolean result = WinProductUtils.ensureServerDirectoryExists(ctx.owner, ctx.webDAVClient, ctx.targetPath,
				Configed.getResourceValue("PanelDriverUpload.makeFilePath.text"),
				Configed.getResourceValue("PanelDriverUpload.makeFilePath.title"));

		Logging.info(this, "makePath result ", ctx.targetPath, " exists or created ", result);
		boolean stateServerPath = ctx.webDAVClient.existsAndIsDirectory(ctx.targetPath.getPath().replace("\\", "/"));
		ctx.serverPathChecked.setSelected(stateServerPath);
		if (stateServerPath) {
			try {
				WinProductUtils.uploadFileOrDirectory(ctx.webDAVClient, ctx.driverPath,
						ctx.targetPath.getPath().replace("\\", "/") + "/");
			} catch (IOException iox) {
				Logging.error(iox, "Copy error:\n", iox);
				showErrorDialog("Copy error: " + iox.getMessage());
			}
		} else {
			Logging.info(this, "targetPath does not exist");
		}
		return null;
	}

	@Override
	protected void done() {
		setWaitCursor(false);
		ctx.executeButton.setEnabled(true);
		JOptionPane.showMessageDialog(ctx.owner, Configed.getResourceValue("PanelDriverUploadWorker.reportMessage"),
				Configed.getResourceValue("CompleteWinProduct.reportTitle"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void setWaitCursor(boolean wait) {
		if (ctx.owner != null) {
			ctx.owner.setCursor(wait ? Globals.WAIT_CURSOR : null);
		}
	}

	private void showErrorDialog(String message) {
		SwingUtilities.invokeLater(
				() -> JOptionPane.showMessageDialog(ctx.owner, message, "Error", JOptionPane.ERROR_MESSAGE));
	}
}
