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

public class PersistenceControllerFactory {
	// private constructor to hide the implicit public one
	private PersistenceControllerFactory() {
	}

	private static PersistenceController staticPersistControl;

	public static boolean sqlAndGetRows = false;
	public static boolean avoidSqlRawData = false;
	public static boolean sqlAndGetHashes = false;
	public static boolean sqlDirect = false;
	public static String directmethodcall = "";

	public static final String DIRECT_METHOD_CALL_CLEANUP_AUDIT_SOFTWARE = "cleanupAuditsoftware";

	/**
	 * This creation method constructs a new Controller instance and lets a
	 * static variable point to it When next time we need a Controller we can
	 * choose if we take the already constructed one - returned from the static
	 * method getPersistenceController - or construct a new one
	 */
	public static PersistenceController getNewPersistenceController(String server, String user, String password) {
		Logging.info("getNewPersistenceController");
		if (staticPersistControl != null
				&& staticPersistControl.getConnectionState().getState() == ConnectionState.CONNECTED) {
			Logging.info("a PersistenceController exists and we are connected, the existing one will be returned");
			return staticPersistControl;
		}

		PersistenceController persistControl;

		if (sqlAndGetRows) {
			persistControl = new OpsiserviceRawDataPersistenceController(server, user, password);
			Logging.info("a PersistenceController initiated by option sqlAndGetRows got " + (persistControl == null));
		} else if (avoidSqlRawData) {
			sqlAndGetRows = false;
			persistControl = new OpsiserviceNOMPersistenceController(server, user, password);
			Logging.info("a PersistenceController initiated by option avoidSqlRawData got " + (persistControl == null));
		} else if (sqlDirect) {
			persistControl = new OpsiDirectSQLPersistenceController(server, user, password);
			if (directmethodcall.equals(DIRECT_METHOD_CALL_CLEANUP_AUDIT_SOFTWARE)) {
				persistControl.cleanUpAuditSoftware();
			}
			Logging.info("a PersistenceController initiated by option sqlDirect got " + (persistControl == null));
			System.exit(0);
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

				if (persistControl.getOpsiVersion().compareTo(Globals.REQUIRED_SERVICE_VERSION) < 0) {
					String errorInfo = Configed.getResourceValue("PersistenceControllerFactory.requiredServiceVersion")
							+ " " + Globals.REQUIRED_SERVICE_VERSION + ", " + "\n( "
							+ Configed.getResourceValue("PersistenceControllerFactory.foundServiceVersion") + " "
							+ persistControl.getOpsiVersion() + " ) ";

					javax.swing.JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), errorInfo, Globals.APPNAME,
							javax.swing.JOptionPane.OK_OPTION);

					Configed.endApp(1);

					return null;
				}

				if (persistControl.getOpsiVersion().compareTo(Globals.MIN_SUPPORTED_OPSI_VERSION) < 0) {
					String errorInfo = Configed
							.getResourceValue("PersistenceControllerFactory.supportEndedForThisVersion")

							+ "\n( " + Configed.getResourceValue("PersistenceControllerFactory.foundServiceVersion")
							+ " " + persistControl.getOpsiVersion() + " ) ";

					new Thread() {
						private boolean proceed;

						@Override
						public void run() {
							proceed = true;

							de.uib.configed.gui.FTextArea infodialog = new de.uib.configed.gui.FTextArea(
									ConfigedMain.getMainFrame(), Globals.APPNAME, false, // we are not modal
									new String[] { Configed.getResourceValue("FGeneralDialog.ok") }, 300, 200) {
								@Override
								public void doAction1() {
									super.doAction1();
									Logging.info("== leaving not supported info ");
									proceed = false;
									setVisible(false);
								}
							};

							infodialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
							infodialog.setMessage(errorInfo);
							infodialog.setVisible(true);

							int count = 0;

							while (proceed) {
								count++;

								infodialog.setVisible(true);
								Globals.threadSleep(this, 3000);
								Logging.info("== repeating info " + count);

								infodialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
							}
						}
					}.start();
				}

				persistControl.makeConnection();
				persistControl.checkConfiguration();
				persistControl.retrieveOpsiModules();

				if (sqlAndGetRows && !persistControl.isWithMySQL()) {
					Logging.info(" fall back to  " + OpsiserviceNOMPersistenceController.class);
					sqlAndGetRows = false;
					persistControl = new OpsiserviceNOMPersistenceController(server, user, password);

					persistControl.makeConnection();
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

		if (persistControl.getConnectionState().getState() == ConnectionState.CONNECTED) {
			CertificateManager.updateCertificate();
		}

		return staticPersistControl;
	}

	public static PersistenceController getPersistenceController() {
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