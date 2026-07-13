/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.core.domain.productstate.ActionResult;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.LastAction;
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
import de.uib.configed.gui.share.table.gui.ComboBoxModeller;
import de.uib.configed.gui.share.table.gui.DynamicCellEditor;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.UserPreferences;

public class ProductTableModified {
	private static final Map<String, String> REQUIRED_ACTION_FOR_STATUS = Map.ofEntries(
			Map.entry(InstallationStatus.KEY_INSTALLED, "setup"),
			Map.entry(InstallationStatus.KEY_NOT_INSTALLED, "uninstall"));
	private static final String UNEQUAL_ADD_STRING = "≠ ";

	private static final String NONE_STRING = "";
	private static final String NONE_DISPLAY_STRING = "none";
	private static final String FAILED_DISPLAY_STRING = "failed";
	private static final String SUCCESS_DISPLAY_STRING = "success";
	private static final Set<String> defaultDisplayValues = new LinkedHashSet<>();
	static {
		defaultDisplayValues.add(NONE_DISPLAY_STRING);
		defaultDisplayValues.add(SUCCESS_DISPLAY_STRING);
		defaultDisplayValues.add(FAILED_DISPLAY_STRING);
	}

	private static final String MANUALLY = "manually set";

	private static Map<String, String> columnDict;

	private GenericTableViewComponent tableViewComponent;
	private JComponent component;

	private ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	private ColoredTableCellRenderer productsequenceTableCellRenderer;
	private ColoredTableCellRenderer installationInfoTableCellRenderer;

	private ConfigedMain configedMain;
	private Map<String, Map<String, String>> combinedVisualValues;

	private Map<String, String> product2request = new HashMap<>();
	private Map<String, List<String>> possibleActions;
	private Map<String, Map<String, Object>> globalProductInfos;
	private Map<String, Map<String, Map<String, String>>> changedProductStates;
	private Map<String, Map<String, Map<String, String>>> allClientsProductStates = new HashMap<>();
	private Set<String> availableProductNames = new HashSet<>();
	private Set<String> missingProducts = new LinkedHashSet<>();
	private boolean suppressCollectiveActionPropagation;

	private String productBeingEdited;

