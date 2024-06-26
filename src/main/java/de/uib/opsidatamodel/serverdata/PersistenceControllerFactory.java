/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.regex.Pattern;

import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.certificate.CertificateManager;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public final class PersistenceControllerFactory {
	private static final Pattern OTP_PATTERN = Pattern.compile("^[\\d]{6}$");
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
			String password, String otp) {
		Logging.info("getNewPersistenceController");

		if (!otp.isEmpty() && !OTP_PATTERN.matcher(otp).matches()) {
			Logging.error("One Time Password (OTP) should only contain digits and be 6 characters long.");
			return null;
		}

		OpsiServiceNOMPersistenceController persistenceController = new OpsiServiceNOMPersistenceController(server,
				user, password, otp);
		Logging.info(
				"a PersistenceController initiated by option sqlAndGetRows got " + (persistenceController == null));

		Logging.info("a PersistenceController initiated, got null? " + (persistenceController == null));

		while (persistenceController.getConnectionState().getState() == ConnectionState.UNDEFINED
				|| persistenceController.getConnectionState().getState() == ConnectionState.RETRY_CONNECTION) {
			persistenceController.getUserDataService().checkMultiFactorAuthenticationPD();
		}

		staticPersistControl = persistenceController;

		if (persistenceController.getConnectionState().getState() == ConnectionState.CONNECTED) {
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
