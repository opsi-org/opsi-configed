/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;

import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.productpage.PanelProductSettings.ProductSettingsType;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewComponent.TableSideEffectStrategy;
import de.uib.configed.gui.features.table.GenericTableViewEffect;
import de.uib.configed.gui.features.table.GenericTableViewModel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.features.table.RowData.RowState;
import de.uib.configed.gui.features.table.RowDiffStrategy;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.features.table.TableConfig;
import de.uib.configed.gui.features.tree.AbstractGroupTree;
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.UserPreferences;

@SuppressWarnings("java:S1200")
public class ProductTable {
	private static Map<String, String> columnDict;

	private GenericTableViewComponent tableViewComponent;
	private JComponent component;

	private ConfigedMain configedMain;

	private ProductTree productTree;
	private PanelProductSettings panelProductSettings;
	private ProductConfigurationEngine engine;

	private ProductOptionsComboBoxModeller comboBoxModeller;
	private ProductTableExtractor extractor;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ProductTable(ConfigedMain configedMain, ProductSettingsType type, ProductTree productTree,
			PanelProductSettings panelProductSettings, Supplier<PopupMouseListener> popupMouseListenerSupplier) {
		this.configedMain = configedMain;
		this.productTree = productTree;
		this.panelProductSettings = panelProductSettings;
		this.engine = new ProductConfigurationEngine(this, configedMain);

		TableSideEffectStrategy sideEffectStrategy = (GenericTableViewEffect effect) -> switch (effect) {
		case GenericTableViewEffect.Selection() -> this::applyChangedValue;
		case GenericTableViewEffect.StoreVisibleColulmns(List<String> visibleColumns) -> () -> storeVisibleColumns(type,
				visibleColumns);
		case GenericTableViewEffect.CellEdited(int row, int column, Object newValue) -> () -> onCellEdited(row, column,
				newValue);
		default -> null;
		};

		TableConfig tableConfig = TableConfig.builder().fillViewportHeight(true).showTableHeader(true).dragEnabled(true)
				.autoCreateRowSorter(false).reorderingAllowed(false).columnSelectionAllowed(false)
				.enableHeaderContextMenu(true).selectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
				.sortKeys(null).build();

		comboBoxModeller = new ProductOptionsComboBoxModeller();
		ProductTableColumnFactory columnFactory = new ProductTableColumnFactory(comboBoxModeller, configedMain, this);
		List<TableColumnConfig> columns = columnFactory.buildProductColumnConfigs(type);

		GenericTableViewModel model = GenericTableViewModel.builder().rows(new ArrayList<>()).columns(columns)
				.tableConfig(tableConfig).diffStrategy(new ProductRowDiffStrategy()).keyValueTable(false).isDirty(false)
				.build();

		tableViewComponent = new GenericTableViewComponent(model, sideEffectStrategy, popupMouseListenerSupplier);
		component = tableViewComponent.initUI();

		this.extractor = new ProductTableExtractor(tableViewComponent);

		comboBoxModeller.setTableViewComponent(tableViewComponent);
		comboBoxModeller.setProductConfigurationEngine(engine);
	}

	private void onCellEdited(int row, int column, Object newValue) {
		updateProductStates(row, column, newValue);

		String productId = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
				String.class);
		String columnKey = tableViewComponent.getColumnByModelIndex(column).getKey();

		if (ProductState.KEY_INSTALLATION_STATUS.equals(columnKey)) {
			String installationStatusValue = extractValueForColumn(newValue, ProductState.KEY_INSTALLATION_STATUS);
			engine.setProductVersionBasedOnInstallationStatus(productId, installationStatusValue);
		}

		if (ProductState.KEY_ACTION_REQUEST.equals(columnKey) && !engine.isSuppressCollectiveActionPropagation()) {
			String actionRequestValue = extractValueForColumn(newValue, ProductState.KEY_ACTION_REQUEST);
			engine.changeActionRequest(productId, actionRequestValue);
		}

		if (ProductState.KEY_INSTALLATION_INFO.equals(columnKey)) {
			String installationInfoValue = extractValueForColumn(newValue, ProductState.KEY_INSTALLATION_INFO);
			engine.setInstallationInfo(productId, installationInfoValue);
		}

