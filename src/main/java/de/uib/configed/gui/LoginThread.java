/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.text.MessageFormat;

import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.ConnectionErrorReporter;
import de.uib.configed.core.infrastructure.ConnectionErrorType;
import de.uib.configed.core.infrastructure.ConnectionState;
import de.uib.configed.core.infrastructure.HostData;
import de.uib.configed.core.infrastructure.ServerFacade;
import de.uib.configed.share.logging.Logging;

/**
 * This class is used to handle the login process in a separate thread.
 */
public class LoginThread extends Thread {
	private LoginDialog loginDialog;
	private HostData selectedHost;

	private OpsiServiceNOMPersistenceController persistenceController;

	public LoginThread(LoginDialog loginDialog, HostData selectedHost) {
		this.loginDialog = loginDialog;
		this.selectedHost = selectedHost;
	}

	@Override
	public void run() {
		Logging.info(this, "get persis");
		boolean invalidHost = selectedHost.getHost() == null || selectedHost.getHost().isEmpty();
		boolean invalidUser = selectedHost.getUser() == null || selectedHost.getUser().isEmpty();
		boolean invalidPw = String.valueOf(selectedHost.getPassword()) == null
				|| String.valueOf(selectedHost.getPassword()).isEmpty();
		if (!selectedHost.useSSO() && (invalidHost || invalidUser || invalidPw)) {
			Logging.error(this, "No host, user or password provided");
			Logging.debug(this, "Validate credentials: invalids ", invalidHost, invalidUser, invalidPw);
			loginDialog.setActivated(true);
			return;
		}
		persistenceController = PersistenceControllerFactory.getNewPersistenceController(selectedHost);

		Logging.updateLogfile();
		Logging.info(this, "got persis, == null ", persistenceController == null);

		Logging.info(this, "waitingTask can be set to ready");
		actAfterWaiting();
	}

	private void actAfterWaiting() {
		Logging.debug(this, "actAfterWaiting");
		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED
				&& ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast(Globals.MIN_SERVER_VERSION)) {
			login();
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
				showWarningDialog(connectionState);
			}

			loginDialog.returnToLogin();
		}
	}

	private void login() {
		loginDialog.setInfoText(Configed.getResourceValue("LoadingObserver.start"));
		Logging.info(this, "connected with persis ", persistenceController);
		if (selectedHost.useSSO()) {
			// Using SSO, so the browser window is currently in the foreground.
			// Bring the login dialog back to the front.
			loginDialog.setVisible(true);
			loginDialog.toFront();
		}

		ConfigedMain configedMain = new ConfigedMain();
		configedMain.setPersistenceController(persistenceController);
		configedMain.loadDataAndGo();
	}

	private static void showWarningDialog(ConnectionState connectionState) {
		String message;
		ConnectionErrorType errorType = ConnectionErrorType.GENERAL_ERROR;

		if (connectionState.getState() == ConnectionState.TIMEOUT) {
			message = Configed.getResourceValue("LoginDialog.timeoutReached");
			errorType = ConnectionErrorType.TIMEOUT_ERROR;
		} else if (ServerFacade.getOpsiServerVersionRetriever() != null
				&& !ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast(Globals.MIN_MAJOR_VERSION)
				&& connectionState.getState() != ConnectionState.NOT_CONNECTED) {
			message = Configed.getResourceValue("LoginDialog.oldServerVersion");
		} else if (ServerFacade.getOpsiServerVersionRetriever() != null
				&& !ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast(Globals.MIN_SERVER_VERSION)
				&& connectionState.getState() != ConnectionState.NOT_CONNECTED) {
			message = String.format(Configed.getResourceValue("LoginDialog.minServerVersion"),
					Globals.MIN_SERVER_VERSION, ServerFacade.getOpsiServerVersionRetriever().getServerVersion());
		} else if (connectionState.getState() == ConnectionState.ERROR) {
			message = new MessageFormat(Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.content"))
					.format(new Object[] { connectionState.getMessage() });
		} else {
			message = null;
		}

		if (message != null) {
			ConnectionErrorReporter.getInstance().notify(message, errorType);
		}
	}
}
