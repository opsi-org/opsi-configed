/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.core.domain.datachanges.ProductpropertiesUpdateCollection;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.core.infrastructure.messagebus.MessagebusListener;
import de.uib.configed.core.infrastructure.messagebus.WebSocketEvent;
import de.uib.configed.gui.data.InstallationStateUpdateManager;
import de.uib.configed.gui.features.productpage.PanelProductSettings;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class ProductPageManager implements MessagebusListener {
	// the properties for one product and all selected clients
	private List<Map<String, Object>> productProperties;
	private Map<String, ProductpropertiesUpdateCollection> clientProductpropertiesUpdateCollections;

	private InstallationStateUpdateManager updateManager;

	// Map<client, <product, <propertykey, propertyvalue>>>
	private Map<String, Map<String, Map<String, String>>> collectChangedLocalbootStates = new HashMap<>();
	private Map<String, Map<String, Map<String, String>>> collectChangedNetbootStates = new HashMap<>();

	/*
	* for each product:
	* a collection of all clients
	* that contains name value pairs with changed data
	*/
	private ProductpropertiesUpdateCollection clientProductpropertiesUpdateCollection;

	// a map of products, product --> list of used as an indicator that a product is in the depot
	private Map<String, List<String>> possibleActions = new HashMap<>();

	private Map<String, List<Object>> mergedProductProperties;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;
	private ClientConfiguration clientConfiguration;

	private PanelProductSettings panelInUse;

	public ProductPageManager(ConfigedMain configedMain, ClientConfiguration clientConfiguration) {
		this.configedMain = configedMain;
		this.clientConfiguration = clientConfiguration;

		updateManager = new InstallationStateUpdateManager(configedMain,
				clientConfiguration.getPanelLocalbootProductSettings().getProductTable(),
				clientConfiguration.getPanelNetbootProductSettings().getProductTable());

		possibleActions = persistenceController.getDataServices().product
				.getPossibleActionsPD(configedMain.getDepotRepresentative());

		Messagebus.getInstance().getWebSocket().registerListener(this);
	}

	public void setLocalbootProductsPage() {
		List<String> localbootProductDisplayFieldsList = getDisplayFieldsList(
				persistenceController.getDataServices().product.getProductOnClientsDisplayFieldsLocalbootProducts());
		setProductsPage(collectChangedLocalbootStates,
				getAttributesFromProductDisplayFields(localbootProductDisplayFieldsList),
				OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING, clientConfiguration.getPanelLocalbootProductSettings());
	}

	public void setNetbootProductsPage() {
		List<String> netbootProductDisplayFieldsList = getDisplayFieldsList(
				persistenceController.getDataServices().product.getProductOnClientsDisplayFieldsNetbootProducts());
		setProductsPage(collectChangedNetbootStates,
				getAttributesFromProductDisplayFields(netbootProductDisplayFieldsList),
				OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING, clientConfiguration.getPanelNetbootProductSettings());
	}

	private void setProductsPage(Map<String, Map<String, Map<String, String>>> changedProductStates,
			List<String> attributes, String productServerString, PanelProductSettings panelProductSettings) {
		if (configedMain.checkSynchronous(configedMain.getDepotsOfSelectedClients())) {
			configedMain.setDepotRepresentative();
		} else {
			// In this case, we need to go back to the client configuration
			clientConfiguration.setSelectedIndex(0);
			return;
		}

		panelInUse = panelProductSettings;

		Map<String, List<Map<String, String>>> statesAndActions = persistenceController.getDataServices().product
				.getMapOfProductStatesAndActions(configedMain.getSelectedClients(), attributes, productServerString);

		clientProductpropertiesUpdateCollections = new HashMap<>();
		panelProductSettings.clearEditing();

		Logging.debug(this, "setProductsPage,  depotRepresentative:", configedMain.getDepotRepresentative());
		possibleActions = persistenceController.getDataServices().product
				.getPossibleActionsPD(configedMain.getDepotRepresentative());

		// we retrieve the properties for all clients and products

		// it is necessary to do this before resetting selection below (*) since there a
		// listener is triggered
		// which loads the productProperties for each client separately

		persistenceController.getDataServices().product.retrieveProductPropertiesPD(configedMain.getSelectedSet());

		Set<String> oldProductSelection = panelProductSettings.getProductTable().getSelectedIDs();

		Logging.info(this, "setProductsPage: oldProductSelection ", oldProductSelection);
		Logging.debug(this, "setProductsPage: changedProductStates ", changedProductStates);

		Set<String> productNames;
		if (OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING.equals(productServerString)) {
			productNames = persistenceController.getDataServices().product
					.getAllLocalbootProductNames(configedMain.getDepotRepresentative());
		} else {
			productNames = persistenceController.getDataServices().product
					.getAllNetbootProductNames(configedMain.getDepotRepresentative());
		}

		panelProductSettings.setData(configedMain.getSelectedClients(), productNames, statesAndActions,
				persistenceController.getDataServices().product.getProductGlobalInfosPD(
						configedMain.getDepotRepresentative()),
				possibleActions, changedProductStates);

		if (!oldProductSelection.isEmpty()) {
			panelProductSettings.getProductTable().setPendingSelection(oldProductSelection);
		}
		if (panelProductSettings.isFilteredBySelection()) {
			panelProductSettings.getProductTable().reduceToSelected();
		}

		panelProductSettings.restoreFilter();
		panelProductSettings.getProductTable().setPendingSelection(oldProductSelection);
	}

	public void updateProductTableForClient(String clientId, String productType) {
		if (clientConfiguration.getSelectedIndex() == 1
				&& OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING.equals(productType)) {
			List<String> attributes = getAttributesFromProductDisplayFields(
					getDisplayFieldsList(persistenceController.getDataServices().product
							.getProductOnClientsDisplayFieldsLocalbootProducts()));
			updateManager.updateProductTableForClient(clientId, attributes);
		} else if (clientConfiguration.getSelectedIndex() == 2
				&& OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING.equals(productType)) {
			List<String> attributes = getAttributesFromProductDisplayFields(getDisplayFieldsList(
					persistenceController.getDataServices().product.getProductOnClientsDisplayFieldsNetbootProducts()));
			updateManager.updateProductTableForClient(clientId, attributes);
		} else {
			Logging.info(this, "in updateProduct nothing to update because Tab for productType ", productType,
					"not open or configed not yet initialized");
		}
	}

	public static List<String> getDisplayFieldsList(Map<String, Boolean> productDisplayFields) {
		List<String> result = new ArrayList<>();
		for (Entry<String, Boolean> productDisplay : productDisplayFields.entrySet()) {
			if (Boolean.TRUE.equals(productDisplay.getValue())) {
				result.add(productDisplay.getKey());
			}
		}

		return result;
	}

	public PanelProductSettings getPanelInUse() {
		return panelInUse;
	}

	private static List<String> getAttributesFromProductDisplayFields(List<String> productDisplayFields) {
		List<String> attributes = new ArrayList<>();
		for (String v : productDisplayFields) {
			if (ProductState.KEY_VERSION_INFO.equals(v)) {
				attributes.add(ProductState.KEY_PACKAGE_VERSION);
				attributes.add(ProductState.KEY_PRODUCT_VERSION);
				attributes.add(ProductState.KEY_VERSION_INFO);
			} else if (ProductState.KEY_INSTALLATION_INFO.equals(v)) {
				attributes.add(ProductState.KEY_ACTION_PROGRESS);
				attributes.add(ProductState.KEY_ACTION_RESULT);
				attributes.add(ProductState.KEY_LAST_ACTION);
				attributes.add(ProductState.KEY_INSTALLATION_INFO);
			} else {
				attributes.add(v);
			}
		}

		// Remove uneeded attributes
		attributes.remove(ProductState.KEY_PRODUCT_PRIORITY);

		attributes.add(ProductState.KEY_LAST_STATE_CHANGE);

		return attributes;
	}

	public void setProductEdited(String productName, PanelProductSettings sourcePanel) {
		// called from ProductSettings

		Logging.debug(this, "setProductEdited ", productName);

		if (clientProductpropertiesUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(clientProductpropertiesUpdateCollection);
		}
		clientProductpropertiesUpdateCollection = null;

		if (clientProductpropertiesUpdateCollections.get(productName) == null) {
			// have we got already a clientProductpropertiesUpdateCollection for this
			// product?
			// if not, we produce one

			clientProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(
					configedMain.getSelectedClients(), productName);

			clientProductpropertiesUpdateCollections.put(productName, clientProductpropertiesUpdateCollection);
			UpdateCollectionManager.addToGlobalUpdateCollection(clientProductpropertiesUpdateCollection);
		} else {
			clientProductpropertiesUpdateCollection = clientProductpropertiesUpdateCollections.get(productName);
		}

		collectTheProductProperties(productName);

		configedMain.getDependenciesModel().setActualProduct(productName);

		Logging.debug(this, " --- mergedProductProperties ", mergedProductProperties);

		Map<String, Object> originalMap = persistenceController.getDataServices().product
				.getProductPropertiesWithoutDefaults(configedMain.getSelectedClients(), List.of(productName))
				.get(productName);

		sourcePanel.initEditing(productName, productProperties, POJOReMapper.remap(mergedProductProperties),
				clientProductpropertiesUpdateCollection, originalMap != null ? originalMap : new HashMap<>());
	}

	private void collectTheProductProperties(String productEdited) {
		// we build
		// --
		// -- the map of the merged product properties from combining the properties of
		// all selected clients

		Logging.info(this, "collectTheProductProperties for ", productEdited);
		productProperties = configedMain.getSelectedClients().stream()
				.map(clientId -> persistenceController.getDataServices().product.getProductPropertiesPD(clientId,
						productEdited))
				.toList();

		mergedProductProperties = ConfigedUtilityMethods.mergeMaps(productProperties);
	}

	public void updateProductStates() {
		Logging.debug(this, "update product states: collectChangedLocalbootStates ", collectChangedLocalbootStates);
		Logging.debug(this, "update product states: collectChangedNetbootStates ", collectChangedNetbootStates);

		updateManager.updateProductStates(collectChangedLocalbootStates, OpsiPackage.TYPE_LOCALBOOT);
		updateManager.updateProductStates(collectChangedNetbootStates, OpsiPackage.TYPE_NETBOOT);
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// Not required to implement.
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		// Sleep for a little because otherwise we cannot get the needed data from the server.
		Utils.threadSleep(this, 5);

		if (!WebSocketEvent.GENERAL_EVENT.toString().equals(message.get("type")) && !message.containsKey("event")) {
			return;
		}

		String eventType = (String) message.get("event");

		if (WebSocketEvent.PRODUCT_ON_CLIENT_CREATED.toString().equals(eventType)
				|| WebSocketEvent.PRODUCT_ON_CLIENT_UPDATED.toString().equals(eventType)
				|| WebSocketEvent.PRODUCT_ON_CLIENT_DELETED.toString().equals(eventType)) {
			updateManager.updateProduct(POJOReMapper.remap(message.get("data")));
		}
	}
}
