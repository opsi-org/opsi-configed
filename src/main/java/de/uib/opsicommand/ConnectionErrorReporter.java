/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsicommand.certificate.CertificateManager;
import de.uib.utils.logging.Logging;

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
	 * Constructs new instance of {@link ConnectionErrorReporter} with provided
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
		JButton alwaysTrust = new JButton(Configed.getResourceValue("ConnectionErrorReporter.alwaysTrust"));
		alwaysTrust.setToolTipText(Configed.getResourceValue("ConnectionErrorReporter.alwaysTrustTooltip"));

		JButton trustOnce = new JButton(Configed.getResourceValue("ConnectionErrorReporter.trustOnlyOnce"));
		trustOnce.setToolTipText(Configed.getResourceValue("ConnectionErrorReporter.trustOnlyOnceTooltip"));

		JOptionPane pane = new JOptionPane();
		pane.setMessage(message);
		pane.setOptions(new Object[] { Configed.getResourceValue("buttonCancel"), alwaysTrust, trustOnce });

		JDialog dialog = pane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConnectionErrorReporter.failedServerVerification"));

		alwaysTrust.addActionListener((ActionEvent event) -> {
			dialog.setVisible(false);

			CertificateManager.downloadCertificateFile();
			CertificateManager.saveCertificate();
			if (conStat.getState() != ConnectionState.INTERRUPTED) {
				conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
			}
		});

		trustOnce.addActionListener((ActionEvent event) -> {
			dialog.setVisible(false);

			CertificateManager.downloadCertificateFile();
			if (conStat.getState() != ConnectionState.INTERRUPTED) {
				conStat = new ConnectionState(ConnectionState.RETRY_CONNECTION);
			}
		});

		dialog.setVisible(true);

		// This means, we canceled the dialog or we clicked on the cancel button
		if (pane.getValue() == null || pane.getValue().equals(Configed.getResourceValue("buttonCancel"))) {
			conStat = new ConnectionState(ConnectionState.INTERRUPTED);
		}
	}

	private void displayGeneralDialog(String message) {
		JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("ConnectionErrorReporter.failedServerVerification"),
				JOptionPane.OK_CANCEL_OPTION);

		conStat = new ConnectionState(ConnectionState.INTERRUPTED);
	}

	private synchronized void displayMFADialog() {
		Logging.info("Unauthorized, show password dialog");

		JPasswordField passwordField = new JPasswordField();

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
				new Object[] { Configed.getResourceValue("ConnectionErrorReporter.provideNewPassword"), passwordField },
				Configed.getResourceValue("ConnectionErrorReporter.enterNewPassword"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		// The user has clicked on the OK button.
		if (answer == 0) {
			ConfigedMain.setPassword(new String(passwordField.getPassword()));
		} else {
			displayCancelConfigedDialog();
		}
	}

	private void displayCancelConfigedDialog() {
		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConnectionErrorReporter.closeConfigedInfo"),
				Configed.getResourceValue("ConnectionErrorReporter.closeConfigedTitle"), JOptionPane.OK_CANCEL_OPTION);

		if (answer == 0) {
			displayMFADialog();
		} else {
			Main.endApp(Main.NO_ERROR);
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
