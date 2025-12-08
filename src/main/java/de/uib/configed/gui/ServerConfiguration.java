/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import de.uib.configed.core.domain.datachanges.ConfigUpdateCollection;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.features.hostconfigs.PanelHostConfig;
import de.uib.configed.share.logging.Logging;

public class ServerConfiguration extends JTabbedPane {
	private PanelHostConfig panelHostConfig;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ServerConfiguration() {
		panelHostConfig = new PanelHostConfig(this::setHostConfigTab, true);

		setHostConfigTab();

		super.addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);
	}

	private void setHostConfigTab() {
		Logging.info(this, "setNetworkconfigurationPage for server");
		ConfigUpdateCollection configUpdateCollection = new ConfigUpdateCollection();

		UpdateCollectionManager.addToGlobalUpdateCollection(configUpdateCollection);

		List<Map<String, List<Object>>> additionalConfigs = new ArrayList<>(1);
		Map<String, List<Object>> defaultValuesMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();
		additionalConfigs.add(defaultValuesMap);
		panelHostConfig.initEditing(
				persistenceController.getHostInfoCollections().getConfigServer() + " (configuration server)",
				additionalConfigs.get(0), POJOReMapper.remap(additionalConfigs), configUpdateCollection,
				OpsiServiceNOMPersistenceController.getPropertyClassesServer(), null, false);
	}

	public boolean isCurrentUserRoleSelected() {
		return panelHostConfig.isSelected(persistenceController.getExecutioner().getUsername());
	}
}
