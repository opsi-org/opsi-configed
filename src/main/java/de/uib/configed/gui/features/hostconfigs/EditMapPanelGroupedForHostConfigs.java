/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hostconfigs;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.datachanges.UpdateCollection;
import de.uib.configed.core.domain.permission.UserConfig;
import de.uib.configed.core.domain.permission.UserConfigProducing;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.datapanel.DefaultEditMapPanel;
import de.uib.configed.gui.share.datapanel.EditMapPanelX;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.tree.XTree;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.share.PopupMouseListener;
import de.uib.configed.share.logging.Logging;

// works on a map of pairs of type String - List
public class EditMapPanelGroupedForHostConfigs extends DefaultEditMapPanel implements TreeSelectionListener {
	private static final int USER_START_INDEX = 1;

	private static final int INITIAL_DIVIDER_LOCATION = 350;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private PopupMenuTrait popupForUserpath;
	private PopupMenuTrait popupForUserpathes;
	private PopupMenuTrait popupForRolepath;
	private PopupMenuTrait popupForRolepathes;

	private List<String> theRoles;

	private boolean isServerConfig;

	private JSplitPane splitPane;
	protected XTree tree;
	private JPanel emptyRightPane;
	private HostConfigTreeModel treemodel;
	private HostConfigNodeRenderer cellRenderer;

	private NavigableMap<String, String> givenClasses;
	private NavigableSet<String> keyclasses;
	protected Map<String, DefaultEditMapPanel> partialPanels;
	private NavigableMap<String, Map<String, Object>> virtualLines;

	private boolean includeAdditionalTooltipText;
	private Map<String, Object> originalMap;

	public EditMapPanelGroupedForHostConfigs(final DefaultEditMapPanel.Actor actor, boolean isServerConfig) {
		super(true);

		this.actor = actor;
		this.isServerConfig = isServerConfig;

		setupLayout();

		setupPopups();
		setupPopupTexts();
		setupPopupMouseListeners();
	}

	private void setupPopups() {
		setupPopupMenu();
		setupPopupForUserpathes();
		setupPopupForUserpath();
		setupPopupForRolepathes();
		setupPopupForRolepath();
	}

