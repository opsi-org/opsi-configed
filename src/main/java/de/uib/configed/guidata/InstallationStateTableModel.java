/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FShowList;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.LastAction;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.productstate.TargetConfiguration;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.ComboBoxModeller;
import de.uib.utils.logging.Logging;

/**
 * Defining the TableModel for the product table for a specific client. Since we
 * here have the required data the class implements the ComboBoxModeler for
 * getting cell editors.
 */
public class InstallationStateTableModel extends AbstractTableModel implements ComboBoxModeller {
	public static final String UNEQUAL_ADD_STRING = "≠ ";

	public static final Map<String, String> REQUIRED_ACTION_FOR_STATUS = Map.ofEntries(
			Map.entry(InstallationStatus.KEY_INSTALLED, "setup"),
			Map.entry(InstallationStatus.KEY_NOT_INSTALLED, "uninstall"));

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

	private String actualProduct = "";

	private ConfigedMain configedMain;

	private List<String> sortedProductsList;

	private int onGoingCollectiveChangeEventCount = -1;

	private Map<Integer, Map<String, String>> changeEventCount2product2request;

	// state key (column name) --> product name --> visual value
	private Map<String, Map<String, String>> combinedVisualValues;

	private Map<String, Set<String>> product2setOfClientsWithNewAction;
	// for each product, we shall collect the clients that have a changed action
	// request

	private int[] filter;
	// filter is a function
	// row --> somerow (from super.table)

	// it holds
	// product(row) = product(somerow)

	private int[] filterInverse;

	// for each product, we remember the visual action that is set
	private Set<String> missingImplementationForAR;

	// (clientId -> (productId -> (product state key -> product state value)))
	private Map<String, Map<String, Map<String, String>>> allClientsProductStates;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private Map<String, Map<String, Map<String, String>>> collectChangedStates;
	private final List<String> selectedClients;
	private Map<String, List<String>> possibleActions; // product-->possibleActions
	private Map<String, Map<String, Object>> globalProductInfos;
	private Set<String> productNames;

	private Set<String> missingProducts = new HashSet<>();

	private List<String> columnsToDisplay;
	// the columns for which code exists
	private List<String> preparedColumns;
	private List<String> columnTitles;
	// the indices of the displayColumns in the displayColumns
	private int[] indexPreparedColumns;
	private boolean[] editablePreparedColumns;

	public InstallationStateTableModel(ConfigedMain configedMain,
			Map<String, Map<String, Map<String, String>>> collectChangedStates, Set<String> productNames,
			Map<String, List<Map<String, String>>> statesAndActions, Map<String, List<String>> possibleActions,
			Map<String, Map<String, Object>> productGlobalInfos, List<String> displayColumns) {
		Logging.info(this.getClass(), "creating an InstallationStateTableModel ");
		if (statesAndActions == null) {
			Logging.info(this.getClass(), " statesAndActions null ");
		} else {
			Logging.info(this.getClass(), " statesAndActions " + statesAndActions.size());
		}

		this.configedMain = configedMain;

		this.collectChangedStates = collectChangedStates;
		this.selectedClients = configedMain.getSelectedClients();

		this.possibleActions = possibleActions;
		this.globalProductInfos = productGlobalInfos;

		initColumnNames(displayColumns);
		initChangedStates();

		missingImplementationForAR = new TreeSet<>();
		product2setOfClientsWithNewAction = new HashMap<>();

		this.productNames = productNames;
		sortedProductsList = new ArrayList<>(productNames);

		Logging.debug(this.getClass(), "productNames " + productNames);

		initalizeProductStates(statesAndActions);
	}

	// collects titles for the columns prepared in this class
	public static synchronized void restartColumnDict() {
		columnDict = null;
	}

