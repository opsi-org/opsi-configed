/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
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

import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.script.Interpreter;

public class RemoteCommandExecutor extends SwingWorker<Void, String> {
	private String command;
	private List<String> targetClients;
	private FDialogRemoteControl fDialogRemoteControl;

	public RemoteCommandExecutor(FDialogRemoteControl fDialogRemoteControl, String firstSelectedClient,
			List<String> targetClients) {
		this.fDialogRemoteControl = fDialogRemoteControl;
		this.command = firstSelectedClient;
		this.targetClients = targetClients;
	}

	@Override
	protected Void doInBackground() throws Exception {
		for (String targetClient : targetClients) {
			String cmd = interpretCommand(command, targetClient);
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
					publish(command + " on " + targetClient + " >" + line + "\n");
				}
			} catch (IOException ex) {
				Logging.error(ex, "Runtime error for command >>", cmd, "<<, : ", ex);
			}
		}
		return null;
	}

	private String interpretCommand(String command, String targetClient) {
		String cmd = fDialogRemoteControl.getValue(command);

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

		HostInfo pcInfo = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().get(targetClient);
		values.put("%ipaddress%", pcInfo.getIpAddress());
		values.put("%hardwareaddress%", pcInfo.getMacAddress());
		values.put("%inventorynumber%", pcInfo.getInventoryNumber());
		values.put("%opsihostkey%", pcInfo.getHostKey());
		values.put("%depotid%", pcInfo.getInDepot());
		values.put("%configserverid%", persistenceController.getHostInfoCollections().getConfigServer());

		trans.setValues(values);

		return trans.interpret();
	}

	@Override
	protected void process(List<String> logLines) {
		for (String logLine : logLines) {
			fDialogRemoteControl.appendLog(logLine);
		}
	}
}
