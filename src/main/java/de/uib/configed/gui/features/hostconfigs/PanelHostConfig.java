/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hostconfigs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.swing.GroupLayout;
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

public class PanelHostConfig extends JPanel {
	// delegate
	private EditMapPanelGroupedForHostConfigs editMapPanel;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private Runnable configUpdater;

	public PanelHostConfig(Runnable configUpdater, boolean isServerConfig) {
		this.configUpdater = configUpdater;

		buildPanel(isServerConfig);
	}

	private void reloadHostConfig() {
		Logging.info(this, "reloadHostConfig");

		ChangedDataManager.checkSaveAll(true);

		persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		persistenceController.reloadData(CacheIdentifier.HOST_CONFIGS.toString());

		configUpdater.run();
	}

	private void saveHostConfig() {
		Logging.debug(this, "saveHostConfig");
		ChangedDataManager.checkSaveAll(false);
	}

	private void buildPanel(boolean isServerConfig) {
		editMapPanel = new EditMapPanelGroupedForHostConfigs(new DefaultEditMapPanel.Actor() {
			@Override
			public void reloadData() {
				reloadHostConfig();
			}

			@Override
			public void saveData() {
				saveHostConfig();
			}
		}, isServerConfig);

		editMapPanel.getMapTableModel()
				.registerDataChangedObserver(ChangedDataManager.getHostConfigsDataChangedKeeper());

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createParallelGroup().addComponent(editMapPanel));

		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup().addComponent(editMapPanel));
	}

	public void initEditing(String labeltext, Map<String, List<Object>> configVisualMap,
			Collection<Map<String, Object>> collectionConfigStored, ConfigUpdateCollection configUpdateCollection,
			NavigableMap<String, String> classesMap, Map<String, List<Object>> originalMap,
			boolean includeAdditionalTooltipText) {
		Logging.info(this, "initEditing, label:", labeltext);
		editMapPanel.setSubpanelClasses(classesMap);
		if (originalMap != null) {
			editMapPanel.setOriginalMap(POJOReMapper.remap(originalMap));
		}
		editMapPanel.includeAdditionalTooltipText(includeAdditionalTooltipText);
		editMapPanel.setEditableMap(POJOReMapper.remap(configVisualMap),
				persistenceController.getConfigDataService().getConfigOptionsPD());
		editMapPanel.updateData(configUpdateCollection, collectionConfigStored);
		editMapPanel.setLabel(labeltext);
	}

	public boolean isSelected(String obj) {
		return editMapPanel.isSelected(obj);
	}
}
