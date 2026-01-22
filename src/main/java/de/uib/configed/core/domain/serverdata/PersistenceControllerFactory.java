/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata;

import java.util.regex.Pattern;

import de.uib.configed.core.domain.serverdata.dataservice.UserDataService;
import de.uib.configed.core.infrastructure.ConnectionState;
import de.uib.configed.core.infrastructure.certificate.CertificateManager;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

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
	 * method getPersistenceController - or construct a new one. If user, server
	 * and otp is empty we try to use sso.
	 */
	public static OpsiServiceNOMPersistenceController getNewPersistenceController(String server, String user,
			String password, String otp, boolean useSSO) {
		Logging.info("getNewPersistenceController");

		if (!otp.isEmpty() && !OTP_PATTERN.matcher(otp).matches()) {
			Logging.error("One Time Password (OTP) should only contain digits and be 6 characters long.");
			return null;
		}

		OpsiServiceNOMPersistenceController persistenceController = new OpsiServiceNOMPersistenceController(server,
				user, password, otp, useSSO);
		if (useSSO && persistenceController.getExecutioner() == null) {
			Logging.error("Failed to create a PersistenceController instance using sso.");
			return null;
		}
		Logging.info("a PersistenceController initiated by option sqlAndGetRows got ", persistenceController == null);

		Logging.info("a PersistenceController initiated, got null? ", persistenceController == null);

		while (persistenceController.getConnectionState().getState() == ConnectionState.UNDEFINED
				|| persistenceController.getConnectionState().getState() == ConnectionState.RETRY_CONNECTION) {
			persistenceController.getDataServices().user.checkMultiFactorAuthenticationPD(user);
		}

		staticPersistControl = persistenceController;

		if (persistenceController.getConnectionState().getState() == ConnectionState.CONNECTED) {
			Logging.debug("PersistenceControllerFactory.getNewPersistenceController() - connected");
			UserDataService userDataService = persistenceController.getDataServices().user;
			Logging.debug("PersistenceControllerFactory.getNewPersistenceController() - userDataService:",
					userDataService);

			if (!useSSO) {
				boolean isMultiFactorAuthenticationEnabled = userDataService.usesMultiFactorAuthentication();
				Logging.debug(
						"PersistenceControllerFactory.getNewPersistenceController() - isMultiFactorAuthenticationEnabled:",
						isMultiFactorAuthenticationEnabled);

				Utils.setMultiFactorAuthenticationEnabled(isMultiFactorAuthenticationEnabled);
				Logging.debug(
						"PersistenceControllerFactory.getNewPersistenceController() - setMultiFactorAuthenticationEnabled:",
						Utils.isMultiFactorAuthenticationEnabled());
			}

			ParallelTaskExecutor executor = new ParallelTaskExecutor();
			executor.runInParallel(() -> persistenceController.getDataServices().userRoles.checkConfigurationPD());
			if (!Utils.isCertificateVerificationDisabled()) {
				executor.runInParallel(CertificateManager::updateCertificate);
			}
			executor.waitForCompletion();
		}

		return staticPersistControl;
	}

	public static OpsiServiceNOMPersistenceController getPersistenceController() {
		return staticPersistControl;
	}

	public static ConnectionState getConnectionState() {
		if (staticPersistControl == null) {
			Logging.info("PersistenceControllerFactory getConnectionState,  staticPersistControl null");

			return ConnectionState.ConnectionUndefined;
		}

		ConnectionState result = staticPersistControl.getConnectionState();
		Logging.info("PersistenceControllerFactory getConnectionState ", result);
		return result;
	}
}
