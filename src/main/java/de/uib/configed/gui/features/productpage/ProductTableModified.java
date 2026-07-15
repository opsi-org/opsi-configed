/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.core.domain.productstate.ActionResult;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ProductPageManager;
import de.uib.configed.gui.data.ColoredTableCellRenderer;
import de.uib.configed.gui.data.ColoredTableCellRendererByIndex;
import de.uib.configed.gui.features.productpage.PanelProductSettings.ProductSettingsType;
import de.uib.configed.gui.features.productpage.ProductSettingsTableModel.ProductNameTableCellRenderer;
import de.uib.configed.gui.features.productpage.ProductSettingsTableModel.ProductVersionCellRenderer;
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
import de.uib.configed.gui.share.table.gui.AdaptingCellEditor;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.DynamicCellEditor;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.UserPreferences;

public class ProductTableModified {
	private static Map<String, String> columnDict;

	private GenericTableViewComponent tableViewComponent;
	private JComponent component;

	private ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	private ColoredTableCellRenderer productsequenceTableCellRenderer;
	private ColoredTableCellRenderer installationInfoTableCellRenderer;

	private ConfigedMain configedMain;

	private ProductTree productTree;
	private PanelProductSettings panelProductSettings;
	private ProductConfigurationEngine engine;

	private ProductOptionsComboBoxModeller comboBoxModeller;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ProductTableModified(ConfigedMain configedMain, ProductSettingsType type, ProductTree productTree,
			PanelProductSettings panelProductSettings, Supplier<PopupMouseListener> popupMouseListenerSupplier) {
		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex();
		priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

		productsequenceTableCellRenderer = new ColoredTableCellRenderer();
		productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

		installationInfoTableCellRenderer = new ColoredTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe sind instanceof returns false if null
				if (value instanceof String stringValue) {
					if (stringValue.startsWith(ActionResult.getLabel(ActionResult.FAILED))) {
						setForeground(Globals.PANEL_PRODUCT_SETTINGS_FAILED_COLOR);
					} else if (stringValue.startsWith(ActionResult.getLabel(ActionResult.SUCCESSFUL))) {
						setForeground(Globals.OK_COLOR);
					} else {
						// Don't set foreground if no special result
					}
				}

				return this;
			}
		};

		this.configedMain = configedMain;
		this.productTree = productTree;
		this.panelProductSettings = panelProductSettings;
		this.engine = new ProductConfigurationEngine(this, configedMain);

		List<TableColumnConfig> columns = buildProductColumnConfigs(type);

		TableSideEffectStrategy sideEffectStrategy = (GenericTableViewEffect effect) -> {
			return switch (effect) {
			case GenericTableViewEffect.Selection() -> this::applyChangedValue;
			case GenericTableViewEffect.StoreVisibleColulmns(List<String> visibleColumns) -> () -> storeVisibleColumns(
					type, visibleColumns);
			case GenericTableViewEffect.CellEdited(int row, int column, Object newValue) -> () -> onCellEdited(row,
					column, newValue);
			default -> null;
			};
		};

		TableConfig tableConfig = TableConfig.builder().fillViewportHeight(true).showTableHeader(true).dragEnabled(true)
				.autoCreateRowSorter(false).reorderingAllowed(false).columnSelectionAllowed(false)
				.enableHeaderContextMenu(true).selectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
				.sortKeys(null).build();

		GenericTableViewModel model = GenericTableViewModel.builder().rows(new ArrayList<>()).columns(columns)
				.tableConfig(tableConfig).diffStrategy(new ProductRowDiffStrategy()).keyValueTable(false).isDirty(false)
				.build();

		tableViewComponent = new GenericTableViewComponent(model, sideEffectStrategy, popupMouseListenerSupplier);
		component = tableViewComponent.initUI();

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

		Logging.devel(ProductTableModified.class, "titles", columnDict);
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

	private List<TableColumnConfig> buildProductColumnConfigs(ProductSettingsType type) {

		List<TableColumnConfig> columns = new ArrayList<>();
		comboBoxModeller = new ProductOptionsComboBoxModeller();

		// productId
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_ID)
				.header(getColumnTitle(ProductState.KEY_PRODUCT_ID)).editable(false).prefferedWidth(170)
				.toggleable(false).renderer(new ProductNameTableCellRenderer(tableViewComponent)).build());

		// productName
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_NAME)
				.header(getColumnTitle(ProductState.KEY_PRODUCT_NAME)).editable(false).prefferedWidth(170)
				.renderer(new ColorTableCellRenderer()).build());

		// installationStatus — editable, combo box editor, custom renderer
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_INSTALLATION_STATUS)
				.header(getColumnTitle(ProductState.KEY_INSTALLATION_STATUS)).editable(true).prefferedWidth(60)
				.renderer(new ColoredTableCellRendererByIndex(InstallationStatus.getLabel2TextColor()))
				.editor(new AdaptingCellEditor(new JComboBox<>(), comboBoxModeller, true))
				.comparator(createInstallationStatusComparator()).build());

		// installationInfo — editable, dynamic combo editor, result-colored renderer
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_INSTALLATION_INFO)
				.header(getColumnTitle(ProductState.KEY_INSTALLATION_INFO)).editable(true).prefferedWidth(60)
				.renderer(installationInfoTableCellRenderer)
				.editor(new DynamicCellEditor(new JComboBox<>(), comboBoxModeller)).build());

		// actionRequest — editable, combo box editor, custom sort order
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_ACTION_REQUEST)
				.header(getColumnTitle(ProductState.KEY_ACTION_REQUEST)).editable(true).prefferedWidth(60)
				.renderer(new ColoredTableCellRendererByIndex(ActionRequest.getLabel2TextColor()))
				.editor(new AdaptingCellEditor(new JComboBox<>(), comboBoxModeller, true))
				.comparator(createActionRequestComparator()).build());

		// priority — right-aligned, numeric comparator
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_PRIORITY)
				.header(getColumnTitle(ProductState.KEY_PRODUCT_PRIORITY)).editable(false).prefferedWidth(40)
				.renderer(priorityclassTableCellRenderer)
				.comparator(Comparator.comparingInt(ProductSettingsTableModel::parseIntOrMinusOne)).build());

		// actionSequence — right-aligned, numeric comparator
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_ACTION_SEQUENCE)
				.header(getColumnTitle(ProductState.KEY_ACTION_SEQUENCE)).editable(false).prefferedWidth(40)
				.renderer(productsequenceTableCellRenderer)
				.comparator(Comparator.comparingInt(ProductSettingsTableModel::parseIntOrMinusOne)).build());

		// lastStateChange
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_LAST_STATE_CHANGE)
				.header(getColumnTitle(ProductState.KEY_LAST_STATE_CHANGE)).editable(false).prefferedWidth(40)
				.renderer(new ColoredTableCellRenderer()).build());

		// versionInfo — custom renderer for conflict detection
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_VERSION_INFO)
				.header(getColumnTitle(ProductState.KEY_VERSION_INFO)).editable(false).prefferedWidth(60)
				.renderer(new ProductVersionCellRenderer(configedMain)).build());

		Map<String, Boolean> productDisplayFields = type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS
				? PersistenceControllerFactory.getPersistenceController().getDataServices().product
						.getProductOnClientsDisplayFieldsLocalbootProducts()
				: PersistenceControllerFactory.getPersistenceController().getDataServices().product
						.getProductOnClientsDisplayFieldsNetbootProducts();
		List<String> displayFields = ProductPageManager.getDisplayFieldsList(productDisplayFields);

		Iterator<TableColumnConfig> iterator = columns.iterator();
		while (iterator.hasNext()) {
			TableColumnConfig column = iterator.next();
			if (!displayFields.contains(column.getKey())) {
				columns.set(columns.indexOf(column), column.withVisible(false));
			}
		}

		return columns;
	}

	private static Comparator<Object> createInstallationStatusComparator() {
		List<String> order = List.of(InstallationStatus.KEY_INSTALLED, InstallationStatus.KEY_UNKNOWN,
				InstallationStatus.KEY_NOT_INSTALLED);
		return (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
	}

	private static Comparator<Object> createActionRequestComparator() {
		List<String> order = List.of(ActionRequest.KEY_SETUP, ActionRequest.KEY_UPDATE, ActionRequest.KEY_UNINSTALL,
				ActionRequest.KEY_ALWAYS, ActionRequest.KEY_ONCE, ActionRequest.KEY_CUSTOM, ActionRequest.KEY_NONE);
		return (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
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
			if (ProductState.KEY_INSTALLATION_STATUS.equals(colKey) || ProductState.KEY_ACTION_REQUEST.equals(colKey)
					|| ProductState.KEY_INSTALLATION_INFO.equals(colKey)) {
				if (originalValue == null) {
					return RowState.MISSING_DATA;
				}
				if (!Objects.equals(currentValue, originalValue)) {
					return RowState.MODIFIED;
				}
			}
			return RowState.NORMAL;
		}
	}

	public void clearProductChangedStates() {
		engine.clearProductChangedStates();
	}

	public JTable getStrippedTable() {
		List<String[]> data = new ArrayList<>();
		List<TableColumnConfig> columns = tableViewComponent.getVisibleColumns();
		List<RowData> rows = tableViewComponent.getRows();

		for (int j = 0; j < rows.size(); j++) {
			RowData rowData = rows.get(j);
			boolean strippIt = true;
			String[] actCol = new String[columns.size()];

			for (int i = 0; i < columns.size(); i++) {
				TableColumnConfig columnConfig = columns.get(i);
				Object cellValue = rowData.getValue(columnConfig.getKey(), Object.class);
				String cellValueString = cellValue == null ? "" : cellValue.toString();
				actCol[i] = cellValueString;

				strippIt = shouldStripIt(columnConfig.getHeader(), cellValueString, strippIt);
			}

			if (!strippIt) {
				data.add(actCol);
			}
		}

		// Create jTable with stripped data
		int rowCount = data.size();
		int colCount = columns.size();
		String[][] strippedData = new String[rowCount][colCount];
		for (int i = 0; i < data.size(); i++) {
			strippedData[i] = data.get(i);
		}

		return new JTable(strippedData, extractHeaders(columns));
	}

	private static String[] extractHeaders(List<TableColumnConfig> columns) {
		String[] headers = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			headers[i] = columns.get(i).getHeader();
		}
		return headers;
	}

	private boolean shouldStripIt(String columnName, String cellValueString, boolean previousValue) {
		String installationStatusLabel = Configed.getResourceValue("InstallationStateTableModel.installationStatus");
		String reportLabel = Configed.getResourceValue("InstallationStateTableModel.report");
		String actionRequestLabel = Configed.getResourceValue("InstallationStateTableModel.actionRequest");

		boolean strippIt = previousValue;

		if (installationStatusLabel.equals(columnName)
				&& !InstallationStatus.KEY_NOT_INSTALLED.equals(cellValueString)) {
			strippIt = false;
		} else if (reportLabel.equals(columnName) && cellValueString != null && !cellValueString.isEmpty()) {
			strippIt = false;
		} else if (actionRequestLabel.equals(columnName) && !"none".equals(cellValueString)) {
			strippIt = false;
		} else {
			// Keep row if we don't have explicit strip rules for this column
			// This maintains backward compatibility with the warning log behavior
			Logging.debug(this, "checking strip condition for columnName: ", columnName);
		}

		return strippIt;
	}
}