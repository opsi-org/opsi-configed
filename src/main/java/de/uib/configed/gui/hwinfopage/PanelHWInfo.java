package de.uib.configed.gui.hwinfopage;

import java.awt.Dimension;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.tree.IconNode;
import de.uib.configed.tree.IconNodeRenderer;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
import de.uib.utilities.tree.XTree;

public class PanelHWInfo extends JPanel implements TreeSelectionListener {
	private static final String CLASS_COMPUTER_SYSTEM = "COMPUTER_SYSTEM";
	private static final String CLASS_BASE_BOARD = "BASE_BOARD";

	private static final List<String> hwClassesForByAudit = new ArrayList<>();
	static {
		hwClassesForByAudit.add(CLASS_COMPUTER_SYSTEM);
		hwClassesForByAudit.add(CLASS_BASE_BOARD);
	}

	private static final String KEY_VENDOR = "vendor";
	private static final String KEY_MODEL = "model";
	private static final String KEY_PRODUCT = "product";

	private static final String SCANPROPERTYNAME = "SCANPROPERTIES";
	private static final String SCANTIME = "scantime";

	private Map<String, List<Map<String, Object>>> hwInfo;
	private String treeRootTitle;
	private List<Map<String, List<Map<String, Object>>>> hwConfig;
	private String title = "HW Information";

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

	private int hGap = Globals.HGAP_SIZE / 2;
	private int vGap = Globals.VGAP_SIZE / 2;

	private boolean withPopup;

	private ConfigedMain main;

	public PanelHWInfo(ConfigedMain main) {
		this(true, main);
	}

	public PanelHWInfo(boolean withPopup, ConfigedMain main) {
		this.withPopup = withPopup;
		this.main = main;
		buildPanel();
	}

	private static String encodeString(String s) {
		return s;

	}

	private void buildPanel() {

		panelByAuditInfo = new PanelHWByAuditDriver(main);

		tree = new XTree(null);

		JScrollPane jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		jScrollPaneTree.setMinimumSize(new Dimension(200, 200));
		jScrollPaneTree.setPreferredSize(new Dimension(400, 200));

		tableModel = new HWInfoTableModel();
		JTable table = new JTable(tableModel, null);
		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.setTableHeader(null);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(300);

		table.setDragEnabled(true);
		if (!ConfigedMain.THEMES) {
			table.setBackground(Globals.nimbusBackground);
		}
		JPanel embed = new JPanel();
		GroupLayout layoutEmbed = new GroupLayout(embed);
		embed.setLayout(layoutEmbed);

		layoutEmbed.setHorizontalGroup(layoutEmbed.createSequentialGroup().addGap(hGap, hGap, hGap)
				.addComponent(table, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(hGap, hGap, hGap));

		layoutEmbed.setVerticalGroup(layoutEmbed.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addComponent(table, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(vGap, vGap, vGap));

		JScrollPane jScrollPaneInfo = new JScrollPane(embed);
		jScrollPaneInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPaneTree, jScrollPaneInfo);

		GroupLayout layoutBase = new GroupLayout(this);
		setLayout(layoutBase);

		layoutBase.setHorizontalGroup(layoutBase.createSequentialGroup().addGap(hGap, hGap, hGap)
				.addGroup(layoutBase.createParallelGroup()
						.addGroup(layoutBase.createSequentialGroup().addGap(hGap - 2, hGap - 2, hGap - 2)
								.addComponent(panelByAuditInfo, 30, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addGap(hGap - 2, hGap - 2, hGap - 2)

						).addComponent(contentPane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGap(hGap, hGap, hGap));

		layoutBase.setVerticalGroup(layoutBase.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addComponent(panelByAuditInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(vGap / 2, vGap / 2, vGap / 2)
				.addComponent(contentPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

		);

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
						Logging.info(this, "create report");
						// TODO letzter scan, Auswahl für den ByAudit-Treiberpfad???
						HashMap<String, String> metaData = new HashMap<>();
						metaData.put("header", Configed.getResourceValue("PanelHWInfo.createPDF.title"));
						title = "";
						if (main.getHostsStatusInfo().getInvolvedDepots().length() != 0) {
							title = title + "Depot: " + main.getHostsStatusInfo().getInvolvedDepots();
						}
						if (main.getHostsStatusInfo().getSelectedClientNames().length() != 0) {
							title = title + "; Client: " + main.getHostsStatusInfo().getSelectedClientNames();
						}
						metaData.put("title", title);
						metaData.put("keywords", "hardware infos");

						ExporterToPDF pdfExportTable = new ExporterToPDF(createHWInfoTableModelComplete());
						pdfExportTable.setMetaData(metaData);
						pdfExportTable.setPageSizeA4Landscape();
						// create pdf // no filename, onlyselectedRows=false
						pdfExportTable.execute(null, false);

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

	/** overwrite in subclasses */
	protected void reload() {
		Logging.debug(this, "reload action");
	}

	private void floatExternal() {
		PanelHWInfo copyOfMe;
		de.uib.configed.gui.GeneralFrame externalView;

		copyOfMe = new PanelHWInfo(false, main);
		copyOfMe.setHardwareConfig(hwConfig);
		copyOfMe.setHardwareInfo(hwInfo, treeRootTitle);

		copyOfMe.expandRows(tree.getToggledRows(rootPath));
		copyOfMe.setSelectedRow(tree.getMinSelectionRow());

		externalView = new de.uib.configed.gui.GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.setLocationRelativeTo(ConfigedMain.getMainFrame());

		externalView.setVisible(true);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	private static ImageIcon createImageIcon(String path) {
		return Globals.createImageIcon(path, "");

	}

	private void createRoot(String name) {
		root = new IconNode(name);
		Icon icon = createImageIcon("hwinfo_images/DEVICE.png");
		root.setClosedIcon(icon);
		root.setLeafIcon(icon);
		root.setOpenIcon(icon);

		treeModel = new DefaultTreeModel(root);

		tree.setModel(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(new IconNodeRenderer());

		rootPath = tree.getPathForRow(0);
	}

	private String addUnit(String value, String unit) {
		if (value.isEmpty()) {
			return value;
		}

		BigInteger v = null;

		try {
			v = new BigInteger(value);
		} catch (Exception e) {
			Logging.warning(this, "could not create BigInteger from value " + value, e);
			return value + " " + unit;
		}

		int mult = 1000;
		if ("byte".equalsIgnoreCase(unit)) {
			mult = 1024;
		}
		// TODO: nano, micro
		if (v.compareTo(BigInteger.valueOf((long) mult * mult * mult)) >= 0) {
			return ((float) Math.round(v.floatValue() * 1000 / ((long) mult * mult * mult)) / 1000) + " G" + unit;
		} else if (v.compareTo(BigInteger.valueOf((long) mult * mult)) >= 0) {
			return ((float) Math.round(v.floatValue() * 1000 / (mult * mult)) / 1000) + " M" + unit;
		} else if (v.compareTo(BigInteger.valueOf(mult)) >= 0) {
			return ((float) Math.round(v.floatValue() * 1000 / (mult)) / 1000) + " k" + unit;
		}
		return value + " " + unit;
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

	private List<String[]> getDataForNode(IconNode node, boolean reduceScanToByAuditClasses) {

		if (node == null || !node.isLeaf()) {
			return new ArrayList<>();
		}

		TreeNode[] path = node.getPath();
		if (path.length < 3) {
			return new ArrayList<>();
		}

		String hwClassUI = path[1].toString();
		String hwClass = (String) hwClassMapping.get(hwClassUI);

		if (hwClass != null && reduceScanToByAuditClasses && !hwClassesForByAudit.contains(hwClass)) {
			return new ArrayList<>();
		}

		List<Map<String, Object>> devices = hwInfo.get(hwClass);
		Map<String, Object> deviceInfo = node.getDeviceInfo();
		if (devices == null || deviceInfo == null) {
			return new ArrayList<>();
		}

		List<Map<String, Object>> values = null;

		for (int j = 0; j < hwConfig.size(); j++) {

			Map<String, List<Map<String, Object>>> whc = hwConfig.get(j);
			if (whc != null) {
				Map<String, Object> whcClass = whc.get("Class").get(0);
				if (whcClass.get("Opsi").equals(hwClass)) {
					values = whc.get("Values");
					break;
				}
			} else {
				Logging.error(this, "hwConfig.get(" + j + ") is null");
			}
		}
		List<String[]> data = new ArrayList<>();
		if (values != null) {
			for (int j = 0; j < values.size(); j++) {
				Map<String, Object> v = values.get(j);
				String opsi = (String) v.get("Opsi");
				Logging.debug(this, "opsi " + opsi);

				// table row keys //no encoding needed
				String ui = encodeString((String) v.get("UI"));
				String unit = null;
				if (v.containsKey("Unit")) {
					unit = (String) v.get("Unit");
					Logging.debug(this, "unit  " + unit);
				}
				Iterator<String> iter = deviceInfo.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equalsIgnoreCase(opsi)) {
						String cv = "";

						if (deviceInfo.get(key) == null) {
							continue;
						}

						if (deviceInfo.get(key) instanceof String) {
							cv = (String) deviceInfo.get(key);
						} else {
							cv = "" + deviceInfo.get(key);
						}

						if (reduceScanToByAuditClasses && hwClass != null) {
							Logging.debug(this, "key " + opsi);

							if (hwClass.equals(CLASS_COMPUTER_SYSTEM)) {
								if (opsi.equalsIgnoreCase(KEY_VENDOR)) {
									vendorStringComputerSystem = cv;

								} else if (opsi.equalsIgnoreCase(KEY_MODEL)) {
									modelString = cv;

								}
							} else if (hwClass.equals(CLASS_BASE_BOARD)) {
								if (opsi.equalsIgnoreCase(KEY_VENDOR)) {
									vendorStringBaseBoard = cv;

								} else if (opsi.equalsIgnoreCase(KEY_PRODUCT)) {
									productString = cv;

								}
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
			Iterator<String> iter = deviceInfo.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				String[] row = { key, (String) deviceInfo.get(key) };
				data.add(row);
				Logging.debug(this, "hwClass row  " + hwClass + ": " + Arrays.toString(row));
			}
		}

		return data;
	}

	private void setNode(IconNode node) {
		tableModel.setData(getDataForNode(node));
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
		}
		setNode(node);

	}

	public void setHardwareConfig(List<Map<String, List<Map<String, Object>>>> hwConfig) {
		this.hwConfig = hwConfig;
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

	public void setHardwareInfo(Map<String, List<Map<String, Object>>> hwInfo, String treeRootTitle) {
		initByAuditStrings();
		panelByAuditInfo.emptyByAuditStrings();

		this.hwInfo = hwInfo;
		this.treeRootTitle = treeRootTitle;

		if (hwInfo == null) {
			createRoot(treeRootTitle);
			tableModel.setData(new ArrayList<>());
			return;
		}

		List<Map<String, Object>> hwInfoSpecial = hwInfo.get(SCANPROPERTYNAME);
		String rootname = "";

		if (hwInfoSpecial != null && !hwInfoSpecial.isEmpty() && hwInfoSpecial.get(0) != null
				&& hwInfoSpecial.get(0).get(SCANTIME) != null) {
			rootname = "Scan " + (String) hwInfoSpecial.get(0).get(SCANTIME);
		}
		title = rootname;

		createRoot(rootname);
		tableModel.setData(new ArrayList<>());

		if (hwConfig == null) {
			Logging.info("hwConfig null");
			return;
		}

		hwClassMapping = new HashMap<>();
		String[] hwClassesUI = new String[hwConfig.size()];
		for (int i = 0; i < hwConfig.size(); i++) {
			Map<String, List<Map<String, Object>>> whc = hwConfig.get(i);
			hwClassesUI[i] = (String) whc.get("Class").get(0).get("UI");
			hwClassMapping.put(hwClassesUI[i], whc.get("Class").get(0).get("Opsi"));
		}

		Arrays.sort(hwClassesUI);

		for (int i = 0; i < hwClassesUI.length; i++) {
			// get next key - value - pair
			String hwClassUI = hwClassesUI[i];
			String hwClass = (String) hwClassMapping.get(hwClassUI);

			List<Map<String, Object>> devices = hwInfo.get(hwClass);
			if (devices == null) {
				Logging.debug(this, "No devices of hwclass " + hwClass + " found");
				continue;
			}

			IconNode classNode = new IconNode(encodeString(hwClassUI));
			Icon classIcon;
			classIcon = createImageIcon("hwinfo_images/" + hwClass + ".png");
			if (classIcon == null) {
				classIcon = createImageIcon("hwinfo_images/DEVICE.png");
			}

			classNode.setClosedIcon(classIcon);
			classNode.setLeafIcon(classIcon);
			classNode.setOpenIcon(classIcon);
			root.add(classNode);

			Map<String, List<Map<String, Object>>> displayNames = new HashMap<>();

			for (int j = 0; j < devices.size(); j++) {
				Map<String, Object> deviceInfo = devices.get(j);
				String displayName = (String) deviceInfo.get("name");
				if (displayName == null || displayName.isEmpty()) {
					displayName = hwClass + "_" + j;
				}

				if (!displayNames.containsKey(displayName)) {
					displayNames.put(displayName, new ArrayList<>());
				}
				displayNames.get(displayName).add(devices.get(j));
			}

			int num = 0;
			String[] names = new String[devices.size()];
			Iterator<String> iter = displayNames.keySet().iterator();
			while (iter.hasNext()) {
				String displayName = iter.next();
				List<Map<String, Object>> devs = displayNames.get(displayName);

				for (int j = 0; j < devs.size(); j++) {
					Map<String, Object> dev = devs.get(j);
					String dn = displayName;
					if (devs.size() > 1) {
						dn += " (" + j + ")";
					}

					dev.put("displayName", dn);
					names[num] = dn;
					num++;
				}
			}

			Arrays.sort(names);

			for (int j = 0; j < names.length; j++) {
				for (int k = 0; k < devices.size(); k++) {
					if (names[j].equals(devices.get(k).get("displayName"))) {
						IconNode iconNode = new IconNode(encodeString((String) devices.get(k).get("displayName")));
						iconNode.setClosedIcon(classIcon);
						iconNode.setLeafIcon(classIcon);
						iconNode.setOpenIcon(classIcon);
						iconNode.setDeviceInfo(devices.get(k));
						classNode.add(iconNode);
						scanNodes(iconNode);
						break;
					}
				}
			}
		}

		treeModel.nodeChanged(root);
		tree.expandRow(0);
		tree.expandRow(1);

	}

	private static class HWInfoTableModel extends AbstractTableModel {
		private List<String[]> data;
		private final String[] header = { "Name", "Wert" };

		public HWInfoTableModel() {
			super();
			data = new ArrayList<>();
		}

		public void setData(List<String[]> data) {
			this.data = data;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			return header[column];
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data.get(row)[col];
		}
	}

	private void getLocalizedHashMap() {

		hwOpsiToUI = new HashMap<>();

		for (Map<String, List<Map<String, Object>>> hardwareMap : hwConfig) {
			List<Map<String, Object>> values = hardwareMap.get("Values");
			for (int j = 0; j < values.size(); j++) {
				Map<String, Object> valuesMap = values.get(j);
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
		// TODO
		DefaultTableModel tableModelComplete = new DefaultTableModel();
		JTable jTableComplete = new JTable(tableModelComplete);

		List<String> childValues;

		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_hardware"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_device"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_name"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_value"));

		for (int i = 0; i < treeModel.getChildCount(treeModel.getRoot()); i++) {
			Object child = treeModel.getChild(treeModel.getRoot(), i);
			// get ArrayList
			List<Map<String, Object>> al = hwInfo.get(hwClassMapping.get(child.toString()));
			Iterator<Map<String, Object>> alIterator = al.iterator();

			boolean first = true;
			while (alIterator.hasNext()) {
				Map<String, Object> hm = alIterator.next();
				if (first) {
					// second column, first element

					childValues = new ArrayList<>();

					// first column
					childValues.add(child.toString());
					childValues.add(hm.get("displayName").toString());
					Iterator<String> hmIterator = hm.keySet().iterator();
					boolean firstValue = true;
					while (hmIterator.hasNext()) {
						String hmKey = hmIterator.next();
						if (!"displayName".equals(hmKey) && !"type".equals(hmKey)) {
							if (firstValue) {
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey).toString());
								firstValue = false;
							} else {
								childValues = new ArrayList<>();
								childValues.add("");
								childValues.add("");
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey).toString());
							}
							tableModelComplete.addRow(childValues.toArray());
						}
					}

					first = false;
				} else {
					childValues = new ArrayList<>();

					// first column empty
					childValues.add("");
					childValues.add(hm.get("displayName").toString());
					Iterator<String> hmIterator = hm.keySet().iterator();
					boolean firstValue = true;
					while (hmIterator.hasNext()) {
						String hmKey = hmIterator.next();
						if (!"displayName".equals(hmKey) && !"type".equals(hmKey)) {
							if (firstValue) {
								firstValue = false;
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey).toString());
							} else {
								childValues = new ArrayList<>();
								childValues.add("");
								childValues.add("");
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey).toString());
							}
							tableModelComplete.addRow(childValues.toArray());
						}
					}
				}
			}
		}
		return jTableComplete;
	}
}
