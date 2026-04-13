/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;

public class DriverUploadDialog {
	private PanelDriverUpload panelDriverUpload;

	private ConfigedMain configedMain;

	private JDialog dialog;

	public DriverUploadDialog(ConfigedMain configedMain) {
		super();

		this.configedMain = configedMain;

		ConfigedMain.getMainFrame().activateLoadingCursor();
		panelDriverUpload = new PanelDriverUpload(configedMain);
		ConfigedMain.getMainFrame().deactivateLoadingCursor();

		JOptionPane optionPane = new JOptionPane(panelDriverUpload, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null,
				new Object[] { panelDriverUpload.getButtonUploadDrivers(), Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(), Configed.getResourceValue("FDriverUpload.title"));
		dialog.setModal(false);

		panelDriverUpload.setDialog(dialog);
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
		panelDriverUpload.evaluateWinProducts();
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

		dialog.pack();
	}
}
