/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditRecord;

public class ConnectionErrorReporter implements ConnectionErrorListener {
	private ConnectionState conStat;

	public ConnectionErrorReporter(ConnectionState conStat) {
		this.conStat = conStat;
	}

	@Override
	public void onError(String message, ConnectionErrorType errorType) {
		switch (errorType) {
		case FAILED_CERTIFICATE_VALIDATION_ERROR:
			displayFailedCertificateValidationDialog(message);
			break;
		case FAILED_CERTIFICATE_DOWNLOAD_ERROR:
			displayGeneralDialog(message);
			break;
		case INVALID_HOSTNAME_ERROR:
			displayGeneralDialog(message);
			break;
		case MFA_ERROR:
			displayMFADialog();
			break;
		default:
			Logging.notice(this, "unhandeld error type: " + errorType);
		}
	}

	private void displayFailedCertificateValidationDialog(String message) {
		final FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("JSONthroughHTTP.failedServerVerification"), true,
				new String[] { Configed.getResourceValue(Configed.getResourceValue("UIManager.cancelButtonText")),
						Configed.getResourceValue("JSONthroughHTTP.alwaysTrust"),
						Configed.getResourceValue("JSONthroughHTTP.trustOnlyOnce") },
				420, 260);

		fErrorMsg.setTooltipButtons(null, Configed.getResourceValue("JSONthroughHTTP.alwaysTrustTooltip"),
				Configed.getResourceValue("JSONthroughHTTP.trustOnlyOnceTooltip"));

		try {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(() -> {
					fErrorMsg.setMessage(message);
					fErrorMsg.setAlwaysOnTop(true);

					if (ConfigedMain.getMainFrame() == null && ConfigedMain.dPassword != null) {
						fErrorMsg.setLocationRelativeTo(ConfigedMain.dPassword);
					}

					fErrorMsg.setVisible(true);
				});
			}
		} catch (InterruptedException e) {
			Logging.info(this, "Thread was interrupted");
			Thread.currentThread().interrupt();
		} catch (InvocationTargetException e) {
			Logging.debug(this, "exception thrown during doRun: " + e);
		}

		int choice = fErrorMsg.getResult();

		if (choice == 1) {
			conStat = new ConnectionState(ConnectionState.INTERRUPTED);
		} else if (choice == 2) {
			CertificateDownloader
					.downloadCertificateFile(ServerFacade.produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE));
			CertificateManager.saveCertificate(CertificateDownloader.getDownloadedCertificateFile());
			conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
		} else if (choice == 3) {
			CertificateDownloader
					.downloadCertificateFile(ServerFacade.produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE));
			conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
		}
	}

	private void displayGeneralDialog(String message) {
		final FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("JSONthroughHTTP.failedServerVerification"), true,
				new String[] { Configed.getResourceValue(Configed.getResourceValue("FGeneralDialog.ok")) }, 420, 200);

		try {
			SwingUtilities.invokeAndWait(() -> {
				fErrorMsg.setMessage(message);
				fErrorMsg.setAlwaysOnTop(true);

				if (ConfigedMain.getMainFrame() == null && ConfigedMain.dPassword != null) {
					fErrorMsg.setLocationRelativeTo(ConfigedMain.dPassword);
				}

				fErrorMsg.setVisible(true);
			});
		} catch (InvocationTargetException e) {
			Logging.debug(this, "exception thrown during doRun: " + e);
		} catch (InterruptedException e) {
			Logging.info(this, "Thread was interrupted");
			Thread.currentThread().interrupt();
		}

		int choice = fErrorMsg.getResult();

		Logging.devel(this, "choice: " + choice);

		if (choice == 1) {
			conStat = new ConnectionState(ConnectionState.INTERRUPTED);
			Logging.devel(this, "conStat now: " + conStat);
		}
	}

	private static void displayMFADialog() {
		Logging.info("Unauthorized, show password dialog");

		Map<String, String> groupData = new LinkedHashMap<>();
		groupData.put("password", "");
		Map<String, String> labels = new HashMap<>();
		labels.put("password", Configed.getResourceValue("DPassword.jLabelPassword"));
		Map<String, Boolean> editable = new HashMap<>();
		editable.put("password", true);
		Map<String, Boolean> secrets = new HashMap<>();
		secrets.put("password", true);

		final FEditRecord newPasswordDialog = new FEditRecord(
				Configed.getResourceValue("JSONthroughHTTP.provideNewPassword"));
		newPasswordDialog.setRecord(groupData, labels, null, editable, secrets);

		try {
			SwingUtilities.invokeAndWait(() -> {
				newPasswordDialog.setTitle(
						Configed.getResourceValue("JSONthroughHTTP.enterNewPassword") + " (" + Globals.APPNAME + ")");
				newPasswordDialog.init();
				newPasswordDialog.setSize(420, 210);
				newPasswordDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
				newPasswordDialog.setModal(true);
				newPasswordDialog.setAlwaysOnTop(true);
				newPasswordDialog.setVisible(true);
			});
		} catch (InvocationTargetException e) {
			Logging.debug("exception thrown during doRun: " + e);
		} catch (InterruptedException e) {
			Logging.info("Thread was interrupted");
			Thread.currentThread().interrupt();
		}

		ConfigedMain.password = newPasswordDialog.getData().get("password");
	}

	public ConnectionState getConStat() {
		return conStat;
	}
}
