/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.script.Interpreter;

public class RemoteCommandExecutor extends SwingWorker<Void, String> {
	private String command;
	private String client;
	private RemoteControlDialog remoteControlDialog;

	public RemoteCommandExecutor(RemoteControlDialog remoteControlDialog, String command, String client) {
		this.remoteControlDialog = remoteControlDialog;
		this.command = command;
		this.client = client;
	}

	@Override
	protected Void doInBackground() throws Exception {
		String cmd = interpretCommand(command, client);

		List<String> parts = Interpreter.splitToList(cmd);
		try {
			Logging.debug(this, "startRemoteControlForSelectedClients, cmd: ", cmd, " splitted to ", parts);

			ProcessBuilder pb = new ProcessBuilder(parts);
			pb.redirectErrorStream(true);

			Process proc = pb.start();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));

			String line = null;
			while ((line = br.readLine()) != null) {
				publish(command + " on " + client + " >" + line + "\n");
			}
		} catch (IOException ex) {
			Logging.error(ex, "Runtime error for command>>", cmd, "<<,: ", ex);
		}

		return null;
	}

	private String interpretCommand(String command, String targetClient) {
		String cmd = remoteControlDialog.getValue(command);

		Interpreter trans = new Interpreter(new String[] { "%host%", "%hostname%", "%ipaddress%", "%inventorynumber%",
				"%hardwareaddress%", "%opsihostkey%", "%depotid%", "%configserverid%" });

		trans.setCommand(cmd);

		Map<String, String> values = new HashMap<>();
		values.put("%host%", targetClient);
		String hostName = targetClient;
		Logging.info(this, " targetClient ", targetClient);
		if (targetClient.contains(".")) {
			String[] parts = targetClient.split("\\.");
			Logging.info(this, " targetClient ", Arrays.toString(parts));
			hostName = parts[0];
		}

		values.put("%hostname%", hostName);

		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();

		HostInfo pcInfo = persistenceController.getDataServices().hostInfoCollections.getMapOfPCInfoMaps()
				.get(targetClient);
		values.put("%ipaddress%", pcInfo.getString(HostInfo.CLIENT_IP_ADDRESS_KEY));
		values.put("%hardwareaddress%", pcInfo.getString(HostInfo.CLIENT_MAC_ADDRESS_KEY));
		values.put("%inventorynumber%", pcInfo.getString(HostInfo.CLIENT_INVENTORY_NUMBER_KEY));
		values.put("%opsihostkey%", pcInfo.getString(HostInfo.HOST_KEY_KEY));
		values.put("%depotid%", pcInfo.getString(HostInfo.DEPOT_OF_CLIENT_KEY));
		values.put("%configserverid%", persistenceController.getDataServices().hostInfoCollections.getConfigServer());

		trans.setValues(values);

		return trans.interpret();
	}

	@Override
	protected void process(List<String> logLines) {
		for (String logLine : logLines) {
			remoteControlDialog.appendLog(logLine);
		}
	}
}
