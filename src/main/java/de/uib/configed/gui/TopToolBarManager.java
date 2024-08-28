/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JToolBar;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.modulelicense.OpsiLicensing;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.utils.Icons;

public class TopToolBarManager {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;

	public TopToolBarManager(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public JToolBar getOpsiLicensingToolBar(OpsiLicensing opsiLicensing) {
		JButton reloadButton = new JButton(Icons.getIntellijIcon("refresh"));
		reloadButton.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.buttonReload"));
		reloadButton.addActionListener((ActionEvent actionEvent) -> {
			LicensingInfoMap.requestRefresh();
			opsiLicensing.reload();
		});

		JToolBar toolBar = new JToolBar();
		toolBar.add(reloadButton);

		return toolBar;
	}

	public JToolBar getHealthCheckToolBar(HealthCheck healthCheck) {
		JButton downloadButton = new JButton(Icons.getIntellijIcon("download"));
		downloadButton.setToolTipText(Configed.getResourceValue("download"));
		downloadButton.addActionListener(actionEvent -> healthCheck.saveAsZip());

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(downloadButton);

		return jToolBar;
	}

	public JToolBar getConfigurationToolBar() {
		JButton addClientButton = new JButton(Icons.getIntellijIcon("add"));
		addClientButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuAddClient"));
		addClientButton.addActionListener(event -> configedMain.callNewClientDialog());
		addClientButton.setEnabled(!persistenceController.getConfigDataService().getDisabledClientMenuEntries()
				.contains(UserRolesConfigDataService.ITEM_ADD_CLIENT));

		JButton clientSearchButton = new JButton(Icons.getIntellijIcon("search"));
		clientSearchButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		clientSearchButton.addActionListener(event -> configedMain.callClientSelectionDialog());

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(addClientButton);
		jToolBar.add(clientSearchButton);

		return jToolBar;
	}
}
