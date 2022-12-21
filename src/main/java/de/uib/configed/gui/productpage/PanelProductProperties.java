package de.uib.configed.gui.productpage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

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
	// JScrollPane paneProducts;
	public PanelGenEditTable paneProducts;
	protected OpsiPackage selectedOpsiPackage;
	protected java.util.List<String> depotsOfPackage;

	private ProductInfoPane infoPane; // right pane
	protected PanelEditDepotProperties panelEditProperties;
	public AbstractEditMapPanel propertiesPanel;

	protected int hMin = 200;

	final int fwidth_lefthanded = 600;
	final int splitterLeftRight = 15;
	final int fheight = 450;

	final int fwidth_column_productname = 170;
	final int fwidth_column_productcompletename = 170;

	protected TableCellRenderer propertiesTableCellRenderer;

	// protected final Map<String,Object> emptyVisualData = new
	// HashMap<String,Object>();

	protected LinkedHashMap<String, Boolean> productDisplayFields;

	protected ArrayList<String> selectedProducts;

	JPopupMenu popup;

	protected ConfigedMain mainController;

	public PanelProductProperties(ConfigedMain mainController) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.mainController = mainController;
		init();

		setDividerLocation(fwidth_lefthanded);
		setResizeWeight(0.5);
	}

	protected void init() {
		// tableProducts = new JTable();
		// tableProducts.setDragEnabled(true);

		selectedProducts = new ArrayList<>();

		depotsOfPackage = new ArrayList<>();

		TableUpdateCollection updateCollection = new TableUpdateCollection();
		GenTableModel model = new GenTableModel(null, mainController.globalProductsTableProvider, -1,
				(TableModelListener) paneProducts, updateCollection);

		final Vector<String> columnNames = model.getColumnNames();

		paneProducts = new PanelGenEditTable("", 0, false, 0, false, PanelGenEditTable.POPUPS_MINIMAL, true) {

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
					// initAllProperties();
					logging.info(this, "selected  no row ");

					// infoPane.clearEditValue
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
						selectedOpsiPackage = null;
						depotsOfPackage.clear();
					} else {
						String productEdited = "" + theTable.getValueAt(row, columnNames.indexOf("productId"));

						String depotId = ""
						// + theTable.getValueAt(row, columnNames.indexOf("depotId"))
						;

						logging.info(this, "selected  depotId, product: " + depotId + ", " + productEdited);

						/*
						 * logging.info(this, "package " + theTable.getValueAt(row,
						 * columnNames.indexOf("productId")) + "; "
						 * + theTable.getValueAt(row, columnNames.indexOf("productVersion")) + "; "
						 * + theTable.getValueAt(row, columnNames.indexOf("packageVersion"))
						 * );
						 */
						/*
						 * markAllWith(
						 * row,
						 * theTable.getValueAt(row, columnNames.indexOf("productId")),
						 * theTable.getValueAt(row, columnNames.indexOf("productVersion")),
						 * theTable.getValueAt(row, columnNames.indexOf("packageVersion"))
						 * );
						 */
						// depotsOfPackage.clear();
						java.util.List<String> depotsOfPackageAsRetrieved = new ArrayList<>();

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

						/*
						 * selectedOpsiPackage = new OpsiPackage(
						 * "" + theTable.getValueAt(row, columnNames.indexOf("productId")),
						 * "" + theTable.getValueAt(row, columnNames.indexOf("productVersion")),
						 * "" + theTable.getValueAt(row, columnNames.indexOf("packageVersion")),
						 * "" + theTable.getValueAt(row, columnNames.indexOf("productType"))
						 * );
						 * logging.info(this, "selected " + selectedOpsiPackage + "\ndepots "+
						 * depotsOfPackage);
						 */

						logging.debug(this, "selectedRowChanged depotsOfPackage " + depotsOfPackage);

						infoPane.clearEditing();
						if (depotsOfPackage != null && depotsOfPackage.size() > 0) {

							infoPane.setEditValues(productEdited,
									"" + theTable.getValueAt(row, columnNames.indexOf("productVersion")),
									"" + theTable.getValueAt(row, columnNames.indexOf("packageVersion")),
									depotsOfPackage.get(0));
						} ;

						/*
						 * we leave setting the properties to panelEditProperties
						 * 
						 * Map<String, Object> visualData = mainController.getPersistenceController()
						 * .getDefaultProductProperties(depotId).get(productEdited);
						 * 
						 * //we could merge the properties for all marked depots
						 * //but prefer to display the properties for the selected depot
						 * 
						 * 
						 * if (visualData == null) //no properties
						 * {
						 * //produce empty map
						 * visualData = emptyVisualData;
						 * }
						 * 
						 * propertiesPanel.setEditableMap(
						 * 
						 * visualData,
						 * 
						 * mainController.getPersistenceController()
						 * .getProductPropertyOptionsMap(depotId, productEdited)
						 * );
						 * 
						 */

						panelEditProperties.setDepotListData(depotsOfPackage, productEdited);

					}
				}

			}

			/*
			 * private void markAllWith(int row, Object productId, Object productVersion,
			 * Object packageVersion)
			 * {
			 * depotsOfPackage.clear();
			 * 
			 * followSelectionListener = false;
			 * theTable.setRowSelectionInterval(row, row);
			 * for (int i = 0; i < theTable.getRowCount(); i++)
			 * {
			 * if (
			 * 
			 * theTable.getValueAt(i, columnNames.indexOf("productId")).equals(productId)
			 * &&
			 * theTable.getValueAt(i,
			 * columnNames.indexOf("productVersion")).equals(productVersion)
			 * &&
			 * theTable.getValueAt(i,
			 * columnNames.indexOf("packageVersion")).equals(packageVersion)
			 * )
			 * {
			 * theTable.addRowSelectionInterval(i, i);
			 * depotsOfPackage.add("" + theTable.getValueAt(i,
			 * columnNames.indexOf("depotId")));
			 * }
			 * }
			 * theTable.addRowSelectionInterval(row,row);
			 * followSelectionListener = true;
			 * }
			 */

			/*
			 * private Map<String, Object> mergeProperties(
			 * Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties,
			 * java.util.List depots,
			 * String productId) {
			 * Map<String, Object> result = new HashMap<>();
			 * 
			 * if (depots == null || depots.size() == 0)
			 * return result;
			 * 
			 * // Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties
			 * =
			 * // mainController.getPersistenceController().getDepot2product2properties();
			 * 
			 * Map<String, ConfigName2ConfigValue> propertiesDepot0 =
			 * depot2product2properties.get(depots.get(0));
			 * 
			 * if (depots.size() == 1) {
			 * if (propertiesDepot0 == null || propertiesDepot0.get(productId) == null) {
			 * // ready
			 * } else {
			 * result = propertiesDepot0.get(productId);
			 * }
			 * } else {
			 * int n = 0;
			 * 
			 * while (n < depots.size() && (depot2product2properties.get(depots.get(n)) ==
			 * null
			 * || depot2product2properties.get(depots.get(n)).get(productId) == null)) {
			 * n++;
			 * }
			 * 
			 * if (n == depots.size()) {
			 * // ready
			 * } else {
			 * // create start mergers
			 * ConfigName2ConfigValue properties =
			 * depot2product2properties.get(depots.get(n)).get(productId);
			 * 
			 * for (String key : properties.keySet()) {
			 * java.util.List value = (java.util.List) properties.get(key);
			 * result.put(key, new ListMerger(value));
			 * }
			 * 
			 * // merge the other depots
			 * for (int i = 1; i < depots.size(); i++) {
			 * properties = depot2product2properties.get(depots.get(i)).get(productId);
			 * 
			 * for (String key : properties.keySet()) {
			 * java.util.List value = (java.util.List) properties.get(key);
			 * if (result.get(key) == null)
			 * // we need a new property. it is not common
			 * {
			 * ListMerger merger = new ListMerger(value);
			 * // logging.debug(this, " new property, merger " + merger);
			 * merger.setHavingNoCommonValue();
			 * result.put(key, merger);
			 * } else {
			 * ListMerger merger = (ListMerger) result.get(key);
			 * result.put(key, merger.merge(value));
			 * }
			 * }
			 * }
			 * }
			 * }
			 * 
			 * return result;
			 * 
			 * }
			 */

		};

		paneProducts.setTableModel(model);
		// paneProducts.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		paneProducts.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// paneProducts.setTableColumnInvisible( columnNames.indexOf( "depotId" ));

		LinkedHashMap<Integer, SortOrder> sortDescriptor = new LinkedHashMap<Integer, SortOrder>();
		sortDescriptor.put(columnNames.indexOf("productId"), SortOrder.ASCENDING); // productId
		sortDescriptor.put(columnNames.indexOf("productVersion"), SortOrder.ASCENDING); // productId
		sortDescriptor.put(columnNames.indexOf("packageVersion"), SortOrder.ASCENDING); // productId
		// sortDescriptor.put(columnNames.indexOf("depotId"), SortOrder.ASCENDING);
		// //productId

		paneProducts.setSortOrder(sortDescriptor);

		setLeftComponent(paneProducts);

		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, false, false);
		// propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false,
		// true, true, EditMapPanelX.PropertyHandlerType.REMOVE_CLIENT_SPECIFIC_VALUE );
		logging.info(this, " created properties Panel, is  EditMapPanelX instance No. " + EditMapPanelX.objectCounter);
		((EditMapPanelX) propertiesPanel)
				.setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName().toString()));
		propertiesPanel.registerDataChangedObserver(mainController.getGeneralDataChangedKeeper());
		propertiesPanel.setStoreData(null);
		propertiesPanel.setUpdateCollection(null);

		// ((EditMapPanelX) propertiesPanel).setPropertyHandlerType(
		// EditMapPanelX.PropertyHandlerType.REMOVE_CLIENT_SPECIFIC_VALUE );

		panelEditProperties = new PanelEditDepotProperties(mainController, propertiesPanel);
		infoPane = new ProductInfoPane(mainController, panelEditProperties);

		infoPane.getPanelProductDependencies().setDependenciesModel(mainController.getDependenciesModel());

		setRightComponent(infoPane);
		// setDividerLocation(800);

		// paneProducts.addMouseListener(new utils.PopupMouseListener(popup));
		// tableProducts.addMouseListener(new utils.PopupMouseListener(popup));
	}

	/*
	 * public void initAllProperties()
	 * {
	 * propertiesPanel.init();
	 * infoPane.setInfo("");
	 * infoPane.setAdvice("");
	 * }
	 */

	/*
	 * protected void reloadAction()
	 * {
	 * //mainController.requestReloadStatesAndActions();
	 * //mainController.getPersistenceController().
	 * productPropertyDefinitionsRequestRefresh();
	 * //mainController.getPersistenceController().productpropertiesRequestRefresh()
	 * ;
	 * 
	 * mainController.resetView(mainController.getViewIndex());
	 * mainController.setDataChanged(false);
	 * }
	 * 
	 * 
	 * public void requestReload()
	 * {
	 * paneProducts.requestReload();
	 * }
	 * 
	 */

}
