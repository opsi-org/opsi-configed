/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.OpsiPackage;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

/**
 * This class is responsible for copying the client. By creating a new client
 * with provided name. Additionally, it copies client's groups, products,
 * product's properties and config states.
 */
public class CopyClient {
	private static OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private HostInfo clientToCopy;
	private String newClientName;
	private String newClientNameWithDomain;
	private String newDescription;
	private String newInventoryNumber;
	private String newNotes;
	private String newIpAddress;
	private String newSystemUUID;
	private String newMacAddress;

	/**
	 * Creates {@link CopyClient} object with provided information.
	 *
	 * @param clientToCopy       client to copy
	 * @param newClientName      client name for the client's copy
	 * @param newDescription     client description
	 * @param newInventoryNumber client inventory number
	 * @param newNotes           client notes
	 * @param newIpAddress       client IP address
	 * @param newSystemUUID      client system UUID
	 * @param newMacAddress      client MAC address
	 */
	public CopyClient(HostInfo clientToCopy, String newClientName, String newDescription, String newInventoryNumber,
			String newNotes, String newIpAddress, String newSystemUUID, String newMacAddress) {
		this.clientToCopy = clientToCopy;
		this.newClientName = newClientName;
		this.newClientNameWithDomain = newClientName + "." + Utils.getDomainFromClientName(clientToCopy.getName());
		this.newDescription = newDescription;
		this.newInventoryNumber = newInventoryNumber;
		this.newNotes = newNotes;
		this.newIpAddress = newIpAddress;
		this.newSystemUUID = newSystemUUID;
		this.newMacAddress = newMacAddress;
	}

	/**
	 * Creates {@link CopyClient} object with provided information.
	 *
	 * @param clientToCopy  client to copy
	 * @param newClientName client name for the client's copy
	 */
	public CopyClient(HostInfo clientToCopy, String newClientName) {
		this(clientToCopy, newClientName, "", "", "", "", "", "");
	}

	/**
	 * Copies provided client, by creating it and copying client's groups,
	 * products, product's properties and config states.
	 */
	public void copy() {
		Logging.debug("Copy client: " + clientToCopy + " -> " + newClientNameWithDomain);
		copyClient();
		copyGroups();
		copyProducts();
		copyProductProperties();
		copyConfigStates();
	}

	private void copyClient() {
		persistenceController.getHostDataService().createClient(newClientName,
				Utils.getDomainFromClientName(clientToCopy.getName()), clientToCopy.getInDepot(), newDescription,
				newInventoryNumber, newNotes, newIpAddress, newSystemUUID, newMacAddress,
				clientToCopy.getShutdownInstall(), clientToCopy.getUefiBoot(), clientToCopy.getWanConfig(), "", "");
	}

	private void copyGroups() {
		Map<String, Set<String>> fGroup2Members = persistenceController.getGroupDataService().getFHostGroup2MembersPD();
		List<String> clientGroups = fGroup2Members.keySet().stream()
				.filter(group -> fGroup2Members.get(group).contains(clientToCopy.getName()))
				.collect(Collectors.toList());

		if (clientGroups.isEmpty()) {
			return;
		}

		persistenceController.getGroupDataService().addHost2Groups(newClientNameWithDomain, clientGroups);
		persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());
	}

	private void copyProducts() {
		Map<String, List<Map<String, String>>> mapOfProductStatesAndActions = persistenceController
				.getProductDataService().getMapOfProductStatesAndActions(Collections.singleton(clientToCopy.getName()));

		if (mapOfProductStatesAndActions.isEmpty()) {
			return;
		}

		for (List<Map<String, String>> productStatesAndActions : mapOfProductStatesAndActions.values()) {
			if (productStatesAndActions.isEmpty()) {
				continue;
			}

			productStatesAndActions.forEach((Map<String, String> productInfo) -> {
				productInfo.values().removeIf(String::isEmpty);
				persistenceController.getProductDataService().updateProductOnClient(newClientNameWithDomain,
						productInfo.get("productId"), getProductType(productInfo.get("productId")), productInfo);
			});
		}

		// Trigger product update.
		persistenceController.getProductDataService().updateProductOnClients();
	}

	private static int getProductType(String productId) {
		if (persistenceController.getProductDataService().getAllLocalbootProductNames().contains(productId)) {
			return OpsiPackage.TYPE_LOCALBOOT;
		} else {
			return OpsiPackage.TYPE_NETBOOT;
		}
	}

	private void copyProductProperties() {
		Map<String, ConfigName2ConfigValue> products = persistenceController.getProductDataService()
				.getProductPropertiesPD(clientToCopy.getName());

		if (products.isEmpty()) {
			return;
		}

		for (Entry<String, ConfigName2ConfigValue> entry : products.entrySet()) {
			persistenceController.getProductDataService().setProductProperties(newClientNameWithDomain, entry.getKey(),
					entry.getValue());
		}

		// Trigger the product's properties update.
		persistenceController.getProductDataService().setProductProperties();
	}

	private void copyConfigStates() {
		Map<String, Object> clientConfigStates = persistenceController.getConfigDataService()
				.getHostConfig(clientToCopy.getName());
		if (clientConfigStates != null) {
			persistenceController.getConfigDataService().setAdditionalConfiguration(newClientNameWithDomain,
					(ConfigName2ConfigValue) clientConfigStates);
			// Trigger the config state update.
			persistenceController.getConfigDataService().setAdditionalConfiguration();
		}
	}
}
