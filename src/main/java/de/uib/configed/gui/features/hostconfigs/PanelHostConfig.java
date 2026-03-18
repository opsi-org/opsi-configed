/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hostconfigs;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import de.uib.configed.core.domain.datachanges.ConfigUpdateCollection;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.share.datapanel.DefaultEditMapPanel;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelHostConfig extends JPanel {
	// delegate
	private EditMapPanelGroupedForHostConfigs editMapPanel;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private Runnable configUpdater;

	public PanelHostConfig(Runnable configUpdater, boolean isServerConfig, boolean isClientConfig) {
		this.configUpdater = configUpdater;

		buildPanel(isServerConfig, isClientConfig);
	}

	private void reloadHostConfig() {
		Logging.info(this, "reloadHostConfig");

		if (!ChangedDataManager.checkSaveAll(true)) {
			return;
		}

		persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		persistenceController.reloadData(CacheIdentifier.HOST_CONFIGS.toString());

		configUpdater.run();
	}

	private void saveHostConfig() {
		Logging.debug(this, "saveHostConfig");
		ChangedDataManager.checkSaveAll(false);
	}

	private void buildPanel(boolean isServerConfig, boolean isClientConfig) {
		editMapPanel = new EditMapPanelGroupedForHostConfigs(new DefaultEditMapPanel.Actor() {
			@Override
			public void reloadData() {
				reloadHostConfig();
			}

			@Override
			public void saveData() {
				saveHostConfig();
			}
		}, isServerConfig, isClientConfig);

		editMapPanel.getMapTableModel().registerDataChangedKeeper(ChangedDataManager.getHostConfigsDataChangedKeeper());

		this.setLayout(new MigLayout("insets 0, fill", "[]", "[]0"));
		this.add(editMapPanel, "grow");
	}

	public void initEditing(String labeltext, Map<String, List<Object>> configVisualMap,
			Collection<Map<String, Object>> collectionConfigStored, ConfigUpdateCollection configUpdateCollection,
			Map<String, String> classesMap, Map<String, List<Object>> originalMap,
			boolean includeAdditionalTooltipText) {
		Logging.info(this, "initEditing, label:", labeltext);
		editMapPanel.setSubpanelClasses(classesMap);
		if (originalMap != null) {
			editMapPanel.setOriginalMap(POJOReMapper.remap(originalMap));
		}
		editMapPanel.includeAdditionalTooltipText(includeAdditionalTooltipText);
		editMapPanel.setEditableMap(POJOReMapper.remap(configVisualMap),
				persistenceController.getDataServices().config.getConfigOptionsPD());
		editMapPanel.updateData(configUpdateCollection, collectionConfigStored);
		editMapPanel.setLabel(labeltext);
	}

	public boolean isSelected(String obj) {
		return editMapPanel.isSelected(obj);
	}
}