	public static synchronized String getColumnTitle(String column) {
		if (columnDict == null) {
			columnDict = new HashMap<>();
			columnDict.put("productId", Configed.getResourceValue("InstallationStateTableModel.productId"));
			columnDict.put(ProductState.KEY_PRODUCT_NAME,
					Configed.getResourceValue("InstallationStateTableModel.productName"));
			columnDict.put(ProductState.KEY_TARGET_CONFIGURATION,
					Configed.getResourceValue("InstallationStateTableModel.targetConfiguration"));
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

		if (columnDict.get(column) == null) {
			return "";
		}

		return columnDict.get(column);
	}

	public synchronized void updateTable(String clientId, SortedSet<String> productIds, List<String> attributes) {
		// Don't update if client not selected / part of this table
		if (!allClientsProductStates.containsKey(clientId)) {
			return;
		}

		// Don't apply update if something has been changed on the product on the client by the user
		if (collectChangedStates.containsKey(clientId)
				&& collectChangedStates.get(clientId).containsKey(productIds.first())) {
			return;
		}

		// add update to list
		List<Map<String, String>> productInfos = persistenceController.getProductDataService()
				.getProductInfos(productIds, clientId, attributes);
		for (Map<String, String> productInfo : productInfos) {
			allClientsProductStates.get(clientId).put(productInfo.get("productId"),
					POJOReMapper.remap(productInfo, new TypeReference<>() {
					}));
		}

		produceVisualStatesFromExistingEntries();
		completeVisualStatesByDefaults();

		for (String productId : productIds) {
			int row = getRowFromProductID(productId);

			if (row > -1) {
				fireTableRowsUpdated(row, row);
			}
		}
	}

	public synchronized void updateTable(String clientId, List<String> attributes) {
		// Don't update if client not selected / part of this table
		if (!allClientsProductStates.containsKey(clientId)) {
			return;
		}

		List<Map<String, String>> productInfos = persistenceController.getProductDataService().getProductInfos(clientId,
				attributes);
		if (!productInfos.isEmpty()) {
			for (Map<String, String> productInfo : productInfos) {
				allClientsProductStates.get(clientId).put(productInfo.get("productId"),
						POJOReMapper.remap(productInfo, new TypeReference<>() {
						}));
			}
		} else {
			allClientsProductStates.get(clientId).clear();
		}

		produceVisualStatesFromExistingEntries();
		completeVisualStatesByDefaults();
		fireTableDataChanged();
	}

	private void initalizeProductStates(Map<String, List<Map<String, String>>> client2listProductState) {
		allClientsProductStates = new HashMap<>();
		remaptoClient2Product2Rowmap(client2listProductState);

		produceVisualStatesFromExistingEntries();
		completeVisualStatesByDefaults();
	}

	private void remaptoClient2Product2Rowmap(Map<String, List<Map<String, String>>> clientAllProductRows) {
		if (clientAllProductRows == null) {
			return;
		}

		// iterate through all clients for which a list of
		// products/states/actionrequests exist
		for (Entry<String, List<Map<String, String>>> client : clientAllProductRows.entrySet()) {
			Map<String, Map<String, String>> productRows = new LinkedHashMap<>();

			allClientsProductStates.put(client.getKey(), productRows);
			// for each client we build the productstates map

			List<Map<String, String>> productRowsList1client = client.getValue();

			for (Map<String, String> stateAndAction : productRowsList1client) {
				// deep copy, but seems to be not complete, therefore not used

				String productId = stateAndAction.get(ProductState.KEY_PRODUCT_ID);
				productRows.put(productId, stateAndAction);
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
					Logging.warning(this, "produceVisualStatesFromExistingEntries, no row for " + client.getKey() + ", "
							+ product.getKey());
					continue;
				}

				// change values for visual output
				changeValuesForVisualOutput(stateAndAction, product.getKey());

				// build visual states
				for (String colKey : ProductState.KEYS) {
					mixToVisualState(combinedVisualValues.get(colKey), product.getKey(), stateAndAction.get(colKey));
				}
			}
		}
	}

	private void changeValuesForVisualOutput(Map<String, String> stateAndAction, String productId) {
		String targetConfiguration = stateAndAction.get(ProductState.KEY_TARGET_CONFIGURATION);
		if (targetConfiguration == null || targetConfiguration.isEmpty()) {
			targetConfiguration = "undefined";
		}

		stateAndAction.put(ProductState.KEY_TARGET_CONFIGURATION, targetConfiguration);

		String priority = "";
		if (globalProductInfos != null && globalProductInfos.get(productId) != null) {
			priority = "" + globalProductInfos.get(productId).get("priority");
		}

		stateAndAction.put(ProductState.KEY_PRODUCT_PRIORITY, priority);
	}

