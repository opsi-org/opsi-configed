/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.GeneralFrame;
import de.uib.configed.tree.IconNode;
import de.uib.configed.tree.IconNodeRenderer;
import de.uib.messages.Messages;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
import de.uib.utilities.tree.XTree;
import utils.Utils;

public class PanelHWInfo extends JPanel implements TreeSelectionListener {
	private static final String CLASS_COMPUTER_SYSTEM = "COMPUTER_SYSTEM";
	private static final String CLASS_BASE_BOARD = "BASE_BOARD";

	private static final Set<String> hwClassesForByAudit = new HashSet<>();
	static {
		hwClassesForByAudit.add(CLASS_COMPUTER_SYSTEM);
		hwClassesForByAudit.add(CLASS_BASE_BOARD);
	}

	private static final String KEY_VENDOR = "vendor";
	private static final String KEY_MODEL = "model";
	private static final String KEY_PRODUCT = "product";

	private static final String SCANPROPERTYNAME = "SCANPROPERTIES";
	private static final String SCANTIME = "scantime";

	private static final int INITIAL_DIVIDER_LOCATION = 350;

	private Map<String, List<Map<String, Object>>> hwInfo;
	private Map<String, Map<String, Object>> devicesInfo;
	private String treeRootTitle;
	private List<Map<String, List<Map<String, Object>>>> hwConfig;

	// for creating pdf
	private Map<String, String> hwOpsiToUI;

	private XTree tree;
	private IconNode root;
	private TreePath rootPath;
	private DefaultTreeModel treeModel;
	private HWInfoTableModel tableModel;
	private Map<String, Object> hwClassMapping;

	private String vendorStringComputerSystem;
	private String vendorStringBaseBoard;
	private String modelString;
	private String productString;

	private PanelHWByAuditDriver panelByAuditInfo;

	private boolean withPopup;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelHWInfo(ConfigedMain configedMain) {
		this(true, configedMain);
	}

	public PanelHWInfo(boolean withPopup, ConfigedMain configedMain) {
		this.withPopup = withPopup;
		this.configedMain = configedMain;
		buildPanel();
	}

	private void buildPanel() {
		panelByAuditInfo = new PanelHWByAuditDriver(configedMain);

		tree = new XTree();
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new IconNodeRenderer());

		JScrollPane jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		tableModel = new HWInfoTableModel();
		JTable table = new JTable(tableModel, null);
		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.setTableHeader(null);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(300);

		table.setDragEnabled(true);

