/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsicommand.certificate.CertificateManager;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.SeparatedDocument;

/**
 * {@code ConnectionErrorReporter} reports connection errors, that occur during
 * the connection, to the users graphically. Different connection errors are
 * handled differently (i.e. different dialogs are displayed to the user).
 * <p>
 * {@code ConnectionErrorReporter} is built using the Singleton design pattern.
 */
public final class ConnectionErrorReporter {
	private static ConnectionErrorReporter instance;
	private static final AtomicBoolean dialogOpened = new AtomicBoolean(false);
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
		case FAILED_CERTIFICATE_DOWNLOAD_ERROR, INVALID_HOSTNAME_ERROR, TIMEOUT_ERROR, GENERAL_ERROR:
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
		pane.setMessageType(JOptionPane.WARNING_MESSAGE);
		pane.setMessage(message);
		pane.setOptions(new Object[] { Configed.getResourceValue("buttonCancel"), alwaysTrust, trustOnce });

		JDialog dialog = pane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConnectionErrorReporter.failedServerVerification"));

		// We need to add action listeners to the buttons, because otherwise 
		// the dialog will not close and nothing would happen
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

		// This means, we canceled/closed the dialog or we clicked on the cancel button
		if (pane.getValue() == null || pane.getValue().equals(Configed.getResourceValue("buttonCancel"))) {
			conStat = new ConnectionState(ConnectionState.INTERRUPTED);
		}
	}

	private void displayGeneralDialog(String message) {
		runOnEventDispatchThread(() -> {
			if (!dialogOpened.compareAndSet(false, true)) {
				return;
			}
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), message,
					Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.title"),
					JOptionPane.OK_CANCEL_OPTION);
			conStat = new ConnectionState(ConnectionState.INTERRUPTED);
			dialogOpened.set(false);
		});
	}

	private void displayMFADialog() {
		Logging.info("Unauthorized, show password dialog");
		Logging.clearErrorListAndHide();
		JPasswordField otpField = new JPasswordField();

		otpField.setDocument(new SeparatedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }, 6,
				Character.MIN_VALUE, 6, true));

		PersistenceControllerFactory.getPersistenceController().getExecutioner().resetOTPWaiter();

		displayConfirmDialogInEventDispatchThread(
				new Object[] { Configed.getResourceValue("ConnectionErrorReporter.provideNewTOTP"), otpField },
				Configed.getResourceValue("ConnectionErrorReporter.enterNewPassword"),
				() -> ConfigedMain.getMainFrame().reconnectOTP(new String(otpField.getPassword())),
				() -> SwingUtilities.invokeLater(this::displayCancelConfigedDialog));
	}

	private void displayCancelConfigedDialog() {
		displayConfirmDialogInEventDispatchThread(
				Configed.getResourceValue("ConnectionErrorReporter.closeConfigedInfo"),
				Configed.getResourceValue("ConnectionErrorReporter.closeConfigedTitle"),
				() -> Main.endApp(Main.NO_ERROR), () -> SwingUtilities.invokeLater(this::displayMFADialog));
	}

	private void displayConfirmDialogInEventDispatchThread(Object message, String title, Runnable onOK,
			Runnable onCancel) {
		if (!dialogOpened.compareAndSet(false, true)) {
			return;
		}

		runOnEventDispatchThread(() -> showConfirmDialog(message, title, onOK, onCancel));
	}

	private static void showConfirmDialog(Object message, String title, Runnable onOK, Runnable onCancel) {
		try {
			int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message, title,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (answer == JOptionPane.OK_OPTION) {
				onOK.run();
			} else {
				onCancel.run();
			}
		} finally {
			dialogOpened.set(false);
		}
	}

	/**
	 * Ensures that the provided Runnable executes on the Swing Event Dispatch
	 * Thread (EDT). If the current thread is the EDT, the task runs
	 * immediately; otherwise, it is executed synchronously on the EDT using
	 * invokeAndWait.
	 *
	 * @param runnable the task to be executed on the EDT
	 */
	private void runOnEventDispatchThread(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable::run);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				Logging.error(this, "Thread was interrupted while waiting for EDT execution.", ie);
			} catch (InvocationTargetException ite) {
				Logging.error(this, "Exception thrown by runnable while executing on the EDT.", ite);
			}
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
