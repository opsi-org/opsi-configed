/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.hostconfigs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.uib.configed.core.domain.datachanges.UpdateCollection;
import de.uib.configed.core.domain.permission.UserConfig;
import de.uib.configed.core.domain.permission.UserConfigProducing;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.datapanel.KeyValueTable;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.share.SplitPaneStateManager;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

// works on a map of pairs of type String - List
public class HostConfigsTree extends JPanel implements TreeSelectionListener {
	private static final int USER_START_INDEX = 1;

	private static final int INITIAL_DIVIDER_LOCATION = 350;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private List<String> theRoles;

	private boolean isServerConfig;
	private boolean isClientConfig;

	private JSplitPane splitPane;
	protected JTree tree;
	private JPanel emptyRightPane;
	private HostConfigTreeModel treemodel;
	private HostConfigNodeRenderer cellRenderer;

	private Map<String, String> givenClasses;
	private NavigableSet<String> keyclasses;
	protected Map<String, KeyValueTable> partialPanels;
	private Map<String, Map<String, Object>> virtualLines;

	private boolean includeAdditionalTooltipText;
	private Map<String, Object> originalMap;

	private KeyValueTableForHostConfigs.Actor actor;

	public HostConfigsTree(final KeyValueTableForHostConfigs.Actor actor, boolean isServerConfig,
			boolean isClientConfig) {
		super();

		this.actor = actor;
		this.isServerConfig = isServerConfig;
		this.isClientConfig = isClientConfig;

		setupLayout();

		setupPopups();
	}

	private void setupPopups() {
		PopupMenuTrait.createAndBindJPopupMenu(tree,
				Map.of(PopupMenuTrait.POPUP_RELOAD, this::reload, PopupMenuTrait.POPUP_DELETE, this::deleteUser,
						PopupMenuTrait.POPUP_ADD, this::addUser),
				event -> isUserPath(tree.getPathForLocation(event.getX(), event.getY())),
				PopupMenuTrait.PopupType.USER);

		PopupMenuTrait.createAndBindJPopupMenu(tree,
				Map.of(PopupMenuTrait.POPUP_RELOAD, this::reload, PopupMenuTrait.POPUP_ADD, this::addUser),
				event -> isUserRoot(tree.getPathForLocation(event.getX(), event.getY())),
				PopupMenuTrait.PopupType.USERS);

		PopupMenuTrait.createAndBindJPopupMenu(tree,
				Map.of(PopupMenuTrait.POPUP_RELOAD, this::reload, PopupMenuTrait.POPUP_DELETE, this::deleteUser,
						PopupMenuTrait.POPUP_ADD, this::addRole),
				event -> isRolePath(tree.getPathForLocation(event.getX(), event.getY()), false),
				PopupMenuTrait.PopupType.ROLE);

		PopupMenuTrait.createAndBindJPopupMenu(tree,
				Map.of(PopupMenuTrait.POPUP_RELOAD, this::reload, PopupMenuTrait.POPUP_ADD, this::addRole),
				event -> isRolePath(tree.getPathForLocation(event.getX(), event.getY()), true),
				PopupMenuTrait.PopupType.ROLES);
	}

	public void setSubpanelClasses(Map<String, String> classesMap) {
		cellRenderer.setTooltips(classesMap);
		givenClasses = classesMap;
	}

	protected void removeSubpanelClass(String key) {
		Logging.info(this, "remove ", key, " from ", givenClasses);
		givenClasses.remove(key);
	}

	private void setupLayout() {

		tree = new JTree();
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		ToolTipManager.sharedInstance().registerComponent(tree);

		cellRenderer = new HostConfigNodeRenderer();
		tree.setCellRenderer(cellRenderer);
		expandAll();

		tree.addTreeSelectionListener(this);

		JScrollPane jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		emptyRightPane = new JPanel();

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPaneTree, emptyRightPane);
		SplitPaneStateManager.registerSplitPane(splitPane, getSplitPaneKey(), INITIAL_DIVIDER_LOCATION);

