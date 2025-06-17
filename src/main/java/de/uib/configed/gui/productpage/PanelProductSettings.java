/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.ChangedDataManager;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ServerActionManager;
import de.uib.configed.gui.ClientMenuManager;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.productgroup.ProductActionPanel;
import de.uib.configed.tree.ProductTree;
import de.uib.opsidatamodel.datachanges.ProductpropertiesUpdateCollection;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Icons;
import de.uib.utils.PopupMouseListener;
import de.uib.utils.datapanel.EditMapPanelX;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.ExporterToCSV;
import de.uib.utils.table.ExporterToPDF;

public class PanelProductSettings extends JSplitPane {
	public enum ProductSettingsType {
		NETBOOT_PRODUCT_SETTINGS, LOCALBOOT_PRODUCT_SETTINGS
	}

	private ProductTable productTable;
	private ProductSettingsTableModel productSettingsTableModel;

	private ProductActionPanel groupPanel;

	private ProductInfoPane infoPane;
	private EditMapPanelX propertiesPanel;

	private ProductTree productTree;

	private ConfigedMain configedMain;

	private ProductSettingsType type;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelProductSettings(ConfigedMain configedMain, ProductTree productTree, ProductSettingsType type) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.productTree = productTree;
		this.configedMain = configedMain;
		this.type = type;

		init();

