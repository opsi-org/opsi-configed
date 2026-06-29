/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.AbstractConfigurationTab;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.datapanel.KeyValueTable;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.SplitPaneStateManager;
import de.uib.configed.share.logging.Logging;

public class PanelProductProperties extends AbstractConfigurationTab {
	private PanelGenEdit paneProducts;
	private ProductInfoPane infoPane;
	private ConfigedMain configedMain;
	private DepotsList depotsList;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelProductProperties(ConfigedMain configedMain, DepotsList depotsList) {
		super(true, false);
		this.configedMain = configedMain;
		this.depotsList = depotsList;

		init();
	}

	private void init() {
		GenTableModel model = createTableModel();
		final List<String> columnNames = model.getColumnNames();

		KeyValueTable propertiesPanel = new KeyValueTable(false, false);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		propertiesPanel.registerDataChangedKeeper(ChangedDataManager.getGeneralDataChangedKeeper());
		propertiesPanel.setStoreData(null);
		propertiesPanel.setUpdateCollection(null);

		PanelEditDepotProperties panelEditProperties = new PanelEditDepotProperties(configedMain, propertiesPanel,
				depotsList);
		paneProducts = new PaneProducts(columnNames, panelEditProperties, propertiesPanel);
		paneProducts.setTableModel(model);
		paneProducts.getGenEditTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneProducts.setFilterKey(FilterKey.DEPOT_PRODUCT_PROPERTIES_TABLE);

		List<SortKey> sortDescriptor = new ArrayList<>();
		sortDescriptor.add(new SortKey(columnNames.indexOf("productId"), SortOrder.ASCENDING));
		sortDescriptor.add(new SortKey(columnNames.indexOf("productVersion"), SortOrder.ASCENDING));
		sortDescriptor.add(new SortKey(columnNames.indexOf("packageVersion"), SortOrder.ASCENDING));

		paneProducts.setSortOrder(sortDescriptor);

		infoPane = new ProductInfoPane(panelEditProperties, null);
		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paneProducts, infoPane);
		splitPane.setResizeWeight(1.0);

		SplitPaneStateManager.registerSplitPane(splitPane, SplitPaneStateManager.PRODUCT_PROPERTIES_SPLIT, 0.8F);

		paneProducts.getGenEditTable().addMouseListener(new PopupMouseListener(createPopupMenu()));

		setComponent(splitPane);
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu popup = new JPopupMenu();

		JMenuItem save = new JMenuItem(Configed.getResourceValue("save"));
		Icons.addIntellijIconToMenuItem(save, "save");
		save.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

		save.addActionListener(actionEvent -> ChangedDataManager.checkSaveAll(false));

		popup.add(save);

		JMenuItem reload = new JMenuItem(Configed.getResourceValue("reload"));
		reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		Icons.addIntellijIconToMenuItem(reload, "refresh");
		reload.addActionListener(actionEvent -> paneProducts.reload());
		popup.add(reload);

		popup.addSeparator();

		JMenuItem menuItemSortAgain = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.sortAsConfigured"));
		menuItemSortAgain.addActionListener(actionEvent -> paneProducts.getGenEditTable().sortAgainAsConfigured());
		popup.add(menuItemSortAgain);

		popup.addSeparator();

		ExporterToCSV exportTable = new ExporterToCSV(paneProducts.getGenEditTable());
		JMenuItem menuItemExportCSV = exportTable.getMenuItemExport();
		popup.add(menuItemExportCSV);

		JMenuItem menuItemExportSelectedCSV = exportTable.getMenuItemExportSelected();
		popup.add(menuItemExportSelectedCSV);

