/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import de.uib.configed.Configed;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;

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

		for (int i = 0; i < selectedClients.size() && !cancelled; i++) {
			String client = selectedClients.get(i);

			appendLine("trying to start up client: " + client);

			persistenceController.getRPCMethodExecutor().wakeOnLanOpsi43(Collections.singleton(client));
			Utils.threadSleep(this, 1000L * delaySecs);
		}

		jButton1.setText(Configed.getResourceValue("buttonClose"));
	}

	@Override
	public void doAction1() {
		cancelled = true;
		super.doAction1();
	}
}
