/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import de.uib.configed.ChangedDataManager;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ExtraFrameController;
import de.uib.configed.Globals;
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

	public JToolBar createGeneralToolBar() {
		JButton jButtonReload = new JButton(Icons.getIntellijIcon("refresh", 24));
		jButtonReload.setToolTipText(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		jButtonReload.addActionListener(event -> configedMain.reload());

		JButton jButtonSaveConfiguration = new JButton(Icons.getIntellijIcon("save", Globals.OPSI_ERROR, 24));
		jButtonSaveConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"));
		jButtonSaveConfiguration.setEnabled(false);
		jButtonSaveConfiguration.addActionListener(event -> ChangedDataManager.checkSaveAll(false));
		jButtonSaveConfiguration.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				// This method is called, when an ancestor of this button is shown.
				// So we set this button as the shown save button
				ChangedDataManager.setShownSaveButton(jButtonSaveConfiguration);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
				// We don't need this here
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				// We don't need this here
			}
		});

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(jButtonReload);
		jToolBar.add(jButtonSaveConfiguration);

		return jToolBar;
	}

	public JToolBar getOpsiLicensingToolBar(OpsiLicensing opsiLicensing) {
		JButton reloadButton = new JButton(Icons.getIntellijIcon("refresh", 24));
		reloadButton.setToolTipText(Configed.getResourceValue("reloadData"));
		reloadButton.addActionListener((ActionEvent actionEvent) -> {
			LicensingInfoMap.requestRefresh();
			opsiLicensing.reload();
		});

		JToolBar toolBar = new JToolBar();
		toolBar.add(reloadButton);

		return toolBar;
	}

	public JToolBar getHealthCheckToolBar(HealthCheck healthCheck) {
		JButton downloadButton = new JButton(Icons.getIntellijIcon("download", 24));
		downloadButton.setToolTipText(Configed.getResourceValue("download"));
		downloadButton.addActionListener(actionEvent -> healthCheck.saveAsZip());

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(downloadButton);

		return jToolBar;
	}

	public JToolBar getConfigurationToolBar() {
		JButton addClientButton = new JButton(Icons.getIntellijIcon("add", 24));
		addClientButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuAddClient"));
		addClientButton.addActionListener(event -> ExtraFrameController.callNewClientDialog());
		addClientButton.setEnabled(!persistenceController.getConfigDataService().getDisabledClientMenuEntries()
				.contains(UserRolesConfigDataService.ITEM_ADD_CLIENT));

		JButton clientSearchButton = new JButton(Icons.getIntellijIcon("search", 24));
		clientSearchButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		clientSearchButton.addActionListener(event -> ExtraFrameController.callClientSelectionDialog(configedMain));

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(addClientButton);
		jToolBar.add(clientSearchButton);

		return jToolBar;
	}

	public JToolBar getLicensingManagementToolbar(MainPanelManager mainPanelManager) {
		JButton reloadButton = new JButton(Icons.getIntellijIcon("refresh", 24));
		reloadButton.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"));
		reloadButton.addActionListener(event -> mainPanelManager.reloadLicensesAction());

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(reloadButton);

		return jToolBar;
	}
}
