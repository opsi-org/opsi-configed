/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.text.MessageFormat;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsicommand.ConnectionErrorReporter;
import de.uib.opsicommand.ConnectionErrorType;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

/**
 * This class is used to handle the login process in a separate thread.
 */
public class LoginThread extends Thread {
	private LoginDialog loginDialog;
	private ConfigedMain configedMain;
	private Object selectedHost;
	private String user;
	private char[] password;
	private char[] otp;
	private boolean useSSO;

	private OpsiServiceNOMPersistenceController persistenceController;

	public LoginThread(LoginDialog loginDialog, ConfigedMain configedMain, Object selectedHost, String user,
			char[] password, char[] otp, boolean useSSO) {
		this.loginDialog = loginDialog;
		this.configedMain = configedMain;
		this.selectedHost = selectedHost;
		this.user = user;
		this.password = password;
		this.otp = otp;
		this.useSSO = useSSO;
	}

	@Override
	public void run() {
		Logging.info(this, "get persis");
		boolean invalidHost = selectedHost == null || selectedHost.toString().isEmpty();
		boolean invalidUser = user == null || user.isEmpty();
		boolean invalidPw = String.valueOf(password) == null || String.valueOf(password).isEmpty();
		if (!useSSO && (invalidHost || invalidUser || invalidPw)) {
			Logging.error(this, "No host, user or password provided");
			Logging.debug(this, "Validate credentials: invalids ", invalidHost, invalidUser, invalidPw);
			loginDialog.setActivated(true);
			return;
		}
		persistenceController = PersistenceControllerFactory.getNewPersistenceController((String) selectedHost, user,
				String.valueOf(password), String.valueOf(otp), useSSO);

		Logging.updateLogfile();
		Logging.info(this, "got persis, == null ", persistenceController == null);

		Logging.info(this, "waitingTask can be set to ready");
		actAfterWaiting();
	}

	private void actAfterWaiting() {
		Logging.debug(this, "actAfterWaiting");
		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED
				&& ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3")) {
			loginDialog.setInfoText(Configed.getResourceValue("LoadingObserver.start"));
			Logging.info(this, "connected with persis ", persistenceController);
			if (useSSO) {
				// Using SSO, so the browser window is currently in the foreground.
				// Bring the login dialog back to the front.
				loginDialog.setVisible(true);
				loginDialog.toFront();
			}
			configedMain.setPersistenceController(persistenceController);
			configedMain.loadDataAndGo();
		} else {
			// Clear cache
			CacheManager.getInstance().clearAllCachedData();
			// return to Passwordfield
			ConnectionState connectionState = PersistenceControllerFactory.getConnectionState();
			if (connectionState.getState() == ConnectionState.INTERRUPTED) {
				// return to password dialog
				Logging.info(this, "interrupted");
			} else {
				Logging.info(this, "not connected, timeout or not authorized. state: ",
						PersistenceControllerFactory.getConnectionState().getState());
				Logging.info(this,
						ServerFacade.getOpsiServerVersionRetriever() != null
								? ("serverVersion " + ServerFacade.getOpsiServerVersionRetriever().getServerVersion())
								: "could not retrieve server version");
				String message;
				ConnectionErrorType errorType = ConnectionErrorType.GENERAL_ERROR;

				if (connectionState.getState() == ConnectionState.TIMEOUT) {
					message = Configed.getResourceValue("LoginDialog.timeoutReached");
					errorType = ConnectionErrorType.TIMEOUT_ERROR;
				} else if (ServerFacade.getOpsiServerVersionRetriever() != null
						&& !ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3")) {
					message = Configed.getResourceValue("LoginDialog.oldServerVersion");
				} else {
					message = new MessageFormat(
							Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.content")).format(
									new Object[] { PersistenceControllerFactory.getConnectionState().getMessage() });
				}

				ConnectionErrorReporter.getInstance().notify(message, errorType);
			}

			loginDialog.returnToLogin();
		}
	}
}
