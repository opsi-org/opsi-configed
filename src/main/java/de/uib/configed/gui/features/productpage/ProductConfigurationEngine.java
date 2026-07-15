/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.swing.JOptionPane;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.core.domain.productstate.ActionResult;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.LastAction;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.logging.Logging;

public class ProductConfigurationEngine {
	private static final Map<String, String> REQUIRED_ACTION_FOR_STATUS = Map.ofEntries(
			Map.entry(InstallationStatus.KEY_INSTALLED, "setup"),
			Map.entry(InstallationStatus.KEY_NOT_INSTALLED, "uninstall"));
	protected static final String UNEQUAL_ADD_STRING = "≠ ";

	private static final String NONE_STRING = "";
	protected static final String NONE_DISPLAY_STRING = "none";
	protected static final String FAILED_DISPLAY_STRING = "failed";
	protected static final String SUCCESS_DISPLAY_STRING = "success";
	private static final String MANUALLY = "manually set";

	private Map<String, String> product2request = new HashMap<>();
	private Map<String, List<String>> possibleActions;
	private Map<String, Map<String, Object>> globalProductInfos;
	private Map<String, Map<String, String>> combinedVisualValues = new HashMap<>();
	private Map<String, Map<String, Map<String, String>>> changedProductStates;
	private Map<String, Map<String, Map<String, String>>> allClientsProductStates;
	private Set<String> availableProductNames = new HashSet<>();
	private Set<String> missingProducts = new LinkedHashSet<>();

	private String productBeingEdited;

	private boolean suppressCollectiveActionPropagation;

	private ProductTableModified productTableModified;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ProductConfigurationEngine(ProductTableModified productTableModified, ConfigedMain configedMain) {
		this.productTableModified = productTableModified;
		this.configedMain = configedMain;
	}

	public void initialize(List<String> selectedClients, Set<String> productNames,
			Map<String, List<Map<String, String>>> statesAndActions,
			Map<String, Map<String, Object>> globalProductInfos,
			Map<String, Map<String, Map<String, String>>> changedProductStates,
			Map<String, List<String>> possibleActions) {
		this.possibleActions = possibleActions;
		this.availableProductNames = new LinkedHashSet<>(productNames);
		this.changedProductStates = changedProductStates;
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
		completeWithDefaults(selectedClients, productNames);
	}

