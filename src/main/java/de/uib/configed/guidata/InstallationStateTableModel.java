package de.uib.configed.guidata;

import java.awt.Color;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

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

import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.InstallationInfo;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.LastAction;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.productstate.TargetConfiguration;
import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.logging.logging;

/**
 * Defining the TableModel for the product table for a specific client. Since we
 * here have the required data the class implements the ComboBoxModeler for
 * getting cell editors.
 */
public class InstallationStateTableModel extends javax.swing.table.AbstractTableModel

		implements ComboBoxModeller, IFInstallationStateTableModel {

	public static final String EMPTYFIELD = "_";
	// public static final String NOT_AVAILABLEstring = "--";
	public static final String CONFLICTstring = Globals.CONFLICT_STATE_STRING;

	public static final Color backgroundGrey = new Color(220, 220, 220);
	public static final Color conflictBackColor = backgroundGrey;
	public static final Color conflictTextColor = backgroundGrey;
	public static final Color defaultBackColor = Color.white;
	public static final Color defaultTextColor = Color.black;
	// public static final Color notUpdatedTextColor = defaultTextColor; // new
	// Color(250, 30, 0); // needed for update_version_display

	final String initString = "";
	public static final String unequalAddstring = "≠ ";

	protected static Map<String, String> columnDict;
	protected static java.util.List<String> columnsLocalized;

	protected String actualProduct = "";

	protected ConfigedMain main;

	protected Vector productsV = null;

	protected int onGoingCollectiveChangeEventCount = -1;

	Map<Integer, Map<String, String>> changeEventCount2product2request;

	/*
	 * protected Map targets; //combined values for selected clients
	 * protected Map states; //combined values for selected clients
	 * protected Map installationInfos; //combined values for selected clients
	 * protected Map results; //combined values for selected clients
	 * protected Map progresses; //combined values for selected clients
	 * protected Map lastActions; //combined values for selected clients
	 * protected Map actions; //combined values for selected clients
	 * protected Map priorities; //combined values for selected clients
	 * protected Map positions; //combined values for selected clients
	 * protected Map versionInfos; //combined values for selected clients
	 * protected Map productVersions; //combined values for selected clients
	 * protected Map packageVersions; //combined values for selected clients
	 */

	protected Map<String, Map<String, String>> combinedVisualValues; // state key (column name) --> product name -->
																		// visual value

	protected Map stateChanges;

	protected Map<String, Set<String>> product2setOfClientsWithNewAction;
	// for each product, we shall collect the clients that have a changed action
	// request
	// protected Map<String, ActionRequest> product2AR;
	// for each product, we remember the visual action that is set
	protected java.util.TreeSet<String> missingImplementationForAR;

	protected Map<String, Map<String, Map<String, String>>> allClientsProductStates; // (clientId -> (productId ->
																						// (product state key -> product
																						// state value)))
																						// protected final Map<String, java.util.List<Map<String, String>>>
																						// allClientsProductlistsSaved;//clientId -> (productrows)

	protected PersistenceController persist;
	protected Map<String, Map<String, Map<String, String>>> collectChangedStates;
	protected final String[] selectedClients;
	protected Map<String, java.util.List<String>> possibleActions; // product-->possibleActions
	protected Map<String, Map<String, Object>> globalProductInfos;
	protected String theClient;
	protected TreeSet<String> tsProductNames;
	protected Vector<String> productNamesInDeliveryOrder;

	protected ActionRequest actionInTreatment;
	// protected boolean changeActionIsSet = false;
	protected java.util.Set<String> missingProducts = new HashSet<String>();

	protected List<String> displayColumns;
	protected int numberOfColumns;
	protected List<String> preparedColumns; // the columns for which code exists
	protected List<String> columnTitles;
	protected int[] indexPreparedColumns; // the indices of the displayColumns in the displayColumns
	protected boolean[] editablePreparedColumns;

	// collects titles for the columns prepared in this class

	public static void restartColumnDict() {
		columnDict = null;
	}

	public static String getColumnTitle(String column) {
		if (columnDict == null) {
			columnDict = new HashMap<String, String>();
			columnDict.put("productId", configed.getResourceValue("InstallationStateTableModel.productId"));
			columnDict.put(ProductState.KEY_productName,
					configed.getResourceValue("InstallationStateTableModel.productName"));
			columnDict.put(ProductState.KEY_targetConfiguration,
					configed.getResourceValue("InstallationStateTableModel.targetConfiguration"));
			columnDict.put(ProductState.KEY_installationStatus,
					configed.getResourceValue("InstallationStateTableModel.installationStatus"));

			columnDict.put("installationInfo", configed.getResourceValue("InstallationStateTableModel.report"));
			// combines the following three
			columnDict.put(ProductState.KEY_actionProgress,
					configed.getResourceValue("InstallationStateTableModel.actionProgress"));
			columnDict.put(ProductState.KEY_actionResult,
					configed.getResourceValue("InstallationStateTableModel.actionResult"));
			columnDict.put(ProductState.KEY_lastAction,
					configed.getResourceValue("InstallationStateTableModel.lastAction"));

			columnDict.put(ProductState.KEY_actionRequest,
					configed.getResourceValue("InstallationStateTableModel.actionRequest"));
			columnDict.put(ProductState.KEY_productPriority,
					configed.getResourceValue("InstallationStateTableModel.priority"));
			columnDict.put(ProductState.KEY_actionSequence, "actionSequence");

			columnDict.put(ProductState.KEY_position,
					configed.getResourceValue("InstallationStateTableModel.position"));

			columnDict.put(ProductState.KEY_versionInfo, "Version");
			// combines the following two
			columnDict.put(ProductState.KEY_productVersion,
					configed.getResourceValue("InstallationStateTableModel.productVersion"));
			columnDict.put(ProductState.KEY_packageVersion,
					configed.getResourceValue("InstallationStateTableModel.packageVersion"));

			columnDict.put(ProductState.KEY_lastStateChange,
					configed.getResourceValue("InstallationStateTableModel.lastStateChange"));

		}

		if (columnDict.get(column) == null)
			return "";

		return columnDict.get(column);
	}

	public static java.util.List<String> localizeColumns(java.util.List<String> cols) {
		java.util.List<String> result = new ArrayList<String>();

		if (columnDict != null) {
			for (String col : cols) {
				if (columnDict.get(col) != null)
					result.add(columnDict.get(col));
			}
		}
		return result;
	}

	public InstallationStateTableModel(String[] selectedClients, ConfigedMain main,
			Map<String, Map<String, Map<String, String>>> collectChangedStates, List<String> listOfInstallableProducts,
			Map<String, java.util.List<Map<String, String>>> statesAndActions,
			Map<String, java.util.List<String>> possibleActions, // product-->possibleActions
			Map<String, Map<String, Object>> productGlobalInfos, List<String> displayColumns) {
		logging.info(this, "creating an InstallationStateTableModel ");
		if (statesAndActions == null)
			logging.info(this, " statesAndActions null ");
		else
			logging.info(this, " statesAndActions " + statesAndActions.size());

		this.main = main;

		this.collectChangedStates = collectChangedStates;
		this.selectedClients = selectedClients;

		this.possibleActions = possibleActions;
		this.globalProductInfos = productGlobalInfos;

		initColumnNames(displayColumns);
		initChangedStates();

		missingImplementationForAR = new java.util.TreeSet<String>();
		product2setOfClientsWithNewAction = new HashMap<String, Set<String>>();

		persist = main.getPersistenceController();
		Collator myCollator = Collator.getInstance();
		// myCollator.setStrength(Collator.PRIMARY); //ignores hyphens
		myCollator.setStrength(Collator.SECONDARY);

		// logging.info(this, "listOfInstallableProducts " + listOfInstallableProducts);
		productNamesInDeliveryOrder = new Vector<String>();
		if (listOfInstallableProducts != null) {
			for (int i = 0; i < listOfInstallableProducts.size(); i++) {
				String product = (String) listOfInstallableProducts.get(i);
				productNamesInDeliveryOrder.add(product);
			}
		}

		tsProductNames = new TreeSet<String>(myCollator);
		tsProductNames.addAll(productNamesInDeliveryOrder);
		productsV = new Vector(tsProductNames);

		logging.debug(this, "tsProductNames " + tsProductNames);

		// allClientsProductlistsSaved = statesAndActions; trying to start a deep copy
		initalizeProductStates(statesAndActions);
	}

	private void initalizeProductStates(Map<String, java.util.List<Map<String, String>>> client2listProductState) {
		allClientsProductStates = new HashMap<String, Map<String, Map<String, String>>>();
		remaptoClient2Product2Rowmap(client2listProductState);

		produceVisualStatesFromExistingEntries();
		completeVisualStatesByDefaults();
	}

	private void remaptoClient2Product2Rowmap(Map<String, java.util.List<Map<String, String>>> clientAllProductRows) {
		if (clientAllProductRows == null)
			return;

		// iterate through all clients for which a list of
		// products/states/actionrequests exist
		// logging.debug(this, "" + clientAllProductRows);

		for (String clientId : clientAllProductRows.keySet()) {
			Map<String, Map<String, String>> productRows = new HashMap<String, Map<String, String>>();

			allClientsProductStates.put(clientId, productRows);
			// for each client we build the productstates map

			List<Map<String, String>> productRowsList1client = clientAllProductRows.get(clientId);

			for (int i = 0; i < productRowsList1client.size(); i++) {
				Map<String, String> stateAndAction = productRowsList1client.get(i);
				// new HashMap<String, String>(productRowsList1client.get(i));
				// deep copy, but seems to be not complete, therefore not used
				// if (clientId.equals("vbrupertwin7-64.uib.local"))
				// logging.info(this, "stateAndAction " + stateAndAction);

				String productId = stateAndAction.get(ProductState.KEY_productId);
				productRows.put(productId, stateAndAction);
			}
		}
	}

	/*
	 * 
	 * private void rebuildVisualStatesFromExistingEntries()
	 * {
	 * 
	 * for (String clientId : allClientsProductStates.keySet())
	 * {
	 * for (String productId : allClientsProductStates.get( clientId).keySet() )
	 * {
	 * Map<String, String> stateAndAction =
	 * allClientsProductStates.get(clientId).get( productId );
	 * 
	 * if (!stateAndAction.get("actionRequest").equalsIgnoreCase("none"))
	 * logging.info(this,
	 * "rebuildVisualStatesFromExistingEntries stateAndAction for client " +
	 * clientId + " product " + productId
	 * + " action " + stateAndAction.get("actionRequest") );
	 * }
	 * 
	 * }
	 * }
	 */

	private void produceVisualStatesFromExistingEntries() {

		combinedVisualValues = new HashMap<String, Map<String, String>>();
		for (String key : ProductState.KEYS) {
			HashMap<String, String> combinedVisualValuesForOneColumn = new HashMap<String, String>();
			combinedVisualValues.put(key, combinedVisualValuesForOneColumn);
		}

		for (String clientId : allClientsProductStates.keySet()) {
			for (String productId : allClientsProductStates.get(clientId).keySet()) {
				Map<String, String> stateAndAction = allClientsProductStates.get(clientId).get(productId);

				if (stateAndAction == null) {
					logging.warning(this,
							"produceVisualStatesFromExistingEntries, no row for " + clientId + ", " + productId);
					continue;
				}

				// change values for visual output
				String targetConfiguration = stateAndAction.get(ProductState.KEY_targetConfiguration);
				if (targetConfiguration == null || targetConfiguration.equals(""))
					targetConfiguration = TargetConfiguration.getLabel(TargetConfiguration.UNDEFINED);
				stateAndAction.put(ProductState.KEY_targetConfiguration, targetConfiguration);

				String priority = "";
				if (globalProductInfos != null && globalProductInfos.get(productId) != null)
					priority = "" + globalProductInfos.get(productId).get("priority");// aProduct.get(ActionSequence.KEY);
				stateAndAction.put(ProductState.KEY_productPriority, priority);

				stateAndAction.put(ProductState.KEY_actionSequence, priority);

				// stateAndAction.put(ProductState.KEY_lastStateChange, "test");

				// build visual states
				for (String colKey : ProductState.KEYS) {
					// logging.debug(this, "produceVisualStates, clientId " + clientId + ",
					// productMap " + productMapAsRetrieved);
					// logging.debug(this, "produceVisualStates, clientId " + clientId + ",
					// combinedVisualValues.get(colKey) " + combinedVisualValues.get(colKey));

					// if (stateAndAction.get(ProductState.KEY_actionRequest).equals("setup"))
					// logging.info(this, "produceVisualStatesFromExistingEntries clientId " +
					// clientId + ", stateAndAction " + stateAndAction);

					if (colKey == ProductState.KEY_actionRequest) {
						logging.debug(this, " ------------before   mixtovisualstate " + "product " + productId
								+ " value " + stateAndAction.get(colKey));
					}

					mixToVisualState(colKey, combinedVisualValues.get(colKey), productId, stateAndAction.get(colKey));
					// logging.info(this, "produceVisualStates, clientId " + clientId + ",
					// lastStateChange " + stateAndAction.get(ProductState.KEY_lastStateChange));
					// logging.info(this, "produceVisualStates, clientId " + clientId + ",
					// lastStateChange " +
					// combinedVisualValues.get(ProductState.KEY_lastStateChange));

				}

			}
		}
	}

	private void completeVisualStatesByDefaults() {
		for (String clientId : selectedClients) {

			// check if productstates exist
			Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
			if (productStates == null) {
				productStates = new HashMap<String, Map<String, String>>();
				allClientsProductStates.put(clientId, productStates);
			}

			// check if products for clients exist
			for (int j = 0; j < productsV.size(); j++) {

				String productId = (String) productsV.get(j);
				Map<String, String> stateAndAction = productStates.get(productId);

				if (stateAndAction == null) {

					/*
					 * stateAndAction = new ProductState(null);
					 * //ProductState.getDEFAULT(); //for testing
					 * //defaults for the product
					 * productStates.put( productId, stateAndAction);
					 * 
					 * stateAndAction.put(ProductState.KEY_productId, productId);
					 * 
					 * 
					 * 
					 * String priority = "";
					 * if (globalProductInfos != null && globalProductInfos.get(productId) != null)
					 * priority = "" +
					 * globalProductInfos.get(productId).get("priority");//aProduct.get(
					 * ActionSequence.KEY);
					 * stateAndAction.put(ProductState.KEY_productPriority, priority);
					 * 
					 * stateAndAction.put(ProductState.KEY_actionSequence, priority);
					 * 
					 */

					// build visual states
					Iterator iter = ProductState.KEYS.iterator();

					String priority = "";

					if (globalProductInfos != null && globalProductInfos.get(productId) != null)
						priority = "" + globalProductInfos.get(productId).get("priority");// aProduct.get(ActionSequence.KEY);

					while (iter.hasNext()) {
						String key = (String) iter.next();

						if (key == ProductState.KEY_productPriority || key == ProductState.KEY_actionSequence) {
							mixToVisualState(key, combinedVisualValues.get(key), productId, priority);
						} else {
							mixToVisualState(key, combinedVisualValues.get(key), productId,
									ProductState.getDEFAULT().get(key));
						}
					}

					/*
					 * 
					 * mixToVisualState (actions, productId, actionRequest);
					 * mixToVisualState (states, productId, installationStatus);
					 * mixToVisualState (productVersions, productId, productVersion);
					 * mixToVisualState (packageVersions, productId, packageVersion);
					 * mixToVisualState(progresses, productId, actionProgress);
					 * 
					 * mixToVisualState(targets, productId, targetConfiguration);
					 * mixToVisualState(results, productId, actionResult);
					 * //logging.debug(this, "results " + results + ", actionResult mixed in: " +
					 * actionResult);
					 * mixToVisualState(lastActions, productId, lastAction);
					 * mixToVisualState(priorities, productId, priority);
					 */
				}
			}
		}
	}

	// protected String mixToVisualState (final String columnName, Map<String,
	// String> visualStates, final String productId, final String mixinValue,
	// boolean initial)

	protected String mixToVisualState(final String columnName, Map<String, String> visualStates, final String productId,
			final String mixinValue) {
		String oldValue = (String) visualStates.get(productId);
		// logging.info(this, " ------------------- mixtovisualstates, columnName " +
		// columnName + " productId " + productId + " oldValue " + oldValue + "
		// mixinValue " + mixinValue);

		String resultValue = oldValue;
		if (oldValue == null)
		// ! states.containsKey(productId)
		{

			// if (columnName.equals("actionRequest"))
			// logging.info(this, " ------------------- mixtovisualstates, columnName " +
			// columnName + " productId " + productId + " oldValue null " + " mixinValue " +
			// mixinValue);
			resultValue = mixinValue;
			visualStates.put(productId, resultValue);
		} else {
			if (!oldValue.equalsIgnoreCase(mixinValue)) {
				resultValue = CONFLICTstring;
				visualStates.put(productId, resultValue);
			}
		}

		/*
		 * if (columnName == ActionRequest.KEY && mixinValue.equals("setup"))
		 * {
		 * logging.info(this, "mixtovisualstate visualStates " + visualStates);
		 * logging.info(this, "mixtovisualstate result " + resultValue);
		 * logging.info(this, "mixtovisualstate oldValue " + oldValue);
		 * logging.info(this, "mixtovisualstate mixinValue " + mixinValue);
		 * }
		 */
		return resultValue;
	}

	private Boolean preparedColumnIsEditable(int j) {
		if (editablePreparedColumns == null || j < 0 || j >= editablePreparedColumns.length)
			return null;

		if (Globals.isGlobalReadOnly())
			return false;

		return editablePreparedColumns[j];
	}

	// builds list of all prepared column key names (preparedColumns)
	// defines which column might be editable (editablePreparedColumns)
	// builds index of the currently displayed columns in terms of the prepared
	// columns (indexPreparedColumns)
	private void initColumnNames(List<String> columnsToDisplay) {
		preparedColumns = new ArrayList<String>();
		editablePreparedColumns = new boolean[16];

		preparedColumns.add(0, ProductState.KEY_productId);
		editablePreparedColumns[0] = false;

		preparedColumns.add(1, ProductState.KEY_productName);
		editablePreparedColumns[1] = false;

		preparedColumns.add(2, ProductState.KEY_targetConfiguration);
		editablePreparedColumns[2] = true;

		preparedColumns.add(3, ProductState.KEY_installationStatus);
		editablePreparedColumns[3] = true;

		preparedColumns.add(4, ProductState.KEY_installationInfo);
		editablePreparedColumns[4] = true; // false;

		preparedColumns.add(5, ProductState.KEY_actionProgress);
		editablePreparedColumns[5] = false;

		preparedColumns.add(6, ProductState.KEY_actionResult);
		editablePreparedColumns[6] = false;

		preparedColumns.add(7, ProductState.KEY_lastAction);
		editablePreparedColumns[7] = false;

		preparedColumns.add(8, ProductState.KEY_actionRequest);
		editablePreparedColumns[8] = true;

		preparedColumns.add(9, ProductState.KEY_productPriority);
		editablePreparedColumns[9] = false;

		preparedColumns.add(10, ProductState.KEY_actionSequence);
		editablePreparedColumns[10] = false;

		preparedColumns.add(11, ProductState.KEY_position);
		editablePreparedColumns[11] = false;

		preparedColumns.add(12, ProductState.KEY_versionInfo);
		editablePreparedColumns[12] = false;

		preparedColumns.add(13, ProductState.KEY_productVersion);
		editablePreparedColumns[13] = false;

		preparedColumns.add(14, ProductState.KEY_packageVersion);
		editablePreparedColumns[14] = false;

		preparedColumns.add(15, ProductState.KEY_lastStateChange);
		editablePreparedColumns[15] = false;

		if (columnsToDisplay == null) {
			logging.error(this, "columnsToDisplay are null");
			return;
		}

		displayColumns = columnsToDisplay;

		logging.info(this, "preparedColumns:  " + preparedColumns);
		logging.info(this, "columnsToDisplay: " + columnsToDisplay);

		indexPreparedColumns = new int[columnsToDisplay.size()];
		columnTitles = new ArrayList<String>();
		{
			Iterator iter = columnsToDisplay.iterator();
			int j = 0;
			while (iter.hasNext()) {
				String column = (String) iter.next();
				logging.debug(this, " ------- treat column " + column);
				int k = preparedColumns.indexOf(column);
				if (k >= 0) {
					indexPreparedColumns[j] = k;
					logging.debug(this, "indexPreparedColumns of displayColumn " + j + " is " + k);
					columnTitles.add(getColumnTitle(column));
				} else {
					logging.info(this, "column " + column + " is not prepared");
					columnTitles.add(column);
				}

				j++;
			}
		}
		numberOfColumns = displayColumns.size();
		logging.info(this, " -------- numberOfColumns " + numberOfColumns);

	}

	public int getColumnIndex(String columnName) {
		return displayColumns.indexOf(columnName);
	}

	private void initChangedStates() {
		for (String clientId : selectedClients) {
			Map<String, Map<String, String>> changedStates = new HashMap<String, Map<String, String>>();
			collectChangedStates.put(clientId, changedStates);
		}
	}

	public void clearCollectChangedStates() {
		collectChangedStates.clear();
		// changeActionIsSet = false;
		// initChangedStates();
	}

	protected void setInstallationInfo(String product, String value) {
		combinedVisualValues.get(ProductState.KEY_installationInfo).put(product, value);

		for (String clientId : selectedClients) {
			setInstallationInfo(clientId, product, value);
		}
	}

	private void setInstallationInfo(String clientId, String product, String value) {
		logging.debug(this,
				"setInstallationInfo for product, client, value " + product + ", " + clientId + ", " + value);

		Map<String, Map<String, String>> changedStatesForClient = (Map<String, Map<String, String>>) (collectChangedStates
				.get(clientId));
		if (changedStatesForClient == null) {
			changedStatesForClient = new HashMap<String, Map<String, String>>();
			collectChangedStates.put(clientId, changedStatesForClient);
		}

		Map<String, String> changedStatesForProduct = (Map<String, String>) changedStatesForClient.get(product);
		if (changedStatesForProduct == null) {
			changedStatesForProduct = new HashMap<String, String>();
			changedStatesForClient.put(product, changedStatesForProduct);
		}

		// reverse from putting together the values in ProductState

		if (value.equals(InstallationInfo.NONEstring) // we set this in the calling method
				|| value.equals(InstallationInfo.NONEdisplayString) // this is asked only for formal independence of the
																	// method
		) {
			changedStatesForProduct.put(ProductState.KEY_lastAction, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_actionResult, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_actionProgress, InstallationInfo.NONEstring);

		} else if (value.equals(InstallationInfo.FAILEDdisplayString)) {
			changedStatesForProduct.put(ProductState.KEY_lastAction, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_actionResult, ActionResult.getLabel(ActionResult.FAILED));
			changedStatesForProduct.put(ProductState.KEY_actionProgress, InstallationInfo.MANUALLY);
		} else if (value.equals(InstallationInfo.SUCCESSdisplayString)) {
			changedStatesForProduct.put(ProductState.KEY_lastAction, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_actionResult, ActionResult.getLabel(ActionResult.SUCCESSFUL));
			changedStatesForProduct.put(ProductState.KEY_actionProgress, InstallationInfo.MANUALLY);
		} else {
			changedStatesForProduct.put(ProductState.KEY_lastAction, ActionResult.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_actionResult, LastAction.getLabel(ActionResult.NONE));
			changedStatesForProduct.put(ProductState.KEY_actionProgress, value);
		}

	}

	private void checkForContradictingAssignments(String clientId, String product, String stateType, String state) {
		if (changeEventCount2product2request == null)
			changeEventCount2product2request = new HashMap<Integer, Map<String, String>>();

		Map<String, String> product2request = changeEventCount2product2request.get(onGoingCollectiveChangeEventCount);

		if (product2request == null) {
			product2request = new HashMap<String, String>();
			changeEventCount2product2request.put(onGoingCollectiveChangeEventCount, product2request);

		}

		logging.debug(this, "checkForContradictingAssignments === product2request " + product2request);

		String existingRequest = product2request.get(product);
		String info = " existingRequest " + existingRequest;

		logging.info(this, "checkForContradictingAssignments " + info + " state " + state);

		if (existingRequest == null || existingRequest.equals("")) {
			product2request.put(product, state);
			logging.debug(this, "checkForContradictingAssignments client " + clientId + ", actualproduct "
					+ actualProduct + ", product " + product + ", stateType " + stateType + ", state " + state);

		}

		else {
			boolean contradicting = !existingRequest.equals(state);
			info = info + " for onGoingCollectiveChangeEventCount " + onGoingCollectiveChangeEventCount
					+ " contradicting " + contradicting;
			if (contradicting) {
				if (actualProduct.equals(product)) {
					logging.info(this, "checkForContradictingAssignments new setting for product is " + state);
					product2request.put(product, state);

					final String infoOfChange = String.format(
							configed.getResourceValue("InstallationStateTableModel.contradictingProductRequirements3"),
							actualProduct, existingRequest, state);
					new Thread() {
						public void run() {
							javax.swing.JOptionPane.showMessageDialog(Globals.mainFrame, infoOfChange,
									configed.getResourceValue(
											"InstallationStateTableModel.contradictingProductRequirements.title"),
									javax.swing.JOptionPane.WARNING_MESSAGE);
						}
					}.start();

				} else {

					logging.warning(this,
							"checkForContradictingAssignments " + info + " client " + clientId + ", actualproduct "
									+ actualProduct + ", product " + product + ", stateType " + stateType + ", state "
									+ state);

					// Contradicting product requirements: \n
					// Product %s \n
					// requires that product %s gets action %s \n
					// but a different product set it to %s

					final String errorInfo = String.format(
							configed.getResourceValue("InstallationStateTableModel.contradictingProductRequirements1"),
							actualProduct, product, state)
							+ String.format(
									configed.getResourceValue(
											"InstallationStateTableModel.contradictingProductRequirements2"),
									existingRequest);

					new Thread() {
						public void run() {
							javax.swing.JOptionPane.showMessageDialog(Globals.mainFrame, errorInfo,
									configed.getResourceValue(
											"InstallationStateTableModel.contradictingProductRequirements.title"),
									javax.swing.JOptionPane.WARNING_MESSAGE);
						}
					}.start();
				}

			}

		}

		// logging.info(this, "checkForContradictingAssignments === " + info + " client
		// " + clientId + ", actualproduct " + actualProduct + ", product " + product +
		// ", stateType " + stateType + ", state " + state);

		logging.info(this, "checkForContradictingAssignments === onGoingCollectiveChangeEventCount: product2request "
				+ onGoingCollectiveChangeEventCount + ": " + product2request);
	}

	private void setChangedState(String clientId, String product, String stateType, String state) {
		Map<String, Map<String, String>> changedStatesForClient = (Map<String, Map<String, String>>) (collectChangedStates
				.get(clientId));
		if (changedStatesForClient == null) {
			changedStatesForClient = new HashMap<String, Map<String, String>>();
			collectChangedStates.put(clientId, changedStatesForClient);
		}

		Map<String, String> changedStatesForProduct = (Map<String, String>) changedStatesForClient.get(product);
		if (changedStatesForProduct == null) {
			changedStatesForProduct = new HashMap<String, String>();
			changedStatesForClient.put(product, changedStatesForProduct);
		}

		logging.info(this, "accumulateProductRequests4event");
		checkForContradictingAssignments(clientId, product, stateType, state);

		changedStatesForProduct.put(stateType, state);
		// test testes setValueAt("None", 2, 6);
		// if (!product.equals( actualProduct)) setValueAt( state, productsV.indexOf (
		// product ), 3 );
		main.getGeneralDataChangedKeeper().dataHaveChanged(this);
	}

	private String getChangedState(String clientId, String product, String stateType) {
		Map<String, Map<String, String>> changedStatesForClient = collectChangedStates.get(clientId);
		if (changedStatesForClient == null)
			return null;

		Map<String, String> changedStatesForProduct = (Map<String, String>) changedStatesForClient.get(product);
		if (changedStatesForProduct == null)
			return null;

		return changedStatesForProduct.get(stateType);
	}

	public boolean infoIfNoClientsSelected() {

		if (selectedClients.length == 0) {
			javax.swing.JOptionPane.showMessageDialog(Globals.mainFrame,
					configed.getResourceValue("InstallationStateTableModel.noClientsSelected"),
					configed.getResourceValue("InstallationStateTableModel.noClientsSelected.title"),
					javax.swing.JOptionPane.INFORMATION_MESSAGE);
			return true;
		}

		return false;
	}

	protected void registerStateChange(String product, String stateType, String value) {
		for (String clientId : selectedClients) {
			setChangedState(clientId, product, stateType, value);
		}
	}

	public void initCollectiveChange() {
		logging.debug(this, "initCollectiveChange");
		setOnGoingCollectiveChangeCount();
		missingImplementationForAR.clear();
	}

	private void tellAndClearMissingProducts(String productId) {
		if (missingProducts.size() > 0) {

			logging.info(this, "required by product " + productId + " but missing " + missingProducts);

			StringBuffer lines = new StringBuffer();

			lines.append(configed.getResourceValue("InstallationStateTableModel.requiredByProduct"));
			lines.append("\n");
			lines.append(productId);
			lines.append("\n\n");
			lines.append(configed.getResourceValue("InstallationStateTableModel.missingProducts"));
			lines.append("\n");

			for (String p : missingProducts) {
				lines.append("\n   ");
				lines.append(p);
			}

			final de.uib.configed.gui.FShowList fMissingProducts = new de.uib.configed.gui.FShowList(Globals.mainFrame,
					Globals.APPNAME + ": "
							+ configed.getResourceValue("InstallationStateTableModel.missingProducts.title"),
					true, new String[] { "ok" }, 400, 300);
			fMissingProducts.setMessage(lines.toString());
			fMissingProducts.setAlwaysOnTop(true);
			fMissingProducts.setVisible(true);

			/*
			 * 
			 * 
			 * javax.swing.JOptionPane.showMessageDialog( Globals.mainFrame,
			 * configed.getResourceValue("InstallationStateTableModel.requiredByProduct") +
			 * "\n"
			 * + productId + "\n\n"
			 * + configed.getResourceValue("InstallationStateTableModel.missingProducts") +
			 * "\n"
			 * + lines,
			 * configed.getResourceValue("InstallationStateTableModel.missingProducts.title"
			 * ),
			 * javax.swing.JOptionPane.INFORMATION_MESSAGE
			 * );
			 */
		}

		// missingProducts.clear();
	}

	public void finishCollectiveChange() {
		logging.info(this, "finishCollectiveChange");

		// produceVisualStatesFromExistingEntries();
		// completeVisualStatesByDefaults();

		// changeValueAt
		// changeActionRequest
		// registerStateChange
		// fireTableDataChanged();

		logging.debug(this, "finishCollectiveChange, changes " + collectChangedStates);

		if (missingImplementationForAR.size() > 0) {

			StringBuffer products = new StringBuffer("\n\n\n");
			for (String prod : missingImplementationForAR) {
				products.append(prod);
				products.append("\n");
			}

			javax.swing.JOptionPane.showMessageDialog(Globals.mainFrame,
					configed.getResourceValue("InstallationStateTableModel.missingImplementationForActionRequest")
							+ products,
					configed.getResourceValue(
							"InstallationStateTableModel.missingImplementationForActionRequest.title"),
					javax.swing.JOptionPane.INFORMATION_MESSAGE);
		}

	}

	protected void initChangeActionRequests() {
		// changeActionIsSet = true;
		product2setOfClientsWithNewAction.clear();

		// for each product, we shall collect the clients that have a changed action
		// request
		missingProducts.clear();

		// if (product2AR == null) product2AR = new HashMap<String, ActionRequest>();

		// return missingProducts;
	}

	/*
	 * protected void setVisualActionRequest( ActionRequest ar, String productId )
	 * {
	 * combinedVisualValues.get(ProductState.KEY_actionRequest).put( productId,
	 * ar.toString() );
	 * 
	 * int row = productsV.indexOf( productId );
	 * int col = displayColumns.indexOf( ProductState.KEY_actionRequest);
	 * logging.info(this, "setVisualActionRequest " + ar + " row " + row + " col " +
	 * col);
	 * 
	 * fireTableCellUpdated(row, col);
	 * }
	 */

	protected void setActionRequest(ActionRequest ar, String productId, String clientId) {

		Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
		if (productStates == null) {
			productStates = new HashMap<String, Map<String, String>>();
			allClientsProductStates.put(clientId, productStates);
		}

		Map<String, String> rowMapForaClient = productStates.get(productId);
		if (rowMapForaClient == null) {
			rowMapForaClient = new HashMap<String, String>();
			productStates.put(productId, rowMapForaClient);
		}
		rowMapForaClient.put(ProductState.KEY_actionRequest, ar.toString());

		// setChangedState( clientId, productId, ActionRequest.KEY, ar.toString() );

	}

	private boolean checkActionIsSupported(String productId, ActionRequest ar) {
		if ((possibleActions == null) || ((possibleActions.get(productId).indexOf(ar.toString())) < 0)) {
			missingImplementationForAR.add(productId);
			return false;
		}

		return true;
	}

	public void collectiveChangeActionRequest(String productId, ActionRequest ar) {

		logging.info(this, "collectiveChangeActionRequest for product " + productId + " to " + ar);

		if (!checkActionIsSupported(productId, ar))
			return;

		initChangeActionRequests();

		if ((possibleActions == null) || ((possibleActions.get(productId).indexOf(ar.toString())) < 0)) {
			logging.error(" the required action is not supported for " + productId);
			return;
		}

		for (String clientId : selectedClients) {
			logging.debug(this, "collectiveChangeActionRequest to " + ar + "  for client " + clientId);
			setActionRequest(ar, productId, clientId);
			recursivelyChangeActionRequest(clientId, productId, ar);
		}

		logging.debug(this, "collectiveChangeActionRequest for product, changed products "
				+ product2setOfClientsWithNewAction.keySet());

		// show the new settings for all products after recursion

		for (String prod : product2setOfClientsWithNewAction.keySet()) {
			logging.debug(this, "collectiveChangeActionRequest for product  " + prod
					+ " changed product for client number : " + product2setOfClientsWithNewAction.get(prod).size());
			logging.debug(this, "collectiveChangeActionRequest we have selected clients  " + selectedClients.length);

			// logging.info(this, "collectiveChangeActionRequest, set value valid if set for
			// all clients " + ar + " for " + prod + " instead of " +
			// combinedVisualValues.get(ProductState.KEY_actionRequest).get(prod));

			// if ( ( product2setOfClientsWithNewAction.get(prod) ).size() <
			// selectedClients.length )
			// -- not each client got a new action for this product
			{
				// String oldVal = combinedVisualValues.get(ProductState.KEY_actionRequest).get(
				// prod );
				String newValUntilNow = null;
				boolean started = false;

				for (String clientId : selectedClients) {
					// String mixInVal = ar.toString();

					if (!started) {
						started = true;
						newValUntilNow = getChangedState(clientId, prod, ProductState.KEY_actionRequest);
					} else {
						if (newValUntilNow == null) {
							if (!(getChangedState(clientId, prod, ProductState.KEY_actionRequest) == null)) {
								newValUntilNow = Globals.CONFLICT_STATE_STRING;
							}
						} else {
							if (newValUntilNow
									.equals(getChangedState(clientId, prod, ProductState.KEY_actionRequest))) {
								// it remains
							} else {
								newValUntilNow = Globals.CONFLICT_STATE_STRING;
							}
						}
					}

				}
				combinedVisualValues.get(ProductState.KEY_actionRequest).put(prod, newValUntilNow);

			}

			/*
			 * else
			 * {
			 * 
			 * String mixInVal = null;
			 * 
			 * for (String clientId : selectedClients)
			 * {
			 * if (mixInVal == null)
			 * // set initial value for the clients
			 * mixInVal = getChangedState(clientId, prod, ProductState.KEY_actionRequest);
			 * 
			 * else
			 * // mix in the value of this client to the value until now
			 * {
			 * if (!mixInVal.equals( getChangedState(clientId, prod,
			 * ProductState.KEY_actionRequest) ))
			 * mixInVal = Globals.CONFLICTSTATEstring;
			 * }
			 * 
			 * logging.info(this, "collectiveChangeActionRequest, set " + ar + " for " +
			 * prod + " instead of " +
			 * combinedVisualValues.get(ProductState.KEY_actionRequest).get(prod) +
			 * ", or perhaps "
			 * + getChangedState(clientId, prod, ProductState.KEY_actionRequest) +
			 * " giving mixin "
			 * + mixInVal);
			 * 
			 * }
			 * //combinedVisualValues.get(ProductState.KEY_actionRequest).put (prod,
			 * ar.toString() );
			 * 
			 * 
			 * if (mixInVal != null)
			 * combinedVisualValues.get(ProductState.KEY_actionRequest).put (prod, mixInVal
			 * );
			 * 
			 * }
			 */
		}

		fireTableDataChanged(); // removes the selection
		// fireTableRowsUpdated(0, productsV.size()-1); //does not trigger a new
		// ordering command
		tellAndClearMissingProducts(productId);

	}

	/*
	 * protected void changeActionRequest (String product, String
	 * theActionRequestString)
	 * {
	 * //logging.info(this, "changeActionRequest called");
	 * ActionRequest actionInTreatment =
	 * ActionRequest.produceFromDisplayLabel(theActionRequestString);
	 * 
	 * initChangeActionRequests();
	 * 
	 * //changedStatesForClient.get(product);
	 * // by recursion, we find all new settings
	 * for (String clientId : selectedClients)
	 * {
	 * 
	 * recursivelyChangeActionRequest (clientId, product, actionInTreatment);
	 * }
	 * 
	 * 
	 * // show the new settings
	 * for (String productId : product2setOfClientsWithNewAction.keySet())
	 * {
	 * if ( ( product2setOfClientsWithNewAction.get(productId) ).size() <
	 * selectedClients.length )
	 * // not each client got a new action for this product
	 * {
	 * //mixToVisualActions(actions, productId, actionInTreatment.toString());
	 * String mixedValue = mixToVisualState(ProductState.KEY_actionRequest,
	 * combinedVisualValues.get(ProductState.KEY_actionRequest), productId,
	 * (product2AR.get(productId)).toString());
	 * if (mixedValue.equals("mixed"))
	 * {
	 * logging.info(this, "changeActionRequest result mixed for product " +
	 * productId );
	 * }
	 * 
	 * }
	 * else
	 * {
	 * combinedVisualValues.get(ProductState.KEY_actionRequest).put (productId,
	 * (product2AR.get(productId)).toString() );
	 * }
	 * }
	 * 
	 * if (missingProducts.size() > 0)
	 * {
	 * logging.info(this, "required by product " + product + " but missing " +
	 * missingProducts);
	 * 
	 * StringBuffer lines = new StringBuffer();
	 * for (String p : missingProducts)
	 * {
	 * lines.append("\n   ");
	 * lines.append(p);
	 * }
	 * 
	 * 
	 * javax.swing.JOptionPane.showMessageDialog( Globals.mainFrame,
	 * configed.getResourceValue("InstallationStateTableModel.requiredByProduct") +
	 * "\n"
	 * + product + "\n\n"
	 * + configed.getResourceValue("InstallationStateTableModel.missingProducts") +
	 * "\n"
	 * + lines,
	 * configed.getResourceValue("InstallationStateTableModel.missingProducts.title"
	 * ),
	 * javax.swing.JOptionPane.INFORMATION_MESSAGE
	 * );
	 * }
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

	protected void recursivelyChangeActionRequest(String clientId, String product, ActionRequest ar)
	// adds the new value to the collection of changed states
	// calls the dependencies for the next turn
	{
		logging.debug(this, "recursivelyChangeActionRequest " + clientId + ", " + product + ", " + ar);

		setChangedState(clientId, product, ActionRequest.KEY, ar.toString());

		Set<String> aSetOfClients = product2setOfClientsWithNewAction.get(product);

		if (aSetOfClients == null) {
			aSetOfClients = new HashSet<String>();
			product2setOfClientsWithNewAction.put(product, aSetOfClients);
		}

		aSetOfClients.add(clientId);

		// product2AR.put (product, ar);

		// actions.put (product, actionInTreatment.toString()); // if necessary change
		// of visible action in combined actions

		// logging.debug(this, "productsV.indexOf must be calculated in a different way
		// in subclass " + productsV.indexOf(actualProduct));
		// int modelRow = getRowFromProductID(actualProduct);
		int modelRow = getRowFromProductID(product);
		logging.debug(this, "recursivelyChangeActionRequest product " + product + " modelRow " + modelRow);
		if (modelRow > -1) {
			logging.debug(this, "recursivelyChangeActionRequest fire update for row  " + modelRow);
			// fireTableCellUpdated(modelRow, displayColumns.indexOf(ActionRequest.KEY)); //
			// tell the table model listeners where a change occurred
			fireTableRowsUpdated(modelRow, modelRow);
			// displayColumns.indexOf(ActionRequest.KEY)); // tell the table model listeners
			// where a change occurred
		}

		logging.debug(this, " change action request for client " + clientId + ",  product " + product + " to " + ar);
		if (ar.getVal() == ActionRequest.NONE) {
			// don't follow
		} else if (ar.getVal() == ActionRequest.UNINSTALL) {
			logging.debug(this, " follow requirements for ActionRequest.UNINSTALL, product " + product);
			// setChangedState(clientId, product, ActionRequest.KEY, ar.toString());
			Map<String, String> requirements = persist.getProductDeinstallRequirements(null, product);
			logging.debug(this, "ProductRequirements for uninstall for " + product + ": " + requirements);
			followRequirements(clientId, requirements);
		} else {
			// setChangedState(clientId, product, ActionRequest.KEY, ar.toString());
			Map<String, String> requirements = persist.getProductPreRequirements(null, product);
			logging.debug(this, "ProductPreRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);

			requirements = persist.getProductRequirements(null, product);
			logging.debug(this, "ProductRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);

			requirements = persist.getProductPostRequirements(null, product);
			logging.debug(this, "ProductPostRequirements for  " + product + ": " + requirements);
			followRequirements(clientId, requirements);

		}

	}

	private void followRequirements(String clientId, Map<String, String> requirements) {
		String requirement;
		String requiredAction;
		String requiredState;

		logging.info(this, "-- followRequirements for client " + clientId + " requirements " + requirements);

		for (String requiredProduct : requirements.keySet()) {
			logging.debug(this, "requiredProduct: " + requiredProduct);
			requirement = requirements.get(requiredProduct);
			requiredAction = ActionRequest.getLabel(ActionRequest.NONE);
			requiredState = InstallationStatus.getLabel(InstallationStatus.UNDEFINED);

			int colonpos = requirement.indexOf(":");
			if (colonpos >= 0) {
				requiredState = requirement.substring(0, colonpos);
				requiredAction = requirement.substring(colonpos + 1);
			}

			logging.debug(this, "followRequirements, required product: " + requiredProduct);
			logging.debug(this, "followRequirements, required action: " + requiredAction);
			logging.debug(this, "followRequirements, required state: " + requiredState);

			if (!tsProductNames.contains(requiredProduct)) {
				logging.warning("followRequirements: required product: '" + requiredProduct + "' not installable");
				missingProducts.add(requiredProduct);
			} else {
				if (getChangedState(clientId, requiredProduct, ActionRequest.KEY) != null) {
					logging.debug(this,
							"required product: '" + requiredProduct + "'  has already been treated - stop recursion");

				}

				// check required product

				// logging.debug(this, "---- requiredProduct " + requiredProduct + ", client " +
				// clientId);
				// retrieving the actual state and actionRequest of the required product
				Map<String, Map<String, String>> productStates = allClientsProductStates.get(clientId);
				if (productStates != null) {
					Map<String, String> stateAndAction = productStates.get(requiredProduct);
					logging.debug(this, "---- stateAndAction " + stateAndAction);

					if (stateAndAction == null)
						stateAndAction = new ProductState(null);

					if (stateAndAction != null) {
						String actionRequestForRequiredProduct = stateAndAction.get(ActionRequest.KEY);

						logging.debug(this, "---- stateAndAction until now: ActionRequest for requiredProduct "
								+ actionRequestForRequiredProduct);

						String installationStatusOfRequiredProduct = stateAndAction.get(InstallationStatus.KEY);

						logging.debug(this, "---- stateAndAction until now: InstallationStatus for requiredProduct "
								+ installationStatusOfRequiredProduct);

						logging.debug(this, "requiredAction " + requiredAction);
						logging.debug(this,
								"ActionRequest.getVal(requiredAction) " + ActionRequest.getVal(requiredAction));
						int requiredAR = ActionRequest.getVal(requiredAction);

						int requiredIS = InstallationStatus.getVal(requiredState);

						logging.debug(this,
								" requiredInstallationsStatus " + InstallationStatus.getDisplayLabel(requiredIS));

						// handle state requests
						if ((requiredIS == InstallationStatus.INSTALLED
								|| requiredIS == InstallationStatus.NOT_INSTALLED)
								// the only relevant states for which we should eventually do something
								&& InstallationStatus.getVal(installationStatusOfRequiredProduct) != requiredIS)
						// we overwrite the required action request
						{

							String requiredStatusS = InstallationStatus.getLabel(requiredIS);
							logging.debug(this, " requiredStatusS " + requiredStatusS);
							String neededAction = de.uib.opsidatamodel.productstate.Config
									.getInstance().requiredActionForStatus.get(requiredStatusS);
							logging.debug(this, " needed action therefore " + neededAction);

							requiredAR // = ActionRequest.leadingTo(requiredIS);
									= ActionRequest.getVal(neededAction);
						}

						// logging.debug(this,"resulting requiredAction " +
						// ActionRequest.getLabel(requiredAR));

						// handle resulting action requests
						if (requiredAR > ActionRequest.NONE)
						/*
						 * requiredAR == ActionRequest.SETUP
						 * || requiredAR == ActionRequest.ALWAYS
						 * || requiredAR == ActionRequest.ONCE
						 * || requiredAR == ActionRequest.CUSTOM
						 * || requiredAR == ActionRequest.UNINSTALL
						 * )
						 */
						{

							checkForContradictingAssignments(clientId, requiredProduct, ActionRequest.KEY,
									ActionRequest.getLabel(requiredAR));

							if (
							// an action is required and already set
							ActionRequest.getVal(actionRequestForRequiredProduct) == requiredAR) {
								logging.info(this, "followRequirements:   no change of action request necessary for "
										+ requiredProduct);
							}

							else {
								String alreadyExistingNewActionRequest = getChangedState(clientId, requiredProduct,
										ActionRequest.KEY);

								if (alreadyExistingNewActionRequest != null) {
									logging.info(this,
											"required product: '" + requiredProduct + "'  has already been treated");
									logging.info(this, "new action was " + alreadyExistingNewActionRequest);

									// logging.error(this, "recursion stop, setting " + requiredAction + " but
									// already set for clientId, product "
									// + clientId + "," + requiredProduct + " : " +
									// alreadyExistingNewActionRequest);
								} else {

									logging.info(this, "ar:   ===== recursion into " + requiredProduct);
									recursivelyChangeActionRequest(clientId, requiredProduct,
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

	public Map<String, Map<String, Object>> getGlobalProductInfos() {
		return globalProductInfos;
	}

	// interface ComboBoxModeller
	public ComboBoxModel getComboBoxModel(int row, int column) {
		actualProduct = (String) productsV.get(row); // products[row];
		// logging.debug(this, "getComboBoxModel(), actualproduct " + actualProduct + "
		// row " + row + " col " + column);

		if (column == displayColumns.indexOf(ActionRequest.KEY)) // selection of actions
		{
			logging.debug(this, " possible actions  " + possibleActions);
			Object[] actionsForProduct = null;
			if (possibleActions != null) {
				ArrayList actionList = new ArrayList();

				// actionList.addALL (List) possibleActions.get(actualProduct)
				// instead of this we take the display strings:

				/*
				 * if (possibleActions.get(actualProduct) == null)
				 * {
				 * actionList.add( ActionRequest.getDisplayLabel( ActionRequest.NOT_AVAILABLE )
				 * );
				 * }
				 * else
				 */
				{

					Iterator iter = ((List) possibleActions.get(actualProduct)).iterator(); // we shall iterate throught
																							// all possible
																							// actionRequest ID strings
																							// for the actual product

					/*
					 * if (!iter.hasNext())
					 * actionList.add( ActionRequest.getDisplayLabel( ActionRequest.NOT_AVAILABLE
					 * ));
					 * else
					 */
					{

						while (iter.hasNext()) {
							String label = (String) iter.next();
							ActionRequest ar = ActionRequest.produceFromLabel(label);
							actionList.add(ActionRequest.getDisplayLabel(ar.getVal()));
						}

						// actionList.add(ActionRequest.UNDEFINEDstring);
						// add UNDEFINED string only to local copy but we dont want to set anything to
						// UNDEFINED
					}
				}

				actionsForProduct = actionList.toArray();

				logging.debug("Possible actions as array  " + actionsForProduct);
			}

			if (actionsForProduct == null)
				actionsForProduct = new String[] { "null" };

			return new DefaultComboBoxModel(actionsForProduct);
		}

		else if (column == displayColumns.indexOf(InstallationStatus.KEY)) // selection of status
		{
			// logging.debug(this,"return InstallationStatus.allStates " +
			// InstallationStatus.allStates);
			if (possibleActions.get(actualProduct) == null)
			// we dont have the product in our depot selection
			{
				String state = (String) combinedVisualValues.get(ProductState.KEY_installationStatus)
						.get(actualProduct);
				if (state == null)
					return new DefaultComboBoxModel(new String[] { "null" });

				return new DefaultComboBoxModel(new String[] {});
			}

			return new DefaultComboBoxModel(InstallationStatus.getDisplayLabelsForChoice());
		}

		else if (column == displayColumns.indexOf(TargetConfiguration.KEY)) // selection of status
		{
			// logging.debug(this,"return InstallationStatus.allStates " +
			// InstallationStatus.allStates);
			if (possibleActions.get(actualProduct) == null)
			// we dont have the product in our depot selection
			{
				String state = (String) combinedVisualValues.get(ProductState.KEY_installationStatus)
						.get(actualProduct);
				if (state == null)
					return new DefaultComboBoxModel(new String[] { "null" });

				return new DefaultComboBoxModel(new String[] {});
			}

			return new DefaultComboBoxModel(TargetConfiguration.getDisplayLabelsForChoice());
		}

		else if (column == displayColumns.indexOf(ProductState.KEY_installationInfo)) {
			String delivered = (String) getValueAt(row, column);
			if (delivered == null)
				delivered = "";

			LinkedHashSet<String> values = new LinkedHashSet<String>();

			if (!InstallationInfo.defaultDisplayValues.contains(delivered))
				values.add(delivered);

			values.addAll(InstallationInfo.defaultDisplayValues);

			/*
			 * if (!delivered.equals(""))
			 * values.add(InstallationInfo.NONEdisplayString);
			 * 
			 * if (!delivered.startsWith(ActionResult.getLabel(ActionResult.SUCCESSFUL ) ) )
			 * values.add(InstallationInfo.SUCCESSdisplayString);
			 * 
			 * if (!delivered.startsWith(ActionResult.getLabel(ActionResult.FAILED ) ) )
			 * values.add(InstallationInfo.FAILEDdisplayString);
			 */

			return new DefaultComboBoxModel(new Vector(values));

		}

		return null;

	}

	// table model

	public int getColumnCount() {
		return numberOfColumns; // 3;
	}

	public int getRowCount() {
		return productsV.size();
	}

	public String getColumnName(int col) {
		return columnTitles.get(col);
		/*
		 * switch (col)
		 * {
		 * case 0 : result = " "; break;
		 * case 1 : result =
		 * configed.getResourceValue("InstallationStateTableModel.installationStatus");
		 * break;
		 * case 2 : result =
		 * configed.getResourceValue("InstallationStateTableModel.productActionProgress"
		 * ); break;
		 * case 3 : result =
		 * configed.getResourceValue("InstallationStateTableModel.actionRequest");
		 * break;
		 * case 4 : result =
		 * configed.getResourceValue("InstallationStateTableModel.productVersion");
		 * break;
		 * case 5 : result =
		 * configed.getResourceValue("InstallationStateTableModel.packageVersion");
		 * break;
		 * };
		 * 
		 * return result;
		 */
	}

	public String getLastStateChange(int row) {
		String actualProduct = (String) productsV.get(row);
		// logging.debug(this,
		// "combinedVisualValues.get(ProductState.KEY_lastStateChange) " +
		// combinedVisualValues.get(ProductState.KEY_lastStateChange));
		// logging.debug(this, " actualProduct, ..get(actualProduct) " + actualProduct +
		// ", " +
		// combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct));
		return combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct);
	}

	protected void setOnGoingCollectiveChangeCount() {
		onGoingCollectiveChangeEventCount++;
	}

	// this method may be overwritten e.g.for row filtering but retrieveValue
	// continues to work
	public Object getValueAt(int row, int displayCol) {
		return retrieveValueAt(row, displayCol);
	}

	private Object retrieveValueAt(int row, int displayCol) {
		// logging.debug (this, "retrieveValueAt, displayCol " + displayCol + " value "
		// +
		// combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct));

		// if (productsV == null || productsV.size() == 0) return "";

		Object result = null;
		actualProduct = (String) productsV.get(row); // products[row];

		/*
		 * boolean productExistsForClient = (states.get(actualProduct) != null);
		 * if (!productExistsForClient)
		 * return NOT_AVAILABLEstring;
		 */

		if (displayCol >= indexPreparedColumns.length)
			return "";

		int col = indexPreparedColumns[displayCol];

		// logging.debug (this, "retrieveValueAt, displayCol " + displayCol + "
		// --------- corresponding preparedCol " + col );

		switch (col) {
		case 0:
			result = actualProduct;
			break;

		case 1:
			result = globalProductInfos.get(actualProduct).get(ProductState.KEY_productName);
			// combinedVisualValues.get(ProductState.KEY_productName).get(actualProduct);
			// there we have not got the value
			break;

		case 2:
			result = combinedVisualValues.get(ProductState.KEY_targetConfiguration).get(actualProduct);
			break;

		case 3:
			// result = "" + states.get(actualProduct);
			InstallationStatus is = InstallationStatus
					.produceFromLabel(combinedVisualValues.get(ProductState.KEY_installationStatus).get(actualProduct));
			result = InstallationStatus.getDisplayLabel(is.getVal());
			break;

		case 4:
			result = combinedVisualValues.get(ProductState.KEY_installationInfo).get(actualProduct);
			break;

		case 5:
			result = combinedVisualValues.get(ProductState.KEY_actionProgress).get(actualProduct);
			break;

		case 6:
			result = combinedVisualValues.get(ProductState.KEY_actionResult).get(actualProduct);
			break;

		case 7:
			result = combinedVisualValues.get(ProductState.KEY_lastAction).get(actualProduct);
			break;

		case 8:
			// result = (String) actions.get(actualProduct) ;
			// if (actualProduct.equals("firefox"))
			// logging.info(this, "value for firefox is " +
			// combinedVisualValues.get(ProductState.KEY_actionRequest).get(actualProduct));
			ActionRequest ar = ActionRequest
					.produceFromLabel(combinedVisualValues.get(ProductState.KEY_actionRequest).get(actualProduct));
			result = ActionRequest.getDisplayLabel(ar.getVal());

			// logging.debug(this," --------- row, col " + row + ", " + col + " result " +
			// result);
			break;

		case 9:
			result = combinedVisualValues.get(ProductState.KEY_productPriority).get(actualProduct);
			break;

		case 10:
			result = combinedVisualValues.get(ProductState.KEY_actionSequence).get(actualProduct);
			// logging.info(this, " actualProduct " + actualProduct + " , actionSequence " +
			// result);
			break;

		case 11:
			result = productNamesInDeliveryOrder.indexOf(actualProduct); // ProductState.KEY_position
			// logging.info(this, " actualProduct " + actualProduct + " , position " +
			// result);
			break;

		case 12:
			String serverProductVersion = (String) getGlobalProductInfos().get(actualProduct)
					.get(de.uib.opsidatamodel.productstate.ProductState.KEY_versionInfo);
			result = combinedVisualValues.get(ProductState.KEY_versionInfo).get(actualProduct);
			if (!(result == null) && !(result.equals(""))) {
				if (!(serverProductVersion == null) && !(serverProductVersion.equals(result)))
					result = unequalAddstring + result;
			}
			break;

		case 13:
			result = combinedVisualValues.get(ProductState.KEY_productVersion).get(actualProduct);
			break;

		case 14:
			result = combinedVisualValues.get(ProductState.KEY_packageVersion).get(actualProduct);
			break;

		case 15:
			result = combinedVisualValues.get(ProductState.KEY_lastStateChange).get(actualProduct);
			// logging.debug(this, " -------(lastStateChange)-- row, col " + row + ", " +
			// col + " result " + result);
			break;

		}

		return result;
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell. If we didn't implement this method,
	 * then the last column would contain text
	 */
	public Class getColumnClass(int c) {
		Object val = retrieveValueAt(0, c);
		if (val == null)
			return null;
		else
			return val.getClass();
	}

	/*
	 * editable columns
	 */
	public boolean isCellEditable(int row, int col) {
		if (preparedColumnIsEditable(indexPreparedColumns[col])) {
			return true;
		}

		return false;
	}

	/*
	 * change method for edited cells
	 */
	public void setValueAt(Object value, int row, int col) {
		logging.debug(this, " actualProduct " + actualProduct + ", set value at " + row + ", " + col);
		changeValueAt(value, row, col);
		fireTableCellUpdated(row, col);
		// logging.debug(this, "set value at " + row + ", " + col);
	}

	/*
	 * public interface RowCondition
	 * {
	 * boolean accept( int row );
	 * }
	 * 
	 * public void setValueAt(Object value, int row, RowCondition rowCondition, int
	 * col)
	 * {
	 * if (rowCondition.accept( row ))
	 * logging.debug(this, "row accepted");
	 * }
	 */

	protected void changeValueAt(Object value, int row, int col) {
		String cl = "nul";
		if (value != null)
			cl = value.getClass().toString();

		logging.debug(this, "actual product " + actualProduct + ", setting value at " + row + "," + col + " to " + value
				+ " (an instance of " + cl + ")");

		infoIfNoClientsSelected();

		// data[row][col] = value; //this is the trivial version
		actualProduct = (String) productsV.get(row);

		if (combinedVisualValues.get(ProductState.KEY_installationStatus).get(actualProduct) == null)
			return; // not a product in our depot

		if (!((String) retrieveValueAt(row, col)).equals((String) value)) {
			if (indexPreparedColumns[col] == preparedColumns.indexOf(InstallationStatus.KEY)) {
				combinedVisualValues.get(ProductState.KEY_installationStatus).put(actualProduct, (String) value);
				registerStateChange(actualProduct, InstallationStatus.KEY, (String) value);
			}

			else if (indexPreparedColumns[col] == preparedColumns.indexOf(TargetConfiguration.KEY)) {
				combinedVisualValues.get(ProductState.KEY_targetConfiguration).put(actualProduct, (String) value);
				registerStateChange(actualProduct, TargetConfiguration.KEY, (String) value);
			}

			else if (indexPreparedColumns[col] == preparedColumns.indexOf(ActionRequest.KEY)) {
				// an action has changed
				// change recursively visible action changes and collect the changes for saving

				// changeActionRequest (actualProduct, (String) value) ;
				initCollectiveChange();
				collectiveChangeActionRequest(actualProduct, ActionRequest.produceFromLabel((String) value));
				finishCollectiveChange();
			}

			else if (indexPreparedColumns[col] == preparedColumns.indexOf(ProductState.KEY_installationInfo)) {
				if (value.equals(InstallationInfo.NONEdisplayString))
					value = InstallationInfo.NONEstring;

				setInstallationInfo(actualProduct, (String) value);
			}

			main.getGeneralDataChangedKeeper().dataHaveChanged(this);
		}

	}

	protected void clearUpdates() {
		main.getGeneralDataChangedKeeper().cancel();
	}

	/**
	 * sets data to the original values clears update collection
	 */
	// it would require the existence of a complete deep copy of the original loaded
	// data
	/*
	 * public void reset()
	 * {
	 * logging.info(this, "reset()");
	 * clearUpdates();
	 * initalizeProductStates( allClientsProductlistsSaved );
	 * fireTableDataChanged();
	 * }
	 */

	/*
	 * only an example for a possible IntPredicate usage
	 * public void setActionRequestWithCondition(ActionRequest ar, IntPredicate
	 * rowCondition)
	 * {
	 * logging.info(this, "setActionRequestWithCondition " + ar + " for " +
	 * getRowCount());
	 * 
	 * if (rowCondition == null)
	 * {
	 * logging.info(this, "setActionRequestWithCondition null");
	 * return;
	 * }
	 * 
	 * 
	 * for (int i = 0; i < getRowCount(); i++)
	 * {
	 * if ( rowCondition.test(i) )
	 * {
	 * logging.info(this, "setAction " + ar + " i " + i);
	 * logging.info(this, "we have product " + getValueAt( i, 0 ));
	 * }
	 * 
	 * }
	 * // table grouppanel
	 * 
	 * }
	 */

}
