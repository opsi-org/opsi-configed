/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRendererByIndex;
import de.uib.configed.guidata.IFInstallationStateTableModel;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.guidata.InstallationStateTableModelFiltered;
import de.uib.configed.productgroup.ProductgroupPanel;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.productstate.ActionProgress;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.ActionSequence;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.productstate.TargetConfiguration;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.IntComparatorForStrings;
import de.uib.utilities.datapanel.DefaultEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.list.StandardListCellRenderer;
import de.uib.utilities.table.AbstractExportTable;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.ListCellOptions;
import de.uib.utilities.table.gui.AdaptingCellEditorValuesByIndex;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.DynamicCellEditor;
import de.uib.utilities.table.gui.StandardTableCellRenderer;
import utils.PopupMouseListener;
import utils.Utils;

public class PanelProductSettings extends JSplitPane implements RowSorterListener {

	private static final int HEIGHT_MIN = 200;

	private static final int FRAME_WIDTH_LEFTHANDED = 1100;
	private static final int FRAME_HEIGHT = 490;

	private static final int WIDTH_COLUMN_PRODUCT_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_COMPLETE_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_STATE = 60;

	private static final int WIDTH_COLUMN_PRODUCT_SEQUENCE = 40;
	private static final int WIDTH_COLUMN_VERSION_INFO = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_PRODUCT_VERSION = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_PACKAGE_VERSION = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_INSTALLATION_INFO = WIDTH_COLUMN_PRODUCT_STATE;

	protected JTable tableProducts;
	private AbstractExportTable exportTable;
	protected JPanel topPane;

	// right pane
	private ProductInfoPane infoPane;
	private DefaultEditMapPanel propertiesPanel;

	private ListCellRenderer<Object> standardListCellRenderer;

	private TableCellRenderer productNameTableCellRenderer;
	private TableCellRenderer productCompleteNameTableCellRenderer;

	private TableCellRenderer targetConfigurationTableCellRenderer;
	private TableCellRenderer installationStatusTableCellRenderer;
	private TableCellRenderer actionProgressTableCellRenderer;
	private TableCellRenderer lastActionTableCellRenderer;
	private TableCellRenderer actionResultTableCellRenderer;
	private TableCellRenderer actionRequestTableCellRenderer;
	private ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	private ColoredTableCellRenderer productsequenceTableCellRenderer;
	private ColoredTableCellRenderer productversionTableCellRenderer;
	private ColoredTableCellRenderer packageversionTableCellRenderer;

	private ColoredTableCellRenderer versionInfoTableCellRenderer;
	private ColoredTableCellRenderer installationInfoTableCellRenderer;

	private ColoredTableCellRenderer lastStateChangeTableCellRenderer;

	private Map<String, Boolean> productDisplayFields;

	private List<? extends SortKey> currentSortKeys;

	private JPopupMenu popup;
	private JMenuItem itemOnDemand;

	private JMenuItem itemSaveAndExecute;

	private String title;

	// State reducedTo1stSelection
	// List reductionList

	private ProductgroupPanel groupPanel;

	protected ConfigedMain configedMain;

	public PanelProductSettings(String title, ConfigedMain configedMain, Map<String, Boolean> productDisplayFields) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.title = title;
		this.configedMain = configedMain;
		this.productDisplayFields = productDisplayFields;
		init();

