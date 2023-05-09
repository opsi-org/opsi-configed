package de.uib.configed.gui.hostconfigs;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.datachanges.AdditionalconfigurationUpdateCollection;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.datapanel.DefaultEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelGrouped;
import de.uib.utilities.logging.Logging;

public class PanelHostConfig extends JPanel {

	public static final String PROPERTY_CLASS_USER = UserConfig.KEY_USER_ROOT;
	public static final String PROPERTY_CLASS_ROLE = UserConfig.KEY_USER_ROLE_ROOT;

	// delegate
	private EditMapPanelGrouped editMapPanel;

	private boolean keylistExtendible = true;
	private boolean entryRemovable = true;
	private boolean reloadable = true;

	public PanelHostConfig() {

		buildPanel();
	}

	// overwrite in subclasses
	protected void reloadHostConfig() {
		Logging.info(this, " in PanelHostConfig: reloadHostConfig");

	}

	// overwrite in subclasses
	protected void saveHostConfig() {
		Logging.debug(this, "saveHostConfig");
	}

	private void handleUserInPropertyClass(String superclass, String user) {
		Logging.info(this, "handleUserInPropertyClass " + user + " in class " + superclass);

		String newpropertyclass = superclass + "." + user;
		AbstractPersistenceController.PROPERTY_CLASSES_SERVER.computeIfAbsent(newpropertyclass, (String arg) -> {
			Logging.debug(this, "putUsersToPropertyclassesTreeMap found another user named " + user + " ["
					+ newpropertyclass + "]");
			return "";
		});
	}

	private void putUsersToPropertyclassesTreeMap() {
		Map<String, Object> configs = PersistenceControllerFactory.getPersistenceController().getConfig(
				PersistenceControllerFactory.getPersistenceController().getHostInfoCollections().getConfigServer());

		for (Map.Entry<String, Object> entry : configs.entrySet()) {
			String key = entry.getKey();

			if (key.startsWith(PROPERTY_CLASS_ROLE + ".")) {
				String user = key.split("\\.")[2];
				Logging.info(this,
						"putUsersToPropertyclassesTreeMap found role (user) " + user + " by config key " + key + "");
				handleUserInPropertyClass(PROPERTY_CLASS_ROLE, user);
			} else if (key.startsWith(PROPERTY_CLASS_USER + ".")) {
				String user = key.split("\\.")[1];
				Logging.info(this,
						"putUsersToPropertyclassesTreeMap found user " + user + " by config key " + key + "");
				if (!"{}".equals(user)) {
					handleUserInPropertyClass(PROPERTY_CLASS_USER, user);
				}
			}
		}
	}

	private void buildPanel() {
		PersistenceControllerFactory.getPersistenceController().checkConfiguration();
		putUsersToPropertyclassesTreeMap();

		editMapPanel = new EditMapPanelGroupedForHostConfigs(
				new de.uib.configed.gui.helper.PropertiesTableCellRenderer(), keylistExtendible, entryRemovable,
				reloadable, new DefaultEditMapPanel.Actor() {
					@Override
					protected void reloadData() {
						reloadHostConfig();
					}

					@Override
					protected void saveData() {
						saveHostConfig();
					}
				});

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createSequentialGroup()

				.addGroup(planeLayout.createParallelGroup()

						.addComponent(editMapPanel))

		);

		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup()

				.addComponent(editMapPanel, Globals.LINE_HEIGHT * 2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

		);
	}

	public void initEditing(String labeltext, Map configVisualMap,
			Map<String, de.uib.utilities.table.ListCellOptions> configOptions, Collection collectionConfigStored,
			AdditionalconfigurationUpdateCollection configurationUpdateCollection, boolean optionsEditable,
			NavigableMap<String, String> classesMap) {
		Logging.info(this, "initEditing "

				+ " optionsEditable " + optionsEditable);
		editMapPanel.setSubpanelClasses(classesMap);
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