		this.setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + " 0 0 0, fill", "[]", "[]0"));
		this.add(splitPane, "grow");
	}

	private String getSplitPaneKey() {
		if (isServerConfig) {
			return SplitPaneStateManager.SERVER_HOST_PARAMETERS_SPLIT;
		}
		if (isClientConfig) {
			return SplitPaneStateManager.CLIENT_HOST_PARAMETERS_SPLIT;
		} else {
			return SplitPaneStateManager.DEPOT_HOST_PARAMETERS_SPLIT;
		}
	}

	/**
	 * setting all data for displaying and editing <br />
	 *
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ConfigOption> optionsMap) {
		Logging.debug(this, " setEditableMap, visualdata keys ", visualdata);
		if (visualdata != null) {
			treemodel = new HostConfigTreeModel(givenClasses.keySet());
			tree.setModel(treemodel);
			expandAll();

			keyclasses = treemodel.getGeneratedKeys();

			generateParts();

			classify(visualdata, keyclasses);

			for (String key : keyclasses) {
				partialPanels.get(key).setEditableMap(virtualLines.get(key), optionsMap);
				partialPanels.get(key).registerDataChangedKeeper(ChangedDataManager.getHostConfigsDataChangedKeeper());
			}
		}
	}

	private void expandAll() {
		for (int row = 0; row < tree.getRowCount(); row++) {
			tree.expandRow(row);
		}
	}

	// apply method of superclass for all partial maps
	public void updateData(UpdateCollection updateCollection, Collection<Map<String, Object>> data) {
		for (String key : keyclasses) {
			partialPanels.get(key).setStoreData(data);
			partialPanels.get(key).setUpdateCollection(updateCollection);
		}
	}

	public void setLabel(String s) {
		if (treemodel == null) {
			return;
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
			// we start at 1 since we eliminate the root node
			List<String> pathForKey = Arrays.stream(selectedPath.getPath()).map(Object::toString).skip(1).toList();

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
			KeyValueTable editMapPanel = new KeyValueTableForHostConfigs(actor, tree, isServerConfig,
					includeAdditionalTooltipText);

			((KeyValueTableForHostConfigs) editMapPanel).setActor(actor);
			editMapPanel.setOriginalMap(originalMap);

			partialPanels.put(key, editMapPanel);
		}

		List<String> theUsers = new ArrayList<>();
		theRoles = new ArrayList<>();

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

		for (Entry<String, KeyValueTable> entry : partialPanels.entrySet()) {
			entry.getValue().setIsEditable(key -> isEditable(key, entry));
		}
	}

	// Modification info and some userroles cannot be edited
	private boolean isEditable(String key, Entry<String, KeyValueTable> partialPanelEntry) {
		Logging.info(this, "entry ", partialPanelEntry, " key ", key);

		if (isServerConfig && !persistenceController.getDataServices().userRoles.hasServerFullPermissionPD()) {
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
			List<Object> values = PersistenceControllerFactory.getPersistenceController().getDataServices().config
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
		if (path == null) {
			return false;
		}

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
		return path != null && path.getPathCount() == 2
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER);
	}

	private static boolean isUserPath(TreePath path) {
		return path != null && path.getPathCount() == 3
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER)
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
		JLabel userLabel = SwingUtils.createBoldLabel("FramingNewUser.textfieldLabel");

		JTextField userField = new JTextField();

		JLabel userRolesLabel = SwingUtils.createBoldLabel("FramingNewUser.listLabel");
		userRolesLabel.setToolTipText(Configed.getResourceValue("FramingNewUser.listLabel.ToolTip"));

		JList<String> userRolesList = new JList<>(theRoles.toArray(new String[0]));

		// With this call all the elements will have a fixed height. Also the empty entry
		userRolesList.setFixedCellHeight((Integer) UIManager.get("Table.rowHeight"));

		JScrollPane userRolesScrollPane = new JScrollPane(userRolesList);

		if (userRolesList.getModel().getSize() > 0) {
			userRolesList.setSelectedIndex(0);
		}
		userRolesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel panel = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[]", "[]0"));

		panel.add(userLabel);
		panel.add(userField, "growx");

		panel.add(userRolesLabel, "gapy " + Globals.GAP_SIZE);
		panel.add(userRolesScrollPane, "grow");

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
		new UserConfigProducing(false, persistenceController.getDataServices().hostInfoCollections.getConfigServer(),
				persistenceController.getDataServices().hostInfoCollections.getDepotNamesList(),
				persistenceController.getDataServices().group.getHostGroupIds(),
				persistenceController.getDataServices().group.getProductGroupsPD().keySet(),
				persistenceController.getDataServices().config.getConfigDefaultValuesPD(),
				persistenceController.getDataServices().config.getConfigOptionsPD()).produce();
	}

	private void addRole() {
		JLabel roleLabel = SwingUtils.createBoldLabel("FramingNewRole.textfieldLabel");

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

			partialPanels.get(key).removeProperties();

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
		PersistenceControllerFactory.getPersistenceController().getDataServices().config.addRoleConfig(name);
	}

	private void setUserConfig(String name, String rolename) {
		Logging.info(this, "setUserConfig ", name, ",", rolename);
		PersistenceControllerFactory.getPersistenceController().getDataServices().config.addUserConfig(name, rolename);
	}

	public void setOriginalMap(Map<String, Object> originalMap) {
		this.originalMap = originalMap;
	}

	public void includeAdditionalTooltipText(boolean include) {
		this.includeAdditionalTooltipText = include;
	}
}