		return popup;
	}

	private GenTableModel createTableModel() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("productId");
		columnNames.add("productName");
		columnNames.add(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE);
		columnNames.add(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
		columnNames.add(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
		columnNames.add(OpsiPackage.SERVICE_KEY_LOCKED);

		return new GenTableModel(null,
				DefaultTableProvider.createWithExternalSource(columnNames, depotsList.getSelectedValuesList()), -1,
				paneProducts, new ArrayList<>());
	}

	@Override
	public void updateContent() {
		Logging.info(this, "updateContent()");
		setProductProperties();
		paneProducts.restoreFilter();
	}

	public void setProductProperties() {
		paneProducts.setTableModel(createTableModel());
		int saveSelectedRow = paneProducts.getGenEditTable().getSelectedRow();
		paneProducts.getTableModel().reset();

		if (paneProducts.getGenEditTable().getRowCount() > 0) {
			if (saveSelectedRow == -1 || paneProducts.getGenEditTable().getRowCount() <= saveSelectedRow) {
				paneProducts.setSelectedRow(0);
			} else {
				paneProducts.setSelectedRow(saveSelectedRow);
			}
		}
	}

	@SuppressWarnings({ "java:S2972" })
	private class PaneProducts extends PanelGenEdit {
		private List<String> columnNames;
		private List<String> depotsOfPackage;
		private PanelEditDepotProperties panelEditProperties;
		private KeyValueTable propertiesPanel;

		public PaneProducts(List<String> columnNames, PanelEditDepotProperties panelEditDepotProperties,
				KeyValueTable propertiesPanel) {
			super("", false, 0, null, true);
			this.columnNames = columnNames;
			this.depotsOfPackage = new ArrayList<>();
			this.panelEditProperties = panelEditDepotProperties;
			this.propertiesPanel = propertiesPanel;

			super.setMinimumSize(new Dimension());
			tableSearchPane.setFiltering();
		}

		@Override
		public void reload() {
			Logging.info(this, "reload()");
			ConfigedMain.getMainFrame().activateLoadingCursor();
			if (!CacheIdentifier.ALL_DATA.toString().equals(persistenceController.getTriggeredEvent())) {
				persistenceController.reloadData(ReloadEvent.DEPOT_PRODUCT_PROPERTIES_DATA_RELOAD.toString());
			}
			int[] selectedRows = genEditTable.getSelectedRows();
			super.reload();
			genEditTable.setRowSelectionInterval(selectedRows[0], selectedRows[selectedRows.length - 1]);
			ConfigedMain.getMainFrame().deactivateLoadingCursor();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			super.valueChanged(e);

			Logging.debug(this, "valueChanged in paneProducts ", e);

			if (e.getValueIsAdjusting()) {
				return;
			}

			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			if (lsm.getSelectedItemsCount() == 1) {
				updateInfoPane(lsm.getMinSelectionIndex());
			} else {
				Logging.info(this, "selected not a unique row ");
				infoPane.clearEditing();
				propertiesPanel.init();
				panelEditProperties.clearDepotListData();
			}
		}

		private void updateInfoPane(int row) {
			Logging.info(this, "selected  row ", row);

			if (row == -1) {
				depotsOfPackage.clear();
			} else {
				String productEdited = "" + genEditTable.getValueAt(row, columnNames.indexOf("productId"));

				Logging.info(this, "selected  product: ", productEdited);

				String versionInfo = OpsiPackage.produceVersionInfo(
						"" + genEditTable.getValueAt(row, columnNames.indexOf("productVersion")),
						"" + genEditTable.getValueAt(row, columnNames.indexOf("packageVersion")));

				List<String> depotsOfPackageAsRetrieved = persistenceController.getDataServices().product
						.getProduct2VersionInfo2DepotsPD()
						.get(genEditTable.getValueAt(row, columnNames.indexOf("productId"))).get(versionInfo);

				Logging.info(this, "valueChanged  versionInfo ", versionInfo);

				depotsOfPackage = new ArrayList<>();

				for (String depot : persistenceController.getDataServices().hostInfoCollections.getDepots().keySet()) {
					if (depotsOfPackageAsRetrieved.indexOf(depot) > -1) {
						depotsOfPackage.add(depot);
					}
				}

				Logging.debug(this, "selectedRowChanged depotsOfPackage ", depotsOfPackage);

				if (!depotsOfPackage.isEmpty()) {
					infoPane.setEditValues(productEdited,
							"" + genEditTable.getValueAt(row, columnNames.indexOf("productVersion")),
							"" + genEditTable.getValueAt(row, columnNames.indexOf("packageVersion")),
							depotsOfPackage.get(0));
				}

				panelEditProperties.setDepotListData(depotsOfPackage, productEdited);
			}
		}
	}
}
