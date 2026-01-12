/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hostconfigs;

import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.datachanges.ConfigUpdateCollection;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.AbstractClientConfigurationTab;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ConfigedUtilityMethods;
import de.uib.configed.gui.UpdateCollectionManager;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class PanelClientHostConfig extends AbstractClientConfigurationTab {
	private PanelHostConfig panelHostConfig;
	private ConfigedMain configedMain;

	private ConfigUpdateCollection configUpdateCollection;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelClientHostConfig(ConfigedMain configedMain) {
		super(true);
		this.configedMain = configedMain;
		panelHostConfig = new PanelHostConfig(this::updateContent, false);

		super.setComponent(panelHostConfig);
	}

	@Override
	protected void updateContent() {
		Logging.info(this, "setNetworkconfigurationPage ");
		Logging.info(this, "setNetworkconfigurationPage  selectedClients ", configedMain.getSelectedClients());

		if (configUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(configUpdateCollection);
		}

		configUpdateCollection = new ConfigUpdateCollection(configedMain.getSelectedClients());
		UpdateCollectionManager.addToGlobalUpdateCollection(configUpdateCollection);

		List<Map<String, Object>> additionalConfigs = persistenceController.getConfigDataService()
				.getHostsConfigsWithDefaults(configedMain.getSelectedClients());
		Map<String, List<Object>> mergedVisualMap = ConfigedUtilityMethods.mergeMaps(additionalConfigs);
		ConfigedUtilityMethods.removeKeysStartingWith(mergedVisualMap,
				OpsiServiceNOMPersistenceController.getConfigKeyStartersNotForClients());

		Map<String, List<Object>> originalMap = ConfigedUtilityMethods.mergeMaps(persistenceController
				.getConfigDataService().getHostsConfigsWithoutDefaults(configedMain.getSelectedClients()));
		panelHostConfig.initEditing(Utils.getListStringRepresentation(configedMain.getSelectedClients()),
				mergedVisualMap, additionalConfigs, configUpdateCollection,
				OpsiServiceNOMPersistenceController.getPropertyClassesClient(), originalMap, true);
	}
}
