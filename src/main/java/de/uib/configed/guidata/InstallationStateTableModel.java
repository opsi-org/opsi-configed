package de.uib.configed.guidata;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (c) 2000-2020 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

/*
test cases:

if several clients are selected:
	changes are saved / initiated for all clients
	
if several products are selected:
	changes are saved / initiated for all clients and all products
	changes can be combined with manual changes for one product 

if several products with contradicting dependencies are set by one action 
	a warning is issued

	There is no warning if an existing setting is reversed by a second action

	
local cancel button (not implemented)
	all changes are reverted


*/

/*
tests:

setting a value 
setting setup for several rows
setting setup with dependency
setting setup with dependencies, also for uninstall

*/

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FShowList;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.Config;
import de.uib.opsidatamodel.productstate.InstallationInfo;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.LastAction;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.productstate.TargetConfiguration;
import de.uib.utilities.logging.Logging;

/**
 * Defining the TableModel for the product table for a specific client. Since we
 * here have the required data the class implements the ComboBoxModeler for
 * getting cell editors.
 */
public class InstallationStateTableModel extends AbstractTableModel implements IFInstallationStateTableModel {

	public static final String EMPTYFIELD = "_";

	public static final String CONFLICT_STRING = Globals.CONFLICT_STATE_STRING;

	public static final String UNEQUAL_ADD_STRING = "≠ ";

	protected static Map<String, String> columnDict;
	protected static List<String> columnsLocalized;

	protected String actualProduct = "";

	protected ConfigedMain main;

	protected List<String> productsV = null;

	protected int onGoingCollectiveChangeEventCount = -1;

	Map<Integer, Map<String, String>> changeEventCount2product2request;

	// state key (column name) --> product name --> visual value
	protected Map<String, Map<String, String>> combinedVisualValues;

	protected Map<String, Set<String>> product2setOfClientsWithNewAction;
	// for each product, we shall collect the clients that have a changed action
	// request

	// for each product, we remember the visual action that is set
	protected NavigableSet<String> missingImplementationForAR;

	// (clientId -> (productId -> (product state key -> product state value)))
	protected Map<String, Map<String, Map<String, String>>> allClientsProductStates;

	protected AbstractPersistenceController persist;
	protected Map<String, Map<String, Map<String, String>>> collectChangedStates;
	protected final String[] selectedClients;
	protected Map<String, List<String>> possibleActions; // product-->possibleActions
	protected Map<String, Map<String, Object>> globalProductInfos;
	protected String theClient;
	protected NavigableSet<String> tsProductNames;
	protected List<String> productNamesInDeliveryOrder;

	protected ActionRequest actionInTreatment;

	protected Set<String> missingProducts = new HashSet<>();

	protected List<String> displayColumns;
	protected int numberOfColumns;
	// the columns for which code exists
	protected List<String> preparedColumns;
	protected List<String> columnTitles;
	// the indices of the displayColumns in the displayColumns
	protected int[] indexPreparedColumns;
	protected boolean[] editablePreparedColumns;

	// collects titles for the columns prepared in this class

	public static void restartColumnDict() {
		columnDict = null;
	}

