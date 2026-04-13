/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */
package de.uib.configed.gui.features.terminal;

import javax.swing.JOptionPane;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.logging.Logging;

public final class TerminalController {
	private static final OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private static ConfigedMain configedMain;

	private TerminalController() {
		// private constructor to prevent instantiation
	}

	public static void setConfigedMain(ConfigedMain configedMain) {
		TerminalController.configedMain = configedMain;
	}

	public static void openTerminalOnClient() {
		openTerminalOnHost("Client");
	}

	public static void openTerminalOnDepot() {
		openTerminalOnHost("ConfigserverOrDepot");
	}

	private static void openTerminalOnHost(String type) {
		if (!"Client".equals(type) && !"ConfigserverOrDepot".equals(type)) {
			throw new IllegalArgumentException("type must be either 'Client' or 'Depot'");
		}
		String connectToHost = ("Client".equals(type)) ? configedMain.getSelectedClients().get(0)
				: configedMain.getSelectedDepots().get(0);
		if (connectToHost == null) {
			throw new IllegalArgumentException("host must not be null. (type: " + type + ")");
		}
		if ("ConfigserverOrDepot".equals(type) && connectToHost
				.equals(persistenceController.getDataServices().hostInfoCollections.getConfigServer())) {
			connectToHost = "Configserver";
		}

		if (!configedMain.isHostConnected(connectToHost) && !"Configserver".equals(connectToHost)) {
			Logging.info(type, " shell access feature is only supported for clients connected with messagebus");
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.openTerminalOn" + type + "Feature.message"));
			return;
		}
		TerminalFrame terminalFrame = new TerminalFrame(configedMain);
		terminalFrame.setMessagebus(Messagebus.getInstance());
		terminalFrame.setSession(connectToHost);
		terminalFrame.display();
	}
}
