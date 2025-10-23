/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SortOrder;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.core.domain.datachanges.ProductpropertiesUpdateCollection;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.core.infrastructure.messagebus.MessagebusListener;
import de.uib.configed.core.infrastructure.messagebus.WebSocketEvent;
import de.uib.configed.gui.data.InstallationStateTableModel;
import de.uib.configed.gui.data.InstallationStateUpdateManager;
import de.uib.configed.gui.data.ListMerger;
import de.uib.configed.gui.features.productpage.PanelProductSettings;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.UserPreferences;

public class ProductPageManager implements MessagebusListener {
	// the properties for one product and all selected clients
	private Collection<Map<String, Object>> productProperties;
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

	private Map<String, ListMerger> mergedProductProperties;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;
	private ClientConfiguration clientConfiguration;

	public ProductPageManager(ConfigedMain configedMain, ClientConfiguration clientConfiguration) {
		this.configedMain = configedMain;
		this.clientConfiguration = clientConfiguration;

		updateManager = new InstallationStateUpdateManager(configedMain,
				clientConfiguration.getPanelLocalbootProductSettings().getProductTable(),
				clientConfiguration.getPanelNetbootProductSettings().getProductTable());

		possibleActions = persistenceController.getProductDataService()
				.getPossibleActionsPD(configedMain.getDepotRepresentative());

		Messagebus.getInstance().getWebSocket().registerListener(this);
	}

	public void setLocalbootProductsPage() {
		setProductsPage(collectChangedLocalbootStates, getLocalbootStateAndActionsAttributes(),
				OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING, clientConfiguration.getPanelLocalbootProductSettings(),
				getLocalbootProductDisplayFieldsList());
	}

	public void setNetbootProductsPage() {
		setProductsPage(collectChangedNetbootStates, Collections.emptyList(), OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING,
				clientConfiguration.getPanelNetbootProductSettings(), getNetbootProductDisplayFieldsList());
	}

	private void setProductsPage(Map<String, Map<String, Map<String, String>>> changedProductStates,
			List<String> attributes, String productServerString, PanelProductSettings panelProductSettings,
			List<String> displayFields) {
		if (configedMain.checkSynchronous(configedMain.getDepotsOfSelectedClients())) {
			configedMain.setDepotRepresentative();
		} else {
			// In this case, we need to go back to the client configuration
			clientConfiguration.setSelectedIndex(0);
			return;
		}

		Map<String, List<Map<String, String>>> statesAndActions = persistenceController.getProductDataService()
				.getMapOfProductStatesAndActions(configedMain.getSelectedClients(), attributes, productServerString);

		clientProductpropertiesUpdateCollections = new HashMap<>();
		panelProductSettings.clearEditing();

		Logging.debug(this, "setProductsPage,  depotRepresentative:", configedMain.getDepotRepresentative());
		possibleActions = persistenceController.getProductDataService()
				.getPossibleActionsPD(configedMain.getDepotRepresentative());

		// we retrieve the properties for all clients and products

		// it is necessary to do this before resetting selection below (*) since there a
		// listener is triggered
		// which loads the productProperties for each client separately

		persistenceController.getProductDataService()
				.retrieveProductPropertiesPD(configedMain.getClientTablePanel().getClientTable().getSelectedSet());

		Set<String> oldProductSelection = panelProductSettings.getProductTable().getSelectedIDs();

		Map<String, SortOrder> sortKeyNames = panelProductSettings.getProductTable().getSortedNames();
		Logging.info(this, "setProductsPage: oldProductSelection ", oldProductSelection);
		Logging.debug(this, "setProductsPage: changedProductStates ", changedProductStates);

		Set<String> productNames;
		if (OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING.equals(productServerString)) {
			productNames = persistenceController.getProductDataService()
					.getAllLocalbootProductNames(configedMain.getDepotRepresentative());
		} else {
			productNames = persistenceController.getProductDataService()
					.getAllNetbootProductNames(configedMain.getDepotRepresentative());
		}

		UserPreferences.set(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING.equals(productServerString)
				? UserPreferences.LOCALBOOT_TABLE_DISPLAY_FIELDS
				: UserPreferences.NETBOOT_TABLE_DISPLAY_FIELDS, String.join(",", displayFields));
		InstallationStateTableModel istmForSelectedClients = new InstallationStateTableModel(
				configedMain.getSelectedClients(), changedProductStates, productNames, statesAndActions,
				possibleActions, persistenceController.getProductDataService()
						.getProductGlobalInfosPD(configedMain.getDepotRepresentative()),
				displayFields);
		panelProductSettings.setTableModel(istmForSelectedClients);

		panelProductSettings.getProductTable().setSortedByNames(sortKeyNames);

		if (!oldProductSelection.isEmpty()) {
			panelProductSettings.getProductTable().setPendingSelection(oldProductSelection);
		}
		if (panelProductSettings.isFilteredMode()) {
			panelProductSettings.getProductTable().reduceToSelected();
		}

		panelProductSettings.updateSearchFields();
		panelProductSettings.restoreFilter();

		int[] columnWidths = ConfigedUtilityMethods.getTableColumnWidths(panelProductSettings.getProductTable());
		ConfigedUtilityMethods.setTableColumnWidths(panelProductSettings.getProductTable(), columnWidths);
	}

