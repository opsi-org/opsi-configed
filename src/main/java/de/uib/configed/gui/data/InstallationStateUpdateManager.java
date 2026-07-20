/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.productpage.ProductTable;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.logging.Logging;

public class InstallationStateUpdateManager {
	private Map<String, Map<String, TreeSet<String>>> productsToUpdate = new HashMap<>();
	private Timer timer;

	private ProductTable tableLocalbootProducts;
	private ProductTable tableNetbootProducts;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public InstallationStateUpdateManager(ConfigedMain configedMain, ProductTable tableLocalbootProducts,
			ProductTable tableNetbootProducts) {
		this.configedMain = configedMain;
		this.tableLocalbootProducts = tableLocalbootProducts;
		this.tableNetbootProducts = tableNetbootProducts;
	}

	public void updateProductTableForClient(String clientId, List<String> attributes) {
		if (isProductsUpdatedForClient(clientId, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING)) {
			updateTableForClient(clientId, attributes, tableLocalbootProducts);
		} else if (isProductsUpdatedForClient(clientId, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING)) {
			updateTableForClient(clientId, attributes, tableNetbootProducts);
		} else {
			Logging.notice(this,
					"Cannot update table because the Product table with the product to update is not open");
		}
	}

	private void updateTableForClient(String clientId, List<String> attributes, ProductTable tableProducts) {
		if (productsToUpdate.get(clientId).get(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING) != null
				&& productsToUpdate.get(clientId).get(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING).size() < 20) {
			applyMultipleCellEdits(clientId,
					productsToUpdate.get(clientId).get(OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING), attributes,
					tableProducts.getTableViewComponent());
		} else {
			tableProducts.updateTable(clientId, attributes);
		}
	}

	private void applyMultipleCellEdits(String clientId, TreeSet<String> productIds, List<String> attributes,
			GenericTableViewComponent tableComponent) {

		if (productIds.isEmpty() || attributes.isEmpty()) {
			return;
		}

		List<Map<String, String>> productInfos = persistenceController.getDataServices().product
				.getProductInfos(productIds, clientId, attributes);

		if (productInfos.isEmpty()) {
			return;
		}

		Map<String, Map<String, Object>> productValueMap = new HashMap<>();
		Set<String> fetchedProductIds = new HashSet<>(productIds);

		for (Map<String, String> info : productInfos) {
			String productId = info.get("productId");
			if (fetchedProductIds.contains(productId)) {
				productValueMap.put(productId, POJOReMapper.remap(info));
			}
		}

		Map<String, Integer> productIdToRowIndex = new HashMap<>();
		for (int rowIdx = 0; rowIdx < tableComponent.getRowCount(); rowIdx++) {
			RowData rowData = tableComponent.getRowByModelIndex(rowIdx);
			String productId = rowData.getValue(ProductState.KEY_PRODUCT_ID, String.class);
			if (productValueMap.containsKey(productId)) {
				productIdToRowIndex.put(productId, rowIdx);
			}
		}

		List<GenericTableViewMsg.CellEdited> edits = computeCellEdits(productIdToRowIndex, attributes, productValueMap,
				tableComponent);

		if (!edits.isEmpty()) {
			tableComponent.dispatch(new GenericTableViewMsg.MultipleCellsEdited(edits));
		}
	}

	private static List<GenericTableViewMsg.CellEdited> computeCellEdits(Map<String, Integer> productIdToRowIndex,
			List<String> attributes, Map<String, Map<String, Object>> productValueMap,
			GenericTableViewComponent tableComponent) {
		List<GenericTableViewMsg.CellEdited> edits = new ArrayList<>();
		for (Map.Entry<String, Map<String, Object>> entry : productValueMap.entrySet()) {
			String productId = entry.getKey();
			Map<String, Object> newValues = entry.getValue();
			Integer rowIdxOpt = productIdToRowIndex.get(productId);

			if (rowIdxOpt == null) {
				continue;
			}

			int rowIdx = rowIdxOpt;
			RowData rowData = tableComponent.getRowByModelIndex(rowIdx);

			for (String attr : attributes) {
				Object currentValue = rowData.getValue(attr, Object.class);
				Object newValue = newValues.get(attr);

				int colIdx = tableComponent.getColumnIndexByKey(attr);
				if (!Objects.equals(currentValue, newValue) && colIdx >= 0) {
					edits.add(new GenericTableViewMsg.CellEdited(rowIdx, colIdx, newValue));
				}
			}
		}

		return edits;
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

				Logging.debug(this, "updateProductStates, collectChangedProductStates , client ",
						changedClientState.getKey(), " values ", clientValues);

				if (clientValues.keySet() == null || clientValues.isEmpty()) {
					continue;
				}

				for (Entry<String, Map<String, String>> productState : clientValues.entrySet()) {
					Map<String, String> productValues = productState.getValue();

					persistenceController.getDataServices().product.updateProductOnClient(changedClientState.getKey(),
							productState.getKey(), productType, productValues);
				}
			}

			// send the collected items
			persistenceController.getDataServices().product.updateProductOnClients();
		}

		clearCollectChangedStates(productType);
	}

	private void clearCollectChangedStates(int productType) {
		if (OpsiPackage.TYPE_LOCALBOOT == productType) {
			tableLocalbootProducts.clearProductChangedStates();
		}

		if (OpsiPackage.TYPE_NETBOOT == productType) {
			tableNetbootProducts.clearProductChangedStates();
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
					ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().getProductPageManager()
							.updateProductTableForClient(clientId, productType);
					productsToUpdate.clear();
				}
			}
		}, 200);
	}
}
