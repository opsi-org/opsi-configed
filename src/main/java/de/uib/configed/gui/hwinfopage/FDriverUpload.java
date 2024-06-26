/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import javax.swing.GroupLayout;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.swing.SecondaryFrame;

public class FDriverUpload extends SecondaryFrame {
	private PanelDriverUpload panelDriverUpload;

	private ConfigedMain configedMain;

	public FDriverUpload(ConfigedMain configedMain) {
		super();

		this.configedMain = configedMain;

		init();

		super.setIconImage(Utils.getMainIcon());
		super.setTitle(Configed.getResourceValue("FDriverUpload.title"));
	}

	private void init() {
		panelDriverUpload = new PanelDriverUpload(configedMain, this);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(panelDriverUpload, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(panelDriverUpload, 100,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void setUploadParameters(String byAuditPath) {
		panelDriverUpload.setByAuditPath(byAuditPath);

		String clientName;

		if (configedMain.getSelectedClients() != null && configedMain.getSelectedClients().size() == 1) {
			clientName = configedMain.getSelectedClients().get(0);
		} else {
			clientName = "";
		}

		panelDriverUpload.setClientName(clientName);
		panelDriverUpload.setDepot();
	}
}