		super.setResizeWeight(1.0);
	}

	private void init() {
		productTable = new ProductTable();

		groupPanel = new ProductActionPanel(this, type);

		groupPanel.setReloadActionHandler(actionEvent -> reloadAction());

		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			groupPanel.setSaveAndExecuteActionHandler(actionEvent -> saveAndExecuteAction());
		}

		groupPanel.setVisible(true);

		JScrollPane paneProducts = new JScrollPane();
		paneProducts.getViewport().add(productTable);
		paneProducts.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		productTable.getSelectionModel().addListSelectionListener(this::applyChangedValue);

		productSettingsTableModel = new ProductSettingsTableModel(productTable);

		JPanel leftPane = new JPanel();
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(Alignment.LEADING)
				.addComponent(groupPanel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(paneProducts, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup()
				.addComponent(groupPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(paneProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		setLeftComponent(leftPane);

		propertiesPanel = new EditMapPanelX(false, true, false);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		propertiesPanel.getMapTableModel()
				.registerDataChangedObserver(ChangedDataManager.getGeneralDataChangedKeeper());

		AbstractPanelEditProperties panelEditProperties = new PanelEditClientProperties(propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties);

		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());

		setRightComponent(infoPane);

		PopupMouseListener popupMouseListener = new PopupMouseListener(producePopupMenu());
		paneProducts.addMouseListener(popupMouseListener);
		productTable.addMouseListener(popupMouseListener);
	}

	public void updateSearchFields() {
		groupPanel.updateSearchFields();
	}

	public void restoreFilter() {
		groupPanel.restoreFilter();
	}

	private JPopupMenu producePopupMenu() {
		JPopupMenu popup = new JPopupMenu();

		JMenuItem save = new JMenuItem(Configed.getResourceValue("save"));
		Icons.addIntellijIconToMenuItem(save, "save");
		save.setEnabled(!persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

		save.addActionListener(actionEvent -> ChangedDataManager.checkSaveAll(false));

		popup.add(save);

		JMenuItem itemOnDemand = new JMenuItem(Configed.getResourceValue("ConfigedMain.Opsiclientd.executeAll"));
		Icons.addIntellijIconToMenuItem(itemOnDemand, "run");
		itemOnDemand.setEnabled(!persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
		itemOnDemand.addActionListener(actionEvent -> saveAndExecuteAction());
		itemOnDemand.setEnabled(type != ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);

		popup.add(itemOnDemand);

		JMenuItem itemOnDemandForSelectedProducts = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Opsiclientd.executeSelected"));
		Icons.addIntellijIconToMenuItem(itemOnDemandForSelectedProducts, "run");
		itemOnDemandForSelectedProducts
				.setEnabled(!persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());
		itemOnDemandForSelectedProducts.addActionListener(
				actionEvent -> ServerActionManager.processActionRequestsSelectedProducts(groupPanel.getVisibility()));
		itemOnDemandForSelectedProducts.setEnabled(type != ProductSettingsType.NETBOOT_PRODUCT_SETTINGS);

		popup.add(itemOnDemandForSelectedProducts);

		popup.addSeparator();

		JMenu resetProductsMenu;
		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			resetProductsMenu = ClientMenuManager.createResetLocalbootProductsMenuItemsTo();
		} else {
			resetProductsMenu = ClientMenuManager.createResetNetbootProductsMenuItemsTo();
		}
		popup.add(resetProductsMenu);

		popup.addSeparator();

		JMenuItem reload = new JMenuItem(Configed.getResourceValue("ConfigedMain.reloadTable"));
		Icons.addIntellijIconToMenuItem(reload, "refresh");
		reload.addActionListener(actionEvent -> reloadAction());
		popup.add(reload);

		JMenuItem createReport = new JMenuItem(Configed.getResourceValue("PanelProductSettings.pdf"));
		Icons.addThemeIconInvertedToMenuItem(createReport, "anyType");
		createReport.addActionListener(actionEvent -> createReport());
		popup.add(createReport);

		ExporterToCSV exportTable = new ExporterToCSV(productTable);
		exportTable.addMenuItemsTo(popup);

		JMenu jMenuVisibleColumns = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		popup.addSeparator();
		popup.add(jMenuVisibleColumns);

		for (Entry<String, Boolean> productDisplayField : getProductDisplayFieldsBasedOnType(type).entrySet()) {
			if ("productId".equals(productDisplayField.getKey())) {
				// fixed column
				continue;
			}

			JCheckBoxMenuItem item = new JCheckBoxMenuItem();
			item.setText(InstallationStateTableModel.getColumnTitle(productDisplayField.getKey()));
			item.setState(productDisplayField.getValue());
			item.addItemListener((ItemEvent e) -> {
				boolean oldstate = productDisplayField.getValue();
				getProductDisplayFieldsBasedOnType(type).put(productDisplayField.getKey(), !oldstate);
				persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTIES.toString());

				// We need to rebuild the shown page in the client configuration to make changes effective
				ConfigedMain.getMainFrame().getClientConfiguration().stateChanged(null);
			});

			jMenuVisibleColumns.add(item);
		}
		return popup;
	}

	private Map<String, Boolean> getProductDisplayFieldsBasedOnType(ProductSettingsType type) {
		return type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS
				? persistenceController.getProductDataService().getProductOnClientsDisplayFieldsLocalbootProducts()
				: persistenceController.getProductDataService().getProductOnClientsDisplayFieldsNetbootProducts();
	}

	private void createReport() {
		Logging.info(this, "create report");
		Map<String, String> metaData = new HashMap<>();

		String title;

		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			title = Configed.getResourceValue("localbootProducts");
		} else {
			title = Configed.getResourceValue("netbootProducts");
		}
		// display, if filter is active,
		// display selected productgroup
		// depot server, selected clients out of statusPane
		metaData.put("header", title);
		metaData.put("subject", title);

		title = "";
		if (ConfigedMain.getMainFrame().getHostsStatusPanel().getInvolvedDepots().length() != 0) {
			title += "Depot : " + ConfigedMain.getMainFrame().getHostsStatusPanel().getInvolvedDepots();
		}
		if (ConfigedMain.getMainFrame().getHostsStatusPanel().getSelectedClientNames().length() != 0) {
			title += "; Clients: " + ConfigedMain.getMainFrame().getHostsStatusPanel().getSelectedClientNames();
		}
		metaData.put("title", title);
		metaData.put("keywords", "product settings");

		// only relevent rows
		ExporterToPDF pdfExportTable = new ExporterToPDF(productTable.getStrippedTable());

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
		if (lsm.getSelectedItemsCount() != 1) {
			Logging.debug(this, "no or several rows selected");
			clearEditing();
		} else {
			int selectedRow = lsm.getMinSelectionIndex();
			Logging.debug(this, "selected ", selectedRow);
			Logging.debug(this, "selected modelIndex ", productTable.convertRowIndexToModel(selectedRow));
			Logging.debug(this, "selected  value at ",
					productTable.getModel().getValueAt(productTable.convertRowIndexToModel(selectedRow), 0));
			ConfigedMain.getMainFrame().getClientConfiguration().getProductPageManager().setProductEdited(
					(String) productTable.getModel().getValueAt(productTable.convertRowIndexToModel(selectedRow), 0),
					this);
		}

		productTree.produceActiveParents();

		productTree.updateSelectedObjectsInTable();
	}

	private void reloadAction() {
		ConfigedMain.getMainFrame().activateLoadingCursor();

		persistenceController.reloadData(ReloadEvent.PRODUCT_DATA_RELOAD.toString());

		// We want to rebuild the shown page in the client configuration after reload
		ConfigedMain.getMainFrame().getClientConfiguration().stateChanged(null);
		ChangedDataManager.setDataChanged(false);

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private void saveAndExecuteAction() {
		Logging.info(this, "saveAndExecuteAction");
		ChangedDataManager.checkSaveAll(false);
		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTIES.toString());
		ServerActionManager.processActionRequestsAllProducts(groupPanel.getVisibility());
	}

	public boolean isFilteredMode() {
		return groupPanel.isFilteredMode();
	}

	public void valueChanged(boolean doSelection) {
		// We want to deactivate filter before changing something
		groupPanel.setFilterMark(false);
		productTable.valueChanged(doSelection, productTree.getSelectionPaths());
	}

	public void setTableModel(InstallationStateTableModel istm) {
		// delete old row sorter before setting new model
		productTable.setModel(istm);
		productSettingsTableModel.setRenderer(istm);

		// We don't want to call setSelection here, since it will be called after this method
		if (!isFilteredMode()) {
			valueChanged(false);
		}

		Logging.debug(this, " tableProducts columns  count ", productTable.getColumnCount());
	}

	public void initEditing(String productID, Collection<Map<String, Object>> storableProductProperties,
			Map<String, Object> editableProductProperties, ProductpropertiesUpdateCollection updateCollection) {
		infoPane.setProductId(productID);
		infoPane.setProductName(persistenceController.getProductDataService().getProductTitle(productID));
		infoPane.setProductInfo(persistenceController.getProductDataService().getProductInfo(productID));
		infoPane.setProductVersion(persistenceController.getProductDataService().getProductVersion(productID)
				+ ProductDataService.FOR_DISPLAY
				+ persistenceController.getProductDataService().getProductPackageVersion(productID) + "   "
				+ persistenceController.getProductDataService().getProductLockedInfo(productID));

		infoPane.setProductAdvice(persistenceController.getProductDataService().getProductAdvice(productID));

		propertiesPanel.setEditableMap(editableProductProperties,
				persistenceController.getProductDataService().getProductPropertyOptionsMap(productID));
		propertiesPanel.updateData(updateCollection, storableProductProperties);
	}

	public void clearEditing() {
		propertiesPanel.setEditableMap(null, null);
		propertiesPanel.updateData(null, null);
		infoPane.clearEditing();
	}

	public ProductTable getProductTable() {
		return productTable;
	}
}