	public static String getColumnTitle(String column) {
		if (columnDict == null) {
			columnDict = new HashMap<>();
			columnDict.put("productId", Configed.getResourceValue("InstallationStateTableModel.productId"));
			columnDict.put(ProductState.KEY_PRODUCT_NAME,
					Configed.getResourceValue("InstallationStateTableModel.productName"));
			columnDict.put(ProductState.KEY_TARGET_CONFIGURATION,
					Configed.getResourceValue("InstallationStateTableModel.targetConfiguration"));
			columnDict.put(ProductState.KEY_INSTALLATION_STATUS,
					Configed.getResourceValue("InstallationStateTableModel.installationStatus"));

			columnDict.put("installationInfo", Configed.getResourceValue("InstallationStateTableModel.report"));
			// combines the following three
			columnDict.put(ProductState.KEY_ACTION_PROGRESS,
					Configed.getResourceValue("InstallationStateTableModel.actionProgress"));
			columnDict.put(ProductState.KEY_ACTION_RESULT,
					Configed.getResourceValue("InstallationStateTableModel.actionResult"));
			columnDict.put(ProductState.KEY_LAST_ACTION,
					Configed.getResourceValue("InstallationStateTableModel.lastAction"));

			columnDict.put(ProductState.KEY_ACTION_REQUEST,
					Configed.getResourceValue("InstallationStateTableModel.actionRequest"));
			columnDict.put(ProductState.KEY_PRODUCT_PRIORITY,
					Configed.getResourceValue("InstallationStateTableModel.priority"));
			columnDict.put(ProductState.KEY_ACTION_SEQUENCE, "actionSequence");

			columnDict.put(ProductState.KEY_POSITION,
					Configed.getResourceValue("InstallationStateTableModel.position"));

			columnDict.put(ProductState.KEY_VERSION_INFO, "Version");
			// combines the following two
			columnDict.put(ProductState.KEY_PRODUCT_VERSION,
					Configed.getResourceValue("InstallationStateTableModel.productVersion"));
			columnDict.put(ProductState.KEY_PACKAGE_VERSION,
					Configed.getResourceValue("InstallationStateTableModel.packageVersion"));

			columnDict.put(ProductState.KEY_LAST_STATE_CHANGE,
					Configed.getResourceValue("InstallationStateTableModel.lastStateChange"));

		}

		if (columnDict.get(column) == null) {
			return "";
		}

		return columnDict.get(column);
	}

	public static List<String> localizeColumns(List<String> cols) {
		List<String> result = new ArrayList<>();

		if (columnDict != null) {
			for (String col : cols) {
				if (columnDict.get(col) != null) {
					result.add(columnDict.get(col));
				}
			}
		}
		return result;
	}

	public InstallationStateTableModel(String[] selectedClients, ConfigedMain main,
			Map<String, Map<String, Map<String, String>>> collectChangedStates, List<String> listOfInstallableProducts,
			Map<String, List<Map<String, String>>> statesAndActions, Map<String, List<String>> possibleActions,
			Map<String, Map<String, Object>> productGlobalInfos, List<String> displayColumns) {
		Logging.info(this, "creating an InstallationStateTableModel ");
		if (statesAndActions == null) {
			Logging.info(this, " statesAndActions null ");
		} else {
			Logging.info(this, " statesAndActions " + statesAndActions.size());
		}

		this.main = main;

		this.collectChangedStates = collectChangedStates;
		this.selectedClients = selectedClients;

		this.possibleActions = possibleActions;
		this.globalProductInfos = productGlobalInfos;

		initColumnNames(displayColumns);
		initChangedStates();

		missingImplementationForAR = new TreeSet<>();
		product2setOfClientsWithNewAction = new HashMap<>();

		persist = main.getPersistenceController();
		Collator myCollator = Collator.getInstance();

		myCollator.setStrength(Collator.SECONDARY);

		productNamesInDeliveryOrder = new ArrayList<>();
		if (listOfInstallableProducts != null) {
			for (int i = 0; i < listOfInstallableProducts.size(); i++) {
				String product = listOfInstallableProducts.get(i);
				productNamesInDeliveryOrder.add(product);
			}
		}

		tsProductNames = new TreeSet<>(myCollator);
		tsProductNames.addAll(productNamesInDeliveryOrder);
		productsV = new ArrayList<>(tsProductNames);

		Logging.debug(this, "tsProductNames " + tsProductNames);

		initalizeProductStates(statesAndActions);
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
			Map<String, Map<String, String>> productRows = new HashMap<>();

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
			HashMap<String, String> combinedVisualValuesForOneColumn = new HashMap<>();
			combinedVisualValues.put(key, combinedVisualValuesForOneColumn);
		}

