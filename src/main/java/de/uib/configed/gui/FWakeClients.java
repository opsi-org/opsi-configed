/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFrame;

import de.uib.configed.Configed;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class FWakeClients extends FShowList {
	private boolean cancelled;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public FWakeClients(JFrame master, String title) {
		super(master, title, false, new String[] { Configed.getResourceValue("FWakeClients.cancel") });

		super.setMessage("");
		super.setButtonsEnabled(true);
	}

	public void act(List<String> selectedClients, int delaySecs) {
		setVisible(true);

		Map<String, List<String>> hostSeparationByDepots = persistenceController.getHostDataService()
				.getHostSeparationByDepots(selectedClients);
		Map<String, Integer> counterByDepots = new HashMap<>();
		Map<String, AbstractExecutioner> executionerForDepots = new HashMap<>();

		int maxSize = 0;

		for (Entry<String, List<String>> depotEntry : hostSeparationByDepots.entrySet()) {
			counterByDepots.put(depotEntry.getKey(), 0);

			maxSize = Math.max(maxSize, depotEntry.getValue().size());
		}

		for (int turn = 0; turn < maxSize && !cancelled; turn++) {
			Set<String> hostsToWakeOnThisTurn = new HashSet<>();

			for (Entry<String, List<String>> depotEntry : hostSeparationByDepots.entrySet()) {
				Logging.info(this, "act on depot " + depotEntry.getKey() + ", executioner != NONE  "
						+ " counterByDepots.get(depot) " + counterByDepots.get(depotEntry.getKey()));

				if (counterByDepots.get(depotEntry.getKey()) < depotEntry.getValue().size()) {
					// Get the executioner for the depot, and create new one if non-existant
					AbstractExecutioner executioner = executionerForDepots.computeIfAbsent(depotEntry.getKey(),
							this::computeExecutionerForDepot);

					// Add executioner to list and update counter
					addHostToWake(executioner, depotEntry, turn, hostsToWakeOnThisTurn, counterByDepots);
				}
			}

			if (ServerFacade.isOpsi43()) {
				persistenceController.getRPCMethodExecutor().wakeOnLanOpsi43(hostsToWakeOnThisTurn);
			} else {
				persistenceController.getRPCMethodExecutor().wakeOnLan(hostsToWakeOnThisTurn, hostSeparationByDepots,
						executionerForDepots);
			}

			Utils.threadSleep(this, 1000L * delaySecs);
		}

		jButton1.setText(Configed.getResourceValue("buttonClose"));
	}

	private AbstractExecutioner computeExecutionerForDepot(String depot) {
		AbstractExecutioner exec1 = persistenceController.retrieveWorkingExec(depot);
		// we try to connect when the first client of a depot should be connected
		if (exec1 == null) {
			appendLine("!! giving up connecting to  " + depot);
		}
		return exec1;
	}

	private void addHostToWake(AbstractExecutioner executioner, Entry<String, List<String>> depotEntry, int turn,
			Set<String> hostsToWakeOnThisTurn, Map<String, Integer> counterByDepots) {
		if (executioner != null) {
			String host = depotEntry.getValue().get(turn);

			String line = String.format("trying to start up   %s    from depot    %s  ", host, depotEntry.getKey());
			appendLine(line);
			Logging.info(this, "act: " + line);
			hostsToWakeOnThisTurn.add(host);
			Logging.info(this, "act: hostsToWakeOnThisTurn " + hostsToWakeOnThisTurn);
			counterByDepots.put(depotEntry.getKey(), counterByDepots.get(depotEntry.getKey()) + 1);
		}
	}

	@Override
	public void doAction1() {
		cancelled = true;
		super.doAction1();
	}
}
