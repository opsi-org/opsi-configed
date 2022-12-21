package de.uib.configed.gui.hwinfopage;

import java.awt.Dimension;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.tree.IconNode;
import de.uib.configed.tree.IconNodeRenderer;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
import de.uib.utilities.tree.XTree;

public class PanelHWInfo extends JPanel implements TreeSelectionListener {
	public static final String class_COMPUTER_SYSTEM = "COMPUTER_SYSTEM";
	public static final String class_BASE_BOARD = "BASE_BOARD";

	public static final ArrayList<String> hwClassesForByAudit = new ArrayList<String>();
	static {
		hwClassesForByAudit.add(class_COMPUTER_SYSTEM);
		hwClassesForByAudit.add(class_BASE_BOARD);
	}

	public static final String key_VENDOR = "vendor";
	public static final String key_MODEL = "model";
	public static final String key_PRODUCT = "product";

	protected Map hwInfo;
	protected String treeRootTitle;
	protected java.util.List hwConfig;
	protected String title = "HW Information";

	// for creating pdf
	private Map<String, String> hwOpsiToUI;
	// protected ConfigedMain mainController;

	protected JSplitPane contentPane;
	protected JScrollPane jScrollPaneTree;
	protected JScrollPane jScrollPaneInfo;
	protected XTree tree;
	protected IconNode root;
	protected TreePath rootPath;
	protected DefaultTreeModel treeModel;
	protected JTable table;
	protected HWInfoTableModel tableModel;
	protected HashMap hwClassMapping;

	protected static final String SCANPROPERTYNAME = "SCANPROPERTIES";
	protected static final String SCANTIME = "scantime";

	protected String vendorStringCOMPUTER_SYSTEM;
	protected String vendorStringBASE_BOARD;
	protected String modelString;
	protected String productString;

	private PanelHWByAuditDriver panelByAuditInfo;

	protected PopupMenuTrait popupMenu;

	protected int hGap = Globals.HGAP_SIZE / 2;
	protected int vGap = Globals.VGAP_SIZE / 2;
	protected int hLabel = Globals.BUTTON_HEIGHT;

	protected IconNode selectedNode;

	protected boolean withPopup;

