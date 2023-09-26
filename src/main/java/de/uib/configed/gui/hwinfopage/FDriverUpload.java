/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.SecondaryFrame;
import utils.Utils;

public class FDriverUpload extends SecondaryFrame {

	private PanelDriverUpload panelDriverUpload;

	private ConfigedMain configedMain;

	public FDriverUpload(ConfigedMain configedMain) {
		super();

		this.configedMain = configedMain;

		init();
		super.setGlobals(Utils.getMap());
		super.setTitle(Configed.getResourceValue("FDriverUpload.title"));
	}

	private void init() {
		panelDriverUpload = new PanelDriverUpload(configedMain, this);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(panelDriverUpload, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(panelDriverUpload, 100,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		if (!Main.THEMES) {
			Containership containerShipAll = new Containership(getContentPane());
			containerShipAll.doForAllContainedCompisOfClass("setBackground",
					new Object[] { Globals.BACKGROUND_COLOR_7 }, JPanel.class);

			containerShipAll.doForAllContainedCompisOfClass("setBackground",
					new Object[] { Globals.BACKGROUND_COLOR_3 }, JTextComponent.class);
		}
	}

	public void setUploadParameters(String byAuditPath) {
		panelDriverUpload.setByAuditPath(byAuditPath);

		String clientName;

		if (configedMain.getSelectedClients() != null && configedMain.getSelectedClients().length == 1) {
			clientName = configedMain.getSelectedClients()[0];
		} else {
			clientName = "";
		}

		panelDriverUpload.setClientName(clientName);
		panelDriverUpload.setDepot(configedMain.getConfigserver());
	}
}