	private ProductTree productTree;
	private PanelProductSettings panelProductSettings;

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
	}

	private void onCellEdited(int row, int column, Object newValue) {
		updateProductStates(row, column, newValue);

		String productId = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
				String.class);
		String columnKey = tableViewComponent.getColumnByModelIndex(column).getKey();

		if (ProductState.KEY_INSTALLATION_STATUS.equals(columnKey)) {
			String installationStatusValue = extractValueForColumn(newValue, ProductState.KEY_INSTALLATION_STATUS);
			setProductVersionBasedOnInstallationStatus(productId, installationStatusValue);
		}

		if (ProductState.KEY_ACTION_REQUEST.equals(columnKey) && !suppressCollectiveActionPropagation) {
			product2request = new HashMap<>();
			String actionRequestValue = extractValueForColumn(newValue, ProductState.KEY_ACTION_REQUEST);
			if (productId != null && actionRequestValue != null) {
				collectiveChangeActionRequest(productId,
						ActionRequest.produceActionRequestFromLabel(actionRequestValue));
			}
		}

		if (ProductState.KEY_INSTALLATION_INFO.equals(columnKey)) {
			String installationInfoValue = extractValueForColumn(newValue, ProductState.KEY_INSTALLATION_INFO);
			setInstallationInfo(productId, installationInfoValue);
		}

		ChangedDataManager.getGeneralDataChangedKeeper().dataHaveChanged(this);
	}

	private void setProductVersionBasedOnInstallationStatus(String product, String installationStatus) {
		String version;
		boolean isEmpty = combinedVisualValues.get(ProductState.KEY_VERSION_INFO).get(product).isEmpty();
		if ("installed".equals(installationStatus) && isEmpty) {
			version = (String) globalProductInfos.get(product).get(ProductState.KEY_VERSION_INFO);
		} else if ("not_installed".equals(installationStatus) && !isEmpty) {
			version = "";
		} else {
			return;
		}

		List<String> selectedClients = configedMain.getSelectedClients();
		for (String clientId : selectedClients) {
			Map<String, Map<String, String>> changedStatesForClient = changedProductStates.computeIfAbsent(clientId,
					arg -> new HashMap<>());

			Map<String, String> changedStatesForProduct = changedStatesForClient.computeIfAbsent(product,
					arg -> new HashMap<>());
			combinedVisualValues.get(ProductState.KEY_VERSION_INFO).put(product, version);
			changedStatesForProduct.put(ProductState.KEY_PRODUCT_VERSION,
					(String) globalProductInfos.get(product).get(ProductState.KEY_PRODUCT_VERSION));
			changedStatesForProduct.put(ProductState.KEY_PACKAGE_VERSION,
					(String) globalProductInfos.get(product).get(ProductState.KEY_PACKAGE_VERSION));

			applyColumnChangeToRow(product, ProductState.KEY_VERSION_INFO, version);
		}
	}

	private void setInstallationInfo(String product, String value) {
		if (NONE_DISPLAY_STRING.equals(value)) {
			value = NONE_STRING;
		}

		combinedVisualValues.get(ProductState.KEY_INSTALLATION_INFO).put(product, value);

		List<String> selectedClients = configedMain.getSelectedClients();
		for (String clientId : selectedClients) {
			setInstallationInfo(clientId, product, value);
		}
	}

	private void setInstallationInfo(String clientId, String product, String value) {
		Logging.debug(this, "setInstallationInfo for product, client, value ", product, ", ", clientId, ", ", value);

		Map<String, Map<String, String>> changedStatesForClient = changedProductStates.computeIfAbsent(clientId,
				arg -> new HashMap<>());

		Map<String, String> changedStatesForProduct = changedStatesForClient.computeIfAbsent(product,
				arg -> new HashMap<>());

		if (value.equals(NONE_STRING) || value.equals(NONE_DISPLAY_STRING)) {
			changedStatesForProduct.put(ProductState.KEY_LAST_ACTION, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_RESULT, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_PROGRESS, NONE_STRING);
		} else if (value.equals(FAILED_DISPLAY_STRING)) {
			changedStatesForProduct.put(ProductState.KEY_LAST_ACTION, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_RESULT, ActionResult.getLabel(ActionResult.FAILED));
			changedStatesForProduct.put(ProductState.KEY_ACTION_PROGRESS, MANUALLY);
		} else if (value.equals(SUCCESS_DISPLAY_STRING)) {
			changedStatesForProduct.put(ProductState.KEY_LAST_ACTION, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_RESULT, ActionResult.getLabel(ActionResult.SUCCESSFUL));
			changedStatesForProduct.put(ProductState.KEY_ACTION_PROGRESS, MANUALLY);
		} else {
			changedStatesForProduct.put(ProductState.KEY_LAST_ACTION, ActionResult.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_RESULT, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_PROGRESS, value);
		}
	}

	private void updateProductStates(int row, int column, Object newValue) {
		String columnId = tableViewComponent.getColumnByModelIndex(column).getKey();
		String value = extractValueForColumn(newValue, columnId);
		if (value == null) {
			return;
		}

		String productId = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
				String.class);
		configedMain.getSelectedClients().forEach((String clientId) -> {
			checkForContradictingAssignments(clientId, productId, columnId, value);

			changedProductStates.computeIfAbsent(clientId, k -> new HashMap<>())
					.computeIfAbsent(productId, k -> new HashMap<>()).put(columnId, value);
		});
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

	public void setPossibleActions(Map<String, List<String>> possibleActions) {
		this.possibleActions = possibleActions;
	}

	public void setChangedProductStates(Map<String, Map<String, Map<String, String>>> changedProductStates) {
		this.changedProductStates = changedProductStates;
	}

	public GenericTableViewComponent getTableViewComponent() {
		return tableViewComponent;
	}

	public JComponent getComponent() {
		return component;
	}

	private List<TableColumnConfig> buildProductColumnConfigs(ProductSettingsType type) {

		List<TableColumnConfig> columns = new ArrayList<>();
		MyComboBoxModller comboBoxModeller = new MyComboBoxModller();

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
			Map<String, Map<String, Object>> globalProductInfos) {

		this.availableProductNames = new LinkedHashSet<>(productNames);
		this.missingProducts = new LinkedHashSet<>();
		this.globalProductInfos = globalProductInfos;

		allClientsProductStates = new HashMap<>();
		if (statesAndActions != null) {
			for (Entry<String, List<Map<String, String>>> client : statesAndActions.entrySet()) {
				Map<String, Map<String, String>> productRows = new LinkedHashMap<>();
				for (Map<String, String> stateAndAction : client.getValue()) {
					productRows.put(stateAndAction.get(ProductState.KEY_PRODUCT_ID), stateAndAction);
				}
				allClientsProductStates.put(client.getKey(), productRows);
			}
		}

		produceVisualStatesFromExistingEntries();

		completeVisualStatesByDefaults(selectedClients, productNames);

		return buildOriginalSnapshot(productNames);
	}

	public void updateTable(String clientId, List<String> attributes) {

		List<Map<String, String>> productInfos = persistenceController.getDataServices().product
				.getProductInfos(clientId, attributes);
		if (!productInfos.isEmpty()) {
			for (Map<String, String> productInfo : productInfos) {
				allClientsProductStates.get(clientId).put(productInfo.get("productId"), productInfo);
			}
		} else {
			allClientsProductStates.get(clientId).clear();
		}

		produceVisualStatesFromExistingEntries();

		// 3. Complete with defaults (from completeVisualStatesByDefaults)
		completeVisualStatesByDefaults(List.of(clientId), availableProductNames);

		// 4. Build display rows with final transformations (from retrieveValueAt)
		List<Map<String, Object>> rows = buildOriginalSnapshot(availableProductNames);

		tableViewComponent.dispatch(new GenericTableViewMsg.ChangeOriginalSnapshot(rows));
	}

	private void completeVisualStatesByDefaults(List<String> selectedClients, Set<String> productNames) {
		for (String clientId : selectedClients) {
			allClientsProductStates.putIfAbsent(clientId, new HashMap<>());
			Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
			for (String productId : productNames) {
				if (productStates.get(productId) == null) {
					completeProductWihtDefaults(productId);
				}
			}
		}
	}

	private void completeProductWihtDefaults(String productId) {
		String priority = "";
		if (globalProductInfos != null && globalProductInfos.get(productId) != null) {
			priority = "" + globalProductInfos.get(productId).get("priority");
		}
		for (String key : ProductState.KEYS) {
			if (key.equals(ProductState.KEY_PRODUCT_PRIORITY)) {
				mixToVisualState(combinedVisualValues.get(key), productId, priority);
			} else {
				mixToVisualState(combinedVisualValues.get(key), productId,
						ProductState.getDefaultProductState().get(key));
			}
		}
	}

	private void produceVisualStatesFromExistingEntries() {
		combinedVisualValues = new HashMap<>();
		for (String key : ProductState.KEYS) {
			combinedVisualValues.put(key, new HashMap<>());
		}

		for (Entry<String, Map<String, Map<String, String>>> client : allClientsProductStates.entrySet()) {
			for (Entry<String, Map<String, String>> product : client.getValue().entrySet()) {
				Map<String, String> stateAndAction = product.getValue();
				if (stateAndAction == null) {
					continue;
				}

				String priority = "";
				if (globalProductInfos != null && globalProductInfos.get(product.getKey()) != null) {
					priority = "" + globalProductInfos.get(product.getKey()).get("priority");
				}
				stateAndAction.put(ProductState.KEY_PRODUCT_PRIORITY, priority);

				for (String colKey : ProductState.KEYS) {
					mixToVisualState(combinedVisualValues.get(colKey), product.getKey(), stateAndAction.get(colKey));
				}
			}
		}
	}

	private List<Map<String, Object>> buildOriginalSnapshot(Set<String> productNames) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (String productId : productNames) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put(ProductState.KEY_PRODUCT_ID, productId);
			row.put(ProductState.KEY_PRODUCT_NAME,
					globalProductInfos.get(productId).get(ProductState.KEY_PRODUCT_NAME));
			row.put(ProductState.KEY_INSTALLATION_STATUS, InstallationStatus
					.produceFromLabel(combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(productId)));
			row.put(ProductState.KEY_ACTION_REQUEST, ActionRequest
					.produceFromLabel(combinedVisualValues.get(ProductState.KEY_ACTION_REQUEST).get(productId)));
			row.put(ProductState.KEY_PRODUCT_PRIORITY,
					combinedVisualValues.get(ProductState.KEY_PRODUCT_PRIORITY).get(productId));
			// position: convert "-1" to ""
			String position = combinedVisualValues.get(ProductState.KEY_ACTION_SEQUENCE).get(productId);
			row.put(ProductState.KEY_ACTION_SEQUENCE, "-1".equals(position) ? "" : position);
			// version info: conditional display logic from actualProductVersion()
			row.put(ProductState.KEY_VERSION_INFO,
					computeVersionDisplay(productId, combinedVisualValues, globalProductInfos));
			row.put(ProductState.KEY_INSTALLATION_INFO,
					combinedVisualValues.get(ProductState.KEY_INSTALLATION_INFO).get(productId));
			row.put(ProductState.KEY_LAST_STATE_CHANGE,
					combinedVisualValues.get(ProductState.KEY_LAST_STATE_CHANGE).get(productId));
			rows.add(row);
		}

		return rows;
	}

	private static String computeVersionDisplay(String productId, Map<String, Map<String, String>> combinedVisualValues,
			Map<String, Map<String, Object>> globalProductInfos) {
		String installationStatus = combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(productId);
		String lastAction = combinedVisualValues.get(ProductState.KEY_LAST_ACTION).get(productId);

		// From actualProductVersion(): show empty for not_installed (with exceptions)
		if ("not_installed".equals(installationStatus) && !"once".equals(lastAction) && !"custom".equals(lastAction)) {
			return "";
		}

		String serverProductVersion = (String) globalProductInfos.get(productId).get(ProductState.KEY_VERSION_INFO);
		String result = combinedVisualValues.get(ProductState.KEY_VERSION_INFO).get(productId);
		if (result != null && !result.isEmpty() && serverProductVersion != null
				&& !serverProductVersion.equals(result)) {
			return UNEQUAL_ADD_STRING + result;
		}
		return result;
	}

	private static void mixToVisualState(Map<String, String> visualStates, String productId, String mixinValue) {
		String oldValue = visualStates.get(productId);
		if (oldValue == null) {
			visualStates.put(productId, mixinValue);
		} else if (!oldValue.equalsIgnoreCase(mixinValue)) {
			visualStates.put(productId, Globals.CONFLICT_STATE_STRING);
		} else {
			// Do nothing.
		}
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

	private void collectiveChangeActionRequest(String productId, ActionRequest ar) {
		Logging.info(this, "collectiveChangeActionRequest for product ", productId, " to ", ar);

		if (!checkActionIsSupported(productId, ar)) {
			return;
		}

		suppressCollectiveActionPropagation = true;
		try {
			for (String clientId : configedMain.getSelectedClients()) {
				setActionRequest(ar, productId, clientId);
				recursivelyChangeActionRequest(clientId, productId, ar, new LinkedHashSet<>());
			}
		} finally {
			suppressCollectiveActionPropagation = false;
		}

		tellAndClearMissingProducts(productId);
	}

	private void tellAndClearMissingProducts(String productId) {
		if (!missingProducts.isEmpty()) {
			Logging.info(this, "required by product ", productId, " but missing ", missingProducts);

			StringBuilder lines = new StringBuilder();

			lines.append(Configed.getResourceValue("InstallationStateTableModel.requiredByProduct"));
			lines.append("\n");
			lines.append(productId);
			lines.append("\n\n");
			lines.append(Configed.getResourceValue("InstallationStateTableModel.missingProducts"));
			lines.append("\n");

			for (String p : missingProducts) {
				lines.append("\n   ");
				lines.append(p);
			}

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), lines,
					Configed.getResourceValue("InstallationStateTableModel.missingProducts.title"),
					JOptionPane.WARNING_MESSAGE);
		}
	}

	private void recursivelyChangeActionRequest(String clientId, String product, ActionRequest ar,
			Set<String> processedProducts) {
		String processedKey = clientId + ":" + product;
		if (processedProducts.contains(processedKey)) {
			return;
		}
		processedProducts.add(processedKey);

		setActionRequest(ar, product, clientId);
		applyColumnChangeToRow(product, ProductState.KEY_ACTION_REQUEST, ar.toString());

		if (ar.getVal() == ActionRequest.NONE) {
			return;
		}

		Map<String, String> requirements = ar.getVal() == ActionRequest.UNINSTALL
				? persistenceController.getDataServices().product.getProductDeinstallRequirements(null, product)
				: persistenceController.getDataServices().product.getProductPreRequirements(null, product);
		followRequirements(clientId, requirements, processedProducts);

		if (ar.getVal() != ActionRequest.UNINSTALL) {
			followRequirements(clientId,
					persistenceController.getDataServices().product.getProductRequirements(null, product),
					processedProducts);
			followRequirements(clientId,
					persistenceController.getDataServices().product.getProductPostRequirements(null, product),
					processedProducts);
		}
	}

	private void followRequirements(String clientId, Map<String, String> requirements, Set<String> processedProducts) {
		if (requirements == null) {
			return;
		}

		for (Entry<String, String> requirement : requirements.entrySet()) {
			String requiredAction = ActionRequest.getLabel(ActionRequest.NONE);
			String requiredState = InstallationStatus.getLabel(InstallationStatus.UNDEFINED);

			int colonPosition = requirement.getValue().indexOf(':');
			if (colonPosition >= 0) {
				requiredState = requirement.getValue().substring(0, colonPosition);
				requiredAction = requirement.getValue().substring(colonPosition + 1);
			}

			if (!availableProductNames.contains(requirement.getKey())) {
				missingProducts.add(requirement.getKey());
				continue;
			}

			checkRequiredProduct(clientId, requirement, requiredAction, requiredState, processedProducts);
		}
	}

	private void checkRequiredProduct(String clientId, Entry<String, String> requirement, String requiredAction,
			String requiredState, Set<String> processedProducts) {
		Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
		if (productStates == null) {
			return;
		}

		Map<String, String> stateAndAction = productStates.get(requirement.getKey());
		if (stateAndAction == null) {
			stateAndAction = ProductState.createDefaultProductState();
		}

		String actionRequestForRequiredProduct = stateAndAction.get(ActionRequest.KEY);
		String installationStatusOfRequiredProduct = stateAndAction.get(InstallationStatus.KEY);

		int requiredAR = ActionRequest.getVal(requiredAction);
		int requiredIS = InstallationStatus.getVal(requiredState);

		if ((requiredIS == InstallationStatus.INSTALLED || requiredIS == InstallationStatus.NOT_INSTALLED)
				&& InstallationStatus.getVal(installationStatusOfRequiredProduct) != requiredIS) {
			String requiredStatus = InstallationStatus.getLabel(requiredIS);
			String neededAction = REQUIRED_ACTION_FOR_STATUS.get(requiredStatus);
			requiredAR = ActionRequest.getVal(neededAction);
		}

		if (requiredAR > ActionRequest.NONE) {
			checkForContradictingAssignments(clientId, requirement.getKey(), ActionRequest.KEY,
					ActionRequest.getLabel(requiredAR));

			if (ActionRequest.getVal(actionRequestForRequiredProduct) == requiredAR) {
				Logging.info(this, "followRequirements:   no change of action request necessary for ",
						requirement.getKey());
				return;
			}

			if (getChangedState(clientId, requirement.getKey(), ActionRequest.KEY) != null) {
				Logging.info(this, "required product: '", requirement.getKey(), "'  has already been treated");
				return;
			}

			recursivelyChangeActionRequest(clientId, requirement.getKey(), new ActionRequest(requiredAR),
					processedProducts);
		}
	}

	private String getChangedState(String clientId, String product, String stateType) {
		Map<String, Map<String, String>> changedStatesForClient = changedProductStates.get(clientId);
		if (changedStatesForClient == null) {
			return null;
		}

		Map<String, String> changedStatesForProduct = changedStatesForClient.get(product);
		if (changedStatesForProduct == null) {
			return null;
		}

		return changedStatesForProduct.get(stateType);
	}

	private void checkForContradictingAssignments(String clientId, String product, String stateType, String state) {
		Logging.debug(this, "checkForContradictingAssignments === product2request ", product2request);

		String existingRequest = product2request.get(product);
		String info = " existingRequest " + existingRequest;

		Logging.info(this, "checkForContradictingAssignments ", info, " state ", state);

		if (existingRequest == null || existingRequest.isEmpty()) {
			product2request.put(product, state);
			Logging.debug(this, "checkForContradictingAssignments client ", clientId, ", actualproduct ",
					productBeingEdited, ", product ", product, ", stateType ", stateType, ", state ", state);
		} else {
			boolean contradicting = !existingRequest.equals(state);
			info = info + " contradicting " + contradicting;
			if (contradicting) {
				if (productBeingEdited.equals(product)) {
					Logging.info(this, "checkForContradictingAssignments new setting for product is ", state);
					product2request.put(product, state);

					final String infoOfChange = String.format(
							Configed.getResourceValue("InstallationStateTableModel.contradictingProductRequirements3"),
							productBeingEdited, existingRequest, state);
					JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), infoOfChange,
							Configed.getResourceValue(
									"InstallationStateTableModel.contradictingProductRequirements.title"),
							JOptionPane.WARNING_MESSAGE);
				} else {
					Logging.warning(this, "checkForContradictingAssignments ", info, " client ", clientId,
							", actualproduct ", productBeingEdited, ", product ", product, ", stateType ", stateType,
							", state ", state);

					final String errorInfo = String.format(
							Configed.getResourceValue("InstallationStateTableModel.contradictingProductRequirements1"),
							productBeingEdited, product, state)
							+ String.format(
									Configed.getResourceValue(
											"InstallationStateTableModel.contradictingProductRequirements2"),
									existingRequest);

					JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), errorInfo,
							Configed.getResourceValue(
									"InstallationStateTableModel.contradictingProductRequirements.title"),
							JOptionPane.WARNING_MESSAGE);
				}
			}
		}

		Logging.info(this, "checkForContradictingAssignments === product2request ", ": ", product2request);
	}

	private boolean checkActionIsSupported(String productId, ActionRequest ar) {
		if (possibleActions == null || possibleActions.get(productId) == null) {
			return false;
		}

		return possibleActions.get(productId).contains(ar.toString());
	}

	private void setActionRequest(ActionRequest ar, String productId, String clientId) {
		Map<String, Map<String, String>> productStates = allClientsProductStates.computeIfAbsent(clientId,
				ignored -> new HashMap<>());
		productStates.computeIfAbsent(productId, ignored -> new HashMap<>()).put(ProductState.KEY_ACTION_REQUEST,
				ar.toString());
		if (changedProductStates != null) {
			changedProductStates.computeIfAbsent(clientId, ignored -> new HashMap<>())
					.computeIfAbsent(productId, ignored -> new HashMap<>())
					.put(ProductState.KEY_ACTION_REQUEST, ar.toString());
		}
		refreshCombinedVisualState(productId, ProductState.KEY_ACTION_REQUEST);
	}

	private void applyColumnChangeToRow(String productId, String columnKey, String value) {
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

	private void refreshCombinedVisualState(String productId, String columnKey) {
		if (combinedVisualValues == null || combinedVisualValues.get(columnKey) == null) {
			return;
		}
		String visualValue = null;
		for (String clientId : configedMain.getSelectedClients()) {
			Map<String, Map<String, String>> clientStates = allClientsProductStates.get(clientId);
			if (clientStates != null && clientStates.get(productId) != null) {
				String value = clientStates.get(productId).get(columnKey);
				if (visualValue == null) {
					visualValue = value;
				} else if (!Objects.equals(visualValue, value)) {
					visualValue = Globals.CONFLICT_STATE_STRING;
					break;
				} else {
					// Do nothing.
				}
			}
		}
		combinedVisualValues.get(columnKey).put(productId, visualValue);
	}

	static LinkedHashSet<String> collectAffectedProducts(String productId, String actionRequest,
			Set<String> productNames,
			java.util.function.BiFunction<String, String, Map<String, String>> requirementsProvider) {
		LinkedHashSet<String> affectedProducts = new LinkedHashSet<>();
		affectedProducts.add(productId);
		collectAffectedProducts(productId, actionRequest, productNames, requirementsProvider, affectedProducts,
				new LinkedHashSet<>());
		return affectedProducts;
	}

	private static void collectAffectedProducts(String productId, String actionRequest, Set<String> productNames,
			java.util.function.BiFunction<String, String, Map<String, String>> requirementsProvider,
			Set<String> affectedProducts, Set<String> processedProducts) {
		String processedKey = productId + ":" + actionRequest;
		if (!processedProducts.add(processedKey)) {
			return;
		}

		Map<String, String> requirements = requirementsProvider.apply(productId, actionRequest);
		if (requirements == null) {
			return;
		}

		for (Entry<String, String> requirement : requirements.entrySet()) {
			String requiredProduct = requirement.getKey();
			if (!productNames.contains(requiredProduct) || !affectedProducts.add(requiredProduct)) {
				continue;
			}
			collectAffectedProducts(requiredProduct, actionRequest, productNames, requirementsProvider,
					affectedProducts, processedProducts);
		}
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

	private class MyComboBoxModller implements ComboBoxModeller {
		@Override
		public ComboBoxModel<String> getComboBoxModel(int row, int column) {
			String[] possibleOptions;

			String columnKey = tableViewComponent.getColumnByModelIndex(column).getKey();
			productBeingEdited = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
					String.class);

			Logging.debug(this, "getComboBoxModel: row=", row, ", column=", column, ", columnKey=", columnKey,
					"actualProduct=", productBeingEdited);
			if (ActionRequest.KEY.equals(columnKey)) {
				possibleOptions = producePossibleActions(productBeingEdited);
			} else if (InstallationStatus.KEY.equals(columnKey)) {
				possibleOptions = producePossibleInstallationStatus(InstallationStatus.getDisplayLabelsForChoice(),
						productBeingEdited);
			} else if (ProductState.KEY_INSTALLATION_INFO.equals(columnKey)) {
				possibleOptions = producePossibleInstallationInfos((String) tableViewComponent.getValueAt(row, column));
			} else {
				Logging.warning(this, "unexpected column ", column);

				return null;
			}

			return new DefaultComboBoxModel<>(possibleOptions);
		}

		private String[] producePossibleActions(String product) {
			Logging.debug(this, " possible actions  ", possibleActions);
			List<String> actionsForProduct = new ArrayList<>();
			if (possibleActions != null) {
				for (String label : possibleActions.get(product)) {
					actionsForProduct.add(ActionRequest.produceFromLabel(label));
				}

				// Add in values in correct ordering
				String[] displayLabels = ActionRequest.getDisplayLabelsForChoice();
				actionsForProduct.retainAll(List.of(displayLabels));

				Logging.debug("Possible actions as array  ", actionsForProduct);
			}

			if (actionsForProduct.isEmpty()) {
				actionsForProduct.add("null");
			}

			return actionsForProduct.toArray(new String[0]);
		}

		private String[] producePossibleInstallationStatus(String[] defaultValues, String product) {
			if (possibleActions.get(product) == null) {
				String state = combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(product);
				if (state == null) {
					Logging.debug(this, "producePossibleInstallationStatus: no possible actions for product ", product,
							" and no state information available");
					return new String[] { "null" };
				}

				Logging.debug(this, "producePossibleInstallationStatus: no possible actions for product ", product);
				return new String[0];
			}

			Logging.debug(this, "producePossibleInstallationStatus: defaultValues=", Arrays.toString(defaultValues),
					", actualProduct=", product);
			return defaultValues;
		}

		private static String[] producePossibleInstallationInfos(String cellValue) {
			if (cellValue == null) {
				cellValue = "";
			}

			Set<String> values = new LinkedHashSet<>();

			Logging.debug("producePossibleInstallationInfos: cellValue=" + cellValue + ", defaultDisplayValues="
					+ defaultDisplayValues);
			if (!defaultDisplayValues.contains(cellValue)) {
				values.add(cellValue);
			}

			values.addAll(defaultDisplayValues);

			Logging.debug("producePossibleInstallationInfos: cellValue=" + cellValue + ", values=" + values);
			return values.toArray(new String[0]);
		}

	}

	public void clearProductChangedStates() {
		if (changedProductStates != null) {
			changedProductStates.clear();
		}
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