/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
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

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FDialogTextfieldWithListSelection;
import de.uib.configed.gui.FramingTextfieldWithListselection;
import de.uib.configed.guidata.ListMerger;
import de.uib.configed.type.ConfigOption;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.datapanel.DefaultEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.ListCellOptions;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
import de.uib.utilities.tree.SimpleTreePath;
import de.uib.utilities.tree.XTree;
import utils.PopupMouseListener;
import utils.Utils;

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

	private LinkedList<String> theRoles;

	private JSplitPane splitPane;
	protected XTree tree;
	private JPanel emptyRightPane;
	private HostConfigTreeModel treemodel;

	private NavigableMap<String, String> givenClasses;
	private NavigableSet<String> keyclasses;
	protected Map<String, DefaultEditMapPanel> partialPanels;
	private NavigableMap<String, Map<String, Object>> virtualLines;

	private boolean includeAdditionalTooltipText;
	private Map<String, Object> originalMap;

	public EditMapPanelGroupedForHostConfigs(TableCellRenderer tableCellRenderer, boolean keylistExtendible,
			boolean keylistEditable, boolean reloadable, final DefaultEditMapPanel.Actor actor) {
		super(tableCellRenderer, keylistExtendible, keylistEditable, reloadable);

		buildPanel();
		this.actor = actor;

		popupmenuAtRow = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_SAVE, PopupMenuTrait.POPUP_RELOAD }) {
			@Override
			public void action(int p) {
				Logging.debug(this, "( EditMapPanelGrouped ) popup " + p);

				if (p == PopupMenuTrait.POPUP_RELOAD) {
					reload();
				} else if (p == PopupMenuTrait.POPUP_SAVE) {
					actor.saveData();
				} else {
					Logging.warning(this, "unexpected action " + p);
				}
			}
		};

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

		MouseListener popupListenerForUserpathes = new PopupMouseListener(popupForUserpathes) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

					if (selPath != null && isUserPath(selPath)) {
						super.maybeShowPopup(e);
					}
				}
			}
		};
		tree.addMouseListener(popupListenerForUserpathes);

		MouseListener popupListenerForUserpath = new PopupMouseListener(popupForUserpath) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					Logging.info(this, " sel path " + selPath);
					if (selPath != null && isUserRoot(selPath)) {
						super.maybeShowPopup(e);
					}
				}
			}
		};
		tree.addMouseListener(popupListenerForUserpath);

		MouseListener popupListenerForRolepathes = new PopupMouseListener(popupForRolepathes) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					Logging.info(this, " sel path " + selPath);
					if (selPath != null && isRolePath(selPath, false)) {
						super.maybeShowPopup(e);
					}
				}
			}
		};
		tree.addMouseListener(popupListenerForRolepathes);

		MouseListener popupListenerForRolepath = new PopupMouseListener(popupForRolepath) {
			@Override
			protected void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					Logging.info(this, " sel path " + selPath);
					if (selPath != null && isRolePath(selPath, true)) {
						super.maybeShowPopup(e);
					}
				}
			}
		};

		tree.addMouseListener(popupListenerForRolepath);
	}

	public void setSubpanelClasses(NavigableMap<String, String> classesMap) {
		givenClasses = classesMap;
	}

	protected void removeSubpanelClass(String key) {
		Logging.info(this, "remove " + key + " from " + givenClasses);
		givenClasses.remove(key);
	}

	private void createPDF() {
		String client = tree.getSelectionPath().getPathComponent(0).toString().trim();

		// TODO get Depotname
		Logging.info(this, "create report");
		HashMap<String, String> metaData = new HashMap<>();
		metaData.put("header", Configed.getResourceValue("EditMapPanelGrouped.createPDF.title"));
		metaData.put("title", "Client: " + client);
		metaData.put("subject", "report of table");
		metaData.put("keywords", Configed.getResourceValue("EditMapPanelGrouped.createPDF.title") + " " + client);

		ExporterToPDF pdfExportTable = new ExporterToPDF(createJTableForPDF());
		pdfExportTable.setClient(client);
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4();
		pdfExportTable.execute(null, false);

	}

	private JTable createJTableForPDF() {
		DefaultTableModel tableModel = new DefaultTableModel();
		JTable jTable = new JTable(tableModel);
		List<String> values;

		tableModel.addColumn(Configed.getResourceValue("EditMapPanelGrouped.createJTableForPDF.property_name"));
		tableModel.addColumn(Configed.getResourceValue("EditMapPanelGrouped.createJTableForPDF.property_value"));

		List<String> keys = mapTableModel.getKeys();
		Logging.info(this, "createJTableForPDF keys " + keys);
		for (String key : keys) {
			String property = "";

			List<?> listelem = ListMerger.getMergedList((List<?>) mapTableModel.getData().get(key));
			if (!listelem.isEmpty()) {
				property = listelem.get(0).toString();
			}

			values = new ArrayList<>();

			// TODO search another possibility to exclude?
			if (!key.contains("saved_search")) {
				values.add(key);
				values.add(property);
				tableModel.addRow(values.toArray());
			}
		}
		return jTable;
	}

	@Override
	protected void buildPanel() {
		splitPane = new JSplitPane();

		tree = new XTree();

		ToolTipManager.sharedInstance().registerComponent(tree);

		tree.setCellRenderer(new HostConfigNodeRenderer());
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

		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
						.addComponent(splitPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));
		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
						.addComponent(splitPane, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));
	}

	/**
	 * setting all data for displaying and editing <br />
	 *
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */
	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap) {
		super.setEditableMap(visualdata, optionsMap);
		Logging.debug(this, " setEditableMap, visualdata keys " + visualdata);
		if (visualdata != null) {

			treemodel = new HostConfigTreeModel(givenClasses);
			tree.setModel(treemodel);
			tree.expandAll();

			keyclasses = treemodel.getGeneratedKeys();

			generateParts();

			classify(visualdata, keyclasses);

			for (String key : keyclasses) {
				partialPanels.get(key).setEditableMap(virtualLines.get(key), optionsMap);
				partialPanels.get(key).getMapTableModel().setObservers(this.mapTableModel.getObservers());
			}
		}
	}

	// apply method of superclass for all partial maps
	@Override
	public void setOptionsEditable(boolean b) {
		super.setOptionsEditable(b);

		for (String key : keyclasses) {

			partialPanels.get(key).setOptionsEditable(b);
		}
	}

	// apply method of superclass for all partial maps
	@Override
	public void setStoreData(Collection<Map<String, Object>> data) {
		super.setStoreData(data);

		for (String key : keyclasses) {

			partialPanels.get(key).setStoreData(data);
		}
	}

	// apply method of superclass for all partial maps
	@Override
	public void setUpdateCollection(Collection updateCollection) {
		super.setUpdateCollection(updateCollection);

		for (String key : keyclasses) {

			partialPanels.get(key).setUpdateCollection(updateCollection);
		}
	}

	@Override
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
		TreePath p = tree.getSelectionPath();

		int divLoc = splitPane.getDividerLocation();

		if (p == null) {
			splitPane.setRightComponent(emptyRightPane);
			splitPane.setDividerLocation(divLoc);
			return;
		}

		boolean isRoot = p.getPathCount() == 1;

		if (isRoot) {
			splitPane.setRightComponent(emptyRightPane);
		} else {
			// we start at 1 since we eliminate the root node
			String key = SimpleTreePath.dottedString(1, p);

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

			EditMapPanelX editMapPanel = new EditMapPanelX(tableCellRenderer, keylistExtendible, keylistEditable,
					reloadable) {
				private void reload() {
					ConfigedMain.getMainFrame().activateLoadingPane();
					TreePath p = tree.getSelectionPath();
					int row = tree.getRowForPath(p);

					actor.reloadData();
					Logging.info(this, "reloaded, return to " + p);
					if (p != null) {

						tree.setExpandsSelectedPaths(true);
						tree.setSelectionInterval(row, row);
						tree.scrollRowToVisible(row);
					}

					ConfigedMain.getMainFrame().disactivateLoadingPane();
				}

				@Override
				protected JPopupMenu definePopup() {
					Logging.debug(this, " (EditMapPanelGrouped) definePopup ");
					return new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_SAVE, PopupMenuTrait.POPUP_RELOAD,
							PopupMenuTrait.POPUP_PDF }) {

						@Override
						public void action(int p) {
							switch (p) {
							case PopupMenuTrait.POPUP_RELOAD:
								reload();
								break;

							case PopupMenuTrait.POPUP_SAVE:
								actor.saveData();
								break;
							case PopupMenuTrait.POPUP_PDF:
								createPDF();
								break;

							default:
								Logging.warning(this, "no case found for JPopupMenu in definePopup");
								break;
							}

						}
					};
				}

				@Override
				protected void buildPanel() {
					setLayout(new BorderLayout());

					table = new JTable(mapTableModel) {
						@Override
						public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
							Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
							if (c instanceof JComponent && showToolTip) {
								addTooltip((JComponent) c, this, names.get(rowIndex), rowIndex);
								setText((JComponent) c, this, vColIndex, rowIndex);
							}
							return c;
						}
					};

					TableCellRenderer colorized = new ColorTableCellRenderer();

					table.setDefaultRenderer(Object.class, colorized);
					table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					table.setRowHeight(Globals.TABLE_ROW_HEIGHT);
					table.addMouseWheelListener(
							mouseWheelEvent -> reactToMouseWheelEvent(mouseWheelEvent.getWheelRotation()));

					jScrollPane = new JScrollPane(table);

					add(jScrollPane, BorderLayout.CENTER);
				}
			};

			editMapPanel.setCellEditor(new SensitiveCellEditorForDataPanel());
			editMapPanel.setActor(actor);

			partialPanels.put(key, editMapPanel);
		}

		List<String> theUsers = new LinkedList<>();
		theRoles = new LinkedList<>();

		theRoles.add(UserConfig.NONE_PROTOTYPE);

		for (String classkey : new TreeSet<>(partialPanels.keySet())) {
			Logging.info(this, "classkey " + classkey);

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

		Logging.info(this, "theRoles found " + theRoles);
		Logging.info(this, "theUsers found " + theUsers);

		for (Entry<String, DefaultEditMapPanel> entry : partialPanels.entrySet()) {
			entry.getValue().setEditableFunction(key -> isEditable(key, entry));
		}
	}

	private void addTooltip(JComponent jc, JTable table, String propertyName, int rowIndex) {

		jc.setToolTipText("<html>" + createTooltipForPropertyName(propertyName) + "</html>");

		// check equals with default

		Object defaultValue;

		if (defaultsMap == null) {
			Logging.warning(this, "no default values available, defaultsMap is null");
		} else if ((defaultValue = defaultsMap.get(table.getValueAt(rowIndex, 0))) == null) {
			Logging.warning(this, "no default Value found");

			jc.setForeground(Globals.OPSI_ERROR);

			jc.setToolTipText(Configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

			jc.setFont(jc.getFont().deriveFont(Font.BOLD));
		} else if (!defaultValue.equals(table.getValueAt(rowIndex, 1))
				|| (originalMap != null && originalMap.containsKey(propertyName))) {
			jc.setFont(jc.getFont().deriveFont(Font.BOLD));
		} else {
			// Do nothing, since it's defaultvalue
		}
	}

	private static void setText(JComponent jc, JTable table, int vColIndex, int rowIndex) {
		if (vColIndex == 1 && Utils.isKeyForSecretValue((String) table.getValueAt(rowIndex, 0))) {
			if (jc instanceof JLabel) {
				((JLabel) jc).setText(Globals.STARRED_STRING);
			} else if (jc instanceof JTextComponent) {
				((JTextComponent) jc).setText(Globals.STARRED_STRING);
			} else {
				// Do nothing
			}
		}
	}

	private String createTooltipForPropertyName(String propertyName) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder tooltip = new StringBuilder();

		if (defaultsMap != null && defaultsMap.get(propertyName) != null) {

			if (includeAdditionalTooltipText) {
				tooltip.append("default (" + getPropertyOrigin(propertyName) + "): ");
			} else {
				tooltip.append("default: ");
			}

			if (Utils.isKeyForSecretValue(propertyName)) {
				tooltip.append(Globals.STARRED_STRING);
			} else {
				tooltip.append(defaultsMap.get(propertyName));
			}
		}

		if (descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
			tooltip.append("<br/><br/>" + descriptionsMap.get(propertyName));
		}

		return tooltip.toString();
	}

	private String getPropertyOrigin(String propertyName) {
		Map<String, ConfigOption> serverConfigs = persistenceController.getConfigDataService().getConfigOptionsPD();

		if (serverConfigs != null && serverConfigs.containsKey(propertyName)
				&& !serverConfigs.get(propertyName).getDefaultValues().equals(defaultsMap.get(propertyName))) {
			return "depot";
		} else {
			return "server";
		}
	}

	// Modification info and some userroles cannot be edited
	private boolean isEditable(String key, Entry<String, DefaultEditMapPanel> partialPanelEntry) {

		Logging.info(this, "entry " + partialPanelEntry + " key " + key);

		Boolean result = true;

		if (key.endsWith(UserConfig.MODIFICATION_INFO_KEY)) {
			result = false;
		} else {
			// we really are in a user branch
			if (UserConfig.getUserFromKey(key) != null) {
				result = isUserKeyEditable(key, partialPanelEntry.getKey());
			}
		}

		Logging.info(this, "key denied ? " + key + " : " + result);
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
			Logging.debug(this, "classify key ------- " + key);
			boolean foundClass = false;
			for (String idCollect : classIdsDescending) {
				if (key.startsWith(idCollect)) {
					virtualLines.get(idCollect).put(key, data.get(key));
					Logging.debug(this, "classify idCollect -------- " + idCollect);
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
			Logging.debug(this, "recognized role path " + path);
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
		ConfigedMain.getMainFrame().activateLoadingPane();
		// partial reload
		buildUserConfig();

		Logging.info(this, "reload");
		TreePath p = tree.getSelectionPath();
		int row = tree.getRowForPath(p);

		actor.reloadData();
		Logging.debug(this, "reloaded, return to " + p);
		if (p != null) {

			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);
			tree.scrollRowToVisible(row);
		}

		ConfigedMain.getMainFrame().disactivateLoadingPane();
	}

	private void addUser() {

		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(null, "add user", false,
				new String[] { Configed.getResourceValue("buttonClose"), Configed.getResourceValue("buttonOK") },

				new Icon[] { Utils.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Utils.createImageIcon("images/cancel16_small.png", "") },
				2, 600, 600, true, null) {

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
		UserConfigProducing up = new UserConfigProducing(false,
				persistenceController.getHostInfoCollections().getConfigServer(),
				persistenceController.getHostInfoCollections().getDepotNamesList(),
				persistenceController.getGroupDataService().getHostGroupIds(),
				persistenceController.getGroupDataService().getProductGroupsPD().keySet(),
				persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
				persistenceController.getConfigDataService().getConfigListCellOptionsPD());

		List<Object> newData = up.produce();

		if (newData == null) {
			Logging.warning(this, "readyObjects for userparts " + null);
		} else {

			if (!newData.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { newData });

				persistenceController.getExecutioner().doCall(omc);
			}

			Logging.info(this, "readyObjects for userparts " + newData.size());
		}
	}

	private void addRole() {

		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(null, "add role", // title
				false, // modal

				new String[] { Configed.getResourceValue("buttonClose"), Configed.getResourceValue("buttonOK") },

				new Icon[] { Utils.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Utils.createImageIcon("images/cancel16_small.png", "") },
				2, 600, 600,
				// lazylayout, i.e, we have a chance to define components and use them for the
				true,
				// layout
				null) {

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

		f.setCenterPane(centerPanel);

		f.setupLayout();
		f.setSize(new Dimension(500, 400));
		f.setVisible(true);
	}

	private void deleteUser() {

		TreePath p = tree.getSelectionPath();

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
				((EditMapPanelX) partialPanels.get(key)).removeProperty(name);
			}

			removeSubpanelClass(key);

			int row = tree.getRowForPath(p);

			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);

			tree.scrollRowToVisible(row);
		}
	}

	private void setRoleConfig(String name, String rolename) {
		Logging.info(this, "setRoleConfig " + name + "," + rolename);
		PersistenceControllerFactory.getPersistenceController().getConfigDataService().addRoleConfig(name, rolename);
	}

	private void setUserConfig(String name, String rolename) {
		Logging.info(this, "setUserConfig " + name + "," + rolename);
		PersistenceControllerFactory.getPersistenceController().getConfigDataService().addUserConfig(name, rolename);
	}

	public void setOriginalMap(Map<String, Object> originalMap) {
		this.originalMap = originalMap;
	}

	public void includeAdditionalTooltipText(boolean include) {
		this.includeAdditionalTooltipText = include;
	}
}