		super.setResizeWeight(1);
	}

	protected void initTopPane() {
		if (tableProducts == null) {
			Logging.error(this, " tableProducts == null ");
			Main.endApp(Main.NO_ERROR);
		}
		topPane = new ProductgroupPanel(this, configedMain, tableProducts);
		topPane.setVisible(true);
		groupPanel = (ProductgroupPanel) topPane;
		groupPanel.setReloadActionHandler((ActionEvent ae) -> {
			Logging.info(this, " in top pane we got event reloadAction " + ae);
			reloadAction();
		});

		groupPanel.setSaveAndExecuteActionHandler((ActionEvent ae) -> {
			Logging.info(this, " in top pane we got event saveAndExecuteAction " + ae);
			saveAndExecuteAction();
		});
	}

	protected void init() {
		tableProducts = new JTable() {
			@Override
			public void setValueAt(Object value, int row, int column) {
				List<String> saveSelectedProducts = getSelectedProducts();
				// only in case of setting ActionRequest needed, since we there call
				// fireTableDataChanged
				super.setValueAt(value, row, column);
				setSelection(new HashSet<>(saveSelectedProducts));
			}
		};
		tableProducts.setDragEnabled(true);

		exportTable = new ExporterToCSV(tableProducts);

		initTopPane();

		JScrollPane paneProducts = new JScrollPane();

		paneProducts.getViewport().add(tableProducts);
		paneProducts.setPreferredSize(new Dimension(FRAME_WIDTH_LEFTHANDED, FRAME_HEIGHT));
		paneProducts.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		if (!Main.THEMES) {
			tableProducts.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
		tableProducts.setShowHorizontalLines(true);
		tableProducts.setGridColor(Globals.PANEL_PRODUCT_SETTINGS_TABLE_GRID_COLOR);
		tableProducts.setRowHeight(Globals.TABLE_ROW_HEIGHT);

		tableProducts.getSelectionModel().addListSelectionListener(this::applyChangedValue);

		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		standardListCellRenderer = new StandardListCellRenderer();

		productNameTableCellRenderer = new ProductNameTableCellRenderer("");

		productCompleteNameTableCellRenderer = new StandardTableCellRenderer("");

		targetConfigurationTableCellRenderer = new ColoredTableCellRendererByIndex(
				TargetConfiguration.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_TARGET_CONFIGURATION) + ": ");

		installationStatusTableCellRenderer = new ColoredTableCellRendererByIndex(
				InstallationStatus.getLabel2TextColor(), InstallationStatus.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_STATUS) + ": ");

		actionProgressTableCellRenderer = new ActionProgressTableCellRenderer(ActionProgress.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_PROGRESS) + ": ");

		actionResultTableCellRenderer = new ColoredTableCellRendererByIndex(ActionResult.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_RESULT) + ": ");

		lastActionTableCellRenderer = new ColoredTableCellRendererByIndex(ActionRequest.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_LAST_ACTION) + ": ");

		actionRequestTableCellRenderer = new ColoredTableCellRendererByIndex(ActionRequest.getLabel2TextColor(),
				ActionRequest.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_REQUEST) + ": ");

		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex(ActionSequence.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_PRIORITY) + ": ");

		lastStateChangeTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_LAST_STATE_CHANGE));

		productsequenceTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_POSITION));

		productversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_VERSION));

		packageversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PACKAGE_VERSION));

		versionInfoTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_VERSION_INFO)) {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe since instanceof returns false if null
				if (value instanceof String) {
					String val = (String) value;

					if (val.isEmpty()) {
						return c;
					}

					if (val.equals(InstallationStateTableModel.CONFLICT_STRING)
							|| val.equals(InstallationStateTableModel.UNEQUAL_ADD_STRING
									+ InstallationStateTableModel.CONFLICT_STRING)) {
						if (!Main.THEMES) {
							c.setBackground(Globals.CONFLICT_STATE_CELL_COLOR);
							c.setForeground(Globals.CONFLICT_STATE_CELL_COLOR);
						} else {
							c.setBackground(Color.PINK);
							c.setForeground(Color.PINK);
						}

					} else {

						String productId = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
						IFInstallationStateTableModel istm = (IFInstallationStateTableModel) (table.getModel());

						String serverProductVersion = "";

						if (istm.getGlobalProductInfos().get(productId) == null) {
							Logging.warning(this,
									" istm.getGlobalProductInfos()).get(productId) == null for productId " + productId);
						} else {
							serverProductVersion = serverProductVersion
									+ ((istm.getGlobalProductInfos()).get(productId))
											.get(ProductState.KEY_VERSION_INFO);
						}

						if (!val.equals(serverProductVersion)) {
							c.setForeground(Globals.FAILED_COLOR);
						}
					}
				}

				return c;
			}
		};

		installationInfoTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_INFO)) {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe sind instanceof returns false if null
				if (value instanceof String) {
					String val = (String) value;
					if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.FAILED)))) {
						c.setForeground(Globals.PANEL_PRODUCT_SETTINGS_FAILED_COLOR);
					} else if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.SUCCESSFUL)))) {
						c.setForeground(Globals.OK_COLOR);
					} else {
						// Don't set foreground if no special result
					}
				}

				return c;
			}
		};

		JPanel leftPane = new JPanel();
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(Alignment.LEADING)
				.addComponent(topPane, HEIGHT_MIN, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(paneProducts, HEIGHT_MIN, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup()
				.addComponent(topPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(paneProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		setLeftComponent(leftPane);

		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, true, false);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		((EditMapPanelX) propertiesPanel)
				.setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName()));
		propertiesPanel.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());
		propertiesPanel.setActor(new DefaultEditMapPanel.Actor() {
			@Override
			public void reloadData() {
				super.reloadData();
				Logging.info(this, "we are in PanelProductSettings");
			}

			@Override
			public void saveData() {
				super.saveData();
				Logging.info(this, "we are in PanelProductSettings");
			}

			@Override
			public void deleteData() {
				super.deleteData();
				Logging.info(this, "we are in PanelProductSettings");
			}
		});

		AbstractPanelEditProperties panelEditProperties = new PanelEditClientProperties(configedMain, propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties);

		propertiesPanel.registerDataChangedObserver(infoPane);

		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());

		setRightComponent(infoPane);

		producePopupMenu(productDisplayFields);

		paneProducts.addMouseListener(new PopupMouseListener(popup));
		tableProducts.addMouseListener(new PopupMouseListener(popup));

		activatePacketSelectionHandling(true);
		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public void setGroupsData(final Map<String, Map<String, String>> data,
			final Map<String, Set<String>> productGroupMembers) {
		groupPanel.setGroupsData(data, productGroupMembers);
		showAll();
	}

	private void activatePacketSelectionHandling(boolean b) {
		if (b) {
			tableProducts.getSelectionModel().addListSelectionListener(groupPanel);
		} else {
			tableProducts.getSelectionModel().removeListSelectionListener(groupPanel);
		}
	}

	public void setSearchFields(List<String> fieldList) {
		groupPanel.setSearchFields(fieldList);
	}

	private class ProductNameTableCellRenderer extends StandardTableCellRenderer {

		public ProductNameTableCellRenderer(String tooltipPrefix) {
			super(tooltipPrefix);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			// Will be done if c==null is true since instanceof
			// returns false if null
			if (!(c instanceof JComponent)) {
				return c;
			}

			JComponent jc = (JComponent) c;

			String stateChange = ((IFInstallationStateTableModel) (table.getModel()))
					.getLastStateChange(convertRowIndexToModel(row));

			if (stateChange == null) {
				stateChange = "";
			}

			stateChange = table.getValueAt(row, column).toString() + ", "
					+ Configed.getResourceValue("InstallationStateTableModel.lastStateChange") + ": " + stateChange;

			jc.setToolTipText(stateChange);

			return jc;
		}
	}

	public void initAllProperties() {
		propertiesPanel.init();
		infoPane.setProductInfo("");
		infoPane.setProductAdvice("");
	}

	private void producePopupMenu(final Map<String, Boolean> checkColumns) {
		popup = new JPopupMenu("");

		JMenuItem save = new JMenuItemFormatted();
		save.setText(Configed.getResourceValue("ConfigedMain.saveConfiguration"));
		save.setEnabled(
				!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly());
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		if (!Main.FONT) {
			save.setFont(Globals.DEFAULT_FONT);
		}

		save.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "actionevent on save-menue");
			configedMain.checkSaveAll(false);
			configedMain.requestReloadStatesAndActions();
		});

		popup.add(save);

		itemOnDemand = new JMenuItemFormatted();
		itemOnDemand.setText(Configed.getResourceValue("ConfigedMain.OpsiclientdEvent_on_demand"));
		itemOnDemand.setEnabled(
				!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly());

		if (!Main.FONT) {
			itemOnDemand.setFont(Globals.DEFAULT_FONT);
		}
		itemOnDemand.addActionListener((ActionEvent e) -> configedMain.fireOpsiclientdEventOnSelectedClients(
				OpsiServiceNOMPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND));

		popup.add(itemOnDemand);

		itemSaveAndExecute = new JMenuItemFormatted();
		itemSaveAndExecute.setText(Configed.getResourceValue("ConfigedMain.savePOCAndExecute"));
		itemSaveAndExecute.setEnabled(
				!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly());
		// dies bit get its intended context
		itemSaveAndExecute.setIcon(Utils.createImageIcon("images/executing_command_blue_16.png", ""));
		if (!Main.FONT) {
			itemSaveAndExecute.setFont(Globals.DEFAULT_FONT);
		}
		itemSaveAndExecute.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "actionevent on save and execute menu item");
			saveAndExecuteAction();
		});
		popup.add(itemSaveAndExecute);
		popup.addSeparator();

		showPopupOpsiclientdEvent(true);

		JMenuItem reload = new JMenuItemFormatted();

		// find itscontext
		reload.setText(Configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(Utils.createImageIcon("images/reload16.png", ""));
		if (!Main.FONT) {
			reload.setFont(Globals.DEFAULT_FONT);
		}
		reload.addActionListener((ActionEvent e) -> {
			Logging.info(this, "reload action");
			reloadAction();
		});
		popup.add(reload);

		JMenuItem createReport = new JMenuItemFormatted();
		createReport.setText(Configed.getResourceValue("PanelProductSettings.pdf"));
		createReport.setIcon(Utils.createImageIcon("images/acrobat_reader16.png", ""));
		if (!Main.FONT) {
			createReport.setFont(Globals.DEFAULT_FONT);
		}
		createReport.addActionListener((ActionEvent e) -> createReport());
		popup.add(createReport);

		exportTable.addMenuItemsTo(popup);

		JMenu sub = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		if (!Main.FONT) {
			sub.setFont(Globals.DEFAULT_FONT);
		}
		popup.addSeparator();
		popup.add(sub);

		Iterator<String> iter = checkColumns.keySet().iterator();

		while (iter.hasNext()) {
			final String columnName = iter.next();

			if ("productId".equals(columnName)) {
				// fixed column
				continue;
			}

			JMenuItem item = new JCheckBoxMenuItem();
			item.setText(InstallationStateTableModel.getColumnTitle(columnName));
			if (!Main.FONT) {
				item.setFont(Globals.DEFAULT_FONT);
			}
			((JCheckBoxMenuItem) item).setState(checkColumns.get(columnName));
			item.addItemListener((ItemEvent e) -> {
				boolean oldstate = checkColumns.get(columnName);
				checkColumns.put(columnName, !oldstate);
				configedMain.requestReloadStatesAndActions();
				configedMain.resetView(configedMain.getViewIndex());
			});

			sub.add(item);
		}
	}

	private void createReport() {
		Logging.info(this, "create report");
		HashMap<String, String> metaData = new HashMap<>();

		// display, if filter is active,
		// display selected productgroup
		// depot server, selected clients out of statusPane

		metaData.put("header", title);
		metaData.put("subject", title);
		title = "";
		if (configedMain.getHostsStatusInfo().getInvolvedDepots().length() != 0) {
			title = title + "Depot : " + configedMain.getHostsStatusInfo().getInvolvedDepots();
		}
		if (configedMain.getHostsStatusInfo().getSelectedClientNames().length() != 0) {
			title = title + "; Clients: " + configedMain.getHostsStatusInfo().getSelectedClientNames();
		}
		metaData.put("title", title);
		metaData.put("keywords", "product settings");

		// only relevent rows
		ExporterToPDF pdfExportTable = new ExporterToPDF(strippTable(tableProducts));

		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();

		// create pdf
		pdfExportTable.execute(null, false);

	}

	private void applyChangedValue(ListSelectionEvent listSelectionEvent) {
		if (listSelectionEvent.getValueIsAdjusting()) {
			return;
		}

		ListSelectionModel lsm = (ListSelectionModel) listSelectionEvent.getSource();
		if (lsm.isSelectionEmpty() || lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex()) {
			Logging.debug(this, "no or several rows selected");
			clearEditing();
		} else {
			int selectedRow = lsm.getMinSelectionIndex();
			Logging.debug(this, "selected " + selectedRow);
			Logging.debug(this, "selected modelIndex " + convertRowIndexToModel(selectedRow));
			Logging.debug(this, "selected  value at "
					+ tableProducts.getModel().getValueAt(convertRowIndexToModel(selectedRow), 0));
			configedMain.setProductEdited(
					(String) tableProducts.getModel().getValueAt(convertRowIndexToModel(selectedRow), 0));
		}
	}

	private JTable strippTable(JTable jTable) {
		boolean strippIt;
		List<String[]> data = new ArrayList<>();
		String[] headers = new String[jTable.getColumnCount()];
		for (int i = 0; i < jTable.getColumnCount(); i++) {
			headers[i] = jTable.getColumnName(i);
		}

		for (int j = 0; j < jTable.getRowCount(); j++) {
			strippIt = true;
			String[] actCol = new String[jTable.getColumnCount()];
			for (int i = 0; i < jTable.getColumnCount(); i++) {

				Object cellValue = jTable.getValueAt(j, i);

				String cellValueString = cellValue == null ? "" : cellValue.toString();

				actCol[i] = cellValueString;

				strippIt = shouldStrippIt(jTable.getColumnName(i), cellValueString);
			}

			if (!strippIt) {
				data.add(actCol);
			}
		}

		// create jTable with selected rows
		int rows = data.size();
		int cols = jTable.getColumnCount();
		String[][] strippedData = new String[rows][cols];
		for (int i = 0; i < data.size(); i++) {
			strippedData[i] = data.get(i);
		}
		return new JTable(strippedData, headers);
	}

	private boolean shouldStrippIt(String columnName, String cellValueString) {

		boolean strippIt = false;

		switch (columnName) {
		case "Stand":
			if (!cellValueString.equals(InstallationStatus.KEY_NOT_INSTALLED)) {
				strippIt = true;
			}
			break;
		case "Report":
			if (!cellValueString.isEmpty()) {
				strippIt = true;
			}
			break;
		case "Angefordert":
			if (!"none".equals(cellValueString)) {
				strippIt = true;
			}
			break;
		default:
			Logging.warning(this, "no case found for columnName in jTable");
			break;
		}

		return strippIt;
	}

	protected void reloadAction() {
		ConfigedMain.getMainFrame().setCursor(Globals.WAIT_CURSOR);

		configedMain.requestReloadStatesAndActions();
		configedMain.resetView(configedMain.getViewIndex());
		configedMain.setDataChanged(false);

		ConfigedMain.getMainFrame().setCursor(null);
	}

	protected void saveAndExecuteAction() {
		Logging.info(this, "saveAndExecuteAction");
		configedMain.checkSaveAll(false);
		configedMain.requestReloadStatesAndActions();

		configedMain.fireOpsiclientdEventOnSelectedClients(
				OpsiServiceNOMPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND);

	}

	private String infoSortKeys(List<? extends RowSorter.SortKey> sortKeys) {
		if (sortKeys == null) {
			return "null";
		}

		StringBuilder result = new StringBuilder("[");
		int i = 0;
		for (RowSorter.SortKey key : sortKeys) {
			i++;
			result.append(key.getColumn() + ".." + key);
		}
		result.append("]");
		Logging.info(this, "infoSortkeys " + result);
		return " (number " + i + ") ";

	}

	@SuppressWarnings("java:S1452")
	public List<? extends SortKey> getSortKeys() {
		Logging.info(this, "getSortKeys : " + infoSortKeys(currentSortKeys));
		return currentSortKeys;
	}

	public void setSortKeys(List<? extends SortKey> currentSortKeys) {
		Logging.info(this, "setSortKeys: " + infoSortKeys(currentSortKeys));
		if (currentSortKeys != null) {
			tableProducts.getRowSorter().setSortKeys(currentSortKeys);
		}
	}

	private void showPopupOpsiclientdEvent(boolean visible) {

		itemOnDemand.setVisible(visible);
		itemSaveAndExecute.setVisible(visible);
	}

	public void clearSelection() {
		tableProducts.clearSelection();
	}

	public void setSelection(Set<String> selectedIDs) {
		activatePacketSelectionHandling(false);
		clearSelection();
		if (selectedIDs != null) {
			if (selectedIDs.isEmpty() && tableProducts.getRowCount() > 0) {
				tableProducts.addRowSelectionInterval(0, 0);
				// show first product if no product given
				Logging.info(this, "setSelection 0");
			} else {
				for (int row = 0; row < tableProducts.getRowCount(); row++) {
					Object productId = tableProducts.getValueAt(row, 0);
					if (selectedIDs.contains(productId)) {
						tableProducts.addRowSelectionInterval(row, row);
					}
				}
			}
		}

		activatePacketSelectionHandling(true);
		groupPanel.findGroup(selectedIDs);
	}

	public Set<String> getSelectedIDs() {
		Set<String> result = new HashSet<>();

		int[] selection = tableProducts.getSelectedRows();

		for (int i = 0; i < selection.length; i++) {
			result.add((String) tableProducts.getValueAt(selection[i], 0));
		}

		return result;
	}

	private int convertRowIndexToModel(int row) {
		return tableProducts.convertRowIndexToModel(row);
	}

	public List<Integer> getSelectedRowsInModelTerms() {
		int[] selection = tableProducts.getSelectedRows();
		ArrayList<Integer> selectionInModelTerms = new ArrayList<>(selection.length);
		for (int i = 0; i < selection.length; i++) {
			selectionInModelTerms.add(convertRowIndexToModel(selection[i]));
		}

		return selectionInModelTerms;
	}

	public void reduceToSet(Set<String> filter) {
		activatePacketSelectionHandling(false);

		InstallationStateTableModelFiltered tModel = (InstallationStateTableModelFiltered) tableProducts.getModel();
		tModel.setFilterFrom(filter);

		Logging.info(this, "reduceToSet  " + filter);
		Logging.info(this, "reduceToSet GuiIsFiltered " + groupPanel.isGuiFiltered());

		groupPanel.setGuiIsFiltered(filter != null && !filter.isEmpty());

		tableProducts.revalidate();
		activatePacketSelectionHandling(true);
	}

	public void reduceToSelected() {
		Set<String> selection = getSelectedIDs();
		Logging.debug(this, "reduceToSelected: selectedIds  " + selection);
		reduceToSet(selection);
		setSelection(selection);
	}

	public void noSelection() {
		InstallationStateTableModelFiltered tModel = (InstallationStateTableModelFiltered) tableProducts.getModel();

		activatePacketSelectionHandling(false);
		tModel.setFilterFrom((Set<String>) null);
		tableProducts.revalidate();
		activatePacketSelectionHandling(true);
	}

	public void showAll() {
		Set<String> selection = getSelectedIDs();
		noSelection();
		setSelection(selection);
	}

	public void setTableModel(IFInstallationStateTableModel istm) {
		// delete old row sorter before setting new model
		tableProducts.setRowSorter(null);

		tableProducts.setModel(istm);

		final Comparator<String> myComparator = Comparator.comparing(String::toString);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableProducts.getModel()) {
			@Override
			protected boolean useToString(int column) {
				return true;
			}

			@Override
			public Comparator<?> getComparator(int column) {
				if (column == 0) {
					return myComparator;
				} else {
					return super.getComparator(column);
				}
			}
		};

		tableProducts.setRowSorter(sorter);
		sorter.addRowSorterListener(this);

		tableProducts.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(tableProducts.getTableHeader().getDefaultRenderer()));

		Logging.debug(this, " tableProducts columns  count " + tableProducts.getColumnCount());
		Enumeration<TableColumn> enumer = tableProducts.getColumnModel().getColumns();

		while (enumer.hasMoreElements()) {
			Logging.debug(this, " tableProducts column  " + enumer.nextElement().getHeaderValue());
		}

		int colIndex = -1;

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_ID)) > -1) {
			TableColumn nameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			nameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_NAME);
			nameColumn.setCellRenderer(productNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_NAME)) > -1) {
			TableColumn completeNameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			completeNameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_COMPLETE_NAME);
			completeNameColumn.setCellRenderer(productCompleteNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_TARGET_CONFIGURATION)) > -1) {
			TableColumn targetColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> targetCombo = new JComboBox<>();
			targetCombo.setRenderer(standardListCellRenderer);

			targetColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(targetCombo, istm,
					TargetConfiguration.getLabel2DisplayLabel()));
			targetColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			targetColumn.setCellRenderer(targetConfigurationTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_STATUS)) > -1) {
			TableColumn statusColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> statesCombo = new JComboBox<>();
			statesCombo.setRenderer(standardListCellRenderer);

			statusColumn.setCellEditor(
					new AdaptingCellEditorValuesByIndex(statesCombo, istm, InstallationStatus.getLabel2DisplayLabel()));
			statusColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_PROGRESS)) > -1) {
			TableColumn progressColumn = tableProducts.getColumnModel().getColumn(colIndex);

			progressColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			progressColumn.setCellRenderer(actionProgressTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_LAST_ACTION)) > -1) {
			TableColumn lastactionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			lastactionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			lastactionColumn.setCellRenderer(lastActionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_RESULT)) > -1) {
			TableColumn actionresultColumn = tableProducts.getColumnModel().getColumn(colIndex);
			actionresultColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			actionresultColumn.setCellRenderer(actionResultTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_REQUEST)) > -1) {
			TableColumn actionColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> actionsCombo = new JComboBox<>();
			actionsCombo.setRenderer(standardListCellRenderer);
			actionColumn.setCellEditor(
					new AdaptingCellEditorValuesByIndex(actionsCombo, istm, ActionRequest.getLabel2DisplayLabel()));
			actionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			actionColumn.setCellRenderer(actionRequestTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_LAST_STATE_CHANGE)) > -1) {
			TableColumn laststatechangeColumn = tableProducts.getColumnModel().getColumn(colIndex);
			laststatechangeColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			laststatechangeColumn.setCellRenderer(lastStateChangeTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_SEQUENCE)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			sorter.setComparator(colIndex, new IntComparatorForStrings());

			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_PRIORITY)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			sorter.setComparator(colIndex, new IntComparatorForStrings());
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_POSITION)) > -1) {
			TableColumn productsequenceColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productsequenceColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			productsequenceColumn.setCellRenderer(productsequenceTableCellRenderer);

			sorter.setComparator(colIndex, new IntComparatorForStrings());
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_VERSION)) > -1) {
			TableColumn productversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productversionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_VERSION);
			productversionColumn.setCellRenderer(productversionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PACKAGE_VERSION)) > -1) {

			TableColumn packageversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			packageversionColumn.setPreferredWidth(WIDTH_COLUMN_PACKAGE_VERSION);
			packageversionColumn.setCellRenderer(packageversionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_VERSION_INFO)) > -1) {
			TableColumn versionInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			versionInfoColumn.setPreferredWidth(WIDTH_COLUMN_VERSION_INFO);
			versionInfoColumn.setCellRenderer(versionInfoTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_INFO)) > -1) {
			TableColumn installationInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			installationInfoColumn.setPreferredWidth(WIDTH_COLUMN_INSTALLATION_INFO);
			installationInfoColumn.setCellRenderer(installationInfoTableCellRenderer);

			JComboBox<String> installationInfoCombo = new JComboBox<>();

			installationInfoCombo.setRenderer(standardListCellRenderer);

			DynamicCellEditor cellEditor = new DynamicCellEditor(installationInfoCombo, istm);

			installationInfoColumn.setCellEditor(cellEditor);
		}
	}

	public void initEditing(String productID, String productTitle, String productInfo, String productHint,
			String productVersion, Collection<Map<String, Object>> storableProductProperties,
			Map editableProductProperties,
			// editmappanelx
			Map<String, ListCellOptions> productpropertyOptionsMap,
			ProductpropertiesUpdateCollection updateCollection) {

		infoPane.setProductId(productID);
		infoPane.setProductName(productTitle);
		infoPane.setProductInfo(productInfo);
		infoPane.setProductVersion(productVersion);

		infoPane.setProductAdvice(productHint);

		Utils.checkCollection(this, "editableProductProperties ", editableProductProperties);
		Utils.checkCollection(this, "productpropertyOptionsMap", productpropertyOptionsMap);

		propertiesPanel.setEditableMap(editableProductProperties, productpropertyOptionsMap);

		propertiesPanel.setStoreData(storableProductProperties);
		propertiesPanel.setUpdateCollection(updateCollection);

	}

	public void clearListEditors() {
		if (propertiesPanel instanceof EditMapPanelX) {
			((EditMapPanelX) propertiesPanel).cancelOldCellEditing();
		}
	}

	public void clearEditing() {
		initEditing("", "", "", "", "", null, null, null, null);
		infoPane.clearEditing();
	}

	// RowSorterListener for table row sorter
	@Override
	public void sorterChanged(RowSorterEvent e) {
		Logging.info(this, "RowSorterEvent " + e);
		currentSortKeys = tableProducts.getRowSorter().getSortKeys();
		Logging.info(this, "sorterChanged, sortKeys: " + infoSortKeys(currentSortKeys));

	}

	public List<String> getSelectedProducts() {
		// in model terms

		List<Integer> selectedRows = getSelectedRowsInModelTerms();

		List<String> selectedProductsList = new ArrayList<>();

		for (int row : selectedRows) {
			selectedProductsList.add((String) tableProducts.getModel().getValueAt(row, 0));
		}

		Logging.info(this, "selectedProducts " + selectedProductsList);

		return selectedProductsList;
	}

	public JTable getTableProducts() {
		return tableProducts;
	}
}
