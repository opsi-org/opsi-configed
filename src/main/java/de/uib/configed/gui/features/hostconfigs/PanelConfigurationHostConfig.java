/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.hostconfigs;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.uib.configed.core.domain.datachanges.ConfigUpdateCollection;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.AbstractConfigurationTab;
import de.uib.configed.gui.ConfigedUtilityMethods;
import de.uib.configed.gui.UpdateCollectionManager;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class PanelConfigurationHostConfig extends AbstractConfigurationTab {
	private PanelHostConfig panelHostConfig;
	private Supplier<List<String>> getSelectedHosts;

	private ConfigUpdateCollection configUpdateCollection;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelConfigurationHostConfig(boolean isClientConfig, Supplier<List<String>> getSelectedHosts) {
		super(true, isClientConfig);
		this.getSelectedHosts = getSelectedHosts;
		panelHostConfig = new PanelHostConfig(this::updateContent, false, isClientConfig);

		super.setComponent(panelHostConfig);
	}

	@Override
	protected void updateContent() {
		// This will get the selected hosts or selected depots depending on the context
		List<String> selectedHosts = getSelectedHosts.get();
		Logging.info(this, "setNetworkconfigurationPage, selectedHosts ", selectedHosts);

		if (configUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(configUpdateCollection);
		}

		configUpdateCollection = new ConfigUpdateCollection(selectedHosts);
		UpdateCollectionManager.addToGlobalUpdateCollection(configUpdateCollection);

		List<Map<String, Object>> additionalConfigs = persistenceController.getDataServices().config
				.getHostsConfigsWithDefaults(selectedHosts);
		Map<String, List<Object>> mergedVisualMap = ConfigedUtilityMethods.mergeMaps(additionalConfigs);
		ConfigedUtilityMethods.removeKeysStartingWith(mergedVisualMap,
				OpsiServiceNOMPersistenceController.getConfigKeyStartersNotForClients());

		Map<String, List<Object>> originalMap = ConfigedUtilityMethods.mergeMaps(
				persistenceController.getDataServices().config.getHostsConfigsWithoutDefaults(selectedHosts));
		panelHostConfig.initEditing(Utils.getListStringRepresentation(selectedHosts), mergedVisualMap,
				additionalConfigs, configUpdateCollection,
				OpsiServiceNOMPersistenceController.getPropertyClassesClient(), originalMap, true);
	}
}
