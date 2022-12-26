package de.uib.configed.gui.hostconfigs;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.datachanges.AdditionalconfigurationUpdateCollection;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.datapanel.AbstractEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelGrouped;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.ListCellOptions;

public class PanelHostConfig extends JPanel {
	// delegate
	protected EditMapPanelGrouped editMapPanel;
	protected JLabel label;

	protected boolean keylistExtendible = true;
	protected boolean entryRemovable = true;
	protected boolean reloadable = true;

	public final String propertyClassUser = UserConfig.KEY_USER_ROOT;
	public final String propertyClassRole = UserConfig.KEY_USER_ROLE_ROOT;

	public PanelHostConfig() {

		buildPanel();
	}

	// overwrite in subclasses
	protected void reloadHostConfig() {
		logging.info(this, " in PanelHostConfig: reloadHostConfig");

	}

	// overwrite in subclasses
	protected void saveHostConfig() {
		logging.debug(this, "saveHostConfig");
	}

	private void handleUserInPropertyClass(String superclass, String user) {
		logging.info(this, "handleUserInPropertyClass " + user + " in class " + superclass);

		String newpropertyclass = superclass + "." + user;

		if (!de.uib.opsidatamodel.PersistenceController.PROPERTYCLASSES_SERVER.containsKey(newpropertyclass)) {
			logging.debug(this, "putUsersToPropertyclassesTreeMap found another user named " + user + " ["
					+ newpropertyclass + "]");
			de.uib.opsidatamodel.PersistenceController.PROPERTYCLASSES_SERVER.put(newpropertyclass, "");
		}
	}

	private void putUsersToPropertyclassesTreeMap() {
		Map<String, Object> configs = PersistenceControllerFactory.getPersistenceController().getConfig(
				PersistenceControllerFactory.getPersistenceController().getHostInfoCollections().getConfigServer());

		for (Map.Entry<String, Object> entry : configs.entrySet()) {
			String key = entry.getKey();

			if (key.startsWith(propertyClassRole + ".")) {
				String user = key.split("\\.")[2];
				logging.info(this,
						"putUsersToPropertyclassesTreeMap found role (user) " + user + " by config key " + key + "");
				handleUserInPropertyClass(propertyClassRole, user);
			} else if (key.startsWith(propertyClassUser + ".")) {
				String user = key.split("\\.")[1];
				logging.info(this,
						"putUsersToPropertyclassesTreeMap found user " + user + " by config key " + key + "");
				if (!user.equals("{}"))
					handleUserInPropertyClass(propertyClassUser, user);
			}
		}
	}

	protected void buildPanel() {
		// boolean serverEditing = (ConfigedMain.getEditingTarget() ==

		label = new JLabel(configed.getResourceValue("MainFrame.jLabel_Config"));

		PersistenceControllerFactory.getPersistenceController().checkConfiguration();
		putUsersToPropertyclassesTreeMap();

		editMapPanel = new EditMapPanelGroupedForHostConfigs(
				new de.uib.configed.gui.helper.PropertiesTableCellRenderer(), keylistExtendible, entryRemovable,
				reloadable, new AbstractEditMapPanel.Actor() {
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
			TreeMap<String, String> classesMap) {
		label.setText(labeltext);

		logging.info(this, "initEditing "

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

	protected void setEditableMap(Map visualdata, Map<String, ListCellOptions> optionsMap) {
		editMapPanel.setEditableMap(visualdata, optionsMap);
	}

}
