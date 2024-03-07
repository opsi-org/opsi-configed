/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.ClientMenuManager;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.productgroup.ProductActionPanel;
import de.uib.configed.tree.ProductTree;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.datapanel.DefaultEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import utils.PopupMouseListener;
import utils.ProductPackageVersionSeparator;
import utils.Utils;

public class PanelProductSettings extends JSplitPane {
	public enum ProductSettingsType {
		NETBOOT_PRODUCT_SETTINGS, LOCALBOOT_PRODUCT_SETTINGS
	}

	private static final int HEIGHT_MIN = 200;

	private JTable tableProducts;
	private ProductSettingsTableModel productSettingsTableModel;

	private ProductActionPanel groupPanel;

	private ProductInfoPane infoPane;
	private EditMapPanelX propertiesPanel;

	private Map<String, Boolean> productDisplayFields;

	private JPopupMenu popup;
	private JMenuItem itemOnDemand;

	private String title;

	private ProductTree productTree;

	private ConfigedMain configedMain;

	private ProductSettingsType type;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelProductSettings(String title, ConfigedMain configedMain, ProductTree productTree,
			Map<String, Boolean> productDisplayFields, ProductSettingsType type) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.title = title;
		this.productTree = productTree;
		this.configedMain = configedMain;
		this.productDisplayFields = productDisplayFields;
		this.type = type;
		init();