	private List<String> getLocalbootStateAndActionsAttributes() {
		List<String> attributes = getAttributesFromProductDisplayFields(getLocalbootProductDisplayFieldsList());

		if (getLocalbootProductDisplayFieldsList().contains(ProductState.KEY_INSTALLATION_INFO)) {
			attributes.add(ProductState.KEY_ACTION_PROGRESS);
			attributes.add(ProductState.KEY_LAST_ACTION);
		}

		// Remove uneeded attributes
		attributes.remove(ProductState.KEY_PRODUCT_PRIORITY);

		attributes.add(ProductState.KEY_LAST_STATE_CHANGE);
		return attributes;
	}

	private List<String> getLocalbootProductDisplayFieldsList() {
		List<String> result = new ArrayList<>();
		for (Entry<String, Boolean> productDisplay : persistenceController.getProductDataService()
				.getProductOnClientsDisplayFieldsLocalbootProducts().entrySet()) {
			if (Boolean.TRUE.equals(productDisplay.getValue())) {
				result.add(productDisplay.getKey());
			}
		}

		return result;
	}

	public void updateProductTableForClient(String clientId, String productType) {
		if (clientConfiguration.getSelectedIndex() == 1
				&& OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING.equals(productType)) {
			List<String> attributes = getLocalbootStateAndActionsAttributes();
			updateManager.updateProductTableForClient(clientId, attributes);
		} else if (clientConfiguration.getSelectedIndex() == 2
				&& OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING.equals(productType)) {
			List<String> attributes = getAttributesFromProductDisplayFields(getNetbootProductDisplayFieldsList());
			// Remove uneeded attributes
			attributes.remove(ProductState.KEY_PRODUCT_PRIORITY);
			attributes.add(ProductState.KEY_LAST_STATE_CHANGE);

			updateManager.updateProductTableForClient(clientId, attributes);
		} else {
			Logging.info(this, "in updateProduct nothing to update because Tab for productType ", productType,
					"not open or configed not yet initialized");
		}
	}

	private List<String> getNetbootProductDisplayFieldsList() {
		List<String> result = new ArrayList<>();

		for (Entry<String, Boolean> productDisplay : persistenceController.getProductDataService()
				.getProductOnClientsDisplayFieldsNetbootProducts().entrySet()) {
			if (Boolean.TRUE.equals(productDisplay.getValue())) {
				result.add(productDisplay.getKey());
			}
		}

		return result;
	}

