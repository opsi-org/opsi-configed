/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2014 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

package de.uib.opsidatamodel;

import javax.swing.JOptionPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.CertificateManager;
import de.uib.opsicommand.ConnectionState;
import de.uib.utilities.logging.Logging;

public final class PersistenceControllerFactory {

	private static AbstractPersistenceController staticPersistControl;

	public static boolean sqlAndGetRows;
	public static boolean avoidSqlRawData;
	public static boolean sqlAndGetHashes;

	// private constructor to hide the implicit public one
	private PersistenceControllerFactory() {
	}

	/**
	 * This creation method constructs a new Controller instance and lets a
	 * static variable point to it When next time we need a Controller we can
	 * choose if we take the already constructed one - returned from the static
	 * method getPersistenceController - or construct a new one
	 */
	public static AbstractPersistenceController getNewPersistenceController(String server, String user,
			String password) {
		Logging.info("getNewPersistenceController");
		if (staticPersistControl != null
				&& staticPersistControl.getConnectionState().getState() == ConnectionState.CONNECTED) {
			Logging.info("a PersistenceController exists and we are connected, the existing one will be returned");
			return staticPersistControl;
		}

		AbstractPersistenceController persistControl;

		if (sqlAndGetRows) {
			persistControl = new OpsiserviceRawDataPersistenceController(server, user, password);
			Logging.info("a PersistenceController initiated by option sqlAndGetRows got " + (persistControl == null));
		} else if (avoidSqlRawData) {
			sqlAndGetRows = false;
			persistControl = new OpsiserviceNOMPersistenceController(server, user, password);
			Logging.info("a PersistenceController initiated by option avoidSqlRawData got " + (persistControl == null));
		} else {
			persistControl = new OpsiserviceRawDataPersistenceController(server, user, password);
			sqlAndGetRows = true;
			Logging.info("a PersistenceController initiated by default, try RawData " + (persistControl == null));
		}

		boolean connected = persistControl.makeConnection();

		while (persistControl.getConnectionState().getState() == ConnectionState.RETRY_CONNECTION) {
			connected = persistControl.makeConnection();
		}

		try {
			if (connected) {
				Logging.info("factory: check source accepted");
				boolean sourceAccepted = persistControl.canCallMySQL();
				Logging.info("factory: source accepted " + sourceAccepted);

				if (sqlAndGetRows && !sourceAccepted) {
					sqlAndGetRows = false;
					persistControl = new OpsiserviceNOMPersistenceController(server, user, password);
				}

				persistControl.makeConnection();
				persistControl.checkMultiFactorAuthentication();
				Globals.isMultiFactorAuthenticationEnabled = persistControl.usesMultiFactorAuthentication();
				persistControl.checkConfiguration();
				persistControl.retrieveOpsiModules();

				if (sqlAndGetRows && !persistControl.isWithMySQL()) {
					Logging.info(" fall back to  " + OpsiserviceNOMPersistenceController.class);
					sqlAndGetRows = false;
					persistControl = new OpsiserviceNOMPersistenceController(server, user, password);

					persistControl.makeConnection();
					persistControl.checkMultiFactorAuthentication();
					Globals.isMultiFactorAuthenticationEnabled = persistControl.usesMultiFactorAuthentication();
					persistControl.checkConfiguration();
					persistControl.retrieveOpsiModules();
				}
			}
		} catch (Exception ex) {
			Logging.error("Error", ex);

			String errorInfo = ex.toString();

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), errorInfo, Globals.APPNAME,
					JOptionPane.OK_OPTION);

			Configed.endApp(2);

			return null;
		}

		staticPersistControl = persistControl;

		if (persistControl.getConnectionState().getState() == ConnectionState.CONNECTED
				&& !Globals.disableCertificateVerification) {
			CertificateManager.updateCertificate();
		}

		return staticPersistControl;
	}

	public static AbstractPersistenceController getPersistenceController() {
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
