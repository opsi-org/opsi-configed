package de.uib.configed.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JFrame;

/**
*  FWakeClients
 * Copyright:     Copyright (c) 2014-2015
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.Executioner;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

public class FWakeClients extends FShowList {
	boolean cancelled = false;
	PersistenceController persist;

	public FWakeClients(JFrame master, String title, PersistenceController persist) {
		super(master, title, false, new String[] { configed.getResourceValue("FWakeClients.cancel") });
		setFont(Globals.defaultFont);
		setMessage("");
		setButtonsEnabled(true);
		this.persist = persist;
		// glassTransparency(true, 1000, 200, 0.04f);
	}

	public void act(String[] selectedClients, int delaySecs) {
		setVisible(true);
		glassTransparency(true, 1000, 200, 0.04f);
		Map<String, java.util.List<String>> hostSeparationByDepots = persist.getHostSeparationByDepots(selectedClients);
		Map<String, Integer> counterByDepots = new HashMap<String, Integer>();
		Map<String, Executioner> executionerForDepots = new HashMap<String, Executioner>();

		int maxSize = 0;

		for (String depot : hostSeparationByDepots.keySet()) {
			counterByDepots.put(depot, 0);
			if (hostSeparationByDepots.get(depot).size() > maxSize)
				maxSize = hostSeparationByDepots.get(depot).size();
		}

		// Cursor oldCursor = getCursor();
		// setCursor(new Cursor(Cursor.WAIT_CURSOR));

		int turn = 0;
		while (turn < maxSize && !cancelled) {
			java.util.Set<String> hostsToWakeOnThisTurn = new HashSet<String>();

			for (String depot : hostSeparationByDepots.keySet()) {

				logging.info(this, "act on depot " + depot + ", executioner != NONE  "
						+ (executionerForDepots.get(depot) != Executioner.NONE)
						+ " counterByDepots.get(depot) " + counterByDepots.get(depot));

				if (executionerForDepots.get(depot) != Executioner.NONE
						&&
						counterByDepots.get(depot) < hostSeparationByDepots.get(depot).size())

				{
					if (executionerForDepots.get(depot) == null) {
						Executioner exec1 = persist.retrieveWorkingExec(depot);
						// we try to connect when the first client of a depot should be connected

						executionerForDepots.put(depot, exec1); // may be Executioner.NONE

						if (exec1 == Executioner.NONE)
							appendLine("!! giving up connecting to  " + depot);

					}

					// logging.info(this, "act , hostSeparationByDepots.get(depot) " +
					// hostSeparationByDepots.get(depot));
					// logging.info(this, "act , counterByDepots.get(depot) " +
					// counterByDepots.get(depot));

					if (executionerForDepots.get(depot) != Executioner.NONE) {
						String host = hostSeparationByDepots.get(depot).get(turn);
						// appendLine("trying to start up " + host + " in depot " + depot);
						// appendLine(String.format("trying to start up %4s %s from depot %s ", turn,
						// host, depot));
						String line = String.format("trying to start up   %s    from depot    %s  ", host, depot);
						appendLine(line);
						logging.info(this, "act: " + line);
						hostsToWakeOnThisTurn.add(host);
						logging.info(this, "act: hostsToWakeOnThisTurn " + hostsToWakeOnThisTurn);
						counterByDepots.put(depot, counterByDepots.get(depot) + 1);
					}
				}
			}

			persist.wakeOnLan(
					hostsToWakeOnThisTurn,
					hostSeparationByDepots,
					executionerForDepots);

			try {
				Thread.sleep(1000 * delaySecs);
			} catch (InterruptedException ies) {
			}

			turn++;
		}

		/*
		 * hostSeparationByDepot.get(depot).size());
		 * 
		 * 
		 * 
		 * 
		 * int i = 0;
		 * while (!cancelled && i < selectedClients.length)
		 * {
		 * for (String depot : hostSeparationByDepot.keySet()
		 * {
		 * 
		 * appendLine("trying to start up " + selectedClients[i]);
		 * persist.wakeOnLan(new String[] {selectedClients[i]}, hostSeparationByDepot);
		 * try
		 * {
		 * Thread.sleep(1000 * delaySecs);
		 * }
		 * catch(InterruptedException ies)
		 * {
		 * }
		 * i++;
		 * }
		 */

		// setCursor(oldCursor);
		jButton1.setText(configed.getResourceValue("FWakeClients.close"));
	}

	/*
	 * 
	 * void act(String[] selectedClients, int delaySecs)
	 * {
	 * Cursor oldCursor = getCursor();
	 * setCursor(new Cursor(Cursor.WAIT_CURSOR));
	 * 
	 * int i = 0;
	 * while (!cancelled && i < selectedClients.length)
	 * {
	 * appendLine("trying to start up " + selectedClients[i]);
	 * persist.wakeOnLan(new String[] {selectedClients[i]} );
	 * try
	 * {
	 * Thread.sleep(1000 * delaySecs);
	 * }
	 * catch(InterruptedException ies)
	 * {
	 * }
	 * i++;
	 * }
	 * setCursor(oldCursor);
	 * jButton1.setText("close");
	 * }
	 */

	@Override
	public void doAction1() {
		cancelled = true;
		super.doAction1();
	}
}
