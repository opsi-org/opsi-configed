
package de.uib.opsidatamodel;

import javax.swing.JOptionPane;

import de.uib.Main;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.CertificateManager;
import de.uib.opsicommand.ConnectionState;
import de.uib.utilities.logging.Logging;

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

		OpsiserviceNOMPersistenceController persistControl = new OpsiserviceNOMPersistenceController(server, user,
				password);
		Logging.info("a PersistenceController initiated, got null? " + (persistControl == null));

		boolean connected = persistControl.makeConnection();

		while (persistControl.getConnectionState().getState() == ConnectionState.RETRY_CONNECTION) {
			connected = persistControl.makeConnection();
		}

		try {
			if (connected) {
				persistControl.checkMultiFactorAuthentication();
				Globals.isMultiFactorAuthenticationEnabled = persistControl.usesMultiFactorAuthentication();
				persistControl.checkConfiguration();
				persistControl.retrieveOpsiModules();
			}
		} catch (Exception ex) {
			Logging.error("Error", ex);

			String errorInfo = ex.toString();

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), errorInfo, Globals.APPNAME,
					JOptionPane.OK_OPTION);

			Main.endApp(2);

			return null;
		}

		staticPersistControl = persistControl;

		if (persistControl.getConnectionState().getState() == ConnectionState.CONNECTED
				&& !Globals.disableCertificateVerification) {
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
