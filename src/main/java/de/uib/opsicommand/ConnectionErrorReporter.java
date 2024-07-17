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

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FTextArea;
import de.uib.opsicommand.certificate.CertificateDownloader;
import de.uib.opsicommand.certificate.CertificateManager;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.FEditRecord;

/**
 * {@code ConnectionErrorReporter} reports connection errors, that occur during
 * the connection, to the users graphically. Different connection errors are
 * handled differently (i.e. different dialogs are displayed to the user).
 * <p>
 * {@code ConnectionErrorReporter} is built using the Singleton design pattern.
 */
public final class ConnectionErrorReporter {
	private static ConnectionErrorReporter instance;
	private ConnectionState conStat;

	private ConnectionErrorReporter(ConnectionState conStat) {
		this.conStat = conStat;
	}

	/**
	 * Constructs new instnace of {@link ConnectionErrorReporter} with provided
	 * information.
	 * <p>
	 * {@link ConnectionState} is used to indicate the connection state. The
	 * connection state changes based on the user's choice.
	 * 
	 * @param conState current connection state.
	 * @return new instance of {@link ConnectionErrorReporter}.
	 */
	public static synchronized ConnectionErrorReporter getNewInstance(ConnectionState conStat) {
		instance = new ConnectionErrorReporter(conStat);
		return instance;
	}

	/**
	 * Retrievies current instance of {@link ConnectionErrorReporter}.
	 * 
	 * @return current instance of {@link ConnectionErrorReporter}.
	 */
	public static synchronized ConnectionErrorReporter getInstance() {
		return instance;
	}

	public static synchronized void destroyInstance() {
		instance = null;
	}

	public void notify(String message, ConnectionErrorType errorType) {
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
			Logging.notice(this, "unhandeld error type: ", errorType);
		}
	}

	private void displayFailedCertificateValidationDialog(String message) {
		final FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConnectionErrorReporter.failedServerVerification"), true,
				new String[] { Configed.getResourceValue("buttonCancel"),
						Configed.getResourceValue("ConnectionErrorReporter.alwaysTrust"),
						Configed.getResourceValue("ConnectionErrorReporter.trustOnlyOnce") },
				530, 260);

		fErrorMsg.setTooltipButtons(null, Configed.getResourceValue("ConnectionErrorReporter.alwaysTrustTooltip"),
				Configed.getResourceValue("ConnectionErrorReporter.trustOnlyOnceTooltip"));

		fErrorMsg.setMessage(message);
		fErrorMsg.setAlwaysOnTop(true);
		fErrorMsg.setLocationRelativeTo(ConfigedMain.getFrame());

		if (!SwingUtilities.isEventDispatchThread()) {
			launchDialogInEDT(fErrorMsg);
		} else {
			fErrorMsg.setVisible(true);
		}

		int choice = fErrorMsg.getResult();

		if (choice == 1) {
			conStat = new ConnectionState(ConnectionState.INTERRUPTED);
		} else if (choice == 2) {
			CertificateDownloader.downloadCertificateFile();
			CertificateManager.saveCertificate(CertificateDownloader.getDownloadedCertificateFile());
			if (conStat.getState() != ConnectionState.INTERRUPTED) {
				conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
			}
		} else if (choice == 3) {
			CertificateDownloader.downloadCertificateFile();
			if (conStat.getState() != ConnectionState.INTERRUPTED) {
				conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
			}
		} else {
			// There are only three options a user can select from.
		}
	}

	private void displayGeneralDialog(String message) {
		final FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConnectionErrorReporter.failedServerVerification"), true,
				new String[] { Configed.getResourceValue("buttonClose") }, 420, 200);

		fErrorMsg.setMessage(message);
		fErrorMsg.setAlwaysOnTop(true);
		fErrorMsg.setLocationRelativeTo(ConfigedMain.getFrame());

		if (!SwingUtilities.isEventDispatchThread()) {
			launchDialogInEDT(fErrorMsg);
		} else {
			fErrorMsg.setVisible(true);
		}

		int choice = fErrorMsg.getResult();

		if (choice == 1) {
			conStat = new ConnectionState(ConnectionState.INTERRUPTED);
		}
	}

	private synchronized void displayMFADialog() {
		Logging.info("Unauthorized, show password dialog");

		Map<String, String> groupData = new LinkedHashMap<>();
		groupData.put("password", "");
		Map<String, String> labels = new HashMap<>();
		labels.put("password", Configed.getResourceValue("LoginDialog.jLabelPassword"));
		Map<String, Boolean> editable = new HashMap<>();
		editable.put("password", true);
		Map<String, Boolean> secrets = new HashMap<>();
		secrets.put("password", true);

		final FEditRecord newPasswordDialog = new FEditRecord(
				Configed.getResourceValue("ConnectionErrorReporter.provideNewPassword"));
		newPasswordDialog.setRecord(groupData, labels, null, editable, secrets);

		newPasswordDialog.setTitle(Configed.getResourceValue("ConnectionErrorReporter.enterNewPassword"));
		newPasswordDialog.init();
		newPasswordDialog.setSize(420, 210);
		newPasswordDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		newPasswordDialog.setModal(true);
		newPasswordDialog.setAlwaysOnTop(true);

		if (!SwingUtilities.isEventDispatchThread()) {
			launchDialogInEDT(newPasswordDialog);
		} else {
			newPasswordDialog.setVisible(true);
		}

		if (newPasswordDialog.isCancelled()) {
			displayCancelConfigedDialog();
		} else {
			ConfigedMain.setPassword(newPasswordDialog.getData().get("password"));
		}
	}

	private void displayCancelConfigedDialog() {
		final FTextArea fErrorMsg = new FTextArea(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConnectionErrorReporter.closeConfigedTitle"), true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 420,
				200);

		fErrorMsg.setTooltipButtons(Configed.getResourceValue("ConnectionErrorReporter.closeConfigedCancelHint"),
				Configed.getResourceValue("ConnectionErrorReporter.closeConfigedCloseHint"), null);
		fErrorMsg.setMessage(Configed.getResourceValue("ConnectionErrorReporter.closeConfigedInfo"));
		fErrorMsg.setAlwaysOnTop(true);
		fErrorMsg.setLocationRelativeTo(ConfigedMain.getFrame());

		if (!SwingUtilities.isEventDispatchThread()) {
			launchDialogInEDT(fErrorMsg);
		} else {
			fErrorMsg.setVisible(true);
		}

		int choice = fErrorMsg.getResult();

		if (choice == 1) {
			displayMFADialog();
		} else {
			Main.endApp(Main.NO_ERROR);
		}
	}

	private static void launchDialogInEDT(JDialog dialog) {
		try {
			SwingUtilities.invokeAndWait(() -> dialog.setVisible(true));
		} catch (InvocationTargetException e) {
			Logging.debug("exception thrown during doRun: ", e);
		} catch (InterruptedException e) {
			Logging.info("Thread was interrupted");
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Retrieves connection state after user's choice.
	 * <p>
	 * Depending on the user's choice, a connection can be in the following
	 * states:
	 * <p>
	 * <ul>
	 * <li>{@code INTERRUPTED} indicates that the user was informed about the
	 * error, and that nothing could be done to resolve the error or user has
	 * ignored the error.</li>
	 * <li>{@code RETRY_CONNECTION} indicates that the user was informed about
	 * the error, and that the error was resolved.</li>
	 * </ul>
	 * 
	 * @return connection state.
	 */
	public ConnectionState getConnectionState() {
		return conStat;
	}
}