		super.setResizeWeight(1);
	}

	private void initTopPane() {
		tableProducts = new JTable() {
			@Override
			public void setValueAt(Object value, int row, int column) {
				Set<String> saveSelectedProducts = getSelectedIDs();
				// only in case of setting ActionRequest needed, since we there call
				// fireTableDataChanged
				super.setValueAt(value, row, column);
				setSelection(saveSelectedProducts);
			}
		};

		tableProducts.setDragEnabled(true);

		groupPanel = new ProductActionPanel(this, tableProducts);
		groupPanel.setReloadActionHandler((ActionEvent ae) -> {
			Logging.info(this, " in top pane we got event reloadAction " + ae);
			reloadAction();
		});

		groupPanel.setSaveAndExecuteActionHandler((ActionEvent ae) -> {
			Logging.info(this, " in top pane we got event saveAndExecuteAction " + ae);
			saveAndExecuteAction();
		});

		groupPanel.setVisible(true);
	}

	private void init() {
		initTopPane();

		JScrollPane paneProducts = new JScrollPane();

		paneProducts.getViewport().add(tableProducts);
		paneProducts.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		tableProducts.getSelectionModel().addListSelectionListener(this::applyChangedValue);

		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		productSettingsTableModel = new ProductSettingsTableModel(tableProducts);

		JPanel leftPane = new JPanel();
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(Alignment.LEADING)
				.addComponent(groupPanel, HEIGHT_MIN, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(paneProducts, HEIGHT_MIN, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup()
				.addComponent(groupPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(paneProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		setLeftComponent(leftPane);

		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, true, false);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		propertiesPanel.setCellEditor(new SensitiveCellEditorForDataPanel());
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
		});

		AbstractPanelEditProperties panelEditProperties = new PanelEditClientProperties(configedMain, propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties);

		propertiesPanel.registerDataChangedObserver(infoPane);

		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());

		setRightComponent(infoPane);

		producePopupMenu(productDisplayFields);

		paneProducts.addMouseListener(new PopupMouseListener(popup));
		tableProducts.addMouseListener(new PopupMouseListener(popup));

		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public void updateSearchFields() {
		groupPanel.updateSearchFields();
	}

	public void initAllProperties() {
		propertiesPanel.init();
		infoPane.setProductInfo("");
		infoPane.setProductAdvice("");
	}

	private void producePopupMenu(final Map<String, Boolean> checkColumns) {
		popup = new JPopupMenu();

		JMenuItem save = new JMenuItem(Configed.getResourceValue("save"), Utils.getSaveIcon());
		save.setEnabled(!persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

		save.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "actionevent on save-menue");
			configedMain.checkSaveAll(false);
			configedMain.requestReloadStatesAndActions();
		});

		popup.add(save);

		itemOnDemand = new JMenuItem(Configed.getResourceValue("ConfigedMain.Opsiclientd.executeAll"),
				Utils.createImageIcon("images/executing_command_blue_16.png", ""));
		itemOnDemand.setEnabled(!persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
		itemOnDemand.addActionListener((ActionEvent e) -> saveAndExecuteAction());
		itemOnDemand.setEnabled(type != ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);

		popup.add(itemOnDemand);

		JMenuItem itemOnDemandForSelectedProducts = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Opsiclientd.executeSelected"),
				Utils.createImageIcon("images/executing_command_blue_16.png", ""));
		itemOnDemandForSelectedProducts
				.setEnabled(!persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
		itemOnDemandForSelectedProducts
				.addActionListener((ActionEvent e) -> configedMain.processActionRequestsSelectedProducts());
		itemOnDemandForSelectedProducts.setEnabled(type != ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);

		if (ServerFacade.isOpsi43()) {
			popup.add(itemOnDemandForSelectedProducts);
		}

		popup.addSeparator();

		ClientMenuManager clientMenuManager = ClientMenuManager.getInstance();
		JMenu resetProductsMenu = new JMenu(Configed.getResourceValue("MainFrame.jMenuResetProducts"));
		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			clientMenuManager.addResetLocalbootProductsMenuItemsTo(resetProductsMenu);
		} else {
			clientMenuManager.addResetNetbootProductsMenuItemsTo(resetProductsMenu);
		}
		popup.add(resetProductsMenu);

		popup.addSeparator();

		showPopupOpsiclientdEvent(true);

		JMenuItem reload = new JMenuItem(Configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(Utils.createImageIcon("images/reload16.png", ""));
		reload.addActionListener((ActionEvent e) -> {
			Logging.info(this, "reload action");
			reloadAction();
		});
		popup.add(reload);

		JMenuItem createReport = new JMenuItem(Configed.getResourceValue("PanelProductSettings.pdf"));
		createReport.setIcon(Utils.createImageIcon("images/acrobat_reader16.png", ""));
		createReport.addActionListener((ActionEvent e) -> createReport());
		popup.add(createReport);

		ExporterToCSV exportTable = new ExporterToCSV(tableProducts);
		exportTable.addMenuItemsTo(popup);

		JMenu sub = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		popup.addSeparator();
		popup.add(sub);

		for (Entry<String, Boolean> checkColumn : checkColumns.entrySet()) {
			if ("productId".equals(checkColumn.getKey())) {
				// fixed column
				continue;
			}

			JCheckBoxMenuItem item = new JCheckBoxMenuItem();
			item.setText(InstallationStateTableModel.getColumnTitle(checkColumn.getKey()));
			item.setState(checkColumn.getValue());
			item.addItemListener((ItemEvent e) -> {
				boolean oldstate = checkColumn.getValue();
				checkColumns.put(checkColumn.getKey(), !oldstate);
				configedMain.requestReloadStatesAndActions();
				configedMain.resetView(configedMain.getViewIndex());
			});

			sub.add(item);
		}
	}

	private void createReport() {
		Logging.info(this, "create report");
		Map<String, String> metaData = new HashMap<>();

		// display, if filter is active,
		// display selected productgroup
		// depot server, selected clients out of statusPane

		metaData.put("header", title);
		metaData.put("subject", title);
		title = "";
		if (ConfigedMain.getMainFrame().getHostsStatusPanel().getInvolvedDepots().length() != 0) {
			title = title + "Depot : " + ConfigedMain.getMainFrame().getHostsStatusPanel().getInvolvedDepots();
		}
		if (ConfigedMain.getMainFrame().getHostsStatusPanel().getSelectedClientNames().length() != 0) {
			title = title + "; Clients: " + ConfigedMain.getMainFrame().getHostsStatusPanel().getSelectedClientNames();
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
			Logging.debug(this, "selected modelIndex " + tableProducts.convertRowIndexToModel(selectedRow));
			Logging.debug(this, "selected  value at "
					+ tableProducts.getModel().getValueAt(tableProducts.convertRowIndexToModel(selectedRow), 0));
			configedMain.setProductEdited(
					(String) tableProducts.getModel().getValueAt(tableProducts.convertRowIndexToModel(selectedRow), 0));
		}

		productTree.produceActiveParents();
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
				strippIt = shouldStrippIt(jTable.getColumnName(i), cellValueString, strippIt);
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

	private boolean shouldStrippIt(String columnName, String cellValueString, boolean previuosValue) {
		boolean strippIt = previuosValue;

		if (Configed.getResourceValue("InstallationStateTableModel.installationStatus").equals(columnName)
				&& !InstallationStatus.KEY_NOT_INSTALLED.equals(cellValueString)) {
			strippIt = false;
		} else if (Configed.getResourceValue("InstallationStateTableModel.report").equals(columnName)
				&& (cellValueString != null && !cellValueString.isEmpty())) {
			strippIt = false;
		} else if (Configed.getResourceValue("InstallationStateTableModel.actionRequest").equals(columnName)
				&& !"none".equals(cellValueString)) {
			strippIt = false;
		} else {
			Logging.warning(this, "no case found for columnName in jTable");
		}

		return strippIt;
	}

	protected void reloadAction() {
		ConfigedMain.getMainFrame().activateLoadingCursor();

		configedMain.requestReloadStatesAndActions();
		configedMain.resetView(configedMain.getViewIndex());
		configedMain.setDataChanged(false);

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	protected void saveAndExecuteAction() {
		Logging.info(this, "saveAndExecuteAction");
		configedMain.checkSaveAll(false);
		configedMain.requestReloadStatesAndActions();

		if (ServerFacade.isOpsi43()) {
			configedMain.processActionRequestsAllProducts();
		} else {
			configedMain.fireOpsiclientdEventOnSelectedClients(
					OpsiServiceNOMPersistenceController.OPSI_CLIENTD_EVENT_ON_DEMAND);
		}
	}

	@SuppressWarnings("java:S1452")
	public List<? extends SortKey> getSortKeys() {
		if (tableProducts.getRowSorter() != null) {
			return tableProducts.getRowSorter().getSortKeys();
		} else {
			return Collections.singletonList(new SortKey(0, SortOrder.ASCENDING));
		}
	}

	public void setSortKeys(List<? extends SortKey> currentSortKeys) {
		Logging.info(this, "setSortKeys : " + currentSortKeys);
		if (tableProducts.getRowSorter() != null) {
			tableProducts.getRowSorter().setSortKeys(currentSortKeys);
		}
	}

	private void showPopupOpsiclientdEvent(boolean visible) {
		itemOnDemand.setVisible(visible);
	}

	public void setSelection(Set<String> selectedIDs) {
		tableProducts.clearSelection();

		if (selectedIDs == null) {
			Logging.info("selectedIds is null");
		} else if (selectedIDs.isEmpty() && tableProducts.getRowCount() > 0) {
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

	public Set<String> getSelectedIDs() {
		Set<String> result = new HashSet<>();

		for (int selectionElement : tableProducts.getSelectedRows()) {
			result.add((String) tableProducts.getValueAt(selectionElement, 0));
		}

		return result;
	}

	public List<Integer> getSelectedRowsInModelTerms() {
		int[] selection = tableProducts.getSelectedRows();
		List<Integer> selectionInModelTerms = new ArrayList<>(selection.length);
		for (int selectionElement : selection) {
			selectionInModelTerms.add(tableProducts.convertRowIndexToModel(selectionElement));
		}

		return selectionInModelTerms;
	}

	public boolean isFilteredMode() {
		return groupPanel.isFilteredMode();
	}

	public void reduceToSet(Set<String> filter) {
		InstallationStateTableModel tModel = (InstallationStateTableModel) tableProducts.getModel();
		tModel.setFilterFrom(filter);

		Logging.info(this, "reduceToSet  " + filter);

		groupPanel.setFilteredMode(filter != null && !filter.isEmpty());

		tableProducts.revalidate();
	}

	public void reduceToSelected() {
		Set<String> selection = getSelectedIDs();
		Logging.debug(this, "reduceToSelected: selectedIds  " + selection);
		reduceToSet(selection);
		setSelection(selection);
	}

	public void setFilter(Set<String> filter) {
		groupPanel.setFilteredMode(false);
		if (tableProducts.getModel() instanceof InstallationStateTableModel) {
			((InstallationStateTableModel) tableProducts.getModel()).setFilterFrom(filter);
		}
	}

	public void showAll() {
		Set<String> selection = getSelectedIDs();
		setFilter(null);
		setSelection(selection);
	}

	public void valueChanged(TreePath[] selectionPaths) {
		if (selectionPaths == null) {
			setFilter(null);
		} else if (selectionPaths.length == 1) {
			nodeSelection((DefaultMutableTreeNode) selectionPaths[0].getLastPathComponent());
		} else {
			Set<String> productIds = new HashSet<>();
			for (TreePath path : selectionPaths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (!node.getAllowsChildren()) {
					productIds.add(node.getUserObject().toString());
				}
			}
			setFilter(productIds);
			setSelection(productIds);
		}
	}

	private void nodeSelection(DefaultMutableTreeNode node) {
		if (node.getAllowsChildren()) {
			Set<String> productIds = ProductTree.getChildrenRecursively(node);
			setFilter(productIds);
		} else {
			Set<String> productIds = Collections.singleton(node.toString());
			setFilter(productIds);
			setSelection(productIds);
		}
	}

	public void setTableModel(InstallationStateTableModel istm) {
		// delete old row sorter before setting new model

		tableProducts.setModel(istm);
		productSettingsTableModel.setRenderer(istm);

		valueChanged(productTree.getSelectionPaths());

		Logging.debug(this, " tableProducts columns  count " + tableProducts.getColumnCount());
		Enumeration<TableColumn> enumer = tableProducts.getColumnModel().getColumns();

		while (enumer.hasMoreElements()) {
			Logging.debug(this, " tableProducts column  " + enumer.nextElement().getHeaderValue());
		}
	}

	public void initEditing(String productID, Collection<Map<String, Object>> storableProductProperties,
			Map editableProductProperties, ProductpropertiesUpdateCollection updateCollection) {
		infoPane.setProductId(productID);
		infoPane.setProductName(persistenceController.getProductDataService().getProductTitle(productID));
		infoPane.setProductInfo(persistenceController.getProductDataService().getProductInfo(productID));
		infoPane.setProductVersion(persistenceController.getProductDataService().getProductVersion(productID)
				+ ProductPackageVersionSeparator.FOR_DISPLAY
				+ persistenceController.getProductDataService().getProductPackageVersion(productID) + "   "
				+ persistenceController.getProductDataService().getProductLockedInfo(productID));

		infoPane.setProductAdvice(persistenceController.getProductDataService().getProductHint(productID));

		propertiesPanel.setEditableMap(editableProductProperties,
				persistenceController.getProductDataService().getProductPropertyOptionsMap(productID));
		propertiesPanel.setStoreData(storableProductProperties);
		propertiesPanel.setUpdateCollection(updateCollection);
	}

	public void clearListEditors() {
		propertiesPanel.cancelOldCellEditing();
	}

	private void clearEditing() {
		propertiesPanel.setEditableMap(null, null);
		propertiesPanel.setStoreData(null);
		propertiesPanel.setUpdateCollection(null);
		infoPane.clearEditing();
	}

	public JTable getTableProducts() {
		return tableProducts;
	}
}
