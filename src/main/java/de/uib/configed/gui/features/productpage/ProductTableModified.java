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
import de.uib.configed.gui.data.InstallationStateTableModel;
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
import de.uib.configed.gui.features.tree.ProductTree;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.table.gui.AdaptingCellEditor;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.ComboBoxModeller;
import de.uib.configed.gui.share.table.gui.DynamicCellEditor;
import de.uib.configed.share.logging.Logging;

public class ProductTableModified {
	private static final String NONE_DISPLAY_STRING = "none";
	private static final String FAILED_DISPLAY_STRING = "failed";
	private static final String SUCCESS_DISPLAY_STRING = "success";
	private static final Set<String> defaultDisplayValues = new LinkedHashSet<>();
	static {
		defaultDisplayValues.add(NONE_DISPLAY_STRING);
		defaultDisplayValues.add(SUCCESS_DISPLAY_STRING);
		defaultDisplayValues.add(FAILED_DISPLAY_STRING);
	}

	private Set<String> pendingSelection;
	private GenericTableViewComponent tableViewComponent;
	private JComponent component;

	private ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	private ColoredTableCellRenderer productsequenceTableCellRenderer;
	private ColoredTableCellRenderer installationInfoTableCellRenderer;

	private ConfigedMain configedMain;
	private Map<String, Map<String, String>> combinedVisualValues;

	private Map<String, List<String>> possibleActions;
	private Map<String, Map<String, Map<String, String>>> changedProductStates;
	private Map<String, Map<String, Map<String, String>>> allClientsProductStates = new HashMap<>();
	private Set<String> availableProductNames = new HashSet<>();
	private Set<String> missingProducts = new LinkedHashSet<>();
	private boolean suppressCollectiveActionPropagation;

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
		List<String> order = columns.stream().map(TableColumnConfig::getHeader).toList();
		Logging.devel(this, "columns", columns, "order", order);

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
		if (ProductState.KEY_ACTION_REQUEST.equals(tableViewComponent.getColumnByModelIndex(column).getKey())
				&& !suppressCollectiveActionPropagation) {
			String productId = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
					String.class);
			String actionRequestValue = extractValueForColumn(newValue, ProductState.KEY_ACTION_REQUEST);
			if (productId != null && actionRequestValue != null) {
				collectiveChangeActionRequest(productId,
						ActionRequest.produceActionRequestFromLabel(actionRequestValue));
			}
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
		configedMain.getSelectedClients().forEach((String clientId) -> {
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

	private void storeVisibleColumns(ProductSettingsType type, List<String> visibleColumns) {
		Map<String, Boolean> productDisplayFields = getProductDisplayFieldsBasedOnType(type);
		for (Entry<String, Boolean> productDisplayField : productDisplayFields.entrySet()) {
			productDisplayFields.put(productDisplayField.getKey(),
					visibleColumns.contains(productDisplayField.getKey()));
		}

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
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_ID)).editable(false)
				.prefferedWidth(170).toggleable(false).renderer(new ProductNameTableCellRenderer(tableViewComponent))
				.build());

