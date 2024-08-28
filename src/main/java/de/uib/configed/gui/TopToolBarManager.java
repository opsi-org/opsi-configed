/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.JButton;
import javax.swing.JToolBar;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.utils.Icons;

public class TopToolBarManager {
	private JToolBar configurationToolBar;

	OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory.getPersistenceController();

	private ConfigedMain configedMain;

	public TopToolBarManager(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		initConfigurationToolBar();
	}

	private void initConfigurationToolBar() {
		JButton addClientButton = new JButton(Icons.getIntellijIcon("add"));
		addClientButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuAddClient"));
		addClientButton.addActionListener(event -> configedMain.callNewClientDialog());
		addClientButton.setEnabled(!persistenceController.getConfigDataService().getDisabledClientMenuEntries()
				.contains(UserRolesConfigDataService.ITEM_ADD_CLIENT));

		JButton clientSearchButton = new JButton(Icons.getIntellijIcon("search"));
		clientSearchButton.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		clientSearchButton.addActionListener(event -> configedMain.callClientSelectionDialog());

		configurationToolBar = new JToolBar();
		configurationToolBar.add(addClientButton);
		configurationToolBar.add(clientSearchButton);
	}

	public JToolBar getConfigurationToolBar() {
		return configurationToolBar;
	}
}
