/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.OpsiPackage;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

/**
 * This class is responsible for copying the client. By creating a new client
 * with provided name. Additionally, it copies client's products, product's
 * properties and config states.
 */
public class CopyClient {
	private static OpsiserviceNOMPersistenceController persist = PersistenceControllerFactory
			.getPersistenceController();

	private HostInfo clientToCopy;
	private String newClientName;
	private String newClientNameWithDomain;

	/**
	 * Creates CopyClient object with provided information.
	 * 
	 * @param clientToCopy  client to copy
	 * @param newClientName client name for the client's copy
	 */
	public CopyClient(HostInfo clientToCopy, String newClientName) {
		this.clientToCopy = clientToCopy;
		this.newClientName = newClientName;
		this.newClientNameWithDomain = newClientName + "." + getDomainFromClientName();
	}

	/**
	 * Copies provided client, by creating it and copying client's products,
	 * product's properties and config states.
	 */
	public void copy() {
		copyClient();
		copyGroups();
		copyProducts();
		copyProductProperties();
		copyConfigStates();
	}

	private void copyClient() {
		persist.createClient(newClientName, getDomainFromClientName(), clientToCopy.getInDepot(),
				clientToCopy.getDescription(), clientToCopy.getInventoryNumber(), clientToCopy.getNotes(),
				clientToCopy.getIpAddress(), clientToCopy.getSystemUUID(), clientToCopy.getMacAddress(),
				clientToCopy.getShutdownInstall(), clientToCopy.getUefiBoot(), clientToCopy.getWanConfig(), "", "", "");
		persist.getHostInfoCollections().addOpsiHostName(newClientNameWithDomain);
	}

	private void copyGroups() {
		Map<String, Set<String>> fGroup2Members = persist.getFGroup2Members();
		List<String> clientGroups = fGroup2Members.keySet().stream()
				.filter(group -> fGroup2Members.get(group).contains(clientToCopy.getName()))
				.collect(Collectors.toList());

		if (clientGroups.isEmpty()) {
			return;
		}

		clientGroups.forEach(clientGroup -> persist.addObject2Group(newClientNameWithDomain, clientGroup));
		persist.fObject2GroupsRequestRefresh();
	}

	private void copyProducts() {
		Map<String, List<Map<String, String>>> mapOfProductStatesAndActions = persist
				.getMapOfProductStatesAndActions(new String[] { clientToCopy.getName() });

		if (mapOfProductStatesAndActions.isEmpty()) {
			return;
		}

		for (List<Map<String, String>> productStatesAndActions : mapOfProductStatesAndActions.values()) {
			if (productStatesAndActions.isEmpty()) {
				continue;
			}

			productStatesAndActions.forEach((Map<String, String> productInfo) -> {
				productInfo.values().removeIf(String::isEmpty);
				persist.updateProductOnClient(newClientNameWithDomain, productInfo.get("productId"),
						getProductType(productInfo.get("productId")), productInfo);
			});
		}

		// Trigger product update.
		persist.updateProductOnClients();
	}

	private static int getProductType(String productId) {

		if (persist.getAllLocalbootProductNames().contains(productId)) {
			return OpsiPackage.TYPE_LOCALBOOT;
		} else {
			return OpsiPackage.TYPE_NETBOOT;
		}
	}

	private void copyProductProperties() {
		Map<String, List<Map<String, String>>> mapOfProductStatesAndActions = persist
				.getMapOfProductStatesAndActions(new String[] { clientToCopy.getName() });

		if (mapOfProductStatesAndActions.isEmpty()) {
			return;
		}

		for (List<Map<String, String>> productStatesAndActions : mapOfProductStatesAndActions.values()) {
			if (productStatesAndActions.isEmpty()) {
				continue;
			}

			productStatesAndActions.forEach((Map<String, String> productInfo) -> {
				Map<String, Object> clientProductProperties = persist.getProductProperties(newClientNameWithDomain,
						productInfo.get("productId"));
				clientProductProperties.clear();
				clientProductProperties
						.putAll(persist.getProductProperties(clientToCopy.getName(), productInfo.get("productId")));
				persist.setProductProperties(newClientNameWithDomain, productInfo.get("productId"),
						clientProductProperties);
			});
		}

		// Trigger the product's properties update.
		persist.setProductProperties();
	}

	private void copyConfigStates() {
		Map<String, Object> clientConfigStates = persist.getConfig(newClientNameWithDomain);

		clientConfigStates.clear();
		clientConfigStates.putAll(persist.getConfig(clientToCopy.getName()));

		persist.setAdditionalConfiguration(newClientNameWithDomain, (ConfigName2ConfigValue) clientConfigStates);

		// Trigger the config state update.
		persist.setAdditionalConfiguration();
	}

	@SuppressWarnings("java:S109")
	private String getDomainFromClientName() {
		String[] splittedClientName = clientToCopy.getName().split("\\.");
		return splittedClientName[1] + "." + splittedClientName[2];
	}
}
