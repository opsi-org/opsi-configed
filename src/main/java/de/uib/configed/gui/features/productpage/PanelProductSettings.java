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
import java.util.Map;
import java.util.Map.Entry;

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
import de.uib.configed.gui.data.InstallationStateTableModel;
import de.uib.configed.gui.features.productgroup.ProductActionPanel;
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

	private ProductTable productTable;
	private ProductSettingsTableModel productSettingsTableModel;

	private ProductActionPanel groupPanel;

	private ProductInfoPane infoPane;
	private KeyValueTable propertiesPanel;

	private ProductTree productTree;

	private JSplitPane contentPane;

	private ConfigedMain configedMain;
	private Runnable updater;

	private ProductSettingsType type;

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
		leftPane.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[]0"));
		leftPane.add(groupPanel, "growx");
		leftPane.add(paneProducts, "grow, push, hmin 100");

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

		productTable.addMouseListener(new PopupMouseListener(producePopupMenu()));

		productTable.getTableHeader()
				.addMouseListener(new PopupMouseListener(ClientMenuManager.getPopupMenuClone(jMenuVisibleColumns)));

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

		ExporterToCSV exportTable = new ExporterToCSV(productTable);
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
			item.setText(InstallationStateTableModel.getColumnTitle(productDisplayField.getKey()));
			item.setState(productDisplayField.getValue());
			item.addItemListener(itemEvent -> toggleDisplayField(productDisplayField));

			jMenuVisibleColumns.add(item);
		}
		return popup;
	}

	private void toggleDisplayField(Entry<String, Boolean> productDisplayField) {
		getProductDisplayFieldsBasedOnType(type).put(productDisplayField.getKey(), !productDisplayField.getValue());
		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTY_STATES.toString());

		// We need to rebuild the shown page in the client configuration to make changes effective
		ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().stateChanged(null);
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
			Logging.debug(this, "selected modelIndex ", selectedRow);
			Logging.debug(this, "selected  value at ", productTable.getValueAt(selectedRow, 0));
			ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().getProductPageManager()
					.setProductEdited((String) productTable.getValueAt(selectedRow, 0), this);
		}

		productTree.produceActiveParents();

		productTree.updateSelectedObjectsInTable();
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
		productTable.valueChanged(doSelection,
				productTree.filterMostSpecificNodes(productTree.extractNodes(productTree.getSelectionPaths())));
	}

	public void setTableModel(InstallationStateTableModel istm) {
		// delete old row sorter before setting new model
		productTable.setModel(istm);
		productSettingsTableModel.setRenderer(istm);

		// We don't want to call setSelection here, since it will be called after this method
		if (!isFilteredBySelection()) {
			valueChanged(false);
		}

		Logging.debug(this, " tableProducts columns  count ", productTable.getColumnCount());
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

	public ProductTable getProductTable() {
		return productTable;
	}
}