		// productName
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_NAME)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_NAME)).editable(false)
				.prefferedWidth(170).renderer(new ColorTableCellRenderer()).build());

		// installationStatus — editable, combo box editor, custom renderer
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_INSTALLATION_STATUS)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_STATUS)).editable(true)
				.prefferedWidth(60)
				.renderer(new ColoredTableCellRendererByIndex(InstallationStatus.getLabel2TextColor()))
				.editor(new AdaptingCellEditor(new JComboBox<>(), comboBoxModeller, true))
				.comparator(createInstallationStatusComparator()).build());

		// installationInfo — editable, dynamic combo editor, result-colored renderer
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_INSTALLATION_INFO)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_INFO)).editable(true)
				.prefferedWidth(60).renderer(installationInfoTableCellRenderer)
				.editor(new DynamicCellEditor(new JComboBox<>(), comboBoxModeller)).build());

		// actionRequest — editable, combo box editor, custom sort order
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_ACTION_REQUEST)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_REQUEST)).editable(true)
				.prefferedWidth(60).renderer(new ColoredTableCellRendererByIndex(ActionRequest.getLabel2TextColor()))
				.editor(new AdaptingCellEditor(new JComboBox<>(), comboBoxModeller, true))
				.comparator(createActionRequestComparator()).build());

		// priority — right-aligned, numeric comparator
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_PRIORITY)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_PRIORITY)).editable(false)
				.prefferedWidth(40).renderer(priorityclassTableCellRenderer)
				.comparator(Comparator.comparingInt(ProductSettingsTableModel::parseIntOrMinusOne)).build());

		// actionSequence — right-aligned, numeric comparator
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_ACTION_SEQUENCE)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_SEQUENCE)).editable(false)
				.prefferedWidth(40).renderer(productsequenceTableCellRenderer)
				.comparator(Comparator.comparingInt(ProductSettingsTableModel::parseIntOrMinusOne)).build());

		// lastStateChange
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_LAST_STATE_CHANGE)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_LAST_STATE_CHANGE)).editable(false)
				.prefferedWidth(40).renderer(new ColoredTableCellRenderer()).build());

		// versionInfo — custom renderer for conflict detection
		columns.add(TableColumnConfig.builder().key(ProductState.KEY_VERSION_INFO)
				.header(InstallationStateTableModel.getColumnTitle(ProductState.KEY_VERSION_INFO)).editable(false)
				.prefferedWidth(60).renderer(new ProductVersionCellRenderer(configedMain)).build());

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

		// 1. Remap client → (productId → stateMap)
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

		// 2. Produce visual states (from produceVisualStatesFromExistingEntries)
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

				// changeValuesForVisualOutput: inject priority
				String priority = "";
				if (globalProductInfos != null && globalProductInfos.get(product.getKey()) != null) {
					priority = "" + globalProductInfos.get(product.getKey()).get("priority");
				}
				stateAndAction.put(ProductState.KEY_PRODUCT_PRIORITY, priority);

				// mixToVisualState for each key
				for (String colKey : ProductState.KEYS) {
					mixToVisualState(combinedVisualValues.get(colKey), product.getKey(), stateAndAction.get(colKey));
				}
			}
		}

		// 3. Complete with defaults (from completeVisualStatesByDefaults)
		for (String clientId : selectedClients) {
			allClientsProductStates.putIfAbsent(clientId, new HashMap<>());
			Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
			for (String productId : productNames) {
				if (productStates.get(productId) == null) {
					// completeProductWithDefaults
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
			}
		}

		// 4. Build display rows with final transformations (from retrieveValueAt)
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
			return InstallationStateTableModel.UNEQUAL_ADD_STRING + result;
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
		applyActionRequestToRow(product, ar.toString());

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
			String neededAction = InstallationStateTableModel.REQUIRED_ACTION_FOR_STATUS.get(requiredStatus);
			requiredAR = ActionRequest.getVal(neededAction);
		}

		if (requiredAR > ActionRequest.NONE) {
			if (ActionRequest.getVal(actionRequestForRequiredProduct) == requiredAR) {
				return;
			}

			String childKey = clientId + ":" + requirement.getKey();
			if (processedProducts.contains(childKey)) {
				return;
			}

			recursivelyChangeActionRequest(clientId, requirement.getKey(), new ActionRequest(requiredAR),
					processedProducts);
		}
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

	private void applyActionRequestToRow(String productId, String value) {
		int rowIndex = findRowIndexByProductId(productId);
		int columnIndex = tableViewComponent.getColumnIndexByKey(ProductState.KEY_ACTION_REQUEST);
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
			String actualProduct = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
					String.class);

			Logging.devel(this, "getComboBoxModel: row=", row, ", column=", column, ", columnKey=", columnKey,
					"actualProduct=", actualProduct);
			if (ActionRequest.KEY.equals(columnKey)) {
				possibleOptions = producePossibleActions(actualProduct);
			} else if (InstallationStatus.KEY.equals(columnKey)) {
				possibleOptions = producePossibleInstallationStatus(InstallationStatus.getDisplayLabelsForChoice(),
						actualProduct);
			} else if (ProductState.KEY_INSTALLATION_INFO.equals(columnKey)) {
				possibleOptions = producePossibleInstallationInfos((String) tableViewComponent.getValueAt(row, column));
			} else {
				Logging.warning(this, "unexpected column ", column);

				return null;
			}

			return new DefaultComboBoxModel<>(possibleOptions);
		}

		private String[] producePossibleActions(String actualProduct) {
			// selection of actions

			Logging.debug(this, " possible actions  ", possibleActions);
			Logging.devel(this, " possible actions  ", possibleActions.size());
			List<String> actionsForProduct = new ArrayList<>();
			if (possibleActions != null) {
				for (String label : possibleActions.get(actualProduct)) {
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

		private String[] producePossibleInstallationStatus(String[] defaultValues, String actualProduct) {
			// selection of status

			// we dont have the product in our depot selection
			if (possibleActions.get(actualProduct) == null) {
				String state = combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct);
				if (state == null) {
					Logging.devel(this, "producePossibleInstallationStatus: no possible actions for product ",
							actualProduct, " and no state information available");
					return new String[] { "null" };
				}

				Logging.devel(this, "producePossibleInstallationStatus: no possible actions for product ",
						actualProduct);
				return new String[0];
			}

			Logging.devel(this, "producePossibleInstallationStatus: defaultValues=", Arrays.toString(defaultValues),
					", actualProduct=", actualProduct);
			return defaultValues;
		}

		private static String[] producePossibleInstallationInfos(String cellValue) {
			if (cellValue == null) {
				cellValue = "";
			}

			Set<String> values = new LinkedHashSet<>();

			Logging.devel("producePossibleInstallationInfos: cellValue=" + cellValue + ", defaultDisplayValues="
					+ defaultDisplayValues);
			if (!defaultDisplayValues.contains(cellValue)) {
				values.add(cellValue);
			}

			values.addAll(defaultDisplayValues);

			Logging.devel("producePossibleInstallationInfos: cellValue=" + cellValue + ", values=" + values);
			return values.toArray(new String[0]);
		}

	}

}