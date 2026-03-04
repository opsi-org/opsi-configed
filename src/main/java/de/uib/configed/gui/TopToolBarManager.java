/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import de.uib.configed.core.domain.modulelicense.LicensingInfoMap;
import de.uib.configed.core.domain.modulelicense.OpsiLicensing;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.healthcheck.HealthCheckComponent;
import de.uib.configed.gui.healthcheck.settings.HealthCheckSettingsComponent;
import de.uib.configed.gui.share.icons.Icons;

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

	public List<JButton> getOpsiLicensingButtons(OpsiLicensing opsiLicensing) {
		JButton reloadButton = new JButton(Icons.getIntellijIcon("refresh", 24));
		reloadButton.setToolTipText(Configed.getResourceValue("reload"));
		reloadButton.addActionListener((ActionEvent actionEvent) -> {
			LicensingInfoMap.requestRefresh();
			opsiLicensing.reload();
		});

		return List.of(reloadButton);
	}

	public List<JButton> getHealthCheckButtons(HealthCheckComponent healthCheck) {
		JButton downloadButton = new JButton(Icons.getIntellijIcon("download", 24));
		downloadButton.setToolTipText(Configed.getResourceValue("download"));
		downloadButton.addActionListener(actionEvent -> healthCheck.saveAsZip());

		JButton healthCheckSettingsButton = new JButton(Icons.getIntellijIcon("settings", 24));
		healthCheckSettingsButton.setToolTipText(Configed.getResourceValue("HealthCheckSettingsDialog.tooltip"));
		healthCheckSettingsButton.addActionListener(
				actionEvent -> new HealthCheckSettingsComponent().showHealthCheckSettings(configedMain));
		healthCheckSettingsButton.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		return List.of(downloadButton, healthCheckSettingsButton);
	}

	public List<JButton> getConfigurationButtons() {
		JButton addClientButton = new JButton(Icons.getIntellijIcon("add", 24));
		addClientButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuAddClient"));
		addClientButton.addActionListener(event -> ExtraFrameController.callAddClientDialog());
		addClientButton.setEnabled(persistenceController.getDataServices().userRoles.canCreateClients());

		JButton clientSearchButton = new JButton(Icons.getIntellijIcon("search", 24));
		clientSearchButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		clientSearchButton.addActionListener(event -> ExtraFrameController.callClientSelectionDialog(configedMain));

		return List.of(addClientButton, clientSearchButton);
	}

	public List<JButton> getLicensingManagementButtons(MainPanelManager mainPanelManager) {
		JButton reloadButton = new JButton(Icons.getIntellijIcon("refresh", 24));
		reloadButton.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"));
		reloadButton.addActionListener(event -> mainPanelManager.reloadLicensesAction());

		return List.of(reloadButton);
	}
}
