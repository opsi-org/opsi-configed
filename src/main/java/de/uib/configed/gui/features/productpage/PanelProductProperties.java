/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.AbstractClientConfigurationTab;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.ClientConfiguration;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.share.datapanel.EditMapPanelX;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.PanelGenEditPopupManager;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.provider.ExternalSource;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.logging.Logging;

public class PanelProductProperties extends AbstractClientConfigurationTab implements AncestorListener {
	private PanelGenEdit paneProducts;
	private ProductInfoPane infoPane;
	private ConfigedMain configedMain;
	private DepotsList depotsList;

	private JSplitPane splitPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public PanelProductProperties(ConfigedMain configedMain, DepotsList depotsList) {
		super(true);
		this.configedMain = configedMain;
		this.depotsList = depotsList;

		super.addAncestorListener(this);

		init();
	}

	private void init() {
		GenTableModel model = createTableModel();
		final List<String> columnNames = model.getColumnNames();

		EditMapPanelX propertiesPanel = new EditMapPanelX(false, false, false);
		Logging.info(this, " created properties Panel, is  EditMapPanelX");
		propertiesPanel.getMapTableModel().registerDataChangedKeeper(ChangedDataManager.getGeneralDataChangedKeeper());
		propertiesPanel.updateData(null, null);

		PanelEditDepotProperties panelEditProperties = new PanelEditDepotProperties(configedMain, propertiesPanel);
		paneProducts = new PaneProducts(columnNames, panelEditProperties, propertiesPanel);
		paneProducts.setTableModel(model);
		paneProducts.getGenEditTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneProducts.setFilterKey(FilterKey.DEPOT_PRODUCT_PROPERTIES_TABLE);

		Map<Integer, SortOrder> sortDescriptor = new LinkedHashMap<>();
		sortDescriptor.put(columnNames.indexOf("productId"), SortOrder.ASCENDING);
		sortDescriptor.put(columnNames.indexOf("productVersion"), SortOrder.ASCENDING);
		sortDescriptor.put(columnNames.indexOf("packageVersion"), SortOrder.ASCENDING);

		paneProducts.setSortOrder(sortDescriptor);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(paneProducts);

		infoPane = new ProductInfoPane(panelEditProperties);
		infoPane.getPanelProductDependencies().setDependenciesModel(configedMain.getDependenciesModel());
		splitPane.setRightComponent(infoPane);
		splitPane.setResizeWeight(1.0);

		setComponent(splitPane);
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
				new DefaultTableProvider(new ExternalSource(columnNames, depotsList.getSelectedValuesList())), -1,
				paneProducts, updateCollection);
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
		private EditMapPanelX propertiesPanel;

		public PaneProducts(List<String> columnNames, PanelEditDepotProperties panelEditDepotProperties,
				EditMapPanelX propertiesPanel) {
			super("", false, 0, new int[] { PopupMenuTrait.POPUP_RELOAD, PanelGenEditPopupManager.POPUP_SORT_AGAIN },
					true);
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
			super.reload();
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

				depotsOfPackage = new LinkedList<>();

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

	@Override
	public void ancestorAdded(AncestorEvent event) {
		splitPane.setDividerLocation(ClientConfiguration.DIVIDER_LOCATION);
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
		// Not needed for this here
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
		// Not needed for this here
	}
}