		for (Entry<String, Map<String, Map<String, String>>> client : allClientsProductStates.entrySet()) {
			for (String productId : client.getValue().keySet()) {
				Map<String, String> stateAndAction = client.getValue().get(productId);

				if (stateAndAction == null) {
					Logging.warning(this,
							"produceVisualStatesFromExistingEntries, no row for " + client.getKey() + ", " + productId);
					continue;
				}

				// change values for visual output
				String targetConfiguration = stateAndAction.get(ProductState.KEY_TARGET_CONFIGURATION);
				if (targetConfiguration == null || targetConfiguration.equals("")) {
					targetConfiguration = TargetConfiguration.getLabel(TargetConfiguration.UNDEFINED);
				}

				stateAndAction.put(ProductState.KEY_TARGET_CONFIGURATION, targetConfiguration);

				String priority = "";
				if (globalProductInfos != null && globalProductInfos.get(productId) != null) {
					priority = "" + globalProductInfos.get(productId).get("priority");
				}

				stateAndAction.put(ProductState.KEY_PRODUCT_PRIORITY, priority);

				stateAndAction.put(ProductState.KEY_ACTION_SEQUENCE, priority);

				// build visual states
				for (String colKey : ProductState.KEYS) {

					if (colKey.equals(ProductState.KEY_ACTION_REQUEST)) {
						Logging.debug(this, " ------------before   mixtovisualstate " + "product " + productId
								+ " value " + stateAndAction.get(colKey));
					}

					mixToVisualState(combinedVisualValues.get(colKey), productId, stateAndAction.get(colKey));
				}

			}
		}
	}

	private void completeVisualStatesByDefaults() {
		for (String clientId : selectedClients) {

			// check if productstates exist
			allClientsProductStates.putIfAbsent(clientId, new HashMap<>());
			Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);

			// check if products for clients exist
			for (int j = 0; j < productsV.size(); j++) {

				String productId = productsV.get(j);
				Map<String, String> stateAndAction = productStates.get(productId);

				if (stateAndAction == null) {

					// build visual states
					Iterator<String> iter = ProductState.KEYS.iterator();

					String priority = "";

					if (globalProductInfos != null && globalProductInfos.get(productId) != null) {
						priority = "" + globalProductInfos.get(productId).get("priority");
					}

					while (iter.hasNext()) {
						String key = iter.next();

						if (key.equals(ProductState.KEY_PRODUCT_PRIORITY)
								|| key.equals(ProductState.KEY_ACTION_SEQUENCE)) {
							mixToVisualState(combinedVisualValues.get(key), productId, priority);
						} else {
							mixToVisualState(combinedVisualValues.get(key), productId,
									ProductState.getDefaultProductState().get(key));
						}
					}

				}
			}
		}
	}

	protected String mixToVisualState(Map<String, String> visualStates, final String productId,
			final String mixinValue) {
		String oldValue = visualStates.get(productId);

		String resultValue = oldValue;
		if (oldValue == null) {

			resultValue = mixinValue;
			visualStates.put(productId, resultValue);
		} else {
			if (!oldValue.equalsIgnoreCase(mixinValue)) {
				resultValue = CONFLICT_STRING;
				visualStates.put(productId, resultValue);
			}
		}

		return resultValue;
	}

	private boolean preparedColumnIsEditable(int j) {
		if (editablePreparedColumns == null || j < 0 || j >= editablePreparedColumns.length) {
			return false;
		}

		if (Globals.isGlobalReadOnly()) {
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
		editablePreparedColumns = new boolean[16];

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

		preparedColumns.add(11, ProductState.KEY_POSITION);
		editablePreparedColumns[11] = false;

		preparedColumns.add(12, ProductState.KEY_VERSION_INFO);
		editablePreparedColumns[12] = false;

		preparedColumns.add(13, ProductState.KEY_PRODUCT_VERSION);
		editablePreparedColumns[13] = false;

		preparedColumns.add(14, ProductState.KEY_PACKAGE_VERSION);
		editablePreparedColumns[14] = false;

		preparedColumns.add(15, ProductState.KEY_LAST_STATE_CHANGE);
		editablePreparedColumns[15] = false;

		if (columnsToDisplay == null) {
			Logging.error(this, "columnsToDisplay are null");
			return;
		}

		displayColumns = columnsToDisplay;

		Logging.info(this, "preparedColumns:  " + preparedColumns);
		Logging.info(this, "columnsToDisplay: " + columnsToDisplay);

		indexPreparedColumns = new int[columnsToDisplay.size()];
		columnTitles = new ArrayList<>();

		Iterator<String> iter = columnsToDisplay.iterator();
		int j = 0;
		while (iter.hasNext()) {
			String column = iter.next();
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

			j++;
		}

		numberOfColumns = displayColumns.size();
		Logging.info(this, " -------- numberOfColumns " + numberOfColumns);

	}

	@Override
	public int getColumnIndex(String columnName) {
		return displayColumns.indexOf(columnName);
	}

	private void initChangedStates() {
		for (String clientId : selectedClients) {
			Map<String, Map<String, String>> changedStates = new HashMap<>();
			collectChangedStates.put(clientId, changedStates);
		}
	}

	@Override
	public void clearCollectChangedStates() {
		collectChangedStates.clear();

	}

	protected void setInstallationInfo(String product, String value) {
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

		// `value.equals(InstallationInfo.NONE_DISPLAY_STRING)` is asked only for formal independence of the method
		if (value.equals(InstallationInfo.NONE_STRING) || value.equals(InstallationInfo.NONE_DISPLAY_STRING)) {
			changedStatesForProduct.put(ProductState.KEY_LAST_ACTION, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_RESULT, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_PROGRESS, InstallationInfo.NONE_STRING);
		} else if (value.equals(InstallationInfo.FAILED_DISPLAY_STRING)) {
			changedStatesForProduct.put(ProductState.KEY_LAST_ACTION, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_RESULT, ActionResult.getLabel(ActionResult.FAILED));
			changedStatesForProduct.put(ProductState.KEY_ACTION_PROGRESS, InstallationInfo.MANUALLY);
		} else if (value.equals(InstallationInfo.SUCCESS_DISPLAY_STRING)) {
			changedStatesForProduct.put(ProductState.KEY_LAST_ACTION, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_ACTION_RESULT, ActionResult.getLabel(ActionResult.SUCCESSFUL));
			changedStatesForProduct.put(ProductState.KEY_ACTION_PROGRESS, InstallationInfo.MANUALLY);
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

		if (existingRequest == null || existingRequest.equals("")) {
			product2request.put(product, state);
			Logging.debug(this, "checkForContradictingAssignments client " + clientId + ", actualproduct "
					+ actualProduct + ", product " + product + ", stateType " + stateType + ", state " + state);

		}

		else {
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
					new Thread() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), infoOfChange,
									Configed.getResourceValue(
											"InstallationStateTableModel.contradictingProductRequirements.title"),
									JOptionPane.WARNING_MESSAGE);
						}
					}.start();

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

					new Thread() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), errorInfo,
									Configed.getResourceValue(
											"InstallationStateTableModel.contradictingProductRequirements.title"),
									JOptionPane.WARNING_MESSAGE);
						}
					}.start();
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

		main.getGeneralDataChangedKeeper().dataHaveChanged(this);
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

	@Override
	public boolean infoIfNoClientsSelected() {

		if (selectedClients.length == 0) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("InstallationStateTableModel.noClientsSelected"),
					Configed.getResourceValue("InstallationStateTableModel.noClientsSelected.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return true;
		}

		return false;
	}

	protected void registerStateChange(String product, String stateType, String value) {
		for (String clientId : selectedClients) {
			setChangedState(clientId, product, stateType, value);
		}
	}

	@Override
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
					Globals.APPNAME + ": "
							+ Configed.getResourceValue("InstallationStateTableModel.missingProducts.title"),
					true, new String[] { "ok" }, 400, 300);
			fMissingProducts.setMessage(lines.toString());
			fMissingProducts.setAlwaysOnTop(true);
			fMissingProducts.setVisible(true);

		}

	}

	@Override
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

	protected void initChangeActionRequests() {

		product2setOfClientsWithNewAction.clear();

		// for each product, we shall collect the clients that have a changed action
		// request
		missingProducts.clear();

	}

	protected void setActionRequest(ActionRequest ar, String productId, String clientId) {

		Map<String, Map<String, String>> productStates = allClientsProductStates.computeIfAbsent(clientId,
				arg -> new HashMap<>());

		productStates.computeIfAbsent(productId, arg -> new HashMap<>()).put(ProductState.KEY_ACTION_REQUEST,
				ar.toString());

	}

	private boolean checkActionIsSupported(String productId, ActionRequest ar) {
		if ((possibleActions == null) || ((possibleActions.get(productId).indexOf(ar.toString())) < 0)) {
			missingImplementationForAR.add(productId);
			return false;
		}

		return true;
	}

	@Override
	public void collectiveChangeActionRequest(String productId, ActionRequest ar) {

		Logging.info(this, "collectiveChangeActionRequest for product " + productId + " to " + ar);

		if (!checkActionIsSupported(productId, ar)) {
			return;
		}

		initChangeActionRequests();

		if ((possibleActions == null) || ((possibleActions.get(productId).indexOf(ar.toString())) < 0)) {
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
			Logging.debug(this, "collectiveChangeActionRequest we have selected clients  " + selectedClients.length);

			// -- not each client got a new action for this product

			String newValUntilNow = null;
			boolean started = false;

			for (String clientId : selectedClients) {

				if (!started) {
					started = true;
					newValUntilNow = getChangedState(clientId, product.getKey(), ProductState.KEY_ACTION_REQUEST);
				} else {
					if (newValUntilNow == null) {
						if (getChangedState(clientId, product.getKey(), ProductState.KEY_ACTION_REQUEST) != null) {
							newValUntilNow = Globals.CONFLICT_STATE_STRING;
						}
					} else {
						if (newValUntilNow
								.equals(getChangedState(clientId, product.getKey(), ProductState.KEY_ACTION_REQUEST))) {
							// it remains
						} else {
							newValUntilNow = Globals.CONFLICT_STATE_STRING;
						}
					}
				}

			}
			combinedVisualValues.get(ProductState.KEY_ACTION_REQUEST).put(product.getKey(), newValUntilNow);

		}

		// removes the selection
		fireTableDataChanged();

		// ordering command
		tellAndClearMissingProducts(productId);

	}

	protected void recursivelyChangeActionRequest(String clientId, String product, ActionRequest ar)
	// adds the new value to the collection of changed states
	// calls the dependencies for the next turn
	{
		Logging.debug(this, "recursivelyChangeActionRequest " + clientId + ", " + product + ", " + ar);

		setChangedState(clientId, product, ActionRequest.KEY, ar.toString());

		Set<String> aSetOfClients = product2setOfClientsWithNewAction.get(product);

		if (aSetOfClients == null) {
			aSetOfClients = new HashSet<>();
			product2setOfClientsWithNewAction.put(product, aSetOfClients);
		}

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
			// don't follow
		} else if (ar.getVal() == ActionRequest.UNINSTALL) {
			Logging.debug(this, " follow requirements for ActionRequest.UNINSTALL, product " + product);

			Map<String, String> requirements = persist.getProductDeinstallRequirements(null, product);
			Logging.debug(this, "ProductRequirements for uninstall for " + product + ": " + requirements);
			followRequirements(clientId, requirements);
		} else {

			Map<String, String> requirements = persist.getProductPreRequirements(null, product);
			Logging.debug(this, "ProductPreRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);

			requirements = persist.getProductRequirements(null, product);
			Logging.debug(this, "ProductRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);

			requirements = persist.getProductPostRequirements(null, product);
			Logging.debug(this, "ProductPostRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);
		}
	}

	private void followRequirements(String clientId, Map<String, String> requirements) {
		String requiredAction;
		String requiredState;

		Logging.info(this, "-- followRequirements for client " + clientId + " requirements " + requirements);

		for (Entry<String, String> requirement : requirements.entrySet()) {
			Logging.debug(this, "requiredProduct: " + requirement.getKey());
			requiredAction = ActionRequest.getLabel(ActionRequest.NONE);
			requiredState = InstallationStatus.getLabel(InstallationStatus.UNDEFINED);

			int colonpos = requirement.getValue().indexOf(":");
			if (colonpos >= 0) {
				requiredState = requirement.getValue().substring(0, colonpos);
				requiredAction = requirement.getValue().substring(colonpos + 1);
			}

			Logging.debug(this, "followRequirements, required product: " + requirement.getKey());
			Logging.debug(this, "followRequirements, required action: " + requiredAction);
			Logging.debug(this, "followRequirements, required state: " + requiredState);

			if (!tsProductNames.contains(requirement.getKey())) {
				Logging.warning("followRequirements: required product: '" + requirement.getKey() + "' not installable");
				missingProducts.add(requirement.getKey());
			} else {
				if (getChangedState(clientId, requirement.getKey(), ActionRequest.KEY) != null) {
					Logging.debug(this, "required product: '" + requirement.getKey()
							+ "'  has already been treated - stop recursion");
				}

				// check required product

				// retrieving the actual state and actionRequest of the required product
				Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
				if (productStates != null) {
					Map<String, String> stateAndAction = productStates.get(requirement.getKey());
					Logging.debug(this, "---- stateAndAction " + stateAndAction);

					if (stateAndAction == null) {
						stateAndAction = new ProductState(null);
					}

					if (stateAndAction != null) {
						String actionRequestForRequiredProduct = stateAndAction.get(ActionRequest.KEY);

						Logging.debug(this, "---- stateAndAction until now: ActionRequest for requiredProduct "
								+ actionRequestForRequiredProduct);

						String installationStatusOfRequiredProduct = stateAndAction.get(InstallationStatus.KEY);

						Logging.debug(this, "---- stateAndAction until now: InstallationStatus for requiredProduct "
								+ installationStatusOfRequiredProduct);

						Logging.debug(this, "requiredAction " + requiredAction);
						Logging.debug(this,
								"ActionRequest.getVal(requiredAction) " + ActionRequest.getVal(requiredAction));
						int requiredAR = ActionRequest.getVal(requiredAction);

						int requiredIS = InstallationStatus.getVal(requiredState);

						Logging.debug(this,
								" requiredInstallationsStatus " + InstallationStatus.getDisplayLabel(requiredIS));

						// handle state requests
						if ((requiredIS == InstallationStatus.INSTALLED
								|| requiredIS == InstallationStatus.NOT_INSTALLED)
								// the only relevant states for which we should eventually do something
								&& InstallationStatus.getVal(installationStatusOfRequiredProduct) != requiredIS)
						// we overwrite the required action request
						{

							String requiredStatusS = InstallationStatus.getLabel(requiredIS);
							Logging.debug(this, " requiredStatusS " + requiredStatusS);

							String neededAction = Config.requiredActionForStatus.get(requiredStatusS);
							Logging.debug(this, " needed action therefore " + neededAction);

							requiredAR = ActionRequest.getVal(neededAction);
						}

						// handle resulting action requests
						if (requiredAR > ActionRequest.NONE)

						{

							checkForContradictingAssignments(clientId, requirement.getKey(), ActionRequest.KEY,
									ActionRequest.getLabel(requiredAR));

							if (
							// an action is required and already set
							ActionRequest.getVal(actionRequestForRequiredProduct) == requiredAR) {
								Logging.info(this, "followRequirements:   no change of action request necessary for "
										+ requirement.getKey());
							}

							else {
								String alreadyExistingNewActionRequest = getChangedState(clientId, requirement.getKey(),
										ActionRequest.KEY);

								if (alreadyExistingNewActionRequest != null) {
									Logging.info(this, "required product: '" + requirement.getKey()
											+ "'  has already been treated");
									Logging.info(this, "new action was " + alreadyExistingNewActionRequest);

									// already set for clientId, product "

								} else {

									Logging.info(this, "ar:   ===== recursion into " + requirement.getKey());
									recursivelyChangeActionRequest(clientId, requirement.getKey(),
											new ActionRequest(requiredAR));
								}
							}
						}
					}
				}
			}
		}
	}

	protected int getRowFromProductID(String id) {
		return productsV.indexOf(id);
	}

	@Override
	public Map<String, Map<String, Object>> getGlobalProductInfos() {
		return globalProductInfos;
	}

	// interface ComboBoxModeller
	@Override
	public ComboBoxModel<String> getComboBoxModel(int row, int column) {
		actualProduct = productsV.get(row);

		if (column == displayColumns.indexOf(ActionRequest.KEY)) {
			// selection of actions

			Logging.debug(this, " possible actions  " + possibleActions);
			String[] actionsForProduct = null;
			if (possibleActions != null) {
				List<String> actionList = new ArrayList<>();

				// actionList.addALL (List) possibleActions.get(actualProduct)
				// instead of this we take the display strings:

				// we shall iterate throught all possible actionRequest ID strings for the actual product
				Iterator<String> iter = possibleActions.get(actualProduct).iterator();

				while (iter.hasNext()) {
					String label = iter.next();
					ActionRequest ar = ActionRequest.produceFromLabel(label);
					actionList.add(ActionRequest.getDisplayLabel(ar.getVal()));
				}

				// add UNDEFINED string only to local copy but we dont want to set anything to
				// UNDEFINED

				actionsForProduct = actionList.toArray(new String[0]);

				Logging.debug("Possible actions as array  " + actionsForProduct);
			}

			if (actionsForProduct == null) {
				actionsForProduct = new String[] { "null" };
			}

			return new DefaultComboBoxModel<>(actionsForProduct);
		} else if (column == displayColumns.indexOf(InstallationStatus.KEY)) {
			// selection of status

			// we dont have the product in our depot selection
			if (possibleActions.get(actualProduct) == null) {
				String state = combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct);
				if (state == null) {
					return new DefaultComboBoxModel<>(new String[] { "null" });
				}

				return new DefaultComboBoxModel<>(new String[] {});
			}

			return new DefaultComboBoxModel<>(InstallationStatus.getDisplayLabelsForChoice());
		} else if (column == displayColumns.indexOf(TargetConfiguration.KEY)) {
			// selection of status

			// we dont have the product in our depot selection
			if (possibleActions.get(actualProduct) == null) {
				String state = combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct);
				if (state == null) {
					return new DefaultComboBoxModel<>(new String[] { "null" });
				}

				return new DefaultComboBoxModel<>(new String[] {});
			}

			return new DefaultComboBoxModel<>(TargetConfiguration.getDisplayLabelsForChoice());
		} else if (column == displayColumns.indexOf(ProductState.KEY_INSTALLATION_INFO)) {
			String delivered = (String) getValueAt(row, column);
			if (delivered == null) {
				delivered = "";
			}

			LinkedHashSet<String> values = new LinkedHashSet<>();

			if (!InstallationInfo.defaultDisplayValues.contains(delivered)) {
				values.add(delivered);
			}

			values.addAll(InstallationInfo.defaultDisplayValues);

			return new DefaultComboBoxModel<>(values.toArray(new String[0]));
		}

		return null;
	}

	// table model

	@Override
	public int getColumnCount() {
		return numberOfColumns;
	}

	@Override
	public int getRowCount() {
		return productsV.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnTitles.get(col);

	}

	@Override
	public String getLastStateChange(int row) {
		String product = productsV.get(row);

		return combinedVisualValues.get(ProductState.KEY_LAST_STATE_CHANGE).get(product);
	}

	protected void setOnGoingCollectiveChangeCount() {
		onGoingCollectiveChangeEventCount++;
	}

	// this method may be overwritten e.g.for row filtering but retrieveValue
	// continues to work
	@Override
	public Object getValueAt(int row, int displayCol) {
		return retrieveValueAt(row, displayCol);
	}

	private Object retrieveValueAt(int row, int displayCol) {
		actualProduct = productsV.get(row);

		if (displayCol >= indexPreparedColumns.length) {
			return "";
		}

		int col = indexPreparedColumns[displayCol];

		switch (col) {
		case 0:
			return actualProduct;

		case 1:
			return globalProductInfos.get(actualProduct).get(ProductState.KEY_PRODUCT_NAME);

		case 2:
			return combinedVisualValues.get(ProductState.KEY_TARGET_CONFIGURATION).get(actualProduct);

		case 3:
			InstallationStatus is = InstallationStatus.produceFromLabel(
					combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct));
			return InstallationStatus.getDisplayLabel(is.getVal());

		case 4:
			return combinedVisualValues.get(ProductState.KEY_INSTALLATION_INFO).get(actualProduct);

		case 5:
			return combinedVisualValues.get(ProductState.KEY_ACTION_PROGRESS).get(actualProduct);

		case 6:
			return combinedVisualValues.get(ProductState.KEY_ACTION_RESULT).get(actualProduct);

		case 7:
			return combinedVisualValues.get(ProductState.KEY_LAST_ACTION).get(actualProduct);

		case 8:

			ActionRequest ar = ActionRequest
					.produceFromLabel(combinedVisualValues.get(ProductState.KEY_ACTION_REQUEST).get(actualProduct));
			return ActionRequest.getDisplayLabel(ar.getVal());

		case 9:
			return combinedVisualValues.get(ProductState.KEY_PRODUCT_PRIORITY).get(actualProduct);

		case 10:
			return combinedVisualValues.get(ProductState.KEY_ACTION_SEQUENCE).get(actualProduct);

		case 11:
			return productNamesInDeliveryOrder.indexOf(actualProduct);

		case 12:
			String serverProductVersion = (String) getGlobalProductInfos().get(actualProduct)
					.get(ProductState.KEY_VERSION_INFO);
			String result = combinedVisualValues.get(ProductState.KEY_VERSION_INFO).get(actualProduct);
			if (result != null && !(result.equals("")) && serverProductVersion != null
					&& !(serverProductVersion.equals(result))) {
				return UNEQUAL_ADD_STRING + result;
			} else {
				return result;
			}

		case 13:
			return combinedVisualValues.get(ProductState.KEY_PRODUCT_VERSION).get(actualProduct);

		case 14:
			return combinedVisualValues.get(ProductState.KEY_PACKAGE_VERSION).get(actualProduct);

		case 15:
			return combinedVisualValues.get(ProductState.KEY_LAST_STATE_CHANGE).get(actualProduct);

		default:
			return null;
		}
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell. If we didn't implement this method,
	 * then the last column would contain text
	 */
	@Override
	public Class<? extends Object> getColumnClass(int c) {
		Object val = retrieveValueAt(0, c);
		if (val == null) {
			return null;
		} else {
			return val.getClass();
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
		changeValueAt(value, row, col);
		fireTableCellUpdated(row, col);
	}

	protected void changeValueAt(Object value, int row, int col) {
		if (value == null) {
			Logging.error(this, "value to set is null");
			return;
		}

		String cl = value.getClass().toString();

		Logging.debug(this, "actual product " + actualProduct + ", setting value at " + row + "," + col + " to " + value
				+ " (an instance of " + cl + ")");

		infoIfNoClientsSelected();

		actualProduct = productsV.get(row);

		if (combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(actualProduct) == null) {
			// not a product in our depot
			return;
		}

		Object retrieveValue = retrieveValueAt(row, col);
		if (retrieveValue == null) {
			Logging.error(this, "value received from retrieveValueAt(...) is null");
			return;
		}

		if (!((String) retrieveValue).equals(value)) {
			if (indexPreparedColumns[col] == preparedColumns.indexOf(InstallationStatus.KEY)) {
				combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).put(actualProduct, (String) value);
				registerStateChange(actualProduct, InstallationStatus.KEY, (String) value);
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
				if (value.equals(InstallationInfo.NONE_DISPLAY_STRING)) {
					value = InstallationInfo.NONE_STRING;
				}

				setInstallationInfo(actualProduct, (String) value);
			}

			main.getGeneralDataChangedKeeper().dataHaveChanged(this);
		}

	}

	protected void clearUpdates() {
		main.getGeneralDataChangedKeeper().cancel();
	}
}
