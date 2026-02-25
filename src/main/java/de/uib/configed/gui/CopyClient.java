/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.dataservice.ConfigDataService;
import de.uib.configed.gui.features.tree.ClientTree;
import de.uib.configed.gui.type.ConfigName2ConfigValue;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

/**
 * This class is responsible for copying the client. By creating a new client
 * with provided name. Additionally, it copies client's groups, products,
 * product's properties and config states.
 */
public class CopyClient {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private HostInfo clientToCopy;
	private String newClientName;
	private String newClientNameWithDomain;

	public enum CopyOption {
		GROUPS, PRODUCTS, PRODUCT_PROPERTIES, CONFIG_STATES
	}

	/**
	 * Creates {@link CopyClient} object with provided information.
	 *
	 * @param clientToCopy  client to copy
	 * @param newClientName client name for the client's copy
	 */
	public CopyClient(HostInfo clientToCopy, String newClientName) {
		this.clientToCopy = clientToCopy;
		this.newClientName = newClientName;
		this.newClientNameWithDomain = newClientName + "."
				+ Utils.getDomainFromClientName(clientToCopy.getString(HostInfo.HOSTNAME_KEY));
	}

	/**
	 * Copies provided client, by creating it and copying client's groups,
	 * products, product's properties and config states.
	 */
	public void copy(Collection<CopyOption> options) {
		Logging.debug("Copy client: ", clientToCopy, " -> ", newClientNameWithDomain);
		copyClient();
		if (options.contains(CopyOption.GROUPS)) {
			copyGroups();
		}
		if (options.contains(CopyOption.PRODUCTS)) {
			copyProducts();
		}
		if (options.contains(CopyOption.PRODUCT_PROPERTIES)) {
			copyProductProperties();
		}
		if (options.contains(CopyOption.CONFIG_STATES)) {
			copyConfigStates();
		}
	}

	private void copyClient() {
		List<Object> client = List.of(newClientName,
				Utils.getDomainFromClientName(clientToCopy.getString(HostInfo.HOSTNAME_KEY)),
				clientToCopy.getString(HostInfo.DEPOT_OF_CLIENT_KEY), "", "", "", "", "", "", "",
				Boolean.toString(clientToCopy.getWanConfig()), Boolean.toString(clientToCopy.getShutdownInstall()), "",
				"");
		persistenceController.getDataServices().host.createClients(List.of(client));
	}

	private void copyGroups() {
		Map<String, Set<String>> fGroup2Members = persistenceController.getDataServices().group
				.getFHostGroup2MembersPD();
		List<String> clientGroups = fGroup2Members.keySet().stream()
				.filter(group -> fGroup2Members.get(group).contains(clientToCopy.getString(HostInfo.HOSTNAME_KEY)))

				// Exclude "Not assigned" group, as it is not a real group and should not be assigned to the new client.
				.filter(group -> !group.equals(ClientTree.DIRECTORY_NOT_ASSIGNED_NAME)).toList();

		if (clientGroups.isEmpty()) {
			return;
		}

		persistenceController.getDataServices().group.addHost2Groups(newClientNameWithDomain, clientGroups);
		persistenceController.reloadData(CacheIdentifier.FHOST_GROUP_TO_MEMBERS.toString());
	}

	private void copyProducts() {
		Map<String, List<Map<String, String>>> mapOfProductStatesAndActions = persistenceController
				.getDataServices().product
						.getMapOfProductStatesAndActions(Set.of(clientToCopy.getString(HostInfo.HOSTNAME_KEY)));

		if (mapOfProductStatesAndActions.isEmpty()) {
			return;
		}

		for (List<Map<String, String>> productStatesAndActions : mapOfProductStatesAndActions.values()) {
			if (productStatesAndActions.isEmpty()) {
				continue;
			}

			productStatesAndActions.forEach((Map<String, String> productInfo) -> {
				productInfo.values().removeIf(String::isEmpty);
				productInfo.put("clientId", newClientNameWithDomain);
				String oldIdent = productInfo.get("ident");
				String newIdent = oldIdent.replaceFirst(clientToCopy.getString(HostInfo.HOSTNAME_KEY),
						newClientNameWithDomain);
				productInfo.put("ident", newIdent);
				persistenceController.getDataServices().product.updateProductOnClient(newClientNameWithDomain,
						productInfo.get("productId"), getProductType(productInfo.get("productId")), productInfo);
			});
		}

		// Trigger product update.
		persistenceController.getDataServices().product.updateProductOnClients();
	}

	private int getProductType(String productId) {
		return persistenceController.getDataServices().product.getAllLocalbootProductNames().contains(productId)
				? OpsiPackage.TYPE_LOCALBOOT
				: OpsiPackage.TYPE_NETBOOT;
	}

	private void copyProductProperties() {
		Map<String, ConfigName2ConfigValue> products = persistenceController.getDataServices().product
				.getProductPropertiesPD(clientToCopy.getString(HostInfo.HOSTNAME_KEY));

		if (products.isEmpty()) {
			return;
		}

		for (Entry<String, ConfigName2ConfigValue> entry : products.entrySet()) {
			persistenceController.getDataServices().product.setProductProperties(newClientNameWithDomain,
					entry.getKey(), entry.getValue());
		}

		// Trigger the product's properties update.
		persistenceController.getDataServices().product.setProductProperties();
	}

	private void copyConfigStates() {
		ConfigDataService configDataService = persistenceController.getDataServices().config;

		Map<String, Object> hostConfig = configDataService.getHostConfigsPD()
				.get(clientToCopy.getString(HostInfo.HOSTNAME_KEY));
		if (hostConfig == null) {
			return;
		}
		ConfigName2ConfigValue clientConfigStates = new ConfigName2ConfigValue(hostConfig, null);

		configDataService.setConfigStates(newClientNameWithDomain, clientConfigStates);
		// Trigger the config state update.
		configDataService.updateConfigStates();
	}
}
