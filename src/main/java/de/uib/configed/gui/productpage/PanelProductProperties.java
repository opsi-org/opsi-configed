package de.uib.configed.gui.productpage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;

/*
 *   class PanelProductProperties 
 *   for editing depot specific product settings
 *   part of:
 *
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2017 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foun
 *
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.helper.PropertiesTableCellRenderer;
import de.uib.configed.type.OpsiPackage;
import de.uib.utilities.datapanel.AbstractEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class PanelProductProperties extends JSplitPane
// implements RowSorterListener
{

	public PanelGenEditTable paneProducts;
	private List<String> depotsOfPackage;

	private ProductInfoPane infoPane; // right pane
	protected PanelEditDepotProperties panelEditProperties;
	public AbstractEditMapPanel propertiesPanel;

	protected ConfigedMain mainController;

	public PanelProductProperties(ConfigedMain mainController) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.mainController = mainController;
		init();

		setResizeWeight(0.7);
	}

	protected void init() {

		depotsOfPackage = new ArrayList<>();

		TableUpdateCollection updateCollection = new TableUpdateCollection();
		GenTableModel model = new GenTableModel(null, mainController.globalProductsTableProvider, -1, paneProducts,
				updateCollection);

		final List<String> columnNames = model.getColumnNames();

		paneProducts = new PaneProducts(columnNames);

		paneProducts.setTableModel(model);

		paneProducts.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		LinkedHashMap<Integer, SortOrder> sortDescriptor = new LinkedHashMap<>();
		sortDescriptor.put(columnNames.indexOf("productId"), SortOrder.ASCENDING); // productId
		sortDescriptor.put(columnNames.indexOf("productVersion"), SortOrder.ASCENDING); // productId
		sortDescriptor.put(columnNames.indexOf("packageVersion"), SortOrder.ASCENDING); // productId

		paneProducts.setSortOrder(sortDescriptor);

		setLeftComponent(paneProducts);

		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, false, false);

		logging.info(this, " created properties Panel, is  EditMapPanelX instance No. " + EditMapPanelX.objectCounter);
		((EditMapPanelX) propertiesPanel)
				.setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName()));
		propertiesPanel.registerDataChangedObserver(mainController.getGeneralDataChangedKeeper());
		propertiesPanel.setStoreData(null);
		propertiesPanel.setUpdateCollection(null);

		panelEditProperties = new PanelEditDepotProperties(mainController, propertiesPanel);
		infoPane = new ProductInfoPane(mainController, panelEditProperties);

		infoPane.getPanelProductDependencies().setDependenciesModel(mainController.getDependenciesModel());

		setRightComponent(infoPane);
	}

	private class PaneProducts extends PanelGenEditTable {

		private List<String> columnNames;

		public PaneProducts(List<String> columnNames) {
			super("", 0, false, 0, false, PanelGenEditTable.POPUPS_MINIMAL, true);

			this.columnNames = columnNames;
		}

		@Override
		public void reload() {
			logging.info(this, "reload()");

			mainController.getPersistenceController().productPropertyDefinitionsRequestRefresh();
			mainController.getPersistenceController().productpropertiesRequestRefresh();
			super.reload();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			super.valueChanged(e);

			logging.debug(this, "valueChanged in paneProducts " + e);

			if (e.getValueIsAdjusting())
				return;

			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			if (lsm.isSelectionEmpty()) {

				logging.info(this, "selected  no row ");

			}

			if (lsm.isSelectionEmpty() || lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex()) {
				infoPane.clearEditing();
				infoPane.setGrey(true);
			} else
				infoPane.setGrey(false);

			// otherweise selectedRowChanged() works
		}

		@Override
		public void selectedRowChanged()
		// if we got a new selection
		{
			super.selectedRowChanged();

			logging.debug(this, "selectedRowChanged in paneProducts ");

			ListSelectionModel lsm = getListSelectionModel();

			if (lsm.isSelectionEmpty() || lsm.getMinSelectionIndex() != lsm.getMaxSelectionIndex()) {
				logging.info(this, "selected not a unique row ");
				infoPane.clearEditing();
				((EditMapPanelX) propertiesPanel).init();
				panelEditProperties.clearDepotListData();
			}

			else {

				infoPane.setGrey(false);
				int row = lsm.getMinSelectionIndex();

				logging.info(this, "selected  row " + row);

				if (row == -1) {
					depotsOfPackage.clear();
				} else {
					String productEdited = "" + theTable.getValueAt(row, columnNames.indexOf("productId"));

					String depotId = "";

					logging.info(this, "selected  depotId, product: " + depotId + ", " + productEdited);

					List<String> depotsOfPackageAsRetrieved = new ArrayList<>();

					String versionInfo = "";
					boolean retrieval = true;

					try {
						versionInfo = OpsiPackage.produceVersionInfo(
								"" + theTable.getValueAt(row, columnNames.indexOf("productVersion")),
								"" + theTable.getValueAt(row, columnNames.indexOf("packageVersion")));

						depotsOfPackageAsRetrieved = mainController.getPersistenceController()
								.getProduct2VersionInfo2Depots()
								.get(theTable.getValueAt(row, columnNames.indexOf("productId"))).get(versionInfo);

						logging.info(this, "valueChanged  versionInfo (depotsOfPackageAsRetrieved == null)  "
								+ versionInfo + " " + (depotsOfPackageAsRetrieved == null));

					} catch (Exception ex) {
						retrieval = false;
					}

					if (retrieval // no exception
							&& (depotsOfPackageAsRetrieved == null))
						retrieval = false;

					depotsOfPackage = new LinkedList<>();

					if (retrieval) {
						for (String depot : mainController.getPersistenceController().getHostInfoCollections()
								.getDepots().keySet()) {
							if (depotsOfPackageAsRetrieved.indexOf(depot) > -1)
								depotsOfPackage.add(depot);
						}
					}

					logging.debug(this, "selectedRowChanged depotsOfPackage " + depotsOfPackage);

					infoPane.clearEditing();
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
}
