package de.uib.configed.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import de.uib.configed.Globals;
import de.uib.configed.Configed;
import de.uib.opsicommand.Executioner;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.Logging;

public class FWakeClients extends FShowList {
	boolean cancelled = false;
	PersistenceController persist;

	public FWakeClients(JFrame master, String title, PersistenceController persist) {
		super(master, title, false, new String[] { Configed.getResourceValue("FWakeClients.cancel") });
		setFont(Globals.defaultFont);
		setMessage("");
		setButtonsEnabled(true);
		this.persist = persist;

	}

	public void act(String[] selectedClients, int delaySecs) {
		setVisible(true);
		glassTransparency(true, 1000, 200, 0.04f);
		Map<String, List<String>> hostSeparationByDepots = persist.getHostSeparationByDepots(selectedClients);
		Map<String, Integer> counterByDepots = new HashMap<>();
		Map<String, Executioner> executionerForDepots = new HashMap<>();

		int maxSize = 0;

		for (String depot : hostSeparationByDepots.keySet()) {
			counterByDepots.put(depot, 0);
			if (hostSeparationByDepots.get(depot).size() > maxSize)
				maxSize = hostSeparationByDepots.get(depot).size();
		}

		int turn = 0;
		while (turn < maxSize && !cancelled) {
			java.util.Set<String> hostsToWakeOnThisTurn = new HashSet<>();

			for (String depot : hostSeparationByDepots.keySet()) {

				Logging.info(this,
						"act on depot " + depot + ", executioner != NONE  "
								+ (executionerForDepots.get(depot) != Executioner.NONE) + " counterByDepots.get(depot) "
								+ counterByDepots.get(depot));

				if (executionerForDepots.get(depot) != Executioner.NONE
						&& counterByDepots.get(depot) < hostSeparationByDepots.get(depot).size())

				{
					if (executionerForDepots.get(depot) == null) {
						Executioner exec1 = persist.retrieveWorkingExec(depot);
						// we try to connect when the first client of a depot should be connected

						executionerForDepots.put(depot, exec1); // may be Executioner.NONE

						if (exec1 == Executioner.NONE)
							appendLine("!! giving up connecting to  " + depot);

					}

					if (executionerForDepots.get(depot) != Executioner.NONE) {
						String host = hostSeparationByDepots.get(depot).get(turn);

						String line = String.format("trying to start up   %s    from depot    %s  ", host, depot);
						appendLine(line);
						Logging.info(this, "act: " + line);
						hostsToWakeOnThisTurn.add(host);
						Logging.info(this, "act: hostsToWakeOnThisTurn " + hostsToWakeOnThisTurn);
						counterByDepots.put(depot, counterByDepots.get(depot) + 1);
					}
				}
			}

			persist.wakeOnLan(hostsToWakeOnThisTurn, hostSeparationByDepots, executionerForDepots);

			Globals.threadSleep(this, 1000 * delaySecs);
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