		JScrollPane jScrollPaneInfo = new JScrollPane(table);
		jScrollPaneInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPaneTree, jScrollPaneInfo);
		contentPane.setDividerLocation(INITIAL_DIVIDER_LOCATION);

		GroupLayout layoutBase = new GroupLayout(this);
		setLayout(layoutBase);

		layoutBase.setHorizontalGroup(layoutBase.createParallelGroup()
				.addGroup(layoutBase.createSequentialGroup().addComponent(panelByAuditInfo, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(contentPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutBase.setVerticalGroup(layoutBase.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(panelByAuditInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(contentPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		if (withPopup) {
			PopupMenuTrait popupMenu = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD,
					PopupMenuTrait.POPUP_PDF, PopupMenuTrait.POPUP_FLOATINGCOPY }) {
				@Override
				public void action(int p) {
					switch (p) {
					case PopupMenuTrait.POPUP_RELOAD:
						reload();
						break;

					case PopupMenuTrait.POPUP_FLOATINGCOPY:
						floatExternal();
						break;
					case PopupMenuTrait.POPUP_PDF:
						exportPDF();
						break;

					default:
						Logging.warning(this, "no case for PopupMenuTrait found in popupMenu");
						break;
					}
				}
			};

			popupMenu.addPopupListenersTo(new JComponent[] { tree, table });
		}
	}

	private void exportPDF() {
		Logging.info(this, "create report");
		Map<String, String> metaData = new HashMap<>();
		metaData.put("header", Configed.getResourceValue("PanelHWInfo.createPDF.title"));

		metaData.put("title", treeRootTitle);
		metaData.put("keywords", "hardware infos");

		ExporterToPDF pdfExportTable = new ExporterToPDF(createHWInfoTableModelComplete());
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		// create pdf // no filename, onlyselectedRows=false
		pdfExportTable.execute(null, false);
	}

	/** overwrite in subclasses */
	protected void reload() {
		Logging.debug(this, "reload action");
	}

	private void floatExternal() {
		PanelHWInfo copyOfMe;
		GeneralFrame externalView;

		copyOfMe = new PanelHWInfo(false, configedMain);
		copyOfMe.setHardwareInfo(hwInfo);

		copyOfMe.expandRows(tree.getToggledRows(rootPath));
		copyOfMe.setSelectedRow(tree.getMinSelectionRow());

		externalView = new GeneralFrame(null, treeRootTitle, false);
		externalView.addPanel(copyOfMe);
		externalView.setSize(this.getSize());
		externalView.setLocationRelativeTo(ConfigedMain.getMainFrame());

		externalView.setVisible(true);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	private static ImageIcon createImageIcon(String path) {
		return Utils.createImageIcon(path, "");
	}

	private void createRoot(String name) {
		root = new IconNode(name);

		treeModel = new DefaultTreeModel(root);

		tree.setModel(treeModel);

		rootPath = tree.getPathForRow(0);
	}

	private static String addUnit(String value, String unit) {
		if (value.isEmpty()) {
			return value;
		}

		String result = "";
		BigDecimal v = new BigDecimal(value);

		int mult = 1000;
		if ("byte".equalsIgnoreCase(unit)) {
			mult = 1024;
		}
		if (v.compareTo(BigDecimal.valueOf((long) mult * mult * mult)) >= 0) {
			result = ((float) Math.round(v.floatValue() * 1000 / ((long) mult * mult * mult)) / 1000) + " G" + unit;
		} else if (v.compareTo(BigDecimal.valueOf((long) mult * mult)) >= 0) {
			result = ((float) Math.round(v.floatValue() * 1000 / (mult * mult)) / 1000) + " M" + unit;
		} else if (v.compareTo(BigDecimal.valueOf(mult)) >= 0) {
			result = ((float) Math.round(v.floatValue() * 1000 / (mult)) / 1000) + " k" + unit;
		} else {
			result = value + " " + unit;
		}

		return result;
	}

	private void expandRows(List<Integer> rows) {
		tree.expandRows(rows);
	}

	private void setSelectedRow(int row) {
		tree.setSelectionInterval(row, row);
	}

	private List<String[]> getDataForNode(IconNode node) {
		return getDataForNode(node, false);
	}

	private boolean hasData(IconNode node, boolean reduceScanToByAuditClasses) {
		if (node == null || !node.isLeaf() || node.getPath().length < 3) {
			return false;
		}

		TreeNode[] path = node.getPath();

		String hwClassUI = path[1].toString();
		String hwClass = (String) hwClassMapping.get(hwClassUI);

		if (hwClass != null && reduceScanToByAuditClasses && !hwClassesForByAudit.contains(hwClass)) {
			return false;
		}

		List<Map<String, Object>> devices = hwInfo.get(hwClass);

		Map<String, Object> deviceInfo = devicesInfo.get(node.toString());

		return devices != null && deviceInfo != null;
	}

	private List<Map<String, Object>> getValuesFromHwClass(String hwClass) {
		List<Map<String, Object>> values = null;
		for (Map<String, List<Map<String, Object>>> whc : hwConfig) {
			if (whc != null) {
				Map<String, Object> whcClass = whc.get("Class").get(0);
				if (whcClass.get("Opsi").equals(hwClass)) {
					values = whc.get("Values");
					break;
				}
			} else {
				Logging.error(this, "hwConfig element is null");
			}
		}

		return values;
	}

	private List<String[]> getDataForNode(IconNode node, boolean reduceScanToByAuditClasses) {
		if (!hasData(node, reduceScanToByAuditClasses)) {
			return new ArrayList<>();
		}

		TreeNode[] path = node.getPath();

		String hwClassUI = path[1].toString();
		String hwClass = (String) hwClassMapping.get(hwClassUI);

		Map<String, Object> deviceInfo = devicesInfo.get(node.toString());

		List<Map<String, Object>> values = getValuesFromHwClass(hwClass);

		List<String[]> data = new ArrayList<>();
		if (values != null) {
			for (Map<String, Object> value : values) {
				String opsi = (String) value.get("Opsi");
				Logging.debug(this, "opsi " + opsi);

				// table row keys //no encoding needed
				String ui = (String) value.get("UI");
				String unit = null;
				if (value.containsKey("Unit")) {
					unit = (String) value.get("Unit");
					Logging.debug(this, "unit  " + unit);
				}

				for (Entry<String, Object> deviceInfoEntry : deviceInfo.entrySet()) {
					if (deviceInfoEntry.getKey().equalsIgnoreCase(opsi) && deviceInfoEntry.getValue() != null) {
						String cv = "";

						if (deviceInfoEntry.getValue() instanceof String) {
							cv = (String) deviceInfoEntry.getValue();
						} else {
							cv = "" + deviceInfoEntry.getValue();
						}

						if (reduceScanToByAuditClasses && hwClass != null) {
							Logging.debug(this, "key " + opsi);

							if (hwClass.equals(CLASS_COMPUTER_SYSTEM)) {
								if (opsi.equalsIgnoreCase(KEY_VENDOR)) {
									vendorStringComputerSystem = cv;
								} else if (opsi.equalsIgnoreCase(KEY_MODEL)) {
									modelString = cv;
								} else {
									// Not needed, since other values not used for Description on top
								}
							} else if (hwClass.equals(CLASS_BASE_BOARD)) {
								if (opsi.equalsIgnoreCase(KEY_VENDOR)) {
									vendorStringBaseBoard = cv;
								} else if (opsi.equalsIgnoreCase(KEY_PRODUCT)) {
									productString = cv;
								} else {
									// Not needed, since other values not used for Description on top
								}
							} else {
								Logging.warning(this, "unexpected value for hwclass: " + hwClass);
							}
						}

						if (unit != null) {
							cv = addUnit(cv, unit);
						}
						String[] row = { ui, cv };
						data.add(row);
						Logging.debug(this, "hwClass row  version 1 " + hwClass + ": " + Arrays.toString(row));
						break;
					}
				}
			}
		} else {
			for (Entry<String, Object> info : deviceInfo.entrySet()) {
				String[] row = { info.getKey(), (String) info.getValue() };
				data.add(row);
				Logging.debug(this, "hwClass row  " + hwClass + ": " + Arrays.toString(row));
			}
		}

		return data;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// Returns the last path element of the selection.
		IconNode node = (IconNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}

		TreePath selectedPath = tree.getSelectionPath();
		Logging.debug(this, "selectedPath " + selectedPath);
		if (!node.isLeaf()) {
			tree.expandPath(selectedPath);
		} else {
			tableModel.setData(getDataForNode(node));
		}
	}

	private void scanNodes(IconNode node) {
		if (node != null && node.isLeaf()) {
			TreeNode[] path = node.getPath();
			if (path.length < 3) {
				tableModel.setData(new ArrayList<>());
				return;
			}
			String hwClassUI = path[1].toString();
			String hwClass = (String) hwClassMapping.get(hwClassUI);

			if (hwClass != null && (hwClass.equals(CLASS_COMPUTER_SYSTEM) || hwClass.equals(CLASS_BASE_BOARD))) {
				Logging.debug(this, "scanNode found  class_COMPUTER_SYSTEM or class_BASE_BOARD");
				getDataForNode(node, true);

				panelByAuditInfo.setByAuditFields(vendorStringComputerSystem, vendorStringBaseBoard, modelString,
						productString);
			}
		}
	}

	private void initByAuditStrings() {
		vendorStringComputerSystem = "";
		vendorStringBaseBoard = "";
		modelString = "";
		productString = "";
	}

	public void setHardwareInfo(Map<String, List<Map<String, Object>>> hwInfo) {
		if (hwConfig == null) {
			hwConfig = persistenceController.getHardwareDataService()
					.getOpsiHWAuditConfPD(Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry());
		}

		initByAuditStrings();
		panelByAuditInfo.emptyByAuditStrings();

		tableModel.setData(new ArrayList<>());

		this.hwInfo = hwInfo;

		if (hwInfo == null || hwInfo.isEmpty()) {
			treeRootTitle = Configed.getResourceValue("MainFrame.NoHardwareConfiguration");
			createRoot(treeRootTitle);
			return;
		}

		List<Map<String, Object>> hwInfoSpecial = hwInfo.get(SCANPROPERTYNAME);

		if (hwInfoSpecial != null && !hwInfoSpecial.isEmpty() && hwInfoSpecial.get(0) != null
				&& hwInfoSpecial.get(0).get(SCANTIME) != null) {
			treeRootTitle = "Scan " + (String) hwInfoSpecial.get(0).get(SCANTIME);
		}

		createRoot(treeRootTitle);

		hwClassMapping = new HashMap<>();
		String[] hwClassesUI = new String[hwConfig.size()];
		for (int i = 0; i < hwConfig.size(); i++) {
			Map<String, List<Map<String, Object>>> whc = hwConfig.get(i);
			hwClassesUI[i] = (String) whc.get("Class").get(0).get("UI");
			hwClassMapping.put(hwClassesUI[i], whc.get("Class").get(0).get("Opsi"));
		}

		Arrays.sort(hwClassesUI);

		devicesInfo = new HashMap<>();

		for (int i = 0; i < hwClassesUI.length; i++) {
			// get next key - value - pair
			String hwClassUI = hwClassesUI[i];
			String hwClass = (String) hwClassMapping.get(hwClassUI);

			List<Map<String, Object>> devices = hwInfo.get(hwClass);
			if (devices == null) {
				Logging.debug(this, "No devices of hwclass " + hwClass + " found");
				continue;
			}

			IconNode classNode = new IconNode(hwClassUI);
			Icon classIcon = createImageIcon("hwinfo_images/" + hwClass + ".png");
			if (classIcon == null) {
				classIcon = createImageIcon("hwinfo_images/DEVICE.png");
			}

			classNode.setIcon(classIcon);
			root.add(classNode);

			Map<String, List<Map<String, Object>>> displayNames = new HashMap<>();

			for (int j = 0; j < devices.size(); j++) {
				Map<String, Object> deviceInfo = devices.get(j);
				String displayName = (String) deviceInfo.get("name");
				if (displayName == null || displayName.isEmpty()) {
					displayName = hwClass + "_" + j;
				}

				List<Map<String, Object>> displayList = displayNames.computeIfAbsent(displayName,
						s -> new ArrayList<>());

				displayList.add(devices.get(j));
			}

			String[] names = createNamesArray(devices, displayNames);

			createIconNodes(names, devices, classIcon, classNode);
		}

		treeModel.nodeChanged(root);
		tree.expandRow(0);
		tree.expandRow(1);
	}

	private static String[] createNamesArray(List<Map<String, Object>> devices,
			Map<String, List<Map<String, Object>>> displayNames) {
		String[] names = new String[devices.size()];

		int num = 0;

		for (Entry<String, List<Map<String, Object>>> displayEntry : displayNames.entrySet()) {
			List<Map<String, Object>> devs = displayEntry.getValue();

			for (int j = 0; j < devs.size(); j++) {
				Map<String, Object> dev = devs.get(j);
				String dn = displayEntry.getKey();
				if (devs.size() > 1) {
					dn += " (" + j + ")";
				}

				dev.put("displayName", dn);
				names[num] = dn;
				num++;
			}
		}

		return names;
	}

	private void createIconNodes(String[] names, List<Map<String, Object>> devices, Icon classIcon,
			IconNode classNode) {
		Arrays.sort(names);

		for (String name : names) {
			for (Map<String, Object> device : devices) {
				if (name.equals(device.get("displayName"))) {
					IconNode iconNode = new IconNode(device.get("displayName"));
					iconNode.setIcon(classIcon);
					devicesInfo.put((String) device.get("displayName"), device);
					classNode.add(iconNode);
					scanNodes(iconNode);
					break;
				}
			}
		}
	}

	private void getLocalizedHashMap() {
		hwOpsiToUI = new HashMap<>();

		for (Map<String, List<Map<String, Object>>> hardwareMap : hwConfig) {
			List<Map<String, Object>> values = hardwareMap.get("Values");
			for (Map<String, Object> valuesMap : values) {
				String type = (String) valuesMap.get("Opsi");
				String name = (String) valuesMap.get("UI");
				hwOpsiToUI.putIfAbsent(type, name);
			}
		}

		for (Map<String, List<Map<String, Object>>> hardwareMap : hwConfig) {
			String hardwareName = (String) hardwareMap.get("Class").get(0).get("UI");
			String hardwareOpsi = (String) hardwareMap.get("Class").get(0).get("Opsi");

			hwOpsiToUI.putIfAbsent(hardwareOpsi, hardwareName);
		}
	}

	private JTable createHWInfoTableModelComplete() {
		getLocalizedHashMap();

		DefaultTableModel tableModelComplete = new DefaultTableModel();
		JTable jTableComplete = new JTable(tableModelComplete);

		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_hardware"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_device"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_name"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_value"));

		for (int i = 0; i < treeModel.getChildCount(treeModel.getRoot()); i++) {
			Object child = treeModel.getChild(treeModel.getRoot(), i);
			// get ArrayList
			List<Map<String, Object>> al = hwInfo.get(hwClassMapping.get(child.toString()));

			boolean first = true;
			for (Map<String, Object> hm : al) {
				List<String> childValues = new ArrayList<>();
				if (first) {
					childValues.add(child.toString());
					first = false;
				} else {
					childValues.add("");
				}
				childValues.add(hm.get("displayName").toString());
				addRowToModel(hm, childValues, tableModelComplete);
			}
		}
		return jTableComplete;
	}

	private void addRowToModel(Map<String, Object> hm, List<String> childValues, DefaultTableModel tableModelComplete) {
		boolean firstValue = true;
		for (Entry<String, Object> entry : hm.entrySet()) {
			if (!"displayName".equals(entry.getKey()) && !"type".equals(entry.getKey())) {
				if (firstValue) {
					firstValue = false;
					childValues.add(hwOpsiToUI.get(entry.getKey()));
					childValues.add(entry.getValue().toString());
				} else {
					childValues = new ArrayList<>();
					childValues.add("");
					childValues.add("");
					childValues.add(hwOpsiToUI.get(entry.getKey()));
					childValues.add(entry.getValue().toString());
				}
				tableModelComplete.addRow(childValues.toArray());
			}
		}
	}
}
