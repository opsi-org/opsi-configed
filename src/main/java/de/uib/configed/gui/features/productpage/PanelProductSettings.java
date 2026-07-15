/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import de.uib.configed.core.domain.datachanges.ProductpropertiesUpdateCollection;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.dataservice.ProductDataService;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.AbstractConfigurationTab;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.ClientConfiguration;
import de.uib.configed.gui.ClientMenuManager;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ServerActionManager;
import de.uib.configed.gui.features.productgroup.ProductActionPanel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.datapanel.KeyValueTable;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.share.SplitPaneStateManager;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelProductSettings extends AbstractConfigurationTab {
	public enum ProductSettingsType {
		NETBOOT_PRODUCT_SETTINGS, LOCALBOOT_PRODUCT_SETTINGS
	}

	private JMenu jMenuVisibleColumns;

	private ProductActionPanel groupPanel;

	private ProductInfoPane infoPane;
	private KeyValueTable propertiesPanel;

	private ProductTree productTree;

	private JSplitPane contentPane;

	private ConfigedMain configedMain;
	private Runnable updater;

	private ProductSettingsType type;

	private ProductTableModified productTableModified;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelProductSettings(ConfigedMain configedMain, ProductTree productTree, ProductSettingsType type) {
		super(true, true);
		this.configedMain = configedMain;
		this.productTree = productTree;
		this.type = type;

		init();
	}

	private void init() {
		productTableModified = new ProductTableModified(configedMain, type, productTree, this,
				() -> new PopupMouseListener(producePopupMenu()));

		groupPanel = new ProductActionPanel(this, type);

		groupPanel.setReloadActionHandler(actionEvent -> reloadAction());

		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			groupPanel.setSaveAndExecuteActionHandler(actionEvent -> saveAndExecuteAction());
		}

		groupPanel.setVisible(true);

		JPanel leftPane = new JPanel();
		leftPane.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[]0"));
		leftPane.add(groupPanel, "growx");
		leftPane.add(productTableModified.getComponent(), "grow, push, hmin 100");

		propertiesPanel = new KeyValueTable(false, true);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		propertiesPanel.registerDataChangedKeeper(ChangedDataManager.getGeneralDataChangedKeeper());

		AbstractPanelEditProperties panelEditProperties = new PanelEditClientProperties(propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties, type);

		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());

		contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, infoPane);
		contentPane.setResizeWeight(1.0);
		SplitPaneStateManager.registerSplitPane(contentPane,
				type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS
						? SplitPaneStateManager.LOCALBOOT_PRODUCT_SETTINGS_SPLIT
						: SplitPaneStateManager.NETBOOT_PRODUCT_SETTINGS_SPLIT,
				ClientConfiguration.DIVIDER_LOCATION);
		setComponent(contentPane);

		SwingUtils.addKeyBindingToJComponent(this, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), this::reloadAction);
	}

	public void restoreFilter() {
		groupPanel.restoreFilter();
	}

	public void enableFilterMode(boolean enable) {
		groupPanel.setFilterMark(enable);
	}

	public JSplitPane getContentPane() {
		return contentPane;
	}

	public void setUpdater(Runnable updater) {
		this.updater = updater;
	}

	@Override
	protected void updateContent() {
		updater.run();
	}

	private JPopupMenu producePopupMenu() {
		JPopupMenu popup = new JPopupMenu();

		JMenuItem save = new JMenuItem(Configed.getResourceValue("save"));
		Icons.addIntellijIconToMenuItem(save, "save");
		save.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

		save.addActionListener(actionEvent -> ChangedDataManager.checkSaveAll(false));

		popup.add(save);

		JMenuItem itemOnDemand = new JMenuItem(Configed.getResourceValue("ConfigedMain.Opsiclientd.executeAll"));
		Icons.addIntellijIconToMenuItem(itemOnDemand, "run");
		itemOnDemand.addActionListener(actionEvent -> saveAndExecuteAction());
		itemOnDemand.setEnabled(type != ProductSettingsType.NETBOOT_PRODUCT_SETTINGS
				&& !persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		popup.add(itemOnDemand);

		JMenuItem itemOnDemandForSelectedProducts = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Opsiclientd.executeSelected"));
		Icons.addIntellijIconToMenuItem(itemOnDemandForSelectedProducts, "run");
		itemOnDemandForSelectedProducts
				.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());
		itemOnDemandForSelectedProducts.addActionListener(
				actionEvent -> ServerActionManager.processActionRequestsSelectedProducts(groupPanel.getVisibility()));
		itemOnDemandForSelectedProducts.setEnabled(type != ProductSettingsType.NETBOOT_PRODUCT_SETTINGS
				&& !persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		popup.add(itemOnDemandForSelectedProducts);

		popup.addSeparator();

		JMenu resetProductsMenu;
		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			resetProductsMenu = ClientMenuManager.createResetLocalbootProductsMenuItemsTo();
		} else {
			resetProductsMenu = ClientMenuManager.createResetNetbootProductsMenuItemsTo();
		}
		resetProductsMenu.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());
		popup.add(resetProductsMenu);

		popup.addSeparator();

		JMenuItem reload = new JMenuItem(Configed.getResourceValue("reload"));
		reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		Icons.addIntellijIconToMenuItem(reload, "refresh");
		reload.addActionListener(actionEvent -> reloadAction());
		popup.add(reload);

		JMenuItem createReport = new JMenuItem(Configed.getResourceValue("PanelProductSettings.pdf"));
		Icons.addThemeIconInvertedToMenuItem(createReport, "anyType");
		createReport.addActionListener(actionEvent -> createReport());
		popup.add(createReport);

		ExporterToCSV exportTable = new ExporterToCSV(productTableModified.getTableViewComponent().getTable());
		exportTable.addMenuItemsTo(popup);

		jMenuVisibleColumns = new JMenu(Configed.getResourceValue("ConfigedMain.columnVisibility"));
		popup.addSeparator();
		popup.add(jMenuVisibleColumns);

		for (Entry<String, Boolean> productDisplayField : getProductDisplayFieldsBasedOnType(type).entrySet()) {
			if ("productId".equals(productDisplayField.getKey())) {
				// fixed column
				continue;
			}

			JCheckBoxMenuItem item = new JCheckBoxMenuItem();
			item.setText(ProductTableModified.getColumnTitle(productDisplayField.getKey()));
			item.setState(productDisplayField.getValue());
			item.addItemListener(itemEvent -> productTableModified.getTableViewComponent()
					.dispatch(new GenericTableViewMsg.ToggleColumn(productDisplayField.getKey())));

			jMenuVisibleColumns.add(item);
		}
		return popup;
	}

	private Map<String, Boolean> getProductDisplayFieldsBasedOnType(ProductSettingsType type) {
		return type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS
				? persistenceController.getDataServices().product.getProductOnClientsDisplayFieldsLocalbootProducts()
				: persistenceController.getDataServices().product.getProductOnClientsDisplayFieldsNetbootProducts();
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
		metaData.put("title", title);
		metaData.put("keywords", "product settings");

		// only relevent rows
		ExporterToPDF pdfExportTable = new ExporterToPDF(productTableModified.getStrippedTable());

		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();

		// create pdf
		pdfExportTable.execute(null, false);
	}

	private void reloadAction() {
		ConfigedMain.getMainFrame().activateLoadingCursor();

		persistenceController.reloadData(ReloadEvent.PRODUCT_DATA_RELOAD.toString());

		// We want to rebuild the shown page in the client configuration after reload
		ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().stateChanged(null);
		ChangedDataManager.setDataChanged(false);

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private void saveAndExecuteAction() {
		Logging.info(this, "saveAndExecuteAction");
		ChangedDataManager.checkSaveAll(false);
		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTY_STATES.toString());
		ServerActionManager.processActionRequestsAllProducts(groupPanel.getVisibility());
	}

	public boolean isFilteredBySelection() {
		return groupPanel.isFilteredBySelection();
	}

	public void valueChanged(boolean doSelection) {
		// We want to deactivate filter before changing something
		groupPanel.setFilterMark(false);
		productTableModified.valueChanged(doSelection,
				productTree.filterMostSpecificNodes(productTree.extractNodes(productTree.getSelectionPaths())));
	}

	public void setData(List<String> selectedClients, Set<String> productNames,
			Map<String, List<Map<String, String>>> statesAndActions,
			Map<String, Map<String, Object>> globalProductInfos, Map<String, List<String>> possibleActions,
			Map<String, Map<String, Map<String, String>>> changedProductStates) {
		List<Map<String, Object>> rowData = productTableModified.computeDisplayRows(selectedClients, productNames,
				statesAndActions, globalProductInfos, changedProductStates, possibleActions);
		productTableModified.getTableViewComponent().dispatch(new GenericTableViewMsg.ChangeOriginalSnapshot(rowData));
	}

	public void initEditing(String productID, Collection<Map<String, Object>> storableProductProperties,
			Map<String, Object> editableProductProperties, ProductpropertiesUpdateCollection updateCollection,
			Map<String, Object> originalMap) {
		infoPane.setProductId(productID);
		infoPane.setProductName(persistenceController.getDataServices().product.getProductTitle(productID));
		infoPane.setProductInfo(persistenceController.getDataServices().product.getProductInfo(productID));
		infoPane.setProductVersion(persistenceController.getDataServices().product.getProductVersion(productID)
				+ ProductDataService.FOR_DISPLAY
				+ persistenceController.getDataServices().product.getProductPackageVersion(productID) + "   "
				+ persistenceController.getDataServices().product.getProductLockedInfo(productID));

		infoPane.setProductAdvice(persistenceController.getDataServices().product.getProductAdvice(productID));

		propertiesPanel.setOriginalMap(originalMap);
		propertiesPanel.setEditableMap(editableProductProperties,
				persistenceController.getDataServices().product.getProductPropertyOptionsMap(productID));
		propertiesPanel.setStoreData(storableProductProperties);
		propertiesPanel.setUpdateCollection(updateCollection);
	}

	public void clearEditing() {
		propertiesPanel.setEditableMap(null, null);
		propertiesPanel.setStoreData(null);
		propertiesPanel.setUpdateCollection(null);
		infoPane.clearEditing();
	}

	public ProductTableModified getProductTableModified() {
		return productTableModified;
	}

	public JTable getTable() {
		return productTableModified.getTableViewComponent().getTable();
	}
}