	public void produceVisualStatesFromExistingEntries() {
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

	public void updateClientProductStates(String clientId, Collection<Map<String, String>> productInfos) {
		if (!productInfos.isEmpty()) {
			for (Map<String, String> productInfo : productInfos) {
				allClientsProductStates.get(clientId).put(productInfo.get("productId"), productInfo);
			}
		} else {
			allClientsProductStates.get(clientId).clear();
		}
	}

	public void completeWithDefaults(Iterable<String> clients) {
		completeWithDefaults(clients, availableProductNames);
	}

	public void completeWithDefaults(Iterable<String> clients, Iterable<String> productNames) {
		clients.forEach(clientId -> productNames.forEach((String productId) -> {
			if (combinedVisualValues.values().stream().anyMatch(m -> m.containsKey(productId))) {
				return;
			}

			ProductState.KEYS.stream().forEach(key -> mixToVisualState(combinedVisualValues.get(key), productId,
					ProductState.getDefaultProductState().get(key)));
		}));
	}

	public List<Map<String, Object>> buildSnapshot() {
		return availableProductNames.stream().map(this::buildRowForProduct).toList();
	}

	private Map<String, Object> buildRowForProduct(String productId) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put(ProductState.KEY_PRODUCT_ID, productId);
		row.put(ProductState.KEY_PRODUCT_NAME, globalProductInfos.get(productId).get(ProductState.KEY_PRODUCT_NAME));
		row.put(ProductState.KEY_INSTALLATION_STATUS,
				InstallationStatus.produceFromLabel(getVisualValue(ProductState.KEY_INSTALLATION_STATUS, productId)));
		row.put(ProductState.KEY_ACTION_REQUEST,
				ActionRequest.produceFromLabel(getVisualValue(ProductState.KEY_ACTION_REQUEST, productId)));
		row.put(ProductState.KEY_PRODUCT_PRIORITY, getVisualValue(ProductState.KEY_PRODUCT_PRIORITY, productId));
		row.put(ProductState.KEY_ACTION_SEQUENCE,
				normalizePosition(getVisualValue(ProductState.KEY_ACTION_SEQUENCE, productId)));
		row.put(ProductState.KEY_VERSION_INFO, computeVersionDisplay(productId, globalProductInfos));
		row.put(ProductState.KEY_INSTALLATION_INFO, getVisualValue(ProductState.KEY_INSTALLATION_INFO, productId));
		row.put(ProductState.KEY_LAST_STATE_CHANGE, getVisualValue(ProductState.KEY_LAST_STATE_CHANGE, productId));
		return row;
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

	private String computeVersionDisplay(String productId, Map<String, Map<String, Object>> globalProductInfos) {
		String installationStatus = combinedVisualValues.get(ProductState.KEY_INSTALLATION_STATUS).get(productId);
		String lastAction = combinedVisualValues.get(ProductState.KEY_LAST_ACTION).get(productId);

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

	public String getVisualValue(String key, String productId) {
		Map<String, String> values = combinedVisualValues.getOrDefault(key, Map.of());
		return values.get(productId);
	}

	private static String normalizePosition(String position) {
		return "-1".equals(position) ? "" : position;
	}

	public void setProductVersionBasedOnInstallationStatus(String product, String installationStatus) {
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

			productTableModified.applyColumnChangeToRow(product, ProductState.KEY_VERSION_INFO, version);
		}
	}

	public void setInstallationInfo(String product, String value) {
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

	public String getChangedState(String clientId, String product, String stateType) {
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

	public Map<String, Map<String, String>> getClientProductStates(String clientId) {
		return allClientsProductStates.get(clientId);
	}

	public void setActionRequest(ActionRequest ar, String productId, String clientId) {
		Map<String, Map<String, String>> productStates = allClientsProductStates.computeIfAbsent(clientId,
				ignored -> new HashMap<>());
		productStates.computeIfAbsent(productId, ignored -> new HashMap<>()).put(ProductState.KEY_ACTION_REQUEST,
				ar.toString());
		if (changedProductStates != null) {
			changedProductStates.computeIfAbsent(clientId, ignored -> new HashMap<>())
					.computeIfAbsent(productId, ignored -> new HashMap<>())
					.put(ProductState.KEY_ACTION_REQUEST, ar.toString());
		}
		refreshCombinedVisualState(allClientsProductStates, productId, ProductState.KEY_ACTION_REQUEST);
	}

	private void refreshCombinedVisualState(Map<String, Map<String, Map<String, String>>> allClientsProductStates,
			String productId, String columnKey) {
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

	public void clearProductChangedStates() {
		if (changedProductStates != null) {
			changedProductStates.clear();
		}
	}

	public void setChangedProductStates(Map<String, Map<String, Map<String, String>>> changedProductStates) {
		this.changedProductStates = changedProductStates;
	}

	public void updateProductsStates(Iterable<String> clients, String productId, String columnId, String value) {
		clients.forEach((String clientId) -> {
			checkForContradictingAssignments(clientId, productId, columnId, value);

			changedProductStates.computeIfAbsent(clientId, k -> new HashMap<>())
					.computeIfAbsent(productId, k -> new HashMap<>()).put(columnId, value);
		});
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
		productTableModified.applyColumnChangeToRow(product, ProductState.KEY_ACTION_REQUEST, ar.toString());

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
		Map<String, Map<String, String>> productStates = getClientProductStates(clientId);
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

	public void setProductBeingEdited(String productId) {
		productBeingEdited = productId;
	}

	private boolean checkActionIsSupported(String productId, ActionRequest ar) {
		if (possibleActions == null || possibleActions.get(productId) == null) {
			return false;
		}

		return possibleActions.get(productId).contains(ar.toString());
	}

	public void changeActionRequest(String productId, String actionRequestValue) {
		product2request = new HashMap<>();
		if (productId != null && actionRequestValue != null) {
			collectiveChangeActionRequest(productId, ActionRequest.produceActionRequestFromLabel(actionRequestValue));
		}
	}

	public boolean isSuppressCollectiveActionPropagation() {
		return suppressCollectiveActionPropagation;
	}
}
