/* 
 *
 * (c) uib, www.uib.de, 2016, 2022
 *
 * author Rupert Röder
 */

package de.uib.configed.gui.hostconfigs;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FDialogTextfieldWithListSelection;
import de.uib.configed.gui.FramingTextfieldWithListselection;
import de.uib.opsicommand.Executioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.utilities.datapanel.AbstractEditMapPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PopupMenuTrait;

public class EditMapPanelGroupedForHostConfigs extends de.uib.utilities.datapanel.EditMapPanelGrouped

// works on a map of pairs of type String - List
{

	private static final int USER_START_INDEX = 1;

	protected PopupMenuTrait popupForUserpath;
	protected PopupMenuTrait popupForUserpathes;
	protected PopupMenuTrait popupForRolepath;
	protected PopupMenuTrait popupForRolepathes;

	protected JMenuItem popupItemDeleteEntry;

	protected LinkedList<String> theUsers;
	protected LinkedList<String> theRoles;

	public EditMapPanelGroupedForHostConfigs(TableCellRenderer tableCellRenderer, boolean keylistExtendible,
			boolean keylistEditable, boolean reloadable,
			final de.uib.utilities.datapanel.AbstractEditMapPanel.Actor actor) {
		super(tableCellRenderer, keylistExtendible, keylistEditable, reloadable, actor);

		popupForUserpathes = new PopupMenuTrait(
				new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_DELETE, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {

				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();

					break;

				case PopupMenuTrait.POPUP_SAVE:

					break;

				case PopupMenuTrait.POPUP_ADD:
					addUser();
					break;

				case PopupMenuTrait.POPUP_DELETE:

					deleteUser();
					break;

				}

			}
		};

		popupForUserpath = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {

				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();

					break;

				case PopupMenuTrait.POPUP_SAVE:

					break;

				case PopupMenuTrait.POPUP_ADD:
					addUser();
					break;

				}

			}
		};

		popupForRolepathes = new PopupMenuTrait(
				new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_DELETE, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {

				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();

					break;

				case PopupMenuTrait.POPUP_SAVE:

					break;

				case PopupMenuTrait.POPUP_ADD:
					addRole();
					break;

				case PopupMenuTrait.POPUP_DELETE:

					deleteUser();
					break;

				}

			}
		};

		popupForRolepath = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {

				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();

					break;

				case PopupMenuTrait.POPUP_SAVE:

					break;

				case PopupMenuTrait.POPUP_ADD:
					addRole();
					break;

				}

			}
		};

		// text for reload
		popupForUserpath.setText(PopupMenuTrait.POPUP_RELOAD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		popupForUserpathes.setText(PopupMenuTrait.POPUP_RELOAD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		popupForRolepath.setText(PopupMenuTrait.POPUP_RELOAD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		popupForRolepathes.setText(PopupMenuTrait.POPUP_RELOAD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		popupForUserpath.setText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser"));

		popupForUserpath.setToolTipText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser.ToolTip"));

		popupForUserpathes.setText(PopupMenuTrait.POPUP_DELETE,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForUser"));

		popupForUserpathes.setToolTipText(PopupMenuTrait.POPUP_DELETE,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForUser.ToolTip"));

		popupForRolepathes.setText(PopupMenuTrait.POPUP_DELETE,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForRole"));

		popupForRolepathes.setToolTipText(PopupMenuTrait.POPUP_DELETE,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForRole.ToolTip"));

		popupForUserpathes.setText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser"));

		popupForUserpathes.setToolTipText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser.ToolTip"));

		popupForRolepath.setText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole"));

		popupForRolepath.setToolTipText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole.ToolTip"));

		popupForRolepathes.setText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole"));

		popupForRolepathes.setToolTipText(PopupMenuTrait.POPUP_ADD,
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole.ToolTip"));

		MouseListener popupListenerForUserpathes = new utils.PopupMouseListener(popupForUserpathes) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

					if (selPath != null && isUserPath(selPath))
						super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForUserpathes);

		MouseListener popupListenerForUserpath = new utils.PopupMouseListener(popupForUserpath) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					Logging.info(this, " sel path " + selPath);
					if (selPath != null && isUserRoot(selPath))
						super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForUserpath);

		MouseListener popupListenerForRolepathes = new utils.PopupMouseListener(popupForRolepathes) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					Logging.info(this, " sel path " + selPath);
					if (selPath != null && isRolePath(selPath, false))
						super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForRolepathes);

		MouseListener popupListenerForRolepath = new utils.PopupMouseListener(popupForRolepath) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					Logging.info(this, " sel path " + selPath);
					if (selPath != null && isRolePath(selPath, true))
						super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForRolepath);

	}

	@Override
	protected void generateParts() {
		super.generateParts();

		theUsers = new LinkedList<>();
		theRoles = new LinkedList<>();

		theRoles.add(UserConfig.NONE_PROTOTYPE);

		for (String classkey : new TreeSet<>(partialPanels.keySet())) {
			Logging.info(this, "classkey " + classkey);

			String role = roleFromRolerootKey(classkey);

			if (role != null)
				theRoles.add(role);

			else {
				String user = userFromUserrootkey(classkey);
				if (user != null)
					theUsers.add(user);
			}

		}

		Logging.info(this, "theRoles found " + theRoles);
		Logging.info(this, "theUsers found " + theUsers);

		for (Entry<String, AbstractEditMapPanel> entry : partialPanels.entrySet()) {
			entry.getValue().setEditDenier(key -> {

				Logging.info(this, "entry " + entry + " key " + key);

				Boolean result = false;

				if (key.endsWith(UserConfig.MODIFICATION_INFO_KEY)) {
					result = true;

					JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("EditMapPanelGrouped.noManualEditing"), key,
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					String user = UserConfig.getUserFromKey(key);
					if (user != null)
					// we really are in a user branch
					{

						String rolekey = entry.getKey() + "." + UserConfig.HAS_ROLE_ATTRIBUT;

						if (!(key.equals(rolekey)))
						// rolekey may be edited
						{

							String theRole = null;

							List<Object> values = de.uib.opsidatamodel.PersistenceControllerFactory
									.getPersistenceController().getConfigDefaultValues().get(rolekey);

							if (values != null && !values.isEmpty())
								theRole = "" + values.get(0);

							boolean obeyToRole = (theRole != null && !(theRole.equals(UserConfig.NONE_PROTOTYPE)));

							if (obeyToRole) {
								result = true;
								JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), Configed.getResourceValue(
										"EditMapPanelGroupedForHostConfigs.noManualEditingWhereRoleDefined")

										, key, JOptionPane.INFORMATION_MESSAGE);

							}
						}

					}
				}

				Logging.info(this, "key denied ? " + key + " : " + result);
				return result;
			}

			);
		}

	}

	private String roleFromRolerootKey(String key) {
		String result = null;
		String[] splitted = key.split("\\.");

		if (splitted.length == 3 && splitted[0].equals(UserConfig.CONFIGKEY_STR_USER)
				&& splitted[1].equals(UserConfig.ROLE)) {
			result = splitted[2];
			if (result.charAt(0) == '{' && result.charAt(result.length() - 1) == '}')
				result = result.substring(1, result.length() - 1);
		}

		return result;
	}

	private String userFromUserrootkey(String key) {
		String result = null;
		String[] splitted = key.split("\\.");

		if (splitted.length == 2 && splitted[0].equals(UserConfig.CONFIGKEY_STR_USER)
				&& !(splitted[1].equals(UserConfig.ROLE))) {
			result = splitted[1];
			if (result.charAt(0) == '{' && result.charAt(result.length() - 1) == '}')
				result = result.substring(1, result.length() - 1);
		}

		return result;
	}

	private boolean isRolePath(TreePath path, boolean roleRoot) {
		int requiredCount = 4;

		if (roleRoot)
			requiredCount = 3;

		if (path.getPathCount() == requiredCount
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER)
				&& path.getPathComponent(2).toString().equals(UserConfig.ROLE)

		) {
			Logging.debug(this, "recognized role path " + path);
			return true;
		}

		return false;
	}

	private boolean isUserRoot(TreePath path) {
		return path != null && path.getPathCount() == 2
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER);
	}

	private boolean isUserPath(TreePath path) {
		return path != null && path.getPathCount() == 3
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER)
				&& !path.getPathComponent(2).toString().equals(UserConfig.ROLE);
	}

	protected void rebuildTree() {
		Logging.info(this, "rebuild tree, hopefully");
		buildUserConfig();

		PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

		setEditableMap((Map) persist.getConfigDefaultValues(), persist.getConfigOptions());
	}

	@Override
	protected void reload() {
		// partial reload
		PersistenceController persist = PersistenceControllerFactory.getPersistenceController();
		buildUserConfig();

		persist.hostConfigsRequestRefresh();
		persist.configOptionsRequestRefresh();
		super.reload();

	}

	protected void addUser() {

		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(null, // owner frame
				"add user", // title
				false, // modal

				new String[] { Configed.getResourceValue("FGeneralDialog.ok"),
						Configed.getResourceValue("FGeneralDialog.cancel") },

				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				2, 600, 600, true, // lazylayout, i.e, we have a chance to define components and use them for the
				// layout
				null // addPanel predefined
		)

		{

			@Override
			public void doAction1() {
				Logging.info(this, "doAction1");
				super.doAction1();
				Logging.info(this, "addUser action, result Text " + getResultText());
				Logging.info(this, "addUser action, result listelement " + getSelectedListelement());

				setUserConfig(getResultText(), getSelectedListelement());
			}

		};

		FramingTextfieldWithListselection defs = new FramingNewUser();
		defs.setListData(new ArrayList<>(theRoles));

		f.applyFraming(defs);

		JPanel centerPanel = f.initPanel();
		f.setCenterPaneInScrollpane(centerPanel);

		JPanel addPanel = new JPanel();
		addPanel.setBackground(Globals.EDIT_MAP_PANEL_GROUPED_FOR_HOST_CONFIGS_BACKGROUND_COLOR);

		f.setCenterPane(centerPanel);

		f.setupLayout();
		f.setSize(new Dimension(500, 400));
		f.setVisible(true);

		Logging.info(this, "addUser finished, result " + f.getResult());

		if (f.getResult() == 1) {
			Logging.info(this, "addUser ok");
		}

	}

	private void buildUserConfig() {
		PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

		de.uib.opsidatamodel.permission.UserConfigProducing up = new de.uib.opsidatamodel.permission.UserConfigProducing(
				false, // boolean notUsingDefaultUser, if true, we would supply the logged in user)

				persist.getHostInfoCollections().getConfigServer(), // String configserver,
				persist.getHostInfoCollections().getDepotNamesList(), // Collection<String> existingDepots,
				persist.getHostGroupIds(), // Collection<String> existingHostgroups,
				persist.getProductGroups().keySet(), // Collection<String> existingProductgroups,

				// data. on which changes are based
				persist.getConfigDefaultValues(), // Map<String, List<Object>> serverconfigValuesMap,
				persist.getConfigOptions()// Map<String, de.uib.utilities.table.ListCellOptions> configOptionsMap
		);

		List<Object> newData = up.produce();

		if (newData == null) {
			Logging.warning(this, "readyObjects for userparts " + null);
		} else {

			if (!newData.isEmpty()) {

				OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
						new Object[] { Executioner.jsonArray(newData) });

				persist.exec.doCall(omc);
			}

			Logging.info(this, "readyObjects for userparts " + newData.size());
		}
	}

	protected void addRole() {

		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(null, // owner frame
				"add role", // title
				false, // modal

				new String[] { Configed.getResourceValue("FGeneralDialog.ok"),
						Configed.getResourceValue("FGeneralDialog.cancel") },

				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				2, 600, 600, true, // lazylayout, i.e, we have a chance to define components and use them for the
				// layout
				null // addPanel predefined
		) {

			@Override
			public void doAction1() {
				Logging.info(this, "doAction1");
				super.doAction1();
				Logging.info(this, "addUser action, result Text " + getResultText());
				Logging.info(this, "addUser action, result listelement " + getSelectedListelement());

				setRoleConfig(getResultText(), getSelectedListelement());
			}

		};

		FramingTextfieldWithListselection defs = new FramingNewRole();
		defs.setListData(new ArrayList<>(theRoles));
		f.applyFraming(defs);

		JPanel centerPanel = f.initPanel();
		f.setCenterPaneInScrollpane(centerPanel);
		f.setListVisible(false);

		JPanel addPanel = new JPanel();
		addPanel.setBackground(Globals.EDIT_MAP_PANEL_GROUPED_FOR_HOST_CONFIGS_BACKGROUND_COLOR);

		f.setCenterPane(centerPanel);

		f.setupLayout();
		f.setSize(new Dimension(500, 400));
		f.setVisible(true);

	}

	protected void deleteUser() {

		javax.swing.tree.TreePath p = tree.getSelectionPath();

		if (p != null) {
			Logging.info(this, "deleteUser path " + p);

			int startComponentI = USER_START_INDEX;
			StringBuilder keyB = new StringBuilder(p.getPathComponent(startComponentI).toString());
			startComponentI++;
			for (int i = startComponentI; i < p.getPathCount(); i++) {
				keyB.append(".");
				keyB.append(p.getPathComponent(i).toString());
			}
			String key = keyB.toString();
			Logging.info(this, "deleteUser, selected user key " + key);

			List<String> propertyNames = partialPanels.get(key).getNames();
			Logging.info(this, "deleteUser, property names " + propertyNames);
			for (String name : propertyNames) {
				((de.uib.utilities.datapanel.EditMapPanelX) partialPanels.get(key)).removeProperty(name);
			}

			removeSubpanelClass(key);

			int row = tree.getRowForPath(p);

			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);

			tree.scrollRowToVisible(row);
		}
	}

	protected void setRoleConfig(String name, String rolename) {
		Logging.info(this, "setRoleConfig " + name + "," + rolename);
		PersistenceControllerFactory.getPersistenceController().addRoleConfig(name, rolename);
	}

	protected void setUserConfig(String name, String rolename) {
		Logging.info(this, "setUserConfig " + name + "," + rolename);
		PersistenceControllerFactory.getPersistenceController().addUserConfig(name, rolename);
	}

}
