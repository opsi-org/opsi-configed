/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.configed.type.OpsiPackage;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.datapanel.EditMapPanelX;
import de.uib.utils.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.PanelGenEditTable;
import de.uib.utils.table.provider.DefaultTableProvider;
import de.uib.utils.table.provider.ExternalSource;
import de.uib.utils.table.updates.MapBasedTableEditItem;

public class PanelProductProperties extends JSplitPane {
	private PanelGenEditTable paneProducts;
	private ProductInfoPane infoPane;
	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelProductProperties(ConfigedMain configedMain) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.configedMain = configedMain;
		init();
	}

	private void init() {
		GenTableModel model = createTableModel();
		final List<String> columnNames = model.getColumnNames();

		EditMapPanelX propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, false, false);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		propertiesPanel.setCellEditor(new SensitiveCellEditorForDataPanel());
		propertiesPanel.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());
		propertiesPanel.setStoreData(null);
		propertiesPanel.setUpdateCollection(null);

		PanelEditDepotProperties panelEditProperties = new PanelEditDepotProperties(configedMain, propertiesPanel);
		paneProducts = new PaneProducts(columnNames, panelEditProperties, propertiesPanel);
		paneProducts.setTableModel(model);
		paneProducts.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		Map<Integer, SortOrder> sortDescriptor = new LinkedHashMap<>();
		sortDescriptor.put(columnNames.indexOf("productId"), SortOrder.ASCENDING);
		sortDescriptor.put(columnNames.indexOf("productVersion"), SortOrder.ASCENDING);
		sortDescriptor.put(columnNames.indexOf("packageVersion"), SortOrder.ASCENDING);

		paneProducts.setSortOrder(sortDescriptor);

		setLeftComponent(paneProducts);

		infoPane = new ProductInfoPane(panelEditProperties);
		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());
		setRightComponent(infoPane);

		setResizeWeight(1.0);
	}

	private GenTableModel createTableModel() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("productId");
		columnNames.add("productName");
		columnNames.add(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE);
		columnNames.add(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
		columnNames.add(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
		columnNames.add(OpsiPackage.SERVICE_KEY_LOCKED);

		List<MapBasedTableEditItem> updateCollection = new ArrayList<>();
		return new GenTableModel(null,
				new DefaultTableProvider(new ExternalSource(columnNames, configedMain.getSelectedDepots())), -1,
				paneProducts, updateCollection);
	}

	public void setProductProperties() {
		paneProducts.setTableModel(createTableModel());
		int saveSelectedRow = paneProducts.getSelectedRow();
		paneProducts.reset();

		if (paneProducts.getTableModel().getRowCount() > 0) {
			if (saveSelectedRow == -1 || paneProducts.getTableModel().getRowCount() <= saveSelectedRow) {
				paneProducts.setSelectedRow(0);
			} else {
				paneProducts.setSelectedRow(saveSelectedRow);
			}
		}
	}

	public void reload() {
		paneProducts.reload();
	}

	@SuppressWarnings({ "java:S2972" })
	private class PaneProducts extends PanelGenEditTable {
		private List<String> columnNames;
		private List<String> depotsOfPackage;
		private PanelEditDepotProperties panelEditProperties;
		private EditMapPanelX propertiesPanel;

		public PaneProducts(List<String> columnNames, PanelEditDepotProperties panelEditDepotProperties,
				EditMapPanelX propertiesPanel) {
			super("", false, 0, PanelGenEditTable.POPUPS_MINIMAL, true);
			this.columnNames = columnNames;
			this.depotsOfPackage = new ArrayList<>();
			this.panelEditProperties = panelEditDepotProperties;
			this.propertiesPanel = propertiesPanel;
		}

		@Override
		public void reload() {
			Logging.info(this, "reload()");
			ConfigedMain.getMainFrame().activateLoadingCursor();;
			if (!CacheIdentifier.ALL_DATA.toString().equals(persistenceController.getTriggeredEvent())) {
				persistenceController.reloadData(ReloadEvent.DEPOT_PRODUCT_PROPERTIES_DATA_RELOAD.toString());
			}
			super.reload();
			ConfigedMain.getMainFrame().deactivateLoadingCursor();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			super.valueChanged(e);

			Logging.debug(this, "valueChanged in paneProducts " + e);

			if (!e.getValueIsAdjusting()) {
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
		}

		private void updateInfoPane(int row) {
			Logging.info(this, "selected  row " + row);

			if (row == -1) {
				depotsOfPackage.clear();
			} else {
				String productEdited = "" + theTable.getValueAt(row, columnNames.indexOf("productId"));

				Logging.info(this, "selected  product: " + productEdited);

				String versionInfo = OpsiPackage.produceVersionInfo(
						"" + theTable.getValueAt(row, columnNames.indexOf("productVersion")),
						"" + theTable.getValueAt(row, columnNames.indexOf("packageVersion")));

				List<String> depotsOfPackageAsRetrieved = persistenceController.getProductDataService()
						.getProduct2VersionInfo2DepotsPD()
						.get(theTable.getValueAt(row, columnNames.indexOf("productId"))).get(versionInfo);

				Logging.info(this, "valueChanged  versionInfo " + versionInfo);

				depotsOfPackage = new LinkedList<>();

				for (String depot : persistenceController.getHostInfoCollections().getDepots().keySet()) {
					if (depotsOfPackageAsRetrieved.indexOf(depot) > -1) {
						depotsOfPackage.add(depot);
					}
				}

				Logging.debug(this, "selectedRowChanged depotsOfPackage " + depotsOfPackage);

				if (depotsOfPackage != null && !depotsOfPackage.isEmpty()) {
					infoPane.setEditValues(productEdited,
							"" + theTable.getValueAt(row, columnNames.indexOf("productVersion")),
							"" + theTable.getValueAt(row, columnNames.indexOf("packageVersion")),
							depotsOfPackage.get(0));
				}

				panelEditProperties.setDepotListData(depotsOfPackage, productEdited);
			}
		}
	}
}