	private final ArrayList EMPTY = new ArrayList();

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
		/*
		 * logging.info(this, "encode: to encode " + s);
		 * String result;
		 * //result = configed.encodeStringFromService(s);
		 * result = s;
		 * logging.info(this, "encode: encoded " + result);
		 * return result;
		 */
	}

	protected void buildPanel() {
		// setLayout(new BorderLayout());

		// JPanel panelByAuditInfo = new PanelLinedComponents(compis);
		panelByAuditInfo = new PanelHWByAuditDriver(title, main);
		// = new JPanel();

		tree = new XTree(null);

		// createRoot("");

		jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// jScrollPaneTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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

		// embed.setBackground(Color.red);
		jScrollPaneInfo = new JScrollPane(embed);
		jScrollPaneInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// jScrollPaneInfo.getViewport().setBackground(Globals.nimbusBackground);
		// jScrollPaneInfo.setMinimumSize( new Dimension(200, 200) );
		// jScrollPaneInfo.setPREFERRED_SIZE( new Dimension(400, 200) );

		contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPaneTree, jScrollPaneInfo);
		// contentPane.setBackground(Color.yellow);

		// add (contentPane);

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
		// .addGap(vGap, vGap, vGap)
		);

		if (withPopup) {

			popupMenu = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF,
					PopupMenuTrait.POPUP_FLOATINGCOPY })

			{
				public void action(int p) {
					switch (p) {
					case PopupMenuTrait.POPUP_RELOAD:
						reload();
						break;

					case PopupMenuTrait.POPUP_FLOATINGCOPY:
						floatExternal();
						break;
					case PopupMenuTrait.POPUP_PDF:
						logging.info(this, "------------- create report");
						// TODO letzter scan, Auswahl für den ByAudit-Treiberpfad???
						HashMap<String, String> metaData = new HashMap<String, String>();
						metaData.put("header", configed.getResourceValue("PanelHWInfo.createPDF.title"));
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

						/*
						 * old pdf exporting
						 * tableToPDF = new DocumentToPdf (null, metaData); // no filename, metadata
						 * 
						 * tableToPDF.createContentElement("table", CreateHWInfoTableModelComplete());
						 * 
						 * tableToPDF.setPageSizeA4_Landscape(); //
						 * tableToPDF.toPDF(); //
						 **/
						break;
					}
				}
			};

			popupMenu.addPopupListenersTo(new JComponent[] { tree, table });
		}

		// setBackground(Color.green);

	}

	public void setTitle(String s) {
		title = s;
		panelByAuditInfo.setTitle(s);

	}

	/** overwrite in subclasses */
	protected void reload() {
		logging.debug(this, "reload action");
	}

	protected void floatExternal() {
		PanelHWInfo copyOfMe;
		de.uib.configed.gui.GeneralFrame externalView;

		copyOfMe = new PanelHWInfo(false, main);
		copyOfMe.setHardwareConfig(hwConfig);
		copyOfMe.setHardwareInfo(hwInfo, treeRootTitle);

		// copyOfMe.setNode(selectedNode);

		copyOfMe.expandRows(tree.getToggledRows(rootPath));
		copyOfMe.setSelectedRow(tree.getMinSelectionRow());

		externalView = new de.uib.configed.gui.GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.centerOn(Globals.mainFrame);

		externalView.setVisible(true);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	private ImageIcon createImageIcon(String path) {
		return Globals.createImageIcon(path, "");
		/*
		 * java.net.URL imgURL = getClass().getResource(path);
		 * if (imgURL != null)
		 * {
		 * return new ImageIcon(imgURL, null);
		 * }
		 * else
		 * {
		 * logging.debug("Couldn't find file: " + path);
		 * return null;
		 * }
		 */
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

	private void expandRows(Vector<Integer> rows) {
		tree.expandRows(rows);
	}

	private void setSelectedRow(int row) {
		tree.setSelectionInterval(row, row);
	}

	private ArrayList getDataForNode(IconNode node) {
		return getDataForNode(node, false);
	}

	private ArrayList getDataForNode(IconNode node, boolean reduceScanToByAuditClasses) {

		if (node == null || !node.isLeaf())
			return EMPTY;

		TreeNode[] path = node.getPath();
		if (path.length < 3) {
			return EMPTY;
		}

		String hwClassUI = path[1].toString();
		String device = path[2].toString();
		String hwClass = (String) hwClassMapping.get(hwClassUI);

		if (hwClass != null && reduceScanToByAuditClasses && !hwClassesForByAudit.contains(hwClass))
			return null;

		ArrayList devices = (ArrayList) hwInfo.get(hwClass);
		Map deviceInfo = node.getDeviceInfo();
		if ((devices == null) || (deviceInfo == null)) {
			return EMPTY;
		}

		// logging.debug(this, "Selected node: " + device + " " + deviceInfo);

		java.util.List values = null;

		for (int j = 0; j < hwConfig.size(); j++) {
			try {
				Map whc = (Map) hwConfig.get(j);
				if (((String) ((Map) whc.get("Class")).get("Opsi")).equals(hwClass)) {
					values = (java.util.List) whc.get("Values");
					break;
				}
			} catch (NullPointerException ex) {
			}
		}
		ArrayList data = new ArrayList();
		if (values != null) {
			for (int j = 0; j < values.size(); j++) {
				Map v = (Map) values.get(j);
				String opsi = (String) v.get("Opsi");
				logging.debug(this, "opsi " + opsi);
				String ui = encodeString((String) v.get("UI")); // table row keys //no encoding needed
				String unit = null;
				if (v.containsKey("Unit")) {
					unit = (String) v.get("Unit");
					logging.debug(this, "unit  " + unit);
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
							logging.debug(this, "key " + opsi);

							if (hwClass.equals(class_COMPUTER_SYSTEM)) {
								if (opsi.equalsIgnoreCase(key_VENDOR)) {
									vendorStringCOMPUTER_SYSTEM = cv;
									// logging.info(this, "vendorString " + cv);
								} else if (opsi.equalsIgnoreCase(key_MODEL)) {
									modelString = cv;
									// logging.info(this, "modelString " + cv);
								}
							} else if (hwClass.equals(class_BASE_BOARD)) {
								if (opsi.equalsIgnoreCase(key_VENDOR)) {
									vendorStringBASE_BOARD = cv;
									// logging.info(this, "vendorString " + cv);
								} else if (opsi.equalsIgnoreCase(key_PRODUCT)) {
									productString = cv;
									// logging.info(this, "productString " + cv);
								}
							}
						}

						// cv = encodeString (cv); no encoding needed

						if (unit != null) {
							cv = addUnit(cv, unit);
						}
						String[] row = { ui, cv };
						data.add(row);
						logging.debug(this, "hwClass row  version 1 " + hwClass + ": " + Arrays.toString(row));
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
				logging.debug(this, "hwClass row  " + hwClass + ": " + Arrays.toString(row));
			}
		}

		return data;
	}

	private void setNode(IconNode node) {
		selectedNode = node;
		tableModel.setData(getDataForNode(node));
	}

	public void valueChanged(TreeSelectionEvent e) {
		// Returns the last path element of the selection.
		IconNode node = (IconNode) tree.getLastSelectedPathComponent();
		if (node == null)
			return;

		TreePath selectedPath = tree.getSelectionPath();
		logging.debug(this, "selectedPath " + selectedPath);
		int selectedRow = tree.getRowForPath(selectedPath);
		if (!node.isLeaf()) {
			tree.expandPath(selectedPath);
		}
		setNode(node);

	}

	public void setHardwareConfig(java.util.List hwConfig) {
		this.hwConfig = hwConfig;
	}

	private void scanNodes(IconNode node) {
		if (node != null && node.isLeaf()) {
			TreeNode[] path = node.getPath();
			if (path.length < 3) {
				tableModel.setData(new ArrayList());
				return;
			}
			String hwClassUI = path[1].toString();
			String device = path[2].toString();
			String hwClass = (String) hwClassMapping.get(hwClassUI);

			// logging.debug(this, "scanNode hwClass " + hwClass);
			if (hwClass != null && (hwClass.equals(class_COMPUTER_SYSTEM) || hwClass.equals(class_BASE_BOARD))) {
				logging.debug(this, "scanNode found  class_COMPUTER_SYSTEM or class_BASE_BOARD");
				getDataForNode(node, true);

				panelByAuditInfo.setByAuditFields(vendorStringCOMPUTER_SYSTEM, vendorStringBASE_BOARD, modelString,
						productString);
			}

		}
	}

	protected void initByAuditStrings() {
		vendorStringCOMPUTER_SYSTEM = "";
		vendorStringBASE_BOARD = "";
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

		java.util.List hwInfo_special = (java.util.List) hwInfo.get(SCANPROPERTYNAME);
		String rootname = "";

		if (hwInfo_special != null && hwInfo_special.size() > 0) {
			if (hwInfo_special.get(0) != null && ((Map) hwInfo_special.get(0)).get(SCANTIME) != null) {
				rootname = "Scan " + (String) ((Map) hwInfo_special.get(0)).get(SCANTIME);
			}
		}
		title = rootname;

		createRoot(rootname);
		tableModel.setData(new ArrayList());

		{
			logging.info("hwConfig null");
			return;
		}

	}

	private class HWInfoTableModel extends AbstractTableModel {
		private ArrayList data;
		private final String[] header = { "Name", "Wert" };

		public HWInfoTableModel() {
			super();
			data = new ArrayList();
		}

		public void setData(ArrayList data) {
			this.data = data;
			fireTableDataChanged();
		}

		public int getRowCount() {
			return data.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int column) {
			return header[column];
		}

		public Object getValueAt(int row, int col) {
			return ((String[]) data.get(row))[col];
		}
	}

	private void getLocalizedHashMap() {
		String loc = "";

		hwOpsiToUI = new HashMap<String, String>();

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

		Vector childValues;

		tableModelComplete.addColumn(configed.getResourceValue("PanelHWInfo.createPDF.column_hardware"));
		tableModelComplete.addColumn(configed.getResourceValue("PanelHWInfo.createPDF.column_device"));
		tableModelComplete.addColumn(configed.getResourceValue("PanelHWInfo.createPDF.column_name"));
		tableModelComplete.addColumn(configed.getResourceValue("PanelHWInfo.createPDF.column_value"));

		for (int i = 0; i < treeModel.getChildCount(treeModel.getRoot()); i++) {
			Object child = treeModel.getChild(treeModel.getRoot(), i);
			// get ArrayList
			ArrayList al = (ArrayList) hwInfo.get(hwClassMapping.get(child.toString()));
			Iterator<HashMap> al_itr = al.iterator();

			boolean first = true;
			while (al_itr.hasNext()) {
				HashMap hm = al_itr.next();
				if (first) { // second column, first element
					childValues = new Vector();
					childValues.add(child.toString()); // first column
					childValues.add(hm.get("displayName").toString());
					Iterator hm_iter = (Iterator) hm.keySet().iterator();
					boolean firstValue = true;
					while (hm_iter.hasNext()) {
						String hm_key = (String) hm_iter.next();
						if (!hm_key.equals("displayName") && !hm_key.equals("type")) { //
							if (firstValue) {
								childValues.add(hwOpsiToUI.get(hm_key));
								childValues.add(hm.get(hm_key));
								firstValue = false;
							} else {
								childValues = new Vector();
								childValues.add("");
								childValues.add("");
								childValues.add(hwOpsiToUI.get(hm_key));
								childValues.add(hm.get(hm_key));
							}
							tableModelComplete.addRow(childValues);
						}
					}

					first = false;
				} else { // new row, first cell empty
					childValues = new Vector();
					childValues.add(""); // first column empty
					childValues.add(hm.get("displayName").toString());
					Iterator hm_iter = (Iterator) hm.keySet().iterator();
					boolean firstValue = true;
					while (hm_iter.hasNext()) {
						String hm_key = (String) hm_iter.next();
						if (!hm_key.equals("displayName") && !hm_key.equals("type")) {
							if (firstValue) {
								firstValue = false;
								childValues.add(hwOpsiToUI.get(hm_key));
								childValues.add(hm.get(hm_key));
							} else {
								childValues = new Vector();
								childValues.add("");
								childValues.add("");
								childValues.add(hwOpsiToUI.get(hm_key));
								childValues.add(hm.get(hm_key));
							}
							tableModelComplete.addRow(childValues);
						}
					}

				}
			}
		}
		return jTableComplete;

	}

}
