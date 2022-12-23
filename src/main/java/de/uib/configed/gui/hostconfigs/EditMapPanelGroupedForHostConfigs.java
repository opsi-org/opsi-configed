/* 
 *
 * (c) uib, www.uib.de, 2016, 2022
 *
 * author Rupert Röder
 */

package de.uib.configed.gui.hostconfigs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FDialogTextfieldWithListSelection;
import de.uib.configed.gui.FramingTextfieldWithListselection;
import de.uib.opsicommand.OpsiMethodCall;
//import de.uib.configed.guidata.ListMerger;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.ListCellOptions;

public class EditMapPanelGroupedForHostConfigs extends de.uib.utilities.datapanel.EditMapPanelGrouped

// works on a map of pairs of type String - List
{

	// protected JPopupMenu popup0;
	protected JPopupMenu popupForUserpath;
	protected JPopupMenu popupForUserpathes;
	protected JPopupMenu popupForRolepath;
	protected JPopupMenu popupForRolepathes;

	protected JMenuItem popupItemDeleteEntry;

	protected LinkedList<String> theUsers;
	protected LinkedList<String> theRoles;

	// String username;
	// String rolename;

	public EditMapPanelGroupedForHostConfigs(TableCellRenderer tableCellRenderer, boolean keylistExtendible,
			boolean keylistEditable, boolean reloadable,
			final de.uib.utilities.datapanel.AbstractEditMapPanel.Actor actor) {
		super(tableCellRenderer, keylistExtendible, keylistEditable, reloadable, actor);

		/*
		 * popupItemDeleteEntry = new JMenuItem(
		 * configed.getResourceValue("EditMapPanel.PopupMenu.RemoveEntry"));
		 * 
		 * popupItemDeleteEntry.addActionListener(
		 * new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e){
		 * logging.info(this, "deleteUser");
		 * deleteUser();
		 * }
		 * }
		 * );
		 */

		/*
		 * popup0 = new PopupMenuTrait(new Integer[]{
		 * //PopupMenuTrait.POPUP_SAVE,
		 * PopupMenuTrait.POPUP_RELOAD,
		 * PopupMenuTrait.POPUP_ADD
		 * })
		 * {
		 * public void action(int p)
		 * {
		 * logging.info(this, "action popup  " + p);
		 * 
		 * switch(p)
		 * {
		 * case PopupMenuTrait.POPUP_RELOAD:
		 * reload();
		 * 
		 * break;
		 * 
		 * case PopupMenuTrait.POPUP_SAVE:
		 * 
		 * break;
		 * }
		 * 
		 * }
		 * }
		 * ;
		 * 
		 */

		popupForUserpathes = new PopupMenuTrait(new Integer[] {
				// PopupMenuTrait.POPUP_SAVE,
				PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_DELETE, PopupMenuTrait.POPUP_ADD }) {
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

		popupForUserpath = new PopupMenuTrait(new Integer[] {
				// PopupMenuTrait.POPUP_SAVE,
				PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_ADD }) {
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

		popupForRolepathes = new PopupMenuTrait(new Integer[] {
				// PopupMenuTrait.POPUP_SAVE,
				PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_DELETE, PopupMenuTrait.POPUP_ADD }) {
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

		popupForRolepath = new PopupMenuTrait(new Integer[] {
				// PopupMenuTrait.POPUP_SAVE,
				PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_ADD }) {
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
		((PopupMenuTrait) popupForUserpath).setText(PopupMenuTrait.POPUP_RELOAD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		((PopupMenuTrait) popupForUserpathes).setText(PopupMenuTrait.POPUP_RELOAD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		((PopupMenuTrait) popupForRolepath).setText(PopupMenuTrait.POPUP_RELOAD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		((PopupMenuTrait) popupForRolepathes).setText(PopupMenuTrait.POPUP_RELOAD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.reconstructUsers"));

		((PopupMenuTrait) popupForUserpath).setText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser"));

		((PopupMenuTrait) popupForUserpath).setToolTipText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser.ToolTip"));

		((PopupMenuTrait) popupForUserpathes).setText(PopupMenuTrait.POPUP_DELETE,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForUser"));

		((PopupMenuTrait) popupForUserpathes).setToolTipText(PopupMenuTrait.POPUP_DELETE,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForUser.ToolTip"));

		((PopupMenuTrait) popupForRolepathes).setText(PopupMenuTrait.POPUP_DELETE,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForRole"));

		((PopupMenuTrait) popupForRolepathes).setToolTipText(PopupMenuTrait.POPUP_DELETE,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.removeValuesForRole.ToolTip"));

		((PopupMenuTrait) popupForUserpathes).setText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser"));

		((PopupMenuTrait) popupForUserpathes).setToolTipText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addUser.ToolTip"));

		((PopupMenuTrait) popupForRolepath).setText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole"));

		((PopupMenuTrait) popupForRolepath).setToolTipText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole.ToolTip"));

		((PopupMenuTrait) popupForRolepathes).setText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole"));

		((PopupMenuTrait) popupForRolepathes).setToolTipText(PopupMenuTrait.POPUP_ADD,
				configed.getResourceValue("EditMapPanelGroupedForHostConfigs.addRole.ToolTip"));

		/*
		 * MouseListener popupListener0 = new utils.PopupMouseListener(popup0){
		 * 
		 * @Override
		 * protected void maybeShowPopup(MouseEvent e) {
		 * if (e.isPopupTrigger()) {
		 * //int selRow = tree.getRowForLocation(e.getX(), e.getY());
		 * TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		 * 
		 * //if(selRow % 2 == 0) //test
		 * if ( selPath != null && !isUserPath( selPath ) && !isRolePath( selPath,
		 * false) && !isRolePath( selPath, true) )
		 * super.maybeShowPopup(e);
		 * }
		 * }
		 * };
		 * tree.addMouseListener(popupListener0);
		 */

		

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
					logging.info(this, " sel path " + selPath);
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
					logging.info(this, " sel path " + selPath);
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
					logging.info(this, " sel path " + selPath);
					if (selPath != null && isRolePath(selPath, true))
						super.maybeShowPopup(e);
				}
			}
		};
		tree.addMouseListener(popupListenerForRolepath);

	}

	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap) {
		super.setEditableMap(visualdata, optionsMap);
	}

	@Override
	protected void generateParts() {
		super.generateParts();

		theUsers = new LinkedList<>();
		theRoles = new LinkedList<>();

		theRoles.add(UserConfig.NONE_PROTOTYPE);

		for (String classkey : new TreeSet<>(partialPanels.keySet())) {
			logging.info(this, "classkey " + classkey);

			String role = roleFromRolerootKey(classkey);

			if (role != null)
				theRoles.add(role);

			else {
				String user = userFromUserrootkey(classkey);
				if (user != null)
					theUsers.add(user);
			}

		}

		logging.info(this, "theRoles found " + theRoles);
		logging.info(this, "theUsers found " + theUsers);

		for (String classkey : partialPanels.keySet()) {
			(partialPanels.get(classkey)).setEditDenier(key -> {

				logging.info(this, "classkey " + classkey + " key " + key);

				Boolean result = false;

				if (key.endsWith(UserConfig.MODIFICATION_INFO_KEY)) {
					result = true;

					JOptionPane.showMessageDialog(Globals.mainFrame,
							configed.getResourceValue("EditMapPanelGrouped.noManualEditing"), key,
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					String user = UserConfig.getUserFromKey(key);
					if (user != null)
					// we really are in a user branch
					{

						String rolekey = classkey + "." + UserConfig.HAS_ROLE_ATTRIBUT;

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
								JOptionPane.showMessageDialog(Globals.mainFrame, configed.getResourceValue(
										"EditMapPanelGroupedForHostConfigs.noManualEditingWhereRoleDefined")
								// "editing only possible if the user is not assigned to a role"
										, key, JOptionPane.INFORMATION_MESSAGE);

							}
						}

					}
				}

				logging.info(this, "key denied ? " + key + " : " + result);
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
			logging.debug(this, "recognized role path " + path);
			return true;
		}

		return false;
	}

	private boolean isUserRoot(TreePath path) {
		if (path != null && path.getPathCount() == 2
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER))
			return true;

		return false;
	}

	private boolean isUserPath(TreePath path) {
		if (path != null && path.getPathCount() == 3
				&& path.getPathComponent(1).toString().equals(UserConfig.CONFIGKEY_STR_USER)
				&& !path.getPathComponent(2).toString().equals(UserConfig.ROLE))
			return true;

		return false;
	}

	private int getUserStartIndex() {
		return 1;
	}

	protected void rebuildTree() {
		logging.info(this, "rebuild tree, hopefully");
		buildUserConfig();

		PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

		setEditableMap(

				(Map) persist.getConfigDefaultValues(),

				// additionalConfigs.get(0), //old values mapTableModel.getData(),
				persist.getConfigOptions()// old values mapTableModel.getOptionsMap()

		);

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
		addUser("");
	}

	protected void addUser(String rolename) {
		// de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController().addRegisterUserEntry()

		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(null, // owner frame
				"add user", // title
				false, // modal

				new String[] { configed.getResourceValue("FGeneralDialog.ok"),
						configed.getResourceValue("FGeneralDialog.cancel") },

				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				2, // lastButtonNo,with "1" we get only the first button
				600, 600, true, // lazylayout, i.e, we have a chance to define components and use them for the
				// layout
				null // addPanel predefined
		)

		{

			@Override
			public void doAction1() {
				logging.info(this, "doAction1");
				super.doAction1();
				logging.info(this, "addUser action, result Text " + getResultText());
				logging.info(this, "addUser action, result listelement " + getSelectedListelement());

				setUserConfig(getResultText(), getSelectedListelement());
			}

		};

		FramingTextfieldWithListselection defs = new FramingNewUser();
		defs.setListData(new Vector<>(theRoles));

		f.applyFraming(defs);

		JPanel centerPanel = f.initPanel();
		f.setCenterPaneInScrollpane(centerPanel);

		JPanel addPanel = new JPanel();
		addPanel.setBackground(Color.YELLOW);
		

		
		f.setCenterPane(centerPanel);

		

		f.setupLayout();
		f.setSize(new Dimension(500, 400));
		f.setVisible(true);

		logging.info(this, "addUser finished, result " + f.getResult());
		// System.exit(0);

		if (f.getResult() == 1) {
			logging.info(this, "addUser ok");
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

		ArrayList<Object> newData = up.produce();

		if (newData == null) {
			logging.warning(this, "readyObjects for userparts " + null);
		} else {

			if (!newData.isEmpty()) {

				OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
						new Object[] { persist.exec.jsonArray(newData) });

				persist.exec.doCall(omc);
			}

			logging.info(this, "readyObjects for userparts " + newData.size());
		}
	}

	protected void addRole() {

		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(null, // owner frame
				"add role", // title
				false, // modal

				new String[] { configed.getResourceValue("FGeneralDialog.ok"),
						configed.getResourceValue("FGeneralDialog.cancel") },

				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				2, // lastButtonNo,with "1" we get only the first button
				600, 600, true, // lazylayout, i.e, we have a chance to define components and use them for the
				// layout
				null // addPanel predefined
		) {

			@Override
			public void doAction1() {
				logging.info(this, "doAction1");
				super.doAction1();
				logging.info(this, "addUser action, result Text " + getResultText());
				logging.info(this, "addUser action, result listelement " + getSelectedListelement());

				setRoleConfig(getResultText(), getSelectedListelement());
			}

		};

		FramingTextfieldWithListselection defs = new FramingNewRole();
		defs.setListData(new Vector<>(theRoles));
		f.applyFraming(defs);

		JPanel centerPanel = f.initPanel();
		f.setCenterPaneInScrollpane(centerPanel);
		f.setListVisible(false);

		JPanel addPanel = new JPanel();
		addPanel.setBackground(Color.YELLOW);
		

		
		f.setCenterPane(centerPanel);

		

		f.setupLayout();
		f.setSize(new Dimension(500, 400));
		f.setVisible(true);

	}

	protected void deleteUser() {
		
		
		javax.swing.tree.TreePath p = tree.getSelectionPath();

		
		if (p != null) {
			logging.info(this, "deleteUser path " + p);

			int startComponentI = getUserStartIndex();
			StringBuffer keyB = new StringBuffer(p.getPathComponent(startComponentI).toString());
			startComponentI++;
			for (int i = startComponentI; i < p.getPathCount(); i++) {
				keyB.append(".");
				keyB.append(p.getPathComponent(i).toString());
			}
			String key = keyB.toString();
			logging.info(this, "deleteUser, selected user key " + key);
			
			
			Vector<String> propertyNames = partialPanels.get(key).getNames();
			logging.info(this, "deleteUser, property names " + propertyNames);
			for (String name : propertyNames) {
				((de.uib.utilities.datapanel.EditMapPanelX) partialPanels.get(key)).removeProperty(name);
			}

			removeSubpanelClass(key);

			int row = tree.getRowForPath(p);

			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);
			// tree.removeSelectionInterval(row, row);
			tree.scrollRowToVisible(row);
		}
	}

	protected void setRoleConfig(String name, String rolename) {
		logging.info(this, "setRoleConfig " + name + "," + rolename);
		PersistenceControllerFactory.getPersistenceController().addRoleConfig(name, rolename);
	}

	protected void setUserConfig(String name, String rolename) {
		logging.info(this, "setUserConfig " + name + "," + rolename);
		PersistenceControllerFactory.getPersistenceController().addUserConfig(name, rolename);
	}

}
