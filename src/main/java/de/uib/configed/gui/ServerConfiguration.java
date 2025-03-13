/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import de.uib.configed.Configed;
import de.uib.configed.UpdateCollectionManager;
import de.uib.configed.gui.hostconfigs.PanelHostConfig;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.datachanges.ConfigUpdateCollection;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class ServerConfiguration extends JTabbedPane {
	private PanelHostConfig panelHostConfig;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ServerConfiguration() {
		panelHostConfig = new PanelHostConfig(this::setHostConfigTab,
				persistenceController.getUserRolesConfigDataService().hasServerFullPermissionPD());

		setHostConfigTab();

		super.addTab(Configed.getResourceValue("MainFrame.jPanel_NetworkConfig"), panelHostConfig);
	}

	private void setHostConfigTab() {
		Logging.info(this, "setNetworkconfigurationPage for server");
		ConfigUpdateCollection configUpdateCollection = new ConfigUpdateCollection(
				Collections.singletonList(persistenceController.getHostInfoCollections().getConfigServer()));

		UpdateCollectionManager.addToGlobalUpdateCollection(configUpdateCollection);

		List<Map<String, List<Object>>> additionalConfigs = new ArrayList<>(1);
		Map<String, List<Object>> defaultValuesMap = persistenceController.getConfigDataService()
				.getConfigDefaultValuesPD();
		additionalConfigs.add(defaultValuesMap);
		configUpdateCollection.setMasterConfig(true);
		panelHostConfig.initEditing(
				persistenceController.getHostInfoCollections().getConfigServer() + " (configuration server)",
				additionalConfigs.get(0), POJOReMapper.remap(additionalConfigs), configUpdateCollection,
				OpsiServiceNOMPersistenceController.getPropertyClassesServer(), null, false);
	}
}