		ChangedDataManager.getGeneralDataChangedKeeper().dataHaveChanged(this);
	}

	private void updateProductStates(int row, int column, Object newValue) {
		String columnId = tableViewComponent.getColumnByModelIndex(column).getKey();
		String value = extractValueForColumn(newValue, columnId);
		if (value == null) {
			return;
		}

		String productId = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
				String.class);
		engine.updateProductsStates(configedMain.getSelectedClients(), productId, columnId, value);
	}

	private static String extractValueForColumn(Object newValue, String columnId) {
		if (newValue == null) {
			return null;
		}
		if (newValue instanceof String stringValue) {
			return stringValue;
		}
		Map<String, Object> values = POJOReMapper.remap(newValue);
		Object value = values.get(columnId);
		return value == null ? null : value.toString();
	}

	public void reduceToSelected() {
		Set<String> productIds = getSelectedIDs();
		tableViewComponent
				.dispatch(new GenericTableViewMsg.ApplyRowFilter(ProductState.KEY_PRODUCT_ID, productIds, true));
	}

	public void setFilter(Set<String> productIds) {
		setFilter(productIds, false);
	}

	public void setFilter(Set<String> productIds, boolean selectFilteredRows) {
		Set<String> normalizedProductIds = productIds == null ? new HashSet<>() : new HashSet<>(productIds);
		tableViewComponent.dispatch(new GenericTableViewMsg.ApplyRowFilter(ProductState.KEY_PRODUCT_ID,
				normalizedProductIds, selectFilteredRows));
	}

	public void valueChanged(boolean doSelection, List<DefaultMutableTreeNode> filteredNodes) {
		if (filteredNodes.isEmpty()) {
			setFilter(null);
		} else if (filteredNodes.size() == 1) {
			nodeSelection(filteredNodes.get(0));
		} else {
			Set<String> productIds = new HashSet<>();
			Set<String> selectedValues = new HashSet<>();

			for (DefaultMutableTreeNode node : filteredNodes) {
				if (node.getAllowsChildren()) {
					AbstractGroupTree.addAllDescendants(node, productIds);
				} else {
					String value = node.getUserObject().toString();
					productIds.add(value);
					selectedValues.add(value);
				}
			}

			setFilter(productIds, doSelection);

			if (doSelection) {
				setPendingSelection(selectedValues);
			}
		}
	}

	public void nodeSelection(DefaultMutableTreeNode node) {
		if (node.getAllowsChildren()) {
			Set<String> productIds = AbstractGroupTree.getChildrenRecursively(node);
			setFilter(productIds);
		} else {
			Set<String> productIds = Set.of(node.toString());
			setFilter(productIds, true);
		}
	}

	public void setPendingSelection(Set<String> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			tableViewComponent.dispatch(new GenericTableViewMsg.ChangeSelection(new HashSet<>()));
			return;
		}

		Set<String> rowsToSelect = new HashSet<>();
		for (int i = 0; i < tableViewComponent.getRows().size(); i++) {
			RowData rowData = tableViewComponent.getRowByModelIndex(i);
			if (rowData != null) {
				String productId = rowData.getValue(ProductState.KEY_PRODUCT_ID, String.class);
				if (productIds.contains(productId)) {
					rowsToSelect.add(rowData.getId());
				}
			}
		}

		tableViewComponent.dispatch(new GenericTableViewMsg.ChangeSelection(rowsToSelect));
	}

	public static synchronized void restartColumnDict() {
		columnDict = null;
	}

	public static synchronized String getColumnTitle(String column) {
		if (columnDict == null) {
			columnDict = new HashMap<>();
			columnDict.put("productId", Configed.getResourceValue("InstallationStateTableModel.productId"));
			columnDict.put(ProductState.KEY_PRODUCT_NAME,
					Configed.getResourceValue("InstallationStateTableModel.productName"));
			columnDict.put(ProductState.KEY_INSTALLATION_STATUS,
					Configed.getResourceValue("InstallationStateTableModel.installationStatus"));

			columnDict.put(ProductState.KEY_INSTALLATION_INFO,
					Configed.getResourceValue("InstallationStateTableModel.report"));

			columnDict.put(ProductState.KEY_ACTION_REQUEST,
					Configed.getResourceValue("InstallationStateTableModel.actionRequest"));
			columnDict.put(ProductState.KEY_PRODUCT_PRIORITY,
					Configed.getResourceValue("InstallationStateTableModel.priority"));
			columnDict.put(ProductState.KEY_ACTION_SEQUENCE,
					Configed.getResourceValue("InstallationStateTableModel.position"));

			columnDict.put(ProductState.KEY_VERSION_INFO,
					Configed.getResourceValue("InstallationStateTableModel.productVersion"));

			columnDict.put(ProductState.KEY_LAST_STATE_CHANGE,
					Configed.getResourceValue("InstallationStateTableModel.lastStateChange"));
		}

		Logging.devel(ProductTable.class, "titles", columnDict);
		if (columnDict.get(column) == null) {
			return "";
		}

		return columnDict.get(column);
	}

	private void storeVisibleColumns(ProductSettingsType type, List<String> visibleColumns) {
		Map<String, Boolean> productDisplayFields = getProductDisplayFieldsBasedOnType(type);
		for (Entry<String, Boolean> productDisplayField : productDisplayFields.entrySet()) {
			productDisplayFields.put(productDisplayField.getKey(),
					visibleColumns.contains(productDisplayField.getKey()));
		}

		UserPreferences.set(
				type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS ? UserPreferences.LOCALBOOT_TABLE_DISPLAY_FIELDS
						: UserPreferences.NETBOOT_TABLE_DISPLAY_FIELDS,
				String.join(",",
						productDisplayFields.entrySet().stream().filter(Entry::getValue).map(Entry::getKey).toList()));

		ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().stateChanged(null);
	}

	private Map<String, Boolean> getProductDisplayFieldsBasedOnType(ProductSettingsType type) {
		return type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS
				? persistenceController.getDataServices().product.getProductOnClientsDisplayFieldsLocalbootProducts()
				: persistenceController.getDataServices().product.getProductOnClientsDisplayFieldsNetbootProducts();
	}

	private void applyChangedValue() {
		if (tableViewComponent.getSelectedRowCount() != 1) {
			Logging.debug(this, "no or several rows selected");
			panelProductSettings.clearEditing();
		} else {
			String selectedRow = tableViewComponent.getSelectedRows().iterator().next();
			String productId = tableViewComponent.getRowById(selectedRow).getValue(ProductState.KEY_PRODUCT_ID,
					String.class);
			Logging.debug(this, "selected ", selectedRow);
			Logging.debug(this, "selected modelIndex ", selectedRow);
			Logging.debug(this, "selected  value at ", productId);
			ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().getProductPageManager()
					.setProductEdited(productId, panelProductSettings);
		}

		productTree.produceActiveParents();

		productTree.updateSelectedObjectsInTable();
	}

	public Set<String> getSelectedIDs() {
		Set<String> result = new HashSet<>();

		for (String rowId : tableViewComponent.getSelectedRows()) {
			RowData rowData = tableViewComponent.getRowById(rowId);
			if (rowData != null) {
				String productId = rowData.getValue(ProductState.KEY_PRODUCT_ID, String.class);
				result.add(productId);
			}
		}

		return result;
	}

	public GenericTableViewComponent getTableViewComponent() {
		return tableViewComponent;
	}

	public JComponent getComponent() {
		return component;
	}

	public List<Map<String, Object>> computeDisplayRows(List<String> selectedClients, Set<String> productNames,
			Map<String, List<Map<String, String>>> statesAndActions,
			Map<String, Map<String, Object>> globalProductInfos,
			Map<String, Map<String, Map<String, String>>> changedProductStates,
			Map<String, List<String>> possibleActions) {
		engine.initialize(selectedClients, productNames, statesAndActions, globalProductInfos, changedProductStates,
				possibleActions);
		comboBoxModeller.setPossibleActions(possibleActions);

		return engine.buildSnapshot();
	}

	public void updateTable(String clientId, List<String> attributes) {
		engine.updateClientProductStates(clientId,
				persistenceController.getDataServices().product.getProductInfos(clientId, attributes));
		engine.produceVisualStatesFromExistingEntries();
		engine.completeWithDefaults(List.of(clientId));
		List<Map<String, Object>> rows = engine.buildSnapshot();

		tableViewComponent.dispatch(new GenericTableViewMsg.ChangeOriginalSnapshot(rows));
	}

	public void setActionRequestForSelectedProducts(String actionRequest) {
		for (int i = 0; i < tableViewComponent.getRowCount(); i++) {
			String rowId = tableViewComponent.getRowByModelIndex(i).getId();
			int columnIndex = tableViewComponent.getColumnIndexByKey(ProductState.KEY_ACTION_REQUEST);
			if (tableViewComponent.getSelectedRows().contains(rowId)) {
				tableViewComponent.dispatch(new GenericTableViewMsg.CellEdited(i, columnIndex, actionRequest));
			}
		}
	}

	public void applyColumnChangeToRow(String productId, String columnKey, String value) {
		int rowIndex = findRowIndexByProductId(productId);
		int columnIndex = tableViewComponent.getColumnIndexByKey(columnKey);
		if (rowIndex >= 0 && columnIndex >= 0) {
			tableViewComponent.dispatch(new GenericTableViewMsg.CellEdited(rowIndex, columnIndex, value));
		}
	}

	private int findRowIndexByProductId(String productId) {
		for (int i = 0; i < tableViewComponent.getRows().size(); i++) {
			RowData rowData = tableViewComponent.getRows().get(i);
			if (rowData != null && productId.equals(rowData.getValue(ProductState.KEY_PRODUCT_ID, String.class))) {
				return i;
			}
		}
		return -1;
	}

	public static class ProductRowDiffStrategy implements RowDiffStrategy {
		@Override
		public RowState getRowStyle(RowData rowData, String colKey, Object currentValue, Object originalValue) {
			if (colKey == null) {
				return RowState.NORMAL;
			}

			RowState rowState = RowState.NORMAL;
			if (ProductState.KEY_INSTALLATION_STATUS.equals(colKey) || ProductState.KEY_ACTION_REQUEST.equals(colKey)
					|| ProductState.KEY_INSTALLATION_INFO.equals(colKey)) {
				if (originalValue == null) {
					rowState = RowState.MISSING_DATA;
				} else {
					if (!Objects.equals(currentValue, originalValue)) {
						rowState = RowState.MODIFIED;
					}
				}
			}

			return rowState;
		}
	}

	public void clearProductChangedStates() {
		engine.clearProductChangedStates();
	}

	public JTable getStrippedTable() {
		return extractor.getStrippedTable();
	}
}
