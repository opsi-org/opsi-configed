/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
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

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.AbstractConfigurationTab;
import de.uib.configed.gui.ClientConfiguration;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.tree.IconNode;
import de.uib.configed.gui.features.tree.IconNodeRenderer;
import de.uib.configed.gui.messages.Messages;
import de.uib.configed.gui.share.Icons;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.gui.share.tree.XTree;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelHWInfo extends AbstractConfigurationTab implements TreeSelectionListener {
	private static final String CLASS_COMPUTER_SYSTEM = "COMPUTER_SYSTEM";
	private static final String CLASS_BASE_BOARD = "BASE_BOARD";

	// These are the values that should be interpreted as booleans
	public static final Set<String> BOOLEAN_VALUES = Set.of("UEFIBootActive", "SecureBootActive",
			"SecureBootWindowsCA2023");

	private static final Set<String> hwClassesForByAudit = new HashSet<>();
	static {
		hwClassesForByAudit.add(CLASS_COMPUTER_SYSTEM);
		hwClassesForByAudit.add(CLASS_BASE_BOARD);
	}

	private static final String KEY_VENDOR = "vendor";
	private static final String KEY_MODEL = "model";
	private static final String KEY_PRODUCT = "product";

	public static final String SCANPROPERTYNAME = "SCANPROPERTIES";
	public static final String SCANTIME = "scantime";

	private static final int INITIAL_DIVIDER_LOCATION = 350;

	private Map<String, List<Map<String, Object>>> hwInfo;
	private Map<String, Map<String, Object>> devicesInfo;
	private String treeRootTitle;
	private List<Map<String, Object>> hwConfig;

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
	private ClientConfiguration clientConfiguration;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelHWInfo(boolean withPopup, ConfigedMain configedMain, ClientConfiguration clientConfiguration) {
		super(false, true);
		this.withPopup = withPopup;
		this.configedMain = configedMain;
		this.clientConfiguration = clientConfiguration;

		buildContentPanel();
	}

	private void buildContentPanel() {
		panelByAuditInfo = new PanelHWByAuditDriver(configedMain);

		tree = new XTree();
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new IconNodeRenderer());

		JScrollPane jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		tableModel = new HWInfoTableModel();
		JTable table = new JTable(tableModel, null);
		table.setDefaultRenderer(Object.class, new HWInfoCellRenderer());
		table.setTableHeader(null);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(300);

		table.setDragEnabled(true);

		JScrollPane jScrollPaneInfo = new JScrollPane(table);
		jScrollPaneInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPaneTree, jScrollPaneInfo);
		splitPane.setDividerLocation(INITIAL_DIVIDER_LOCATION);

		JPanel contentPanel = new JPanel();
		setComponent(contentPanel);

		contentPanel
				.setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + " 0 0 0, wrap 1", "[grow]", "[][grow]"));
		contentPanel.add(panelByAuditInfo);
		contentPanel.add(splitPane, "grow");

		if (withPopup) {
			new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF,
					PopupMenuTrait.POPUP_FLOATING_COPY }, new JComponent[] { tree, table }) {
				@Override
				public void action(int p) {
					switch (p) {
					case PopupMenuTrait.POPUP_RELOAD -> reload();
					case PopupMenuTrait.POPUP_FLOATING_COPY -> floatExternal();
					case PopupMenuTrait.POPUP_PDF -> exportPDF();
					default -> Logging.warning(this, "no case for PopupMenuTrait found in popupMenu");
					}
				}
			};
		}
	}

	@Override
	protected void updateContent() {
		Logging.info(this, "setHardwareInfoPage for, clients count ", configedMain.getSelectedClients().size());
		setHardwareInfo(persistenceController.getDataServices().hardware
				.getHardwareInfo(configedMain.getSelectedClients().get(0)));
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
		Logging.debug(this, "reload hardware info");
		updateContent();
	}

	private void floatExternal() {
		PanelHWInfo copyOfMe = new PanelHWInfo(false, configedMain, clientConfiguration);
		copyOfMe.setHardwareInfo(hwInfo);

		copyOfMe.tree.expandRows(tree.getToggledRows(rootPath));
		copyOfMe.tree.setSelectionInterval(tree.getMinSelectionRow(), tree.getMinSelectionRow());

		JDialog dialog = new JDialog();
		dialog.setTitle(treeRootTitle);
		dialog.setContentPane(copyOfMe);
		dialog.setSize(getSize());
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	private static Icon createImageIcon(String hwClass) {
		Icon classIcon = Icons.createImageIcon("hwinfo_images/" + hwClass + ".png", "");

		if (classIcon == null) {
			classIcon = Icons.createImageIcon("hwinfo_images/DEVICE.png", "");
		}

		return classIcon;
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

		Map<String, Object> deviceInfo = devicesInfo.get(hwClass + "-" + node.toString());

		return devices != null && deviceInfo != null;
	}

	private List<Map<String, Object>> getValuesFromHwClass(String hwClass) {
		List<Map<String, Object>> values = null;
		for (Map<String, Object> whc : hwConfig) {
			Map<String, Object> whcClass = POJOReMapper.remap(whc.get("Class"));
			if (whcClass.get("Opsi").equals(hwClass)) {
				values = POJOReMapper.remap(whc.get("Values"));
				break;
			}
		}

		return values;
	}

	private List<Object[]> getDataForNode(IconNode node, boolean reduceScanToByAuditClasses) {
		if (!hasData(node, reduceScanToByAuditClasses)) {
			return new ArrayList<>();
		}

		TreeNode[] path = node.getPath();

		String hwClassUI = path[1].toString();
		String hwClass = (String) hwClassMapping.get(hwClassUI);

		Map<String, Object> deviceInfo = devicesInfo.get(hwClass + "-" + node.toString());

		List<Map<String, Object>> values = getValuesFromHwClass(hwClass);

		List<Object[]> data = new ArrayList<>();
		for (Map<String, Object> value : values) {
			String opsi = (String) value.get("Opsi");
			Logging.debug(this, "opsi ", opsi);

			// table row keys //no encoding needed
			String ui = (String) value.get("UI");
			String unit = null;
			if (value.containsKey("Unit")) {
				unit = (String) value.get("Unit");
				Logging.debug(this, "unit  ", unit);
			}

			for (Entry<String, Object> deviceInfoEntry : deviceInfo.entrySet()) {
				if (deviceInfoEntry.getKey().equalsIgnoreCase(opsi) && deviceInfoEntry.getValue() != null) {
					Object cv = findCV(reduceScanToByAuditClasses, hwClass, unit, opsi, deviceInfoEntry.getValue());

					Object[] row = { ui, cv };
					data.add(row);
					Logging.debug(this, "hwClass row  version 1 ", hwClass, ": ", Arrays.toString(row));
					break;
				}
			}
		}

		return data;
	}

	private Object findCV(boolean reduceScanToByAuditClasses, String hwClass, String unit, String opsi,
			Object deviceInfo) {
		// First we check, if the deviceInfo should be a boolean
		if (BOOLEAN_VALUES.contains(opsi) && deviceInfo instanceof Integer integer) {
			return integer.intValue() != 0;
		}

		String cv = "" + deviceInfo;

		// Set these values before adding ending to cv
		if (reduceScanToByAuditClasses && hwClass != null) {
			setValuesDependentOfCV(cv, hwClass, opsi);
		}

		if (unit != null) {
			cv = addUnit(cv, unit);
		}

		return cv;
	}

	private void setValuesDependentOfCV(String cv, String hwClass, String opsi) {
		Logging.debug(this, "key ", opsi);

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
			Logging.warning(this, "unexpected value for hwclass: ", hwClass);
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// Returns the last path element of the selection.
		IconNode node = (IconNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}

		TreePath selectedPath = tree.getSelectionPath();
		Logging.debug(this, "selectedPath ", selectedPath);
		if (!node.isLeaf()) {
			tree.expandPath(selectedPath);
		} else {
			tableModel.setData(getDataForNode(node, false));
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

	private void setHardwareInfo(Map<String, List<Map<String, Object>>> hwInfo) {
		if (hwConfig == null) {
			hwConfig = persistenceController.getDataServices().hardware
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

		setTreeRootTitle(hwInfo);
		createRoot(treeRootTitle);

		initializeHwClassMapping();
		devicesInfo = new HashMap<>();

		processHwClasses(hwInfo);

		treeModel.nodeChanged(root);
		tree.expandRow(0);
	}

	private void setTreeRootTitle(Map<String, List<Map<String, Object>>> hwInfo) {
		List<Map<String, Object>> hwInfoSpecial = hwInfo.get(SCANPROPERTYNAME);

		if (hwInfoSpecial != null && !hwInfoSpecial.isEmpty() && hwInfoSpecial.get(0) != null
				&& hwInfoSpecial.get(0).get(SCANTIME) != null) {
			treeRootTitle = "Scan " + (String) hwInfoSpecial.get(0).get(SCANTIME);
		}
	}

	private void initializeHwClassMapping() {
		hwClassMapping = new TreeMap<>();
		for (Map<String, Object> whc : hwConfig) {
			hwClassMapping.put((String) Map.class.cast(whc.get("Class")).get("UI"),
					Map.class.cast(whc.get("Class")).get("Opsi"));
		}
	}

	private void processHwClasses(Map<String, List<Map<String, Object>>> hwInfo) {
		for (Entry<String, Object> hwClassEntry : hwClassMapping.entrySet()) {
			String hwClass = (String) hwClassEntry.getValue();

			List<Map<String, Object>> devices = hwInfo.get(hwClass);
			if (devices == null) {
				Logging.debug(this, "No devices of hwclass ", hwClass, " found");
				continue;
			}

			IconNode classNode = new IconNode(hwClassEntry.getKey());
			Icon classIcon = createImageIcon(hwClass);

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
					devicesInfo.put(hwClassMapping.get(classNode.getUserObject()) + "-" + device.get("displayName"),
							device);
					classNode.add(iconNode);
					scanNodes(iconNode);
					break;
				}
			}
		}
	}

	private void getLocalizedHashMap() {
		hwOpsiToUI = new HashMap<>();

		for (Map<String, Object> hardwareMap : hwConfig) {
			List<Map<String, Object>> values = POJOReMapper.remap(hardwareMap.get("Values"));
			for (Map<String, Object> valuesMap : values) {
				String type = (String) valuesMap.get("Opsi");
				String name = (String) valuesMap.get("UI");
				hwOpsiToUI.putIfAbsent(type, name);
			}
		}

		for (Map<String, Object> hardwareMap : hwConfig) {
			String hardwareName = (String) Map.class.cast(hardwareMap.get("Class")).get("UI");
			String hardwareOpsi = (String) Map.class.cast(hardwareMap.get("Class")).get("Opsi");

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