	private static List<String> getAttributesFromProductDisplayFields(List<String> productDisplayFields) {
		List<String> attributes = new ArrayList<>();
		for (String v : productDisplayFields) {
			if (ProductState.KEY_VERSION_INFO.equals(v)) {
				attributes.add(ProductState.KEY_PACKAGE_VERSION);
				attributes.add(ProductState.KEY_PRODUCT_VERSION);
			} else if (ProductState.KEY_INSTALLATION_INFO.equals(v)) {
				attributes.add(ProductState.KEY_ACTION_RESULT);
			} else {
				attributes.add(v);
			}
		}

		return attributes;
	}

	public void setProductEdited(String productname, PanelProductSettings sourcePanel) {
		// called from ProductSettings

		Logging.debug(this, "setProductEdited ", productname);

		if (clientProductpropertiesUpdateCollection != null) {
			UpdateCollectionManager.removeFromGlobalUpdateCollection(clientProductpropertiesUpdateCollection);
		}
		clientProductpropertiesUpdateCollection = null;

		if (clientProductpropertiesUpdateCollections.get(productname) == null) {
			// have we got already a clientProductpropertiesUpdateCollection for this
			// product?
			// if not, we produce one

			clientProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(
					configedMain.getSelectedClients(), productname);

			clientProductpropertiesUpdateCollections.put(productname, clientProductpropertiesUpdateCollection);
			UpdateCollectionManager.addToGlobalUpdateCollection(clientProductpropertiesUpdateCollection);
		} else {
			clientProductpropertiesUpdateCollection = clientProductpropertiesUpdateCollections.get(productname);
		}

		collectTheProductProperties(productname);

		configedMain.getDependenciesModel().setActualProduct(productname);

		Logging.debug(this, " --- mergedProductProperties ", mergedProductProperties);

		sourcePanel.initEditing(productname, productProperties, POJOReMapper.remap(mergedProductProperties),
				clientProductpropertiesUpdateCollection);
	}

	private void collectTheProductProperties(String productEdited) {
		// we build
		// --
		// -- the map of the merged product properties from combining the properties of
		// all selected clients

		Logging.info(this, "collectTheProductProperties for ", productEdited);
		mergedProductProperties = new HashMap<>();
		productProperties = new ArrayList<>(configedMain.getSelectedClients().size());

		if (!configedMain.getSelectedClients().isEmpty() && possibleActions.get(productEdited) != null) {
			Map<String, Object> productPropertiesFor1Client = persistenceController.getProductDataService()
					.getProductPropertiesPD(configedMain.getSelectedClients().get(0), productEdited);

			if (productPropertiesFor1Client != null) {
				productProperties.add(productPropertiesFor1Client);

				for (Entry<String, Object> productProperty : productPropertiesFor1Client.entrySet()) {
					// create a merger for product property
					ListMerger merger = new ListMerger((List<?>) productProperty.getValue());

					mergedProductProperties.put(productProperty.getKey(), merger);
				}

				// merge the other clients
				mergeOtherClients(productEdited);
			}
		}
	}

	private void mergeOtherClients(String productEdited) {
		for (int i = 1; i < configedMain.getSelectedClients().size(); i++) {
			String selectedClient = configedMain.getSelectedClients().get(i);

			Map<String, Object> productPropertiesFor1Client = persistenceController.getProductDataService()
					.getProductPropertiesPD(selectedClient, productEdited);

			productProperties.add(productPropertiesFor1Client);

			for (Entry<String, Object> productProperty : productPropertiesFor1Client.entrySet()) {
				List<?> value = (List<?>) productProperty.getValue();

				if (mergedProductProperties.get(productProperty.getValue()) == null) {
					// we need a new property. it is not common

					ListMerger merger = new ListMerger(value);

					merger.setHavingNoCommonValue();
					mergedProductProperties.put(productProperty.getKey(), merger);
				} else {
					ListMerger merger = mergedProductProperties.get(productProperty.getKey());

					ListMerger mergedValue = merger.merge(value);

					// on merging we check if the value is the same as before
					mergedProductProperties.put(productProperty.getKey(), mergedValue);
				}
			}
		}
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