	private void setupPopupMenu() {
		popupMenu = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_SAVE, PopupMenuTrait.POPUP_RELOAD }) {
			@Override
			public void action(int p) {
				Logging.debug(this, "( EditMapPanelGrouped ) popup ", p);

				if (p == PopupMenuTrait.POPUP_RELOAD) {
					reload();
				} else if (p == PopupMenuTrait.POPUP_SAVE) {
					actor.saveData();
				} else {
					Logging.warning(this, "unexpected action ", p);
				}
			}
		};
	}

	private void setupPopupForUserpathes() {
		popupForUserpathes = new PopupMenuTrait(
				new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_DELETE, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {
				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();
					break;

				case PopupMenuTrait.POPUP_ADD:
					addUser();
					break;

				case PopupMenuTrait.POPUP_DELETE:
					deleteUser();
					break;

				default:
					Logging.warning(this, "no case for PopupMenuTrait found in popupForUserpathes");
					break;
				}
			}
		};
	}

	private void setupPopupForUserpath() {
		popupForUserpath = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {
				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();
					break;

				case PopupMenuTrait.POPUP_ADD:
					addUser();
					break;

				default:
					Logging.warning(this, "no case for PopupMenuTrait found in popupForUserpath");
					break;
				}
			}
		};
	}

	private void setupPopupForRolepathes() {
		popupForRolepathes = new PopupMenuTrait(
				new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_DELETE, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {
				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();
					break;

				case PopupMenuTrait.POPUP_ADD:
					addRole();
					break;

				case PopupMenuTrait.POPUP_DELETE:
					deleteUser();
					break;

				default:
					Logging.warning(this, "no case for PopupMenuTrait found in popupForRolepathes");
					break;
				}
			}
		};
	}

	private void setupPopupForRolepath() {
		popupForRolepath = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_ADD }) {
			@Override
			public void action(int p) {
				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();
					break;

				case PopupMenuTrait.POPUP_ADD:
					addRole();
					break;

				default:
					Logging.warning(this, "no case for PopupMenuTrait found in popupForRolepath");
					break;
				}
			}
		};
	}

	private void setupPopupTexts() {
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
	}

	private void setupPopupMouseListeners() {
		MouseListener popupListenerForUserpathes = new PopupMouseListener(popupForUserpathes) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

				if (selPath != null && isUserPath(selPath)) {
					super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForUserpathes);

		MouseListener popupListenerForUserpath = new PopupMouseListener(popupForUserpath) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				Logging.info(this, " sel path ", selPath);
				if (selPath != null && isUserRoot(selPath)) {
					super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForUserpath);

		MouseListener popupListenerForRolepathes = new PopupMouseListener(popupForRolepathes) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				Logging.info(this, " sel path ", selPath);
				if (selPath != null && isRolePath(selPath, false)) {
					super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForRolepathes);

		MouseListener popupListenerForRolepath = new PopupMouseListener(popupForRolepath) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				Logging.info(this, " sel path ", selPath);
				if (selPath != null && isRolePath(selPath, true)) {
					super.maybeShowPopup(e);
				}
			}
		};

		tree.addMouseListener(popupListenerForRolepath);
	}

	public void setSubpanelClasses(NavigableMap<String, String> classesMap) {
		cellRenderer.setTooltips(classesMap);
		givenClasses = classesMap;
	}

	protected void removeSubpanelClass(String key) {
		Logging.info(this, "remove ", key, " from ", givenClasses);
		givenClasses.remove(key);
	}

	private void setupLayout() {
		splitPane = new JSplitPane();

		tree = new XTree();

		ToolTipManager.sharedInstance().registerComponent(tree);

		cellRenderer = new HostConfigNodeRenderer();
		tree.setCellRenderer(cellRenderer);
		tree.expandAll();

		tree.addTreeSelectionListener(this);

		JScrollPane jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		emptyRightPane = new JPanel();

		splitPane.setLeftComponent(jScrollPaneTree);
		splitPane.setRightComponent(emptyRightPane);
		splitPane.setDividerLocation(INITIAL_DIVIDER_LOCATION);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(splitPane, GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(splitPane, 50,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	/**
	 * setting all data for displaying and editing <br />
	 *
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */
	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ConfigOption> optionsMap) {
		super.setEditableMap(visualdata, optionsMap);
		Logging.debug(this, " setEditableMap, visualdata keys ", visualdata);
		if (visualdata != null) {
			treemodel = new HostConfigTreeModel(givenClasses.keySet());
			tree.setModel(treemodel);
			tree.expandAll();

			keyclasses = treemodel.getGeneratedKeys();

			generateParts();

			classify(visualdata, keyclasses);

			for (String key : keyclasses) {
				partialPanels.get(key).setEditableMap(virtualLines.get(key), optionsMap);
				partialPanels.get(key).getMapTableModel().setKeepers(this.mapTableModel.getKeepers());
			}
		}
	}

	// apply method of superclass for all partial maps
	@Override
	public void updateData(UpdateCollection updateCollection, Collection<Map<String, Object>> data) {
		super.updateData(updateCollection, data);

		for (String key : keyclasses) {
			partialPanels.get(key).updateData(updateCollection, data);
		}
	}

	public void setLabel(String s) {
		if (treemodel == null) {
			return;
		}

		if ("".equals(s)) {
			s = Configed.getResourceValue("HostConfigTreeModel.noClientsSelected");
			tree.collapseRow(0);
		}

		treemodel.setRootLabel(s);
	}

	// TreeSelectionListener
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath selectedPath = tree.getSelectionPath();

		int divLoc = splitPane.getDividerLocation();

		if (selectedPath == null) {
			splitPane.setRightComponent(emptyRightPane);
			splitPane.setDividerLocation(divLoc);
			return;
		}

		boolean isRoot = selectedPath.getPathCount() == 1;

		if (isRoot) {
			splitPane.setRightComponent(emptyRightPane);
		} else {
			List<String> pathForKey = Arrays.stream(selectedPath.getPath()).map(Object::toString)
					.collect(Collectors.toList());

			// we start at 1 since we eliminate the root node
			pathForKey.remove(0);

			String key = String.join(".", pathForKey);

			if (partialPanels.get(key) == null) {
				splitPane.setRightComponent(emptyRightPane);
			} else {
				splitPane.setRightComponent(partialPanels.get(key));
			}
		}

		splitPane.setDividerLocation(divLoc);
	}

	private void generateParts() {
		partialPanels = new HashMap<>();

		for (String key : keyclasses) {
			EditMapPanelX editMapPanel = new EditMapPanelForHostConfigs(reloadable, tree, isServerConfig,
					includeAdditionalTooltipText);

			editMapPanel.setActor(actor);
			editMapPanel.setOriginalMap(originalMap);

			partialPanels.put(key, editMapPanel);
		}

		List<String> theUsers = new LinkedList<>();
		theRoles = new LinkedList<>();

		theRoles.add(UserConfig.NONE_PROTOTYPE);

		for (String classkey : new TreeSet<>(partialPanels.keySet())) {
			Logging.info(this, "classkey ", classkey);

			String role = roleFromRolerootKey(classkey);

			if (role != null) {
				theRoles.add(role);
			} else {
				String user = userFromUserrootkey(classkey);
				if (user != null) {
					theUsers.add(user);
				}
			}
		}

		Logging.info(this, "theRoles found ", theRoles);
		Logging.info(this, "theUsers found ", theUsers);

		for (Entry<String, DefaultEditMapPanel> entry : partialPanels.entrySet()) {
			entry.getValue().getMapTableModel().setIsEditable(key -> isEditable(key, entry));
		}
	}

	// Modification info and some userroles cannot be edited
	private boolean isEditable(String key, Entry<String, DefaultEditMapPanel> partialPanelEntry) {
		Logging.info(this, "entry ", partialPanelEntry, " key ", key);

		if (isServerConfig && !persistenceController.getUserRolesConfigDataService().hasServerFullPermissionPD()) {
			return false;
		}

		boolean result;

		if (key.endsWith(UserConfig.MODIFICATION_INFO_KEY)) {
			result = false;
		} else if (UserConfig.getUserFromKey(key) != null) {
			// we really are in a user branch
			result = isUserKeyEditable(key, partialPanelEntry.getKey());
		} else {
			result = true;
		}

		Logging.info(this, "key denied ? ", key, " : ", result);
		return result;
	}

	private static boolean isUserKeyEditable(String key, String partialPanelKey) {
		String rolekey = partialPanelKey + "." + UserConfig.HAS_ROLE_ATTRIBUT;

		// rolekey may be edited
		if (!(key.equals(rolekey))) {
			List<Object> values = PersistenceControllerFactory.getPersistenceController().getConfigDataService()
					.getConfigDefaultValuesPD().get(rolekey);

			boolean obeyToRole = values != null && !values.isEmpty()
					&& !(values.get(0).equals(UserConfig.NONE_PROTOTYPE));

			// key obeys role and therefore cannot be edited
			if (obeyToRole) {
				return false;
			}
		}

		return true;
	}

	private void classify(Map<String, Object> data, NavigableSet<String> classIds) {
		virtualLines = new TreeMap<>();

		for (String id : classIds.descendingSet()) {
			virtualLines.put(id, new TreeMap<>());
		}

		virtualLines.put("", new TreeMap<>());

		if (data == null) {
			return;
		}

		NavigableSet<String> classIdsDescending = classIds.descendingSet();

		for (String key : new TreeSet<>(data.keySet()).descendingSet()) {
			Logging.debug(this, "classify key ------- ", key);
			boolean foundClass = false;
			for (String idCollect : classIdsDescending) {
				if (key.startsWith(idCollect)) {
					virtualLines.get(idCollect).put(key, data.get(key));
					Logging.debug(this, "classify idCollect -------- ", idCollect);
					foundClass = true;
					break;
				}
			}

			if (!foundClass) {
				virtualLines.get("").put(key, data.get(key));
			}
		}
	}

	private static String roleFromRolerootKey(String key) {
		String result = null;
		String[] splitted = key.split("\\.");

		if (splitted.length == 3 && splitted[0].equals(UserConfig.CONFIGKEY_STR_USER)
				&& splitted[1].equals(UserConfig.ROLE)) {
			result = splitted[2];
			if (result.charAt(0) == '{' && result.charAt(result.length() - 1) == '}') {
				result = result.substring(1, result.length() - 1);
			}
		}

		return result;
	}

	private static String userFromUserrootkey(String key) {
		String result = null;
		String[] splitted = key.split("\\.");

		if (splitted.length == 2 && splitted[0].equals(UserConfig.CONFIGKEY_STR_USER)
				&& !(splitted[1].equals(UserConfig.ROLE))) {
			result = splitted[1];
			if (result.charAt(0) == '{' && result.charAt(result.length() - 1) == '}') {
				result = result.substring(1, result.length() - 1);
			}
		}

		return result;
	}

	private boolean isRolePath(TreePath path, boolean roleRoot) {
		int requiredCount = 4;

		if (roleRoot) {
			requiredCount = 3;
		}

		if (path.getPathCount() == requiredCount
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER)
				&& path.getPathComponent(2).toString().equals(UserConfig.ROLE)) {
			Logging.debug(this, "recognized role path ", path);
			return true;
		}

		return false;
	}

	private static boolean isUserRoot(TreePath path) {
		return path.getPathCount() == 2 && path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER);
	}

	private static boolean isUserPath(TreePath path) {
		return path.getPathCount() == 3 && path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER)
				&& !path.getPathComponent(2).toString().equals(UserConfig.ROLE);
	}

	protected void reload() {
		ConfigedMain.getMainFrame().activateLoadingCursor();
		// partial reload
		buildUserConfig();

		Logging.info(this, "reload");
		TreePath p = tree.getSelectionPath();
		int row = tree.getRowForPath(p);

		actor.reloadData();
		Logging.debug(this, "reloaded, return to ", p);
		if (p != null) {
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);
			tree.scrollRowToVisible(row);
		}

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private void addUser() {
		JLabel userLabel = new JLabel(Configed.getResourceValue("FramingNewUser.textfieldLabel"));
		userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));

		JTextField userField = new JTextField();

		JLabel userRolesLabel = new JLabel(Configed.getResourceValue("FramingNewUser.listLabel"));
		userRolesLabel.setToolTipText(Configed.getResourceValue("FramingNewUser.listLabel.ToolTip"));
		userRolesLabel.setFont(userRolesLabel.getFont().deriveFont(Font.BOLD));

		JList<String> userRolesList = new JList<>(theRoles.toArray(new String[0]));

		// With this call all the elements will have a fixed height. Also the empty entry
		userRolesList.setFixedCellHeight((Integer) UIManager.get("Table.rowHeight"));

		JScrollPane userRolesScrollPane = new JScrollPane(userRolesList);

		if (userRolesList.getModel().getSize() > 0) {
			userRolesList.setSelectedIndex(0);
		}
		userRolesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(userLabel).addComponent(userField)
				.addGap(Globals.GAP_SIZE).addComponent(userRolesLabel).addComponent(userRolesScrollPane));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(userLabel)
				.addComponent(userField).addComponent(userRolesLabel).addComponent(userRolesScrollPane));

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), panel,
				Configed.getResourceValue("FramingNewUser.title"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (answer == JOptionPane.OK_OPTION) {
			Logging.info(this, "addUser action, result Text ", userField.getText());
			Logging.info(this, "addUser action, result listelement ", userRolesList.getSelectedValue());
			setUserConfig(userField.getText(), userRolesList.getSelectedValue());
		}
	}

	private void buildUserConfig() {
		new UserConfigProducing(false, persistenceController.getHostInfoCollections().getConfigServer(),
				persistenceController.getHostInfoCollections().getDepotNamesList(),
				persistenceController.getGroupDataService().getHostGroupIds(),
				persistenceController.getGroupDataService().getProductGroupsPD().keySet(),
				persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
				persistenceController.getConfigDataService().getConfigOptionsPD()).produce();
	}

	private void addRole() {
		JLabel roleLabel = new JLabel(Configed.getResourceValue("FramingNewRole.textfieldLabel"));
		roleLabel.setFont(roleLabel.getFont().deriveFont(Font.BOLD));

		String newUserRole = JOptionPane.showInputDialog(ConfigedMain.getMainFrame(), roleLabel,
				Configed.getResourceValue("FramingNewRole.title"), JOptionPane.PLAIN_MESSAGE);

		if (newUserRole != null) {
			setRoleConfig(newUserRole);
		}
	}

	private void deleteUser() {
		TreePath p = tree.getSelectionPath();

		if (p != null) {
			Logging.info(this, "deleteUser path ", p);

			int startComponentI = USER_START_INDEX;
			StringBuilder keyB = new StringBuilder(p.getPathComponent(startComponentI).toString());
			startComponentI++;
			for (int i = startComponentI; i < p.getPathCount(); i++) {
				keyB.append(".");
				keyB.append(p.getPathComponent(i).toString());
			}
			String key = keyB.toString();
			Logging.info(this, "deleteUser, selected user key ", key);

			List<String> propertyNames = partialPanels.get(key).getMapTableModel().getKeys();
			Logging.info(this, "deleteUser, property names ", propertyNames);
			for (String name : propertyNames) {
				((EditMapPanelX) partialPanels.get(key)).removeProperty(name);
			}

			removeSubpanelClass(key);

			int row = tree.getRowForPath(p);

			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);

			tree.scrollRowToVisible(row);
		}
	}

	public boolean isSelected(String obj) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		if (node == null) {
			return false;
		}
		String selectedNode = ((String) node.getUserObject()).replace("{", "").replace("}", "");
		return selectedNode.equals(obj);
	}

	private void setRoleConfig(String name) {
		Logging.info(this, "setRoleConfig ", name);
		PersistenceControllerFactory.getPersistenceController().getConfigDataService().addRoleConfig(name);
	}

	private void setUserConfig(String name, String rolename) {
		Logging.info(this, "setUserConfig ", name, ",", rolename);
		PersistenceControllerFactory.getPersistenceController().getConfigDataService().addUserConfig(name, rolename);
	}

	public void setOriginalMap(Map<String, Object> originalMap) {
		this.originalMap = originalMap;
	}

	public void includeAdditionalTooltipText(boolean include) {
		this.includeAdditionalTooltipText = include;
	}
}
