/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.ChangedDataManager;
import de.uib.configed.Globals;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.datachanges.ConfigUpdateCollection;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.datapanel.DefaultEditMapPanel;
import de.uib.utils.logging.Logging;

public class PanelHostConfig extends JPanel {
	// delegate
	private EditMapPanelGroupedForHostConfigs editMapPanel;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private Runnable configUpdater;

	public PanelHostConfig(Runnable configUpdater, boolean configStatesEditable) {
		this.configUpdater = configUpdater;

		buildPanel(configStatesEditable);
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

	private void buildPanel(boolean configStatesEditable) {
		editMapPanel = new EditMapPanelGroupedForHostConfigs(new DefaultEditMapPanel.Actor() {
			@Override
			public void reloadData() {
				reloadHostConfig();
			}

			@Override
			public void saveData() {
				saveHostConfig();
			}
		}, configStatesEditable);

		editMapPanel.getMapTableModel()
				.registerDataChangedObserver(ChangedDataManager.getHostConfigsDataChangedKeeper());

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createParallelGroup().addComponent(editMapPanel));

		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup().addComponent(editMapPanel,
				Globals.LINE_HEIGHT * 2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
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
				POJOReMapper.remap(persistenceController.getConfigDataService().getConfigOptionsPD()));
		editMapPanel.setStoreData(collectionConfigStored);
		editMapPanel.setUpdateCollection(POJOReMapper.remap(configUpdateCollection));
		editMapPanel.setLabel(labeltext);
	}
}
