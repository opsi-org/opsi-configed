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
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.ExternalSource;
import de.uib.utilities.table.updates.MapBasedTableEditItem;

public class PanelProductProperties extends JSplitPane {
	private PanelGenEditTable paneProducts;
	private List<String> depotsOfPackage;

	// right pane
	private ProductInfoPane infoPane;
	private PanelEditDepotProperties panelEditProperties;
	private EditMapPanelX propertiesPanel;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelProductProperties(ConfigedMain configedMain) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.configedMain = configedMain;
		init();
	}

	private void init() {
		depotsOfPackage = new ArrayList<>();

		GenTableModel model = createTableModel();
		final List<String> columnNames = model.getColumnNames();

		paneProducts = new PaneProducts(columnNames);
		paneProducts.setTableModel(model);
		paneProducts.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		Map<Integer, SortOrder> sortDescriptor = new LinkedHashMap<>();
		sortDescriptor.put(columnNames.indexOf("productId"), SortOrder.ASCENDING); // productId
		sortDescriptor.put(columnNames.indexOf("productVersion"), SortOrder.ASCENDING); // productId
		sortDescriptor.put(columnNames.indexOf("packageVersion"), SortOrder.ASCENDING); // productId

		paneProducts.setSortOrder(sortDescriptor);

		setLeftComponent(paneProducts);

		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, false, false);

		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		propertiesPanel.setCellEditor(new SensitiveCellEditorForDataPanel());
		propertiesPanel.registerDataChangedObserver(configedMain.getGeneralDataChangedKeeper());
		propertiesPanel.setStoreData(null);
		propertiesPanel.setUpdateCollection(null);

		panelEditProperties = new PanelEditDepotProperties(configedMain, propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties);

		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());

		setRightComponent(infoPane);
	}

	private void updateModel() {
		paneProducts.setTableModel(createTableModel());
	}

	private GenTableModel createTableModel() {
		List<String> columnNames = new ArrayList<>();

		columnNames.add("productId");
		columnNames.add("productName");

		// from OpsiPackage.appendValues
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
		updateModel();
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

	private class PaneProducts extends PanelGenEditTable {
		private List<String> columnNames;

		public PaneProducts(List<String> columnNames) {
			super("", false, 0, PanelGenEditTable.POPUPS_MINIMAL, true);

			this.columnNames = columnNames;
		}

		@Override
		public void reload() {
			Logging.info(this, "reload()");
			super.reload();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			super.valueChanged(e);

			Logging.debug(this, "valueChanged in paneProducts " + e);

			if (!e.getValueIsAdjusting()) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			}
		}

		@Override
		public void selectedRowChanged() {
			// if we got a new selection
			Logging.debug(this, "selectedRowChanged in paneProducts ");

			ListSelectionModel lsm = getListSelectionModel();

			if (lsm.isSelectionEmpty() || lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex()) {
				Logging.info(this, "selected not a unique row ");
				infoPane.clearEditing();
				propertiesPanel.init();
				panelEditProperties.clearDepotListData();
			} else {
				int row = lsm.getMinSelectionIndex();

				updateInfoPane(row);
			}
		}

		private void updateInfoPane(int row) {
			Logging.info(this, "selected  row " + row);

			if (row == -1) {
				depotsOfPackage.clear();
			} else {
				String productEdited = "" + theTable.getValueAt(row, columnNames.indexOf("productId"));

				String depotId = "";

				Logging.info(this, "selected  depotId, product: " + depotId + ", " + productEdited);

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
