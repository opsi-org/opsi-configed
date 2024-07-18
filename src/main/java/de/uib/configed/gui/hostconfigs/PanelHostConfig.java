/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.opsidatamodel.datachanges.AdditionalconfigurationUpdateCollection;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.DataChangedObserver;
import de.uib.utils.datapanel.DefaultEditMapPanel;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.ListCellOptions;

public class PanelHostConfig extends JPanel {
	public static final String PROPERTY_CLASS_USER = UserConfig.KEY_USER_ROOT;
	public static final String PROPERTY_CLASS_ROLE = UserConfig.KEY_USER_ROLE_ROOT;

	// delegate
	private EditMapPanelGroupedForHostConfigs editMapPanel;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;

	public PanelHostConfig(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		buildPanel();
	}

	// overwrite in subclasses
	protected void reloadHostConfig() {
		Logging.info(this, "reloadHostConfig");

		configedMain.cancelChanges();

		persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		persistenceController.reloadData(CacheIdentifier.HOST_CONFIGS.toString());

		configedMain.resetView(ConfigedMain.VIEW_NETWORK_CONFIGURATION);
	}

	// overwrite in subclasses
	protected void saveHostConfig() {
		Logging.debug(this, "saveHostConfig");
		configedMain.checkSaveAll(false);
	}

	private void handleUserInPropertyClass(String superclass, String user) {
		Logging.info(this, "handleUserInPropertyClass ", user, " in class ", superclass);

		String newpropertyclass = superclass + "." + user;
		OpsiServiceNOMPersistenceController.getPropertyClassesServer().computeIfAbsent(newpropertyclass,
				(String arg) -> {
					Logging.debug(this, "putUsersToPropertyclassesTreeMap found another user named ", user, " [",
							newpropertyclass, "]");
					return "";
				});
	}

	private void putUsersToPropertyclassesTreeMap() {
		Map<String, Object> configs = PersistenceControllerFactory.getPersistenceController().getConfigDataService()
				.getHostConfig(PersistenceControllerFactory.getPersistenceController().getHostInfoCollections()
						.getConfigServer());

		for (Entry<String, Object> entry : configs.entrySet()) {
			String key = entry.getKey();

			if (key.startsWith(PROPERTY_CLASS_ROLE + ".")) {
				String user = key.split("\\.")[2];
				Logging.info(this, "putUsersToPropertyclassesTreeMap found role (user) ", user, " by config key ", key);
				handleUserInPropertyClass(PROPERTY_CLASS_ROLE, user);
			} else if (key.startsWith(PROPERTY_CLASS_USER + ".")) {
				String user = key.split("\\.")[1];
				Logging.info(this, "putUsersToPropertyclassesTreeMap found user ", user, " by config key ", key);
				if (!"{}".equals(user)) {
					handleUserInPropertyClass(PROPERTY_CLASS_USER, user);
				}
			} else {
				// Do nothing when it's not a user or a userrole
			}
		}
	}

	private void buildPanel() {
		putUsersToPropertyclassesTreeMap();

		editMapPanel = new EditMapPanelGroupedForHostConfigs(new PropertiesTableCellRenderer(), true, true,
				new DefaultEditMapPanel.Actor() {
					@Override
					public void reloadData() {
						reloadHostConfig();
					}

					@Override
					public void saveData() {
						saveHostConfig();
					}
				});

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createParallelGroup().addComponent(editMapPanel));

		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup().addComponent(editMapPanel,
				Globals.LINE_HEIGHT * 2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void initEditing(String labeltext, Map configVisualMap, Map<String, ListCellOptions> configOptions,
			Collection collectionConfigStored, AdditionalconfigurationUpdateCollection configurationUpdateCollection,
			boolean optionsEditable, NavigableMap<String, String> classesMap) {
		initEditing(labeltext, configVisualMap, configOptions, collectionConfigStored, configurationUpdateCollection,
				optionsEditable, classesMap, null, false);
	}

	public void initEditing(String labeltext, Map configVisualMap, Map<String, ListCellOptions> configOptions,
			Collection collectionConfigStored, AdditionalconfigurationUpdateCollection configurationUpdateCollection,
			boolean optionsEditable, NavigableMap<String, String> classesMap, Map<String, Object> originalMap,
			boolean includeAdditionalTooltipText) {
		Logging.info(this, "initEditing  optionsEditable ", optionsEditable);
		editMapPanel.setSubpanelClasses(classesMap);
		if (originalMap != null) {
			editMapPanel.setOriginalMap(originalMap);
		}
		editMapPanel.includeAdditionalTooltipText(includeAdditionalTooltipText);
		editMapPanel.setEditableMap(configVisualMap, configOptions);
		editMapPanel.setStoreData(collectionConfigStored);
		editMapPanel.setUpdateCollection(configurationUpdateCollection);
		editMapPanel.setLabel(labeltext);
		editMapPanel.setOptionsEditable(optionsEditable);
	}

	// delegated methods
	public void registerDataChangedObserver(DataChangedObserver o) {
		editMapPanel.registerDataChangedObserver(o);
	}
}
