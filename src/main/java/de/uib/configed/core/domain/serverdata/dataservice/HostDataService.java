/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;

import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.type.ConfigName2ConfigValue;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.gui.type.HostInfo.ColumnDisplayInfo;
import de.uib.configed.gui.type.Object2GroupEntry;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.logging.TimeCheck;
import de.uib.configed.share.userprefs.UserPreferences;

/**
 * Provides methods for working with host data on the server.
 * <p>
 * Classes ending in {@code DataService} represent somewhat of a layer between
 * server and the client. It enables to work with specific data, that is saved
 * on the server.
 * <p>
 * {@code DataService} classes only allow to retrieve and update data. Data may
 * be internally cached. The internally cached data is identified by a method
 * name. If a method name ends in {@code PD}, it means that method either
 * retrieves or it updates internally cached data. {@code PD} stands for
 * {@code Persistent Data}.
 */
@SuppressWarnings({ "unchecked" })
public class HostDataService extends DataService {
	private static final String KEY_HOST_DISPLAYFIELDS = "configed.host_displayfields";

	private Map<String, Map<String, Object>> hostUpdates;

	public HostDataService(DataServices dataServices) {
		super(dataServices);
	}

	public boolean createClients(Iterable<Map<String, Object>> clients) {
		List<Map<String, Object>> clientsJsonObject = new ArrayList<>();
		List<Map<String, Object>> productsNetbootJsonObject = new ArrayList<>();
		List<Map<String, Object>> groupsJsonObject = new ArrayList<>();

		for (Map<String, Object> client : clients) {
			String hostname = ((String) client.get(HostInfo.HOSTNAME_KEY)).trim();
			String domain = ((String) client.get(HostInfo.CSV_DOMAIN_KEY)).trim();
			String depotId = ((String) client.get(HostInfo.DEPOT_OF_CLIENT_KEY)).trim();
			String macaddress = ((String) client.get(HostInfo.CLIENT_MAC_ADDRESS_KEY)).trim();
			String description = ((String) client.get(HostInfo.CLIENT_DESCRIPTION_KEY)).trim();
			String inventorynumber = ((String) client.get(HostInfo.CLIENT_INVENTORY_NUMBER_KEY)).trim();
			String notes = ((String) client.get(HostInfo.CLIENT_NOTES_KEY)).trim();
			String systemUUID = ((String) client.get(HostInfo.CLIENT_SYSTEM_UUID_KEY)).trim();
			String ipaddress = ((String) client.get(HostInfo.CLIENT_IP_ADDRESS_KEY)).trim();
			List<String> groups = (List<String>) client.get(HostInfo.CSV_GROUPS_KEY);

			boolean wanConfig = Boolean.parseBoolean((String) client.get(HostInfo.CLIENT_WAN_CONFIG_KEY));
			boolean shutdownInstall = Boolean.parseBoolean((String) client.get(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY));

			// A blank/empty string is an illegal opsi-host-key so we need to replace it with null
			String opsiHostKey = ((String) client.get(HostInfo.HOST_KEY_KEY)).isBlank() ? null
					: ((String) client.get(HostInfo.HOST_KEY_KEY)).trim();
			String netbootProduct = ((String) client.get(HostInfo.CSV_NETBOOT_PRODUCT_KEY)).trim();

			String newClientId = hostname + "." + domain;

			if (netbootProduct != null && !netbootProduct.isBlank()) {
				addNetbootProductToList(netbootProduct, newClientId, productsNetbootJsonObject);
			}

			Map<String, Object> hostItem = new HashMap<>();
			hostItem.put(HostInfo.HOSTNAME_KEY, newClientId);
			hostItem.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
			hostItem.put(HostInfo.CLIENT_NOTES_KEY, notes);
			hostItem.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUID);
			hostItem.put(HostInfo.CLIENT_MAC_ADDRESS_KEY, macaddress);
			hostItem.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipaddress);
			hostItem.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventorynumber);
			hostItem.put(HostInfo.HOST_KEY_KEY, opsiHostKey);
			hostItem.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, shutdownInstall);
			hostItem.put(HostInfo.CLIENT_WAN_CONFIG_KEY, wanConfig);

			clientsJsonObject.add(hostItem);

			List<String> valuesDepot = new ArrayList<>();
			ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
			if (depotId == null || depotId.isEmpty()) {
				depotId = dataServices.hostInfoCollections.getConfigServer();
			}
			valuesDepot.add(depotId);
			config.put(OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID, valuesDepot);
			dataServices.config.setConfigStates(newClientId, config);
			addGroupsToList(groups, newClientId, groupsJsonObject);

			HostInfo hostInfo = new HostInfo();
			hostInfo.setValues(hostItem);
			hostInfo.setType(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT);
			dataServices.hostInfoCollections.setLocalHostInfo(newClientId, hostInfo);

			prioritizeSelectedDomain(domain);
		}

		return doCallsForClientCreation(clientsJsonObject, groupsJsonObject, productsNetbootJsonObject);
	}

	private void prioritizeSelectedDomain(String selectedDomain) {
		List<String> domains = new ArrayList<>(dataServices.config.getDomains());
		domains.remove(selectedDomain);
		domains.add(0, selectedDomain);

		List<Object> serialized = IntStream.range(0, domains.size()).mapToObj(i -> (Object) (i + ":" + domains.get(i)))
				.toList();

		dataServices.config.writeDomains(serialized);
	}

	private void addNetbootProductToList(String netbootProduct, String newClientId,
			List<Map<String, Object>> productsNetbootJsonObject) {
		Logging.info(this, "createClient productNetboot ", netbootProduct);
		Map<String, Object> itemProducts = Utils.createNOMitem("ProductOnClient");
		itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, netbootProduct);
		itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
		itemProducts.put("clientId", newClientId);
		itemProducts.put(ProductState.KEY_ACTION_REQUEST, "setup");
		productsNetbootJsonObject.add(itemProducts);
	}

	private void addGroupsToList(List<String> groups, String newClientId, List<Map<String, Object>> groupsJsonObject) {
		Logging.info(this, "createClient", " group ", groups);
		for (String group : groups) {
			Map<String, Object> itemGroup = Utils.createNOMitem(Object2GroupEntry.TYPE_NAME);
			itemGroup.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			itemGroup.put(Object2GroupEntry.GROUP_ID_KEY, group);
			itemGroup.put(Object2GroupEntry.MEMBER_KEY, newClientId);
			groupsJsonObject.add(itemGroup);
		}
	}

	private boolean doCallsForClientCreation(List<Map<String, Object>> clientsJsonObject,
			List<Map<String, Object>> groupsJsonObject, List<Map<String, Object>> productsNetbootJsonObject) {
		boolean result = dataServices.exec.doCall(RPCMethodName.HOST_CREATE_CLIENTS, clientsJsonObject);

		if (result) {
			if (!groupsJsonObject.isEmpty()) {
				result = dataServices.exec.doCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, groupsJsonObject);
			}

			if (!productsNetbootJsonObject.isEmpty()) {
				result = result && dataServices.exec.doCall(RPCMethodName.PRODUCT_ON_CLIENT_CREATE_OBJECTS,
						productsNetbootJsonObject);
			}

			dataServices.config.updateConfigStates();
		}

		return result;
	}

	public boolean renameClient(String hostname, String newHostname) {
		if (dataServices.userRoles.isGlobalReadOnly()) {
			return false;
		}

		dataServices.persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());
		return dataServices.exec.doCall(RPCMethodName.HOST_RENAME_OPSI_CLIENT, hostname, newHostname);
	}

	public void deleteClients(Collection<String> hostIds) {
		if (dataServices.userRoles.isGlobalReadOnly() || hostIds.isEmpty()) {
			return;
		}

		dataServices.exec.doCall(RPCMethodName.HOST_DELETE, hostIds);

		dataServices.persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());
	}

	// executes all updates collected by setHostDescription ...
	public void updateHosts() {
		if (dataServices.userRoles.isGlobalReadOnly()) {
			return;
		}

		// checkHostPermission is done in updateHost

		if (hostUpdates == null) {
			return;
		}

		if (dataServices.exec.doCall(RPCMethodName.HOST_UPDATE_CLIENTS, hostUpdates.values())) {
			hostUpdates.clear();
		}
	}

	public void updateHost(String hostId, String property, Object value) {
		if (hostUpdates == null) {
			hostUpdates = new HashMap<>();
		}

		Map<String, Object> hostUpdateMap = hostUpdates.computeIfAbsent(hostId, (String v) -> {
			Map<String, Object> internalMap = new HashMap<>();
			internalMap.put("id", hostId);
			return internalMap;
		});
		hostUpdateMap.put(property, value);

		hostUpdates.put(hostId, hostUpdateMap);
	}

	public List<Map<String, Object>> getOpsiHosts() {
		Map<String, Object> callFilter = new HashMap<>();
		List<String> hostTypes = new ArrayList<>();
		hostTypes.add(HostInfo.HOST_TYPE_VALUE_OPSI_CONFIG_SERVER);
		hostTypes.add(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER);
		callFilter.put(HostInfo.HOST_TYPE_KEY, hostTypes);
		TimeCheck timer = new TimeCheck(this, "getOpsiHosts").start();
		Logging.notice(this, "host_getObjects");
		List<Map<String, Object>> opsiHosts = dataServices.exec.getListOfMaps(RPCMethodName.HOST_GET_OBJECTS,
				new String[0], callFilter);

		transformTimestampsToLocal(opsiHosts);
		timer.stop();
		return opsiHosts;
	}

	public List<Map<String, Object>> getOpsiClients() {
		TimeCheck timer = new TimeCheck(this, "getOpsiClients").start();
		Logging.notice(this, "host_getClients");
		List<Map<String, Object>> opsiClients = dataServices.exec.getListOfMaps(RPCMethodName.HOST_GET_CLIENTS);
		transformTimestampsToLocal(opsiClients);
		timer.stop();
		return opsiClients;
	}

	private static void transformTimestampsToLocal(List<Map<String, Object>> hostList) {
		for (Map<String, Object> host : hostList) {
			Utils.formatDateTimeStringForMap(host, HostInfo.LAST_SEEN_KEY);
			Utils.formatDateTimeStringForMap(host, HostInfo.CREATED_KEY);
		}
	}

	public List<String> getClientsWithOtherProductVersion(String productId, String productVersion,
			String packageVersion, boolean includeFailedInstallations) {
		List<String> result = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put(OpsiPackage.DB_KEY_PRODUCT_ID, productId);
		callFilter.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
		List<Map<String, Object>> retrievedList = dataServices.exec
				.getListOfMaps(RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS, new String[0], callFilter);
		for (Map<String, Object> m : retrievedList) {
			String client = (String) m.get("clientId");
			String clientProductVersion = (String) m.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
			String clientPackageVersion = (String) m.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
			Object clientProductState = m.get(ProductState.KEY_INSTALLATION_STATUS);
			boolean hasWrongProductVersion = (!POJOReMapper.equalsNull(clientProductVersion)
					&& !productVersion.equals(clientProductVersion))
					|| (!POJOReMapper.equalsNull(clientPackageVersion) && !packageVersion.equals(clientPackageVersion));
			if ((includeFailedInstallations
					&& InstallationStatus.getLabel(InstallationStatus.UNKNOWN).equals(clientProductState))
					|| (InstallationStatus.getLabel(InstallationStatus.INSTALLED).equals(clientProductState)
							&& hasWrongProductVersion)) {
				Logging.debug("getClientsWithOtherProductVersion hit ", m);
				result.add(client);
			}
		}
		Logging.info(this, "getClientsWithOtherProductVersion globally ", result.size());
		return result;
	}

	public void retrieveSessionInfo(List<String> clientIds) {
		Map<String, String> sessionInfo = new HashMap<>();

		Object[] callParameters = clientIds != null && !clientIds.isEmpty() ? new Object[] { clientIds }
				: new Object[] {};

		Map<String, Map<String, Object>> sessionInfos = dataServices.exec
				.getMapOfMaps(RPCMethodName.HOST_CONTROL_GET_ACTIVE_SESSIONS, callParameters);
		for (Entry<String, Map<String, Object>> resultEntry : sessionInfos.entrySet()) {
			String value;

			Object error = resultEntry.getValue().get("error");
			if (error != null) {
				value = Configed.getResourceValue("sessionInfo.noResponse") + ": " + error;
			} else {
				value = createSessionInfoForList(POJOReMapper.remap(resultEntry.getValue().get("result")));
			}

			sessionInfo.put(resultEntry.getKey(), value);
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.SESSION_INFO, sessionInfo);
	}

	/**
	 * Get a map of clients and their session information. If no session
	 * information is available, an empty map is returned, because we only want
	 * to load the data when requested by user
	 * 
	 * @return
	 */
	public Map<String, String> getSessionInfo() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.SESSION_INFO)) {
			return dataServices.cacheManager.getCachedData(CacheIdentifier.SESSION_INFO, Map.class);
		} else {
			return Collections.emptyMap();
		}
	}

	private static String createSessionInfoForList(List<Map<String, Object>> sessionList) {
		StringBuilder value = new StringBuilder();
		for (Map<String, Object> session : sessionList) {
			String username = "" + session.get("UserName");
			String logondomain = "" + session.get("LogonDomain");

			if (!value.toString().isEmpty()) {
				value.append("; ");
			}

			value.append(username + " (" + logondomain + "\\" + username + ")");
		}

		return value.toString();
	}

	/**
	 * Retrieve a set of clients, that are connected to messagebus.
	 * <p>
	 * This method is only a viable option for servers/depots with opsi 4.3
	 * version or higher, since messagebus technology in opsi was introduce with
	 * opsi 4.3.
	 * 
	 * @return a set of clients, that are connected to messagebus
	 */
	public Set<String> getMessagebusConnectedClients() {
		Logging.info(this, "get clients connected with messagebus");
		return new HashSet<>(dataServices.exec.getStringListResult(RPCMethodName.HOST_GET_MESSAGEBUS_CONNECTED_IDS));
	}

	public void setHostValues(Map<String, Object> settings) {
		if (dataServices.userRoles.isGlobalReadOnly()) {
			return;
		}

		dataServices.exec.doCall(RPCMethodName.HOST_UPDATE_OBJECTS, settings);
	}

	public Map<String, Boolean> getHostDisplayFields() {
		retrieveHostDisplayFields();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.HOST_DISPLAY_FIELDS, Map.class);
	}

	public void retrieveHostDisplayFields() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.HOST_DISPLAY_FIELDS)) {
			return;
		}
		Map<String, List<Object>> serverPropertyMap = dataServices.config.getConfigDefaultValuesPD();
		List<String> configuredByService = POJOReMapper.remap(serverPropertyMap.get(KEY_HOST_DISPLAYFIELDS));
		// check if have to initialize the server property
		configuredByService = produceHostDisplayFields(configuredByService);

		Map<String, Boolean> hostDisplayFields = new LinkedHashMap<>();
		// can be overridden by user
		hostDisplayFields.put(HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL, true);
		hostDisplayFields.put(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, true);
		// always shown, we put it here because of ordering and repeat the statement
		// after the loop if it has been set to false

		List<String> userSavedDisplayFields = Arrays
				.asList(UserPreferences.get(UserPreferences.CLIENTS_TABLE_DISPLAY_FIELDS).split(","));
		for (ColumnDisplayInfo info : HostInfo.ORDERED_DISPLAY_COLUMN_INFOS) {
			hostDisplayFields.put(info.label,
					configuredByService.indexOf(info.label) > -1 || userSavedDisplayFields.contains(info.label));
		}

		hostDisplayFields.put(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, true);
		dataServices.cacheManager.setCachedData(CacheIdentifier.HOST_DISPLAY_FIELDS, hostDisplayFields);
	}

	private List<String> produceHostDisplayFields(List<String> givenList) {
		List<String> result = null;
		Map<String, ConfigOption> configOptions = dataServices.config.getConfigOptionsPD();
		Logging.info(this, "produceHost_displayFields configOptions.get(key) ",
				configOptions.get(KEY_HOST_DISPLAYFIELDS));

		if (givenList == null || givenList.isEmpty()) {
			Logging.info(this, "givenList is null or empty: ", givenList);

			List<String> possibleValues = new ArrayList<>();
			possibleValues.add(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.LAST_SEEN_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CREATED_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_OS_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_OS_ARCHITECTURE_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_DEVICE_VENDOR_DISPLAY_FIELD_LABEL);
			possibleValues.add(HostInfo.CLIENT_DEVICE_MODEL_DISPLAY_FIELD_LABEL);

			result = new ArrayList<>();
			result.add(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL);
			result.add(HostInfo.CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL);
			result.add(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);
			result.add(HostInfo.LAST_SEEN_DISPLAY_FIELD_LABEL);
			result.add(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);

			// create config for service
			Map<String, Object> item = Utils.createNOMitem("UnicodeConfig");
			item.put("ident", KEY_HOST_DISPLAYFIELDS);
			item.put("description", "");
			item.put("defaultValues", result);
			item.put("possibleValues", possibleValues);
			item.put("editable", false);
			item.put("multiValue", true);

			dataServices.exec.doCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, item);
		} else {
			result = givenList;
		}

		return result;
	}
}
