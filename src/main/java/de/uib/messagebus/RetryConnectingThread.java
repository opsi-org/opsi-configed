/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class RetryConnectingThread extends Thread {
	private boolean authenticationError;
	private int reconnectWaitMillis = 15000;

	private Messagebus messagebus;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public RetryConnectingThread(boolean authenticationError, Messagebus messagebus) {
		this.authenticationError = authenticationError;
		this.messagebus = messagebus;
	}

	@Override
	public void run() {
		messagebus.setReconnecting(true);
		while (!messagebus.isConnected()) {
			int waitMillis = reconnectWaitMillis;
			if (authenticationError) {
				Logging.notice(this, "Connection to messagebus lost, authentication error");
				persistenceController.makeConnection();
				waitMillis = 1000;
			} else {
				Logging.notice(this, "Connection to messagebus lost, reconnecting in ", reconnectWaitMillis, " ms");
			}
			try {
				Thread.sleep(waitMillis);
				if (messagebus.connect()) {
					break;
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		messagebus.setReconnecting(false);
	}
}