	private void completeVisualStatesByDefaults() {
		for (String clientId : selectedClients) {
			// check if productstates exist
			allClientsProductStates.putIfAbsent(clientId, new HashMap<>());
			Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);

			// check if products for clients exist
			for (String productId : sortedProductsList) {
				Map<String, String> stateAndAction = productStates.get(productId);

				if (stateAndAction == null) {
					completeProductWithDefaults(productId);
				}
			}
		}
	}

	private void completeProductWithDefaults(String productId) {
		// build visual states
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

	private static void mixToVisualState(Map<String, String> visualStates, final String productId,
			final String mixinValue) {
		String oldValue = visualStates.get(productId);
		if (oldValue == null) {
			visualStates.put(productId, mixinValue);
		} else if (!oldValue.equalsIgnoreCase(mixinValue)) {
			visualStates.put(productId, Globals.CONFLICT_STATE_STRING);
		} else {
			// Do nothing if old value equals value to mix in
		}
	}

	private boolean preparedColumnIsEditable(int j) {
		if (editablePreparedColumns == null || j < 0 || j >= editablePreparedColumns.length) {
			return false;
		}

		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return false;
		}

		return editablePreparedColumns[j];
	}

	// builds list of all prepared column key names (preparedColumns)
	// defines which column might be editable (editablePreparedColumns)
	// builds index of the currently displayed columns in terms of the prepared
	// columns (indexPreparedColumns)
	private void initColumnNames(List<String> columnsToDisplay) {
		preparedColumns = new ArrayList<>();
		editablePreparedColumns = new boolean[15];

		preparedColumns.add(0, ProductState.KEY_PRODUCT_ID);
		editablePreparedColumns[0] = false;

		preparedColumns.add(1, ProductState.KEY_PRODUCT_NAME);
		editablePreparedColumns[1] = false;

		preparedColumns.add(2, ProductState.KEY_TARGET_CONFIGURATION);
		editablePreparedColumns[2] = true;

		preparedColumns.add(3, ProductState.KEY_INSTALLATION_STATUS);
		editablePreparedColumns[3] = true;

		preparedColumns.add(4, ProductState.KEY_INSTALLATION_INFO);
		editablePreparedColumns[4] = true;

		preparedColumns.add(5, ProductState.KEY_ACTION_PROGRESS);
		editablePreparedColumns[5] = false;

		preparedColumns.add(6, ProductState.KEY_ACTION_RESULT);
		editablePreparedColumns[6] = false;

		preparedColumns.add(7, ProductState.KEY_LAST_ACTION);
		editablePreparedColumns[7] = false;

		preparedColumns.add(8, ProductState.KEY_ACTION_REQUEST);
		editablePreparedColumns[8] = true;

		preparedColumns.add(9, ProductState.KEY_PRODUCT_PRIORITY);
		editablePreparedColumns[9] = false;

		preparedColumns.add(10, ProductState.KEY_ACTION_SEQUENCE);
		editablePreparedColumns[10] = false;

		preparedColumns.add(11, ProductState.KEY_VERSION_INFO);
		editablePreparedColumns[11] = false;

		preparedColumns.add(12, ProductState.KEY_PRODUCT_VERSION);
		editablePreparedColumns[12] = false;

		preparedColumns.add(13, ProductState.KEY_PACKAGE_VERSION);
		editablePreparedColumns[13] = false;

		preparedColumns.add(14, ProductState.KEY_LAST_STATE_CHANGE);
		editablePreparedColumns[14] = false;

		if (columnsToDisplay == null) {
			Logging.error(this, "columnsToDisplay are null");
			return;
		}

		this.columnsToDisplay = columnsToDisplay;

		Logging.info(this, "preparedColumns:  " + preparedColumns);
		Logging.info(this, "columnsToDisplay: " + columnsToDisplay);

		indexPreparedColumns = new int[columnsToDisplay.size()];
		columnTitles = new ArrayList<>();

		for (int j = 0; j < columnsToDisplay.size(); j++) {
			String column = columnsToDisplay.get(j);
			Logging.debug(this, " ------- treat column " + column);
			int k = preparedColumns.indexOf(column);
			if (k >= 0) {
				indexPreparedColumns[j] = k;
				Logging.debug(this, "indexPreparedColumns of displayColumn " + j + " is " + k);
				columnTitles.add(getColumnTitle(column));
			} else {
				Logging.info(this, "column " + column + " is not prepared");
				columnTitles.add(column);
			}
		}

		Logging.info(this, " -------- numberOfColumns " + columnsToDisplay.size());
	}

	public int getColumnIndex(String columnName) {
		return columnsToDisplay.indexOf(columnName);
	}

	private void initChangedStates() {
		for (String clientId : selectedClients) {
			Map<String, Map<String, String>> changedStates = new HashMap<>();
			collectChangedStates.put(clientId, changedStates);
		}
	}

	public void clearCollectChangedStates() {
		collectChangedStates.clear();
	}

	private void setInstallationInfo(String product, String value) {
		combinedVisualValues.get(ProductState.KEY_INSTALLATION_INFO).put(product, value);

		for (String clientId : selectedClients) {
			setInstallationInfo(clientId, product, value);
		}
	}

	private void setInstallationInfo(String clientId, String product, String value) {
		Logging.debug(this,
				"setInstallationInfo for product, client, value " + product + ", " + clientId + ", " + value);

		Map<String, Map<String, String>> changedStatesForClient = collectChangedStates.computeIfAbsent(clientId,
				arg -> new HashMap<>());

		Map<String, String> changedStatesForProduct = changedStatesForClient.computeIfAbsent(product,
				arg -> new HashMap<>());

		// reverse from putting together the values in ProductState

		// `value.equals(NONE_DISPLAY_STRING)` is asked only for formal independence of the method
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

	private void checkForContradictingAssignments(String clientId, String product, String stateType, String state) {
		if (changeEventCount2product2request == null) {
			changeEventCount2product2request = new HashMap<>();
		}

		Map<String, String> product2request = changeEventCount2product2request
				.computeIfAbsent(onGoingCollectiveChangeEventCount, arg -> new HashMap<>());

		Logging.debug(this, "checkForContradictingAssignments === product2request " + product2request);

		String existingRequest = product2request.get(product);
		String info = " existingRequest " + existingRequest;

		Logging.info(this, "checkForContradictingAssignments " + info + " state " + state);

		if (existingRequest == null || existingRequest.isEmpty()) {
			product2request.put(product, state);
			Logging.debug(this, "checkForContradictingAssignments client " + clientId + ", actualproduct "
					+ actualProduct + ", product " + product + ", stateType " + stateType + ", state " + state);
		} else {
			boolean contradicting = !existingRequest.equals(state);
			info = info + " for onGoingCollectiveChangeEventCount " + onGoingCollectiveChangeEventCount
					+ " contradicting " + contradicting;
			if (contradicting) {
				if (actualProduct.equals(product)) {
					Logging.info(this, "checkForContradictingAssignments new setting for product is " + state);
					product2request.put(product, state);

					final String infoOfChange = String.format(
							Configed.getResourceValue("InstallationStateTableModel.contradictingProductRequirements3"),
							actualProduct, existingRequest, state);
					JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), infoOfChange,
							Configed.getResourceValue(
									"InstallationStateTableModel.contradictingProductRequirements.title"),
							JOptionPane.WARNING_MESSAGE);
				} else {
					Logging.warning(this,
							"checkForContradictingAssignments " + info + " client " + clientId + ", actualproduct "
									+ actualProduct + ", product " + product + ", stateType " + stateType + ", state "
									+ state);

					// Contradicting product requirements: \n
					// Product %s \n
					// requires that product %s gets action %s \n
					// but a different product set it to %s

					final String errorInfo = String.format(
							Configed.getResourceValue("InstallationStateTableModel.contradictingProductRequirements1"),
							actualProduct, product, state)
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

		Logging.info(this, "checkForContradictingAssignments === onGoingCollectiveChangeEventCount: product2request "
				+ onGoingCollectiveChangeEventCount + ": " + product2request);
	}

	private void setChangedState(String clientId, String product, String stateType, String state) {
		Map<String, Map<String, String>> changedStatesForClient = collectChangedStates.computeIfAbsent(clientId,
				arg -> new HashMap<>());

		Map<String, String> changedStatesForProduct = changedStatesForClient.computeIfAbsent(product,
				arg -> new HashMap<>());

		Logging.info(this, "accumulateProductRequests4event");
		checkForContradictingAssignments(clientId, product, stateType, state);

		changedStatesForProduct.put(stateType, state);

		configedMain.getGeneralDataChangedKeeper().dataHaveChanged(this);
	}

	private String getChangedState(String clientId, String product, String stateType) {
		Map<String, Map<String, String>> changedStatesForClient = collectChangedStates.get(clientId);
		if (changedStatesForClient == null) {
			return null;
		}

		Map<String, String> changedStatesForProduct = changedStatesForClient.get(product);
		if (changedStatesForProduct == null) {
			return null;
		}

		return changedStatesForProduct.get(stateType);
	}

	public boolean infoIfNoClientsSelected() {
		if (selectedClients.isEmpty()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("InstallationStateTableModel.noClientsSelected"),
					Configed.getResourceValue("InstallationStateTableModel.noClientsSelected.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return true;
		}

		return false;
	}

	private void registerStateChange(String product, String stateType, String value) {
		for (String clientId : selectedClients) {
			setChangedState(clientId, product, stateType, value);
		}
	}

	public void initCollectiveChange() {
		Logging.debug(this, "initCollectiveChange");
		setOnGoingCollectiveChangeCount();
		missingImplementationForAR.clear();
	}

	private void tellAndClearMissingProducts(String productId) {
		if (!missingProducts.isEmpty()) {
			Logging.info(this, "required by product " + productId + " but missing " + missingProducts);

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

			final FShowList fMissingProducts = new FShowList(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("InstallationStateTableModel.missingProducts.title"), true,
					new String[] { Configed.getResourceValue("buttonClose") }, 400, 300);
			fMissingProducts.setMessage(lines.toString());
			fMissingProducts.setAlwaysOnTop(true);
			fMissingProducts.setVisible(true);
		}
	}

	public void finishCollectiveChange() {
		Logging.info(this, "finishCollectiveChange");

		Logging.debug(this, "finishCollectiveChange, changes " + collectChangedStates);

		if (!missingImplementationForAR.isEmpty()) {
			StringBuilder products = new StringBuilder("\n\n\n");
			for (String prod : missingImplementationForAR) {
				products.append(prod);
				products.append("\n");
			}

			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("InstallationStateTableModel.missingImplementationForActionRequest")
							+ products,
					Configed.getResourceValue(
							"InstallationStateTableModel.missingImplementationForActionRequest.title"),
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void initChangeActionRequests() {
		product2setOfClientsWithNewAction.clear();

		// for each product, we shall collect the clients that have a changed action
		// request
		missingProducts.clear();
	}

	private void setActionRequest(ActionRequest ar, String productId, String clientId) {
		Map<String, Map<String, String>> productStates = allClientsProductStates.computeIfAbsent(clientId,
				arg -> new HashMap<>());
		productStates.computeIfAbsent(productId, arg -> new HashMap<>()).put(ProductState.KEY_ACTION_REQUEST,
				ar.toString());
	}

	private boolean checkActionIsSupported(String productId, ActionRequest ar) {
		if (possibleActions == null || possibleActions.get(productId).indexOf(ar.toString()) < 0) {
			missingImplementationForAR.add(productId);
			return false;
		}

		return true;
	}

	public void collectiveChangeActionRequest(String productId, ActionRequest ar) {
		Logging.info(this, "collectiveChangeActionRequest for product " + productId + " to " + ar);

		if (!checkActionIsSupported(productId, ar)) {
			return;
		}

		initChangeActionRequests();

		if (possibleActions == null || possibleActions.get(productId).indexOf(ar.toString()) < 0) {
			Logging.error(" the required action is not supported for " + productId);
			return;
		}

		for (String clientId : selectedClients) {
			Logging.debug(this, "collectiveChangeActionRequest to " + ar + "  for client " + clientId);
			setActionRequest(ar, productId, clientId);
			recursivelyChangeActionRequest(clientId, productId, ar);
		}

		Logging.debug(this, "collectiveChangeActionRequest for product, changed products "
				+ product2setOfClientsWithNewAction.keySet());

		// show the new settings for all products after recursion

		for (Entry<String, Set<String>> product : product2setOfClientsWithNewAction.entrySet()) {
			Logging.debug(this, "collectiveChangeActionRequest for product  " + product.getKey()
					+ " changed product for client number : " + product.getValue().size());
			Logging.debug(this, "collectiveChangeActionRequest we have selected clients  " + selectedClients.size());

			// -- not each client got a new action for this product
			String visualActionRequest = getVisualActionRequestForSelectedClients(product);

			combinedVisualValues.get(ProductState.KEY_ACTION_REQUEST).put(product.getKey(), visualActionRequest);
		}

		// removes the selection
		fireTableDataChanged();

		// ordering command
		tellAndClearMissingProducts(productId);
	}

	private String getVisualActionRequestForSelectedClients(Entry<String, Set<String>> product) {
		String result = null;
		boolean started = false;

		for (String clientId : selectedClients) {
			if (!started) {
				started = true;
				result = getChangedState(clientId, product.getKey(), ProductState.KEY_ACTION_REQUEST);
			} else if (result == null) {
				if (getChangedState(clientId, product.getKey(), ProductState.KEY_ACTION_REQUEST) != null) {
					return Globals.CONFLICT_STATE_STRING;
				}
			} else if (!result.equals(getChangedState(clientId, product.getKey(), ProductState.KEY_ACTION_REQUEST))) {
				return Globals.CONFLICT_STATE_STRING;
			} else {
				// No conflict between old and new values, they are equal
			}
		}

		return result;
	}

	private void recursivelyChangeActionRequest(String clientId, String product, ActionRequest ar) {
		// adds the new value to the collection of changed states
		// calls the dependencies for the next turn

		Logging.debug(this, "recursivelyChangeActionRequest " + clientId + ", " + product + ", " + ar);

		setChangedState(clientId, product, ActionRequest.KEY, ar.toString());

		Set<String> aSetOfClients = product2setOfClientsWithNewAction.computeIfAbsent(product, s -> new HashSet<>());

		aSetOfClients.add(clientId);

		// of visible action in combined actions

		int modelRow = getRowFromProductID(product);
		Logging.debug(this, "recursivelyChangeActionRequest product " + product + " modelRow " + modelRow);
		if (modelRow > -1) {
			Logging.debug(this, "recursivelyChangeActionRequest fire update for row  " + modelRow);

			// tell the table model listeners where a change occurred
			fireTableRowsUpdated(modelRow, modelRow);

			// where a change occurred
		}

		Logging.debug(this, " change action request for client " + clientId + ",  product " + product + " to " + ar);
		if (ar.getVal() == ActionRequest.NONE) {
			Logging.debug(this, "don't follow");
		} else if (ar.getVal() == ActionRequest.UNINSTALL) {
			Logging.debug(this, " follow requirements for ActionRequest.UNINSTALL, product " + product);

			Map<String, String> requirements = persistenceController.getProductDataService()
					.getProductDeinstallRequirements(null, product);
			Logging.debug(this, "ProductRequirements for uninstall for " + product + ": " + requirements);
			followRequirements(clientId, requirements);
		} else {
			Map<String, String> requirements = persistenceController.getProductDataService()
					.getProductPreRequirements(null, product);
			Logging.debug(this, "ProductPreRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);

			requirements = persistenceController.getProductDataService().getProductRequirements(null, product);
			Logging.debug(this, "ProductRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);

			requirements = persistenceController.getProductDataService().getProductPostRequirements(null, product);
			Logging.debug(this, "ProductPostRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);
		}
	}

	private void followRequirements(String clientId, Map<String, String> requirements) {
		Logging.info(this, "-- followRequirements for client " + clientId + " requirements " + requirements);

		for (Entry<String, String> requirement : requirements.entrySet()) {
			Logging.debug(this, "requiredProduct: " + requirement.getKey());
			String requiredAction = ActionRequest.getLabel(ActionRequest.NONE);
			String requiredState = InstallationStatus.getLabel(InstallationStatus.UNDEFINED);

			int colonpos = requirement.getValue().indexOf(":");
			if (colonpos >= 0) {
				requiredState = requirement.getValue().substring(0, colonpos);
				requiredAction = requirement.getValue().substring(colonpos + 1);
			}

			Logging.debug(this, "followRequirements, required product: " + requirement.getKey());
			Logging.debug(this, "followRequirements, required action: " + requiredAction);
			Logging.debug(this, "followRequirements, required state: " + requiredState);

			if (!productNames.contains(requirement.getKey())) {
				Logging.warning("followRequirements: required product: '" + requirement.getKey() + "' not installable");
				missingProducts.add(requirement.getKey());
			} else {
				if (getChangedState(clientId, requirement.getKey(), ActionRequest.KEY) != null) {
					Logging.debug(this, "required product: '" + requirement.getKey()
							+ "'  has already been treated - stop recursion");
				}

				// check required product
				checkRequiredProduct(clientId, requirement, requiredAction, requiredState);
			}
		}
	}

	private void checkRequiredProduct(String clientId, Entry<String, String> requirement, String requiredAction,
			String requiredState) {
		// retrieving the actual state and actionRequest of the required product
		Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
		if (productStates != null) {
			Map<String, String> stateAndAction = productStates.get(requirement.getKey());
			Logging.debug(this, "---- stateAndAction " + stateAndAction);

			if (stateAndAction == null) {
				stateAndAction = new ProductState(null);
			}

			String actionRequestForRequiredProduct = stateAndAction.get(ActionRequest.KEY);

			Logging.debug(this, "---- stateAndAction until now: ActionRequest for requiredProduct "
					+ actionRequestForRequiredProduct);

			String installationStatusOfRequiredProduct = stateAndAction.get(InstallationStatus.KEY);

			Logging.debug(this, "---- stateAndAction until now: InstallationStatus for requiredProduct "
					+ installationStatusOfRequiredProduct);

			Logging.debug(this, "requiredAction " + requiredAction);
			Logging.debug(this, "ActionRequest.getVal(requiredAction) " + ActionRequest.getVal(requiredAction));
			int requiredAR = ActionRequest.getVal(requiredAction);

			int requiredIS = InstallationStatus.getVal(requiredState);

			Logging.debug(this, " requiredInstallationsStatus " + InstallationStatus.getDisplayLabel(requiredIS));

			// handle state requests
			if ((requiredIS == InstallationStatus.INSTALLED || requiredIS == InstallationStatus.NOT_INSTALLED)
					// the only relevant states for which we should eventually do something
					&& InstallationStatus.getVal(installationStatusOfRequiredProduct) != requiredIS) {
				// we overwrite the required action request

				String requiredStatusS = InstallationStatus.getLabel(requiredIS);
				Logging.debug(this, " requiredStatusS " + requiredStatusS);

				String neededAction = REQUIRED_ACTION_FOR_STATUS.get(requiredStatusS);
				Logging.debug(this, " needed action therefore " + neededAction);

				requiredAR = ActionRequest.getVal(neededAction);
			}

			// handle resulting action requests
			if (requiredAR > ActionRequest.NONE) {
				checkForContradictingAssignments(clientId, requirement.getKey(), ActionRequest.KEY,
						ActionRequest.getLabel(requiredAR));

				// an action is required and already set
				if (ActionRequest.getVal(actionRequestForRequiredProduct) == requiredAR) {
					Logging.info(this,
							"followRequirements:   no change of action request necessary for " + requirement.getKey());
				} else if (getChangedState(clientId, requirement.getKey(), ActionRequest.KEY) != null) {
					Logging.info(this, "required product: '" + requirement.getKey() + "'  has already been treated");

					// already set for clientId, product "
				} else {
					Logging.info(this, "ar:   ===== recursion into " + requirement.getKey());
					recursivelyChangeActionRequest(clientId, requirement.getKey(), new ActionRequest(requiredAR));
				}
			}
		}
	}

	private int getRowFromProductID(String id) {
		int row = sortedProductsList.indexOf(id);

		// Sometimes (e.g. in user roles) the productlist is not complete
		// so we won't find the row - and return -1 directly
		if (row == -1 || filterInverse == null) {
			return row;
		}

		return filterInverse[row];
	}

	public Map<String, Map<String, Object>> getGlobalProductInfos() {
		return globalProductInfos;
	}

	private String[] producePossibleActions() {
		// selection of actions

		Logging.debug(this, " possible actions  " + possibleActions);
		List<String> actionsForProduct = new ArrayList<>();
		if (possibleActions != null) {
			for (String label : possibleActions.get(actualProduct)) {
				ActionRequest ar = ActionRequest.produceFromLabel(label);
				actionsForProduct.add(ActionRequest.getDisplayLabel(ar.getVal()));
			}

			// Add in values in correct ordering
			String[] displayLabels = ActionRequest.getDisplayLabelsForChoice();
			actionsForProduct.retainAll(Arrays.asList(displayLabels));

			Logging.debug("Possible actions as array  " + actionsForProduct);
		}

		if (actionsForProduct.isEmpty()) {
			actionsForProduct.add("null");
		}

		return actionsForProduct.toArray(new String[0]);
	}

	private String[] producePossibleInstallationStatus(String[] defaultValues) {
		// selection of status

		// we dont have the product in our depot selection
		if (possibleActions.get(actualProduct) == null) {
			String state = combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct);
			if (state == null) {
				return new String[] { "null" };
			}

			return new String[0];
		}

		return defaultValues;
	}

	private static String[] producePossibleInstallationInfos(String cellValue) {
		if (cellValue == null) {
			cellValue = "";
		}

		Set<String> values = new LinkedHashSet<>();

		if (!defaultDisplayValues.contains(cellValue)) {
			values.add(cellValue);
		}

		values.addAll(defaultDisplayValues);

		return values.toArray(new String[0]);
	}

	// interface ComboBoxModeller
	@Override
	public ComboBoxModel<String> getComboBoxModel(int row, int column) {
		actualProduct = sortedProductsList.get(originRow(row));

		String[] possibleOptions;

		if (column == columnsToDisplay.indexOf(ActionRequest.KEY)) {
			possibleOptions = producePossibleActions();
		} else if (column == columnsToDisplay.indexOf(InstallationStatus.KEY)) {
			// selection of status
			possibleOptions = producePossibleInstallationStatus(InstallationStatus.getDisplayLabelsForChoice());
		} else if (column == columnsToDisplay.indexOf(TargetConfiguration.KEY)) {
			// selection of target status
			possibleOptions = producePossibleInstallationStatus(TargetConfiguration.getDisplayLabelsForChoice());
		} else if (column == columnsToDisplay.indexOf(ProductState.KEY_INSTALLATION_INFO)) {
			possibleOptions = producePossibleInstallationInfos((String) getValueAt(row, column));
		} else {
			Logging.warning(this, "unexpected column " + column);

			return null;
		}

		return new DefaultComboBoxModel<>(possibleOptions);
	}

	public void setFilterFrom(Set<String> ids) {
		Set<String> reducedIds = null;
		if (ids != null) {
			Logging.info(this, "setFilterFrom, save set " + ids.size());
			reducedIds = new HashSet<>(sortedProductsList);
			reducedIds.retainAll(ids);

			filter = new int[reducedIds.size()];
			int i = 0;

			String[] products = new String[reducedIds.size()];
			for (String id : reducedIds) {
				products[i] = id;
				i++;
			}

			for (i = 0; i < reducedIds.size(); i++) {
				filter[i] = sortedProductsList.indexOf(products[i]);
			}

			setFilter(filter);
		} else {
			setFilter((int[]) null);
		}
	}

	private void setFilter(int[] filter) {
		Logging.info(this, "setFilter " + Arrays.toString(filter));
		this.filter = filter;

		if (filter == null) {
			filterInverse = null;
		} else {
			filterInverse = new int[sortedProductsList.size()];
			for (int j = 0; j < sortedProductsList.size(); j++) {
				filterInverse[j] = -1;
			}
			for (int i = 0; i < filter.length; i++) {
				filterInverse[filter[i]] = i;
			}

			Logging.info(this, "setFilter: filter, filterInverse " + Arrays.toString(filter) + ", "
					+ Arrays.toString(filterInverse));
		}

		fireTableDataChanged();
	}

	private int originRow(int i) {
		if (filter == null) {
			return i;
		} else if (i >= filter.length) {
			Logging.info(this, "originRow, error cannot evaluate filter; i, filter.length " + i + ", " + filter.length);
			return i;
		} else {
			return filter[i];
		}
	}

	// table model
	@Override
	public int getColumnCount() {
		return columnsToDisplay.size();
	}

	@Override
	public int getRowCount() {
		if (filter == null) {
			return sortedProductsList.size();
		} else {
			return filter.length;
		}
	}

	@Override
	public String getColumnName(int col) {
		return columnTitles.get(col);
	}

	public String getLastStateChange(int row) {
		String product = sortedProductsList.get(originRow(row));

		return combinedVisualValues.get(ProductState.KEY_LAST_STATE_CHANGE).get(product);
	}

	private void setOnGoingCollectiveChangeCount() {
		onGoingCollectiveChangeEventCount++;
	}

	// this method may be overwritten e.g.for row filtering but retrieveValue
	// continues to work
	@Override
	public Object getValueAt(int row, int displayCol) {
		Object value = retrieveValueAt(originRow(row), displayCol);
		return value == null ? "" : value;
	}

	private Object retrieveValueAt(int row, int displayCol) {
		actualProduct = sortedProductsList.get(row);

		if (displayCol >= indexPreparedColumns.length) {
			return "";
		}

		int col = indexPreparedColumns[displayCol];

		switch (col) {
		case 0:
			return actualProduct;

		case 1:
			return globalProductInfos.get(actualProduct).get(ProductState.KEY_PRODUCT_NAME);

		case 3:
			InstallationStatus is = InstallationStatus.produceFromLabel(
					combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct));
			return InstallationStatus.getDisplayLabel(is.getVal());

		case 8:
			ActionRequest ar = ActionRequest
					.produceFromLabel(combinedVisualValues.get(ProductState.KEY_ACTION_REQUEST).get(actualProduct));
			return ActionRequest.getDisplayLabel(ar.getVal());

		case 10:
			return getDisplayLabelForPosition();

		case 11:
			return actualProductVersion();

		default:
			return combinedVisualValues.get(preparedColumns.get(col)).get(actualProduct);
		}
	}

	private Object getDisplayLabelForPosition() {
		String position = combinedVisualValues.get(ProductState.KEY_ACTION_SEQUENCE).get(actualProduct);
		return "-1".equals(position) ? "" : position;
	}

	private String actualProductVersion() {
		if ("not_installed".equals(combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct))
				&& (!"once".equals(combinedVisualValues.get(ProductState.KEY_LAST_ACTION).get(actualProduct))
						&& !"custom"
								.equals(combinedVisualValues.get(ProductState.KEY_LAST_ACTION).get(actualProduct)))) {
			return "";
		}

		String serverProductVersion = (String) globalProductInfos.get(actualProduct).get(ProductState.KEY_VERSION_INFO);
		String result = combinedVisualValues.get(ProductState.KEY_VERSION_INFO).get(actualProduct);
		if (result != null && !(result.isEmpty()) && serverProductVersion != null
				&& !(serverProductVersion.equals(result))) {
			return UNEQUAL_ADD_STRING + result;
		} else {
			return result;
		}
	}

	/*
	 * editable columns
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return preparedColumnIsEditable(indexPreparedColumns[col]);
	}

	/*
	 * change method for edited cells
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		Logging.debug(this, " actualProduct " + actualProduct + ", set value at " + row + ", " + col);
		changeValueAt(value, originRow(row), col);
		fireTableCellUpdated(row, col);
	}

	private void changeValueAt(Object value, int row, int col) {
		if (value == null) {
			Logging.error(this, "value to set is null");
			return;
		}

		String cl = value.getClass().toString();

		Logging.debug(this, "actual product " + actualProduct + ", setting value at " + row + "," + col + " to " + value
				+ " (an instance of " + cl + ")");

		infoIfNoClientsSelected();

		actualProduct = sortedProductsList.get(row);

		if (combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct) == null) {
			// not a product in our depot
			return;
		}

		Object retrieveValue = retrieveValueAt(row, col);
		if (retrieveValue == null) {
			Logging.error(this, "value received from retrieveValueAt(...) is null");
			return;
		}

		if (!retrieveValue.equals(value)) {
			changeValue(value, col);
		}
	}

	private void changeValue(Object value, int col) {
		if (indexPreparedColumns[col] == preparedColumns.indexOf(InstallationStatus.KEY)) {
			combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).put(actualProduct, (String) value);
			registerStateChange(actualProduct, InstallationStatus.KEY, (String) value);
			if ("installed".equals(value)
					&& combinedVisualValues.get(ProductState.KEY_VERSION_INFO).get(actualProduct).isEmpty()) {
				setLatestProductVersion();
			}
		} else if (indexPreparedColumns[col] == preparedColumns.indexOf(TargetConfiguration.KEY)) {
			combinedVisualValues.get(ProductState.KEY_TARGET_CONFIGURATION).put(actualProduct, (String) value);
			registerStateChange(actualProduct, TargetConfiguration.KEY, (String) value);
		} else if (indexPreparedColumns[col] == preparedColumns.indexOf(ActionRequest.KEY)) {
			// an action has changed
			// change recursively visible action changes and collect the changes for saving

			initCollectiveChange();
			collectiveChangeActionRequest(actualProduct, ActionRequest.produceFromLabel((String) value));
			finishCollectiveChange();
		} else if (indexPreparedColumns[col] == preparedColumns.indexOf(ProductState.KEY_INSTALLATION_INFO)) {
			if (value.equals(NONE_DISPLAY_STRING)) {
				value = NONE_STRING;
			}

			setInstallationInfo(actualProduct, (String) value);
		} else {
			Logging.warning(this, "unexpected indexPreparedColumns[col] " + indexPreparedColumns[col]);
		}

		configedMain.getGeneralDataChangedKeeper().dataHaveChanged(this);
	}

	private void setLatestProductVersion() {
		for (String clientId : selectedClients) {
			Map<String, Map<String, String>> changedStatesForClient = collectChangedStates.computeIfAbsent(clientId,
					arg -> new HashMap<>());

			Map<String, String> changedStatesForProduct = changedStatesForClient.computeIfAbsent(actualProduct,
					arg -> new HashMap<>());
			String latestVersion = (String) globalProductInfos.get(actualProduct).get(ProductState.KEY_VERSION_INFO);
			combinedVisualValues.get(ProductState.KEY_VERSION_INFO).put(actualProduct, latestVersion);
			changedStatesForProduct.put(ProductState.KEY_PRODUCT_VERSION,
					(String) globalProductInfos.get(actualProduct).get(ProductState.KEY_PRODUCT_VERSION));
			changedStatesForProduct.put(ProductState.KEY_PACKAGE_VERSION,
					(String) globalProductInfos.get(actualProduct).get(ProductState.KEY_PACKAGE_VERSION));
		}
	}
}
