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

	protected Map hwInfo;
	protected String treeRootTitle;
	protected List hwConfig;
	protected String title = "HW Information";

	// for creating pdf
	private Map<String, String> hwOpsiToUI;

	protected JSplitPane contentPane;
	protected JScrollPane jScrollPaneTree;
	protected JScrollPane jScrollPaneInfo;
	protected XTree tree;
	protected IconNode root;
	protected TreePath rootPath;
	protected DefaultTreeModel treeModel;
	protected JTable table;
	protected HWInfoTableModel tableModel;
	protected Map hwClassMapping;

	protected static final String SCANPROPERTYNAME = "SCANPROPERTIES";
	protected static final String SCANTIME = "scantime";

	protected String vendorStringComputerSystem;
	protected String vendorStringBaseBoard;
	protected String modelString;
	protected String productString;

	private PanelHWByAuditDriver panelByAuditInfo;

	protected PopupMenuTrait popupMenu;

	protected int hGap = Globals.HGAP_SIZE / 2;
	protected int vGap = Globals.VGAP_SIZE / 2;
	protected int hLabel = Globals.BUTTON_HEIGHT;

	protected IconNode selectedNode;

	protected boolean withPopup;

	private final List EMPTY = new ArrayList<>();

	ConfigedMain main;

	public PanelHWInfo(ConfigedMain main) {
		this(true, main);
	}

	public PanelHWInfo(boolean withPopup, ConfigedMain main) {
		this.withPopup = withPopup;
		this.main = main;
		buildPanel();
	}

	private String encodeString(String s) {
		return s;

	}

	protected void buildPanel() {

		panelByAuditInfo = new PanelHWByAuditDriver(title, main);

		tree = new XTree(null);

		jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		jScrollPaneTree.setMinimumSize(new Dimension(200, 200));
		jScrollPaneTree.setPreferredSize(new Dimension(400, 200));

		tableModel = new HWInfoTableModel();
		table = new JTable(tableModel, null);
		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.setTableHeader(null);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(300);

		table.setDragEnabled(true);
		table.setBackground(Globals.nimbusBackground);
		JPanel embed = new JPanel();
		GroupLayout layoutEmbed = new GroupLayout(embed);
		embed.setLayout(layoutEmbed);

		layoutEmbed.setHorizontalGroup(layoutEmbed
				.createSequentialGroup().addGap(hGap, hGap, hGap).addComponent(table,
						javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(hGap, hGap, hGap));

		layoutEmbed.setVerticalGroup(layoutEmbed
				.createSequentialGroup().addGap(vGap, vGap, vGap).addComponent(table,
						javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(vGap, vGap, vGap));

		jScrollPaneInfo = new JScrollPane(embed);
		jScrollPaneInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPaneTree, jScrollPaneInfo);

		GroupLayout layoutBase = new GroupLayout(this);
		setLayout(layoutBase);

		layoutBase.setHorizontalGroup(layoutBase.createSequentialGroup().addGap(hGap, hGap, hGap)
				.addGroup(layoutBase.createParallelGroup()
						.addGroup(layoutBase.createSequentialGroup().addGap(hGap - 2, hGap - 2, hGap - 2)
								.addComponent(panelByAuditInfo, 30, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addGap(hGap - 2, hGap - 2, hGap - 2)

						).addComponent(contentPane, 100, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGap(hGap, hGap, hGap));

		layoutBase.setVerticalGroup(layoutBase.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addComponent(panelByAuditInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(vGap / 2, vGap / 2, vGap / 2).addComponent(contentPane, javax.swing.GroupLayout.PREFERRED_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

		);

		if (withPopup) {

			popupMenu = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF,
					PopupMenuTrait.POPUP_FLOATINGCOPY })

			{
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
						Logging.info(this, "------------- create report");
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

						ExporterToPDF pdfExportTable = new ExporterToPDF(CreateHWInfoTableModelComplete());
						pdfExportTable.setMetaData(metaData);
						pdfExportTable.setPageSizeA4_Landscape();
						pdfExportTable.execute(null, false); // create pdf // no filename, onlyselectedRows=false

						break;
					}
				}
			};

			popupMenu.addPopupListenersTo(new JComponent[] { tree, table });
		}

	}

	public void setTitle(String s) {
		title = s;
		panelByAuditInfo.setTitle(s);

	}

	/** overwrite in subclasses */
	protected void reload() {
		Logging.debug(this, "reload action");
	}

	protected void floatExternal() {
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
		externalView.setLocationRelativeTo(Globals.mainFrame);

		externalView.setVisible(true);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	private ImageIcon createImageIcon(String path) {
		return Globals.createImageIcon(path, "");

	}

	protected void createRoot(String name) {
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

	protected String addUnit(String value, String unit) {
		if (value.equals(""))
			return value;

		BigInteger v = null;

		try {
			v = new BigInteger(value);
		} catch (Exception e) {
			return value + " " + unit;
		}

		int mult = 1000;
		if (unit.toLowerCase().equals("byte")) {
			mult = 1024;
		}
		// TODO: nano, micro
		if (v.compareTo(BigInteger.valueOf(mult * mult * mult)) >= 0) {
			return ((float) Math.round(v.floatValue() * 1000 / (mult * mult * mult)) / 1000) + " G" + unit;
		} else if (v.compareTo(BigInteger.valueOf(mult * mult)) >= 0) {
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

	private List getDataForNode(IconNode node) {
		return getDataForNode(node, false);
	}

	private List getDataForNode(IconNode node, boolean reduceScanToByAuditClasses) {

		if (node == null || !node.isLeaf())
			return EMPTY;

		TreeNode[] path = node.getPath();
		if (path.length < 3) {
			return EMPTY;
		}

		String hwClassUI = path[1].toString();
		String hwClass = (String) hwClassMapping.get(hwClassUI);

		if (hwClass != null && reduceScanToByAuditClasses && !hwClassesForByAudit.contains(hwClass))
			return null;

		List devices = (List) hwInfo.get(hwClass);
		Map deviceInfo = node.getDeviceInfo();
		if ((devices == null) || (deviceInfo == null)) {
			return EMPTY;
		}

		List values = null;

		for (int j = 0; j < hwConfig.size(); j++) {
			try {
				Map whc = (Map) hwConfig.get(j);
				if (((Map) whc.get("Class")).get("Opsi").equals(hwClass)) {
					values = (List) whc.get("Values");
					break;
				}
			} catch (NullPointerException ex) {
			}
		}
		List data = new ArrayList<>();
		if (values != null) {
			for (int j = 0; j < values.size(); j++) {
				Map v = (Map) values.get(j);
				String opsi = (String) v.get("Opsi");
				Logging.debug(this, "opsi " + opsi);
				String ui = encodeString((String) v.get("UI")); // table row keys //no encoding needed
				String unit = null;
				if (v.containsKey("Unit")) {
					unit = (String) v.get("Unit");
					Logging.debug(this, "unit  " + unit);
				}
				Iterator iter = deviceInfo.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					if (key.equalsIgnoreCase(opsi)) {
						String cv = "";
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
			Iterator iter = deviceInfo.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				String[] row = { key, (String) deviceInfo.get(key) };
				data.add(row);
				Logging.debug(this, "hwClass row  " + hwClass + ": " + Arrays.toString(row));
			}
		}

		return data;
	}

	private void setNode(IconNode node) {
		selectedNode = node;
		tableModel.setData(getDataForNode(node));
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// Returns the last path element of the selection.
		IconNode node = (IconNode) tree.getLastSelectedPathComponent();
		if (node == null)
			return;

		TreePath selectedPath = tree.getSelectionPath();
		Logging.debug(this, "selectedPath " + selectedPath);
		if (!node.isLeaf()) {
			tree.expandPath(selectedPath);
		}
		setNode(node);

	}

	public void setHardwareConfig(List hwConfig) {
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

	protected void initByAuditStrings() {
		vendorStringComputerSystem = "";
		vendorStringBaseBoard = "";
		modelString = "";
		productString = "";
	}

	public void setHardwareInfo(Map hwInfo, String treeRootTitle) {
		initByAuditStrings();
		panelByAuditInfo.emptyByAuditStrings();

		this.hwInfo = hwInfo;
		this.treeRootTitle = treeRootTitle;

		if (hwInfo == null) {
			createRoot(treeRootTitle);
			tableModel.setData(EMPTY);
			return;
		}

		List hwInfoSpecial = (List) hwInfo.get(SCANPROPERTYNAME);
		String rootname = "";

		if (hwInfoSpecial != null && !hwInfoSpecial.isEmpty() && hwInfoSpecial.get(0) != null
				&& ((Map) hwInfoSpecial.get(0)).get(SCANTIME) != null) {
			rootname = "Scan " + (String) ((Map) hwInfoSpecial.get(0)).get(SCANTIME);
		}
		title = rootname;

		createRoot(rootname);
		tableModel.setData(new ArrayList<>());

		if (hwConfig == null) {
			Logging.info("hwConfig null");
			return;
		}

		if (hwInfo == null) {
			Logging.info("hwInfo null");
			return;
		}

		hwClassMapping = new HashMap<>();
		String[] hwClassesUI = new String[hwConfig.size()];
		for (int i = 0; i < hwConfig.size(); i++) {
			Map whc = (Map) hwConfig.get(i);
			hwClassesUI[i] = (String) ((Map) whc.get("Class")).get("UI");
			hwClassMapping.put(hwClassesUI[i], (String) ((Map) whc.get("Class")).get("Opsi"));

		}

		java.util.Arrays.sort(hwClassesUI);

		for (int i = 0; i < hwClassesUI.length; i++) {
			// get next key - value - pair
			String hwClassUI = hwClassesUI[i];
			String hwClass = (String) hwClassMapping.get(hwClassUI);

			List devices = (List) hwInfo.get(hwClass);
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

			Map displayNames = new HashMap<>();

			for (int j = 0; j < devices.size(); j++) {
				Map deviceInfo = (Map) devices.get(j);
				String displayName = (String) deviceInfo.get("name");
				if ((displayName == null) || displayName.equals("")) {
					displayName = hwClass + "_" + j;
				}

				if (!displayNames.containsKey(displayName)) {
					displayNames.put(displayName, new ArrayList<>());
				}
				((List) displayNames.get(displayName)).add(devices.get(j));
			}

			int num = 0;
			String[] names = new String[devices.size()];
			Iterator iter = displayNames.keySet().iterator();
			while (iter.hasNext()) {
				String displayName = (String) iter.next();
				List devs = (List) displayNames.get(displayName);

				for (int j = 0; j < devs.size(); j++) {
					Map dev = (Map) devs.get(j);
					String dn = displayName;
					if (devs.size() > 1)
						dn += " (" + j + ")";

					dev.put("displayName", dn);
					names[num] = dn;
					num++;
				}
			}

			java.util.Arrays.sort(names);

			for (int j = 0; j < names.length; j++) {
				for (int k = 0; k < devices.size(); k++) {
					if (names[j].equals(((Map) devices.get(k)).get("displayName"))) {
						IconNode iconNode = new IconNode(
								encodeString((String) ((Map) devices.get(k)).get("displayName")));
						iconNode.setClosedIcon(classIcon);
						iconNode.setLeafIcon(classIcon);
						iconNode.setOpenIcon(classIcon);
						iconNode.setDeviceInfo((Map) devices.get(k));
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

	private class HWInfoTableModel extends AbstractTableModel {
		private List data;
		private final String[] header = { "Name", "Wert" };

		public HWInfoTableModel() {
			super();
			data = new ArrayList<>();
		}

		public void setData(List data) {
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
			return ((String[]) data.get(row))[col];
		}
	}

	private void getLocalizedHashMap() {

		hwOpsiToUI = new HashMap<>();

		for (Object obj : hwConfig) {
			Map hardwareMap = (Map) obj;
			List values = (List) hardwareMap.get("Values");
			for (int j = 0; j < values.size(); j++) {
				Map valuesMap = (Map) values.get(j);
				String type = (String) valuesMap.get("Opsi");
				String name = (String) valuesMap.get("UI");
				if (!hwOpsiToUI.containsKey(type))
					hwOpsiToUI.put(type, name);
			}
		}
		for (Object obj : hwConfig) {
			Map hardwareMap = (Map) obj;
			String hardwareName = (String) ((Map) hardwareMap.get("Class")).get("UI");
			String hardwareOpsi = (String) ((Map) hardwareMap.get("Class")).get("Opsi");
			if (!hwOpsiToUI.containsKey(hardwareOpsi))
				hwOpsiToUI.put(hardwareOpsi, hardwareName);
		}

	}

	private JTable CreateHWInfoTableModelComplete() {
		getLocalizedHashMap();
		// TODO
		DefaultTableModel tableModelComplete = new DefaultTableModel();
		JTable jTableComplete = new JTable(tableModelComplete);

		List childValues;

		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_hardware"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_device"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_name"));
		tableModelComplete.addColumn(Configed.getResourceValue("PanelHWInfo.createPDF.column_value"));

		for (int i = 0; i < treeModel.getChildCount(treeModel.getRoot()); i++) {
			Object child = treeModel.getChild(treeModel.getRoot(), i);
			// get ArrayList
			List al = (List) hwInfo.get(hwClassMapping.get(child.toString()));
			Iterator<Map> alIterator = al.iterator();

			boolean first = true;
			while (alIterator.hasNext()) {
				Map hm = alIterator.next();
				if (first) { // second column, first element
					childValues = new ArrayList<>();
					childValues.add(child.toString()); // first column
					childValues.add(hm.get("displayName").toString());
					Iterator hmIterator = hm.keySet().iterator();
					boolean firstValue = true;
					while (hmIterator.hasNext()) {
						String hmKey = (String) hmIterator.next();
						if (!hmKey.equals("displayName") && !hmKey.equals("type")) { //
							if (firstValue) {
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey));
								firstValue = false;
							} else {
								childValues = new ArrayList<>();
								childValues.add("");
								childValues.add("");
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey));
							}
							tableModelComplete.addRow(childValues.toArray());
						}
					}

					first = false;
				} else {
					childValues = new ArrayList<>();
					childValues.add(""); // first column empty
					childValues.add(hm.get("displayName").toString());
					Iterator hmIterator = hm.keySet().iterator();
					boolean firstValue = true;
					while (hmIterator.hasNext()) {
						String hmKey = (String) hmIterator.next();
						if (!hmKey.equals("displayName") && !hmKey.equals("type")) {
							if (firstValue) {
								firstValue = false;
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey));
							} else {
								childValues = new ArrayList<>();
								childValues.add("");
								childValues.add("");
								childValues.add(hwOpsiToUI.get(hmKey));
								childValues.add(hm.get(hmKey));
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
