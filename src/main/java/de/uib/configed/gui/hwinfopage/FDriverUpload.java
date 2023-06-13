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
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.SecondaryFrame;

public class FDriverUpload extends SecondaryFrame {

	private PanelDriverUpload panelDriverUpload;

	private ConfigedMain main;

	public FDriverUpload(ConfigedMain main) {
		super();

		this.main = main;

		init();
		super.setGlobals(Globals.getMap());
		super.setTitle(Globals.APPNAME + " " + Configed.getResourceValue("FDriverUpload.title"));
	}

	private void init() {
		panelDriverUpload = new PanelDriverUpload(main, this);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(panelDriverUpload, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

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

		Logging.info(this, " setUploadParameters " + main.getSelectedClients()[0]);

		if (main.getSelectedClients() != null && main.getSelectedClients().length == 1) {
			panelDriverUpload.setClientName(main.getSelectedClients()[0]);
		} else {
			panelDriverUpload.setClientName("");
		}

		panelDriverUpload.setDepot(main.getConfigserver());
	}
}
