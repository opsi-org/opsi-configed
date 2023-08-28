/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel;

import de.uib.opsicommand.CertificateManager;
import de.uib.opsicommand.ConnectionState;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public final class PersistenceControllerFactory {

	private static OpsiserviceNOMPersistenceController staticPersistControl;

	// private constructor to hide the implicit public one
	private PersistenceControllerFactory() {
	}

	/**
	 * This creation method constructs a new Controller instance and lets a
	 * static variable point to it When next time we need a Controller we can
	 * choose if we take the already constructed one - returned from the static
	 * method getPersistenceController - or construct a new one
	 */
	public static OpsiserviceNOMPersistenceController getNewPersistenceController(String server, String user,
			String password) {
		Logging.info("getNewPersistenceController");
		if (staticPersistControl != null
				&& staticPersistControl.getConnectionState().getState() == ConnectionState.CONNECTED) {
			Logging.info("a PersistenceController exists and we are connected, the existing one will be returned");
			return staticPersistControl;
		}

		OpsiserviceNOMPersistenceController persistenceController = new OpsiserviceNOMPersistenceController(server,
				user, password);
		Logging.info(
				"a PersistenceController initiated by option sqlAndGetRows got " + (persistenceController == null));

		Logging.info("a PersistenceController initiated, got null? " + (persistenceController == null));

		boolean connected = persistenceController.makeConnection();

		while (persistenceController.getConnectionState().getState() == ConnectionState.RETRY_CONNECTION) {
			connected = persistenceController.makeConnection();
		}

		if (connected) {
			persistenceController.checkMultiFactorAuthentication();
			Utils.setMultiFactorAuthenticationEnabled(persistenceController.usesMultiFactorAuthentication());
			persistenceController.checkConfiguration();
			persistenceController.retrieveOpsiModules();
		}

		staticPersistControl = persistenceController;

		if (persistenceController.getConnectionState().getState() == ConnectionState.CONNECTED
				&& !Utils.isCertificateVerificationDisabled()) {
			CertificateManager.updateCertificate();
		}

		return staticPersistControl;
	}

	public static OpsiserviceNOMPersistenceController getPersistenceController() {
		return staticPersistControl;
	}

	public static ConnectionState getConnectionState() {
		if (staticPersistControl == null) {
			Logging.info("PersistenceControllerFactory getConnectionState, " + " staticPersistControl null");

			return ConnectionState.ConnectionUndefined;
		}

		ConnectionState result = staticPersistControl.getConnectionState();
		Logging.info("PersistenceControllerFactory getConnectionState " + result);

		return staticPersistControl.getConnectionState();
	}
}
