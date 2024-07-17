/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.JTable;

import de.uib.configed.ConfigedMain;
import de.uib.configed.type.OpsiPackage;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class InstallationStateUpdateManager {
	private Map<String, Map<String, TreeSet<String>>> productsToUpdate = new HashMap<>();
	private Timer timer;

	private JTable tableLocalbootProducts;
	private JTable tableNetbootProducts;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public InstallationStateUpdateManager(ConfigedMain configedMain, JTable tableLocalbootProducts,
			JTable tableNetbootProducts) {
		this.configedMain = configedMain;
		this.tableLocalbootProducts = tableLocalbootProducts;
		this.tableNetbootProducts = tableNetbootProducts;
	}

	public void updateProductTableForClient(String clientId, List<String> attributes) {
		if (isProductsUpdatedForClient(clientId, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING)
				&& tableLocalbootProducts.getModel() instanceof InstallationStateTableModel) {
			updateTableForClient(clientId, attributes, tableLocalbootProducts);
		} else if (isProductsUpdatedForClient(clientId, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING)
				&& tableNetbootProducts.getModel() instanceof InstallationStateTableModel) {
			updateTableForClient(clientId, attributes, tableNetbootProducts);
		} else {
			Logging.notice(this,
					"Cannot update table because the Product table with the product to update is not open");
		}
	}

	private void updateTableForClient(String clientId, List<String> attributes, JTable tableProducts) {
		InstallationStateTableModel istmForSelectedClients = (InstallationStateTableModel) tableProducts.getModel();

		if (productsToUpdate.get(clientId).get(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING) != null
				&& productsToUpdate.get(clientId).get(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING).size() < 20) {
			istmForSelectedClients.updateTable(clientId,
					productsToUpdate.get(clientId).get(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING), attributes);
		} else {
			istmForSelectedClients.updateTable(clientId, attributes);
		}
	}

	private boolean isProductsUpdatedForClient(String clientId, String productType) {
		return productsToUpdate.get(clientId) != null && productsToUpdate.get(clientId).get(productType) != null
				&& !productsToUpdate.get(clientId).get(productType).isEmpty();
	}

	public void updateProductStates(Map<String, Map<String, Map<String, String>>> collectChangedProductStates,
			int productType) {
		Logging.info(this, "updateProductStates: collectChangedProductStates  ", collectChangedProductStates);

		if (collectChangedProductStates != null && !collectChangedProductStates.isEmpty()) {
			for (Entry<String, Map<String, Map<String, String>>> changedClientState : collectChangedProductStates
					.entrySet()) {
				Map<String, Map<String, String>> clientValues = changedClientState.getValue();

				Logging.debug(this, "updateProductStates, collectChangedProductStates , client "
						+ changedClientState.getKey() + " values " + clientValues);

				if (clientValues.keySet() == null || clientValues.isEmpty()) {
					continue;
				}

				for (Entry<String, Map<String, String>> productState : clientValues.entrySet()) {
					Map<String, String> productValues = productState.getValue();

					persistenceController.getProductDataService().updateProductOnClient(changedClientState.getKey(),
							productState.getKey(), productType, productValues);
				}
			}

			// send the collected items
			persistenceController.getProductDataService().updateProductOnClients();
		}

		clearCollectChangedStates(productType);
	}

	private void clearCollectChangedStates(int productType) {
		if (OpsiPackage.TYPE_LOCALBOOT == productType && tableLocalbootProducts
				.getModel() instanceof InstallationStateTableModel installationStateTableModel) {
			installationStateTableModel.clearCollectChangedStates();
		}

		if (OpsiPackage.TYPE_NETBOOT == productType
				&& tableNetbootProducts.getModel() instanceof InstallationStateTableModel installationStateTableModel) {
			installationStateTableModel.clearCollectChangedStates();
		}
	}

	public void updateProduct(Map<String, Object> data) {
		String productId = (String) data.get("productId");
		String clientId = (String) data.get("clientId");
		String productType = (String) data.get("productType");

		Map<String, TreeSet<String>> clientProducts = productsToUpdate.containsKey(clientId)
				? productsToUpdate.get(clientId)
				: new HashMap<>();
		TreeSet<String> productIds = clientProducts.computeIfAbsent(productType, v -> new TreeSet<>());
		productIds.add(productId);
		clientProducts.put(productType, productIds);
		productsToUpdate.put(clientId, clientProducts);

		if (timer != null) {
			timer.cancel();
		}

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (configedMain.getSelectedClients().size() == 1
						&& clientId.equals(configedMain.getSelectedClients().get(0))) {
					configedMain.updateProductTableForClient(clientId, productType);
					productsToUpdate.clear();
				}
			}
		}, 200);
	}
}
