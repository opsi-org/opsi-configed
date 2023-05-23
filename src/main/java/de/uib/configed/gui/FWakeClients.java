package de.uib.configed.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFrame;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.JSONthroughHTTPS;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.logging.Logging;

public class FWakeClients extends FShowList {
	private boolean cancelled;
	private OpsiserviceNOMPersistenceController persist;

	public FWakeClients(JFrame master, String title, OpsiserviceNOMPersistenceController persist) {
		super(master, title, false, new String[] { Configed.getResourceValue("FWakeClients.cancel") });
		if (!Main.FONT) {
			super.setFont(Globals.defaultFont);
		}
		super.setMessage("");
		super.setButtonsEnabled(true);
		this.persist = persist;
	}

	public void act(String[] selectedClients, int delaySecs) {
		setVisible(true);
		glassTransparency(true, 1000, 200, 0.04F);
		Map<String, List<String>> hostSeparationByDepots = persist.getHostSeparationByDepots(selectedClients);
		Map<String, Integer> counterByDepots = new HashMap<>();
		Map<String, AbstractExecutioner> executionerForDepots = new HashMap<>();

		int maxSize = 0;

		for (Entry<String, List<String>> depotEntry : hostSeparationByDepots.entrySet()) {
			counterByDepots.put(depotEntry.getKey(), 0);
			if (depotEntry.getValue().size() > maxSize) {
				maxSize = depotEntry.getValue().size();
			}
		}

		int turn = 0;
		while (turn < maxSize && !cancelled) {
			Set<String> hostsToWakeOnThisTurn = new HashSet<>();

			for (Entry<String, List<String>> depotEntry : hostSeparationByDepots.entrySet()) {

				Logging.info(this, "act on depot " + depotEntry.getKey() + ", executioner != NONE  "
						+ (executionerForDepots.get(depotEntry.getKey()) != AbstractExecutioner.getNoneExecutioner())
						+ " counterByDepots.get(depot) " + counterByDepots.get(depotEntry.getKey()));

				if (executionerForDepots.get(depotEntry.getKey()) != AbstractExecutioner.getNoneExecutioner()
						&& counterByDepots.get(depotEntry.getKey()) < depotEntry.getValue().size()) {

					if (executionerForDepots.get(depotEntry.getKey()) == null) {
						AbstractExecutioner exec1 = persist.retrieveWorkingExec(depotEntry.getKey());
						// we try to connect when the first client of a depot should be connected

						// may be Executioner.NONE
						executionerForDepots.put(depotEntry.getKey(), exec1);

						if (exec1 == AbstractExecutioner.getNoneExecutioner()) {
							appendLine("!! giving up connecting to  " + depotEntry.getKey());
						}

					}

					if (executionerForDepots.get(depotEntry.getKey()) != AbstractExecutioner.getNoneExecutioner()) {
						String host = depotEntry.getValue().get(turn);

						String line = String.format("trying to start up   %s    from depot    %s  ", host,
								depotEntry.getKey());
						appendLine(line);
						Logging.info(this, "act: " + line);
						hostsToWakeOnThisTurn.add(host);
						Logging.info(this, "act: hostsToWakeOnThisTurn " + hostsToWakeOnThisTurn);
						counterByDepots.put(depotEntry.getKey(), counterByDepots.get(depotEntry.getKey()) + 1);
					}
				}
			}

			if (JSONthroughHTTPS.isOpsi43()) {
				persist.wakeOnLanOpsi43(hostsToWakeOnThisTurn.toArray(new String[0]));
			} else {
				persist.wakeOnLan(hostsToWakeOnThisTurn, hostSeparationByDepots, executionerForDepots);
			}

			Globals.threadSleep(this, 1000L * delaySecs);
			turn++;
		}

		jButton1.setText(Configed.getResourceValue("FWakeClients.close"));
	}

	@Override
	public void doAction1() {
		cancelled = true;
		super.doAction1();
	}
}
