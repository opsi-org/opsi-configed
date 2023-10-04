/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import de.uib.opsicommand.CertificateManager;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.ServerFacade;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public final class PersistenceControllerFactory {

	private static OpsiServiceNOMPersistenceController staticPersistControl;

	// private constructor to hide the implicit public one
	private PersistenceControllerFactory() {
	}

	/**
	 * This creation method constructs a new Controller instance and lets a
	 * static variable point to it When next time we need a Controller we can
	 * choose if we take the already constructed one - returned from the static
	 * method getPersistenceController - or construct a new one
	 */
	public static OpsiServiceNOMPersistenceController getNewPersistenceController(String server, String user,
			String password) {
		Logging.info("getNewPersistenceController");
		if (staticPersistControl != null
				&& staticPersistControl.getConnectionState().getState() == ConnectionState.CONNECTED) {
			Logging.info("a PersistenceController exists and we are connected, the existing one will be returned");
			return staticPersistControl;
		}

		OpsiServiceNOMPersistenceController persistenceController = new OpsiServiceNOMPersistenceController(server,
				user, password);
		Logging.info(
				"a PersistenceController initiated by option sqlAndGetRows got " + (persistenceController == null));

		Logging.info("a PersistenceController initiated, got null? " + (persistenceController == null));

		while (persistenceController.getConnectionState().getState() == ConnectionState.UNDEFINED
				|| persistenceController.getConnectionState().getState() == ConnectionState.RETRY_CONNECTION) {
			persistenceController.getUserDataService().checkMultiFactorAuthenticationPD();

			if (!ServerFacade.isOpsi43()) {
				persistenceController.makeConnection();
			}
		}

		staticPersistControl = persistenceController;

		if (persistenceController.getConnectionState().getState() == ConnectionState.CONNECTED) {
			persistenceController.getModuleDataService().retrieveOpsiModules();

			Utils.setMultiFactorAuthenticationEnabled(
					persistenceController.getUserDataService().usesMultiFactorAuthentication());
			persistenceController.getUserRolesConfigDataService().checkConfigurationPD();

			if (!Utils.isCertificateVerificationDisabled()) {
				CertificateManager.updateCertificate();
			}
		}

		return staticPersistControl;
	}

	public static OpsiServiceNOMPersistenceController getPersistenceController() {
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
