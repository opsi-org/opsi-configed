/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utils.Icons;
import de.uib.utils.swing.SecondaryFrame;

public class DriverUploadDialog extends SecondaryFrame {
	private PanelDriverUpload panelDriverUpload;

	private ConfigedMain configedMain;

	public DriverUploadDialog(ConfigedMain configedMain) {
		super();

		this.configedMain = configedMain;

		panelDriverUpload = new PanelDriverUpload(configedMain, this);
		super.setContentPane(panelDriverUpload);

		super.setIconImage(Icons.getMainIcon());
		super.setTitle(Configed.getResourceValue("FDriverUpload.title"));
	}

	public void setUploadParameters(String byAuditPath) {
		panelDriverUpload.setByAuditPath(byAuditPath);

		String clientName;

		if (configedMain.getSelectedClients().size() == 1) {
			clientName = configedMain.getSelectedClients().get(0);
		} else {
			clientName = "";
		}

		panelDriverUpload.setClientName(clientName);
		panelDriverUpload.setDepot();
	}
}
