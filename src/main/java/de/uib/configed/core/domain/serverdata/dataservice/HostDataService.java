/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uib.configed.core.domain.HostInfoCollections;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.infrastructure.AbstractPOJOExecutioner;
import de.uib.configed.core.infrastructure.OpsiMethodCall;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.HostInfo;
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
public class HostDataService {
	private static final String KEY_HOST_DISPLAYFIELDS = "configed.host_displayfields";

	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private ConfigDataService configDataService;
	private UserRolesConfigDataService userRolesConfigDataService;
	private HostInfoCollections hostInfoCollections;

	private Map<String, Map<String, Object>> hostUpdates;

	public HostDataService(AbstractPOJOExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public boolean createClients(Iterable<List<Object>> clients) {
		List<Map<String, Object>> clientsJsonObject = new ArrayList<>();
		List<Map<String, Object>> productsNetbootJsonObject = new ArrayList<>();
		List<Map<String, Object>> groupsJsonObject = new ArrayList<>();

		for (List<Object> client : clients) {
			String hostname = ((String) client.get(0)).trim();
			String domainname = ((String) client.get(1)).trim();
			String depotId = ((String) client.get(2)).trim();
			String description = ((String) client.get(3)).trim();
			String inventorynumber = ((String) client.get(4)).trim();
			String notes = ((String) client.get(5)).trim();
			String systemUUID = ((String) client.get(6)).trim();
			String macaddress = ((String) client.get(7)).trim();
			String ipaddress = ((String) client.get(8)).trim();

			boolean wanConfig = Boolean.parseBoolean((String) client.get(10));
			boolean shutdownInstall = Boolean.parseBoolean((String) client.get(11));

			// A blank/empty string is an illegal opsi-host-key so we need to replace it with null
			String opsiHostKey = ((String) client.get(12)).isBlank() ? null : ((String) client.get(12)).trim();

			String newClientId = hostname + "." + domainname;

			Map<String, Object> hostItem = Utils.createNOMitem(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT);
			hostItem.put(HostInfo.HOSTNAME_KEY, newClientId);
			hostItem.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
			hostItem.put(HostInfo.CLIENT_NOTES_KEY, notes);
			hostItem.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUID);
			hostItem.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macaddress);
			hostItem.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipaddress);
			hostItem.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventorynumber);
			hostItem.put(HostInfo.HOST_KEY_KEY, opsiHostKey);

			clientsJsonObject.add(hostItem);

			Map<String, Object> itemDepot = Utils.createNOMitem(OpsiServiceNOMPersistenceController.CONFIG_STATE_TYPE);
			List<String> valuesDepot = new ArrayList<>();
			valuesDepot.add(depotId);
			itemDepot.put(OpsiServiceNOMPersistenceController.OBJECT_ID, newClientId);
			itemDepot.put(OpsiServiceNOMPersistenceController.VALUES_ID, valuesDepot);
			itemDepot.put(OpsiServiceNOMPersistenceController.CONFIG_ID,
					OpsiServiceNOMPersistenceController.CONFIG_DEPOT_ID);

			addGroupsToList(((String) client.get(9)), newClientId, groupsJsonObject);

			HostInfo hostInfo = new HostInfo();
			hostInfo.setValues(itemDepot);
			if (depotId == null || depotId.isEmpty()) {
				depotId = hostInfoCollections.getConfigServer();
			}
			hostInfo.setInDepot(depotId);
			hostInfo.setWanConfig(wanConfig);
			hostInfo.setShutdownInstall(shutdownInstall);

			hostInfoCollections.setLocalHostInfo(newClientId, depotId, hostInfo);
		}

		return doCallsForClientCreation(clientsJsonObject, groupsJsonObject, productsNetbootJsonObject);
	}

	private void addGroupsToList(String groupsAsString, String newClientId,
			List<Map<String, Object>> groupsJsonObject) {
		String[] groups;
		if (!groupsAsString.isEmpty()) {
			groups = groupsAsString.replace("\\s,\\s", ",").trim().split(",");
		} else {
			groups = new String[] {};
		}

		Logging.info(this, "createClient", " group ", Arrays.toString(groups));
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
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_UPDATE_CLIENTS, new Object[] { clientsJsonObject });
		boolean result = exec.doCall(omc);

		if (result) {
			if (!groupsJsonObject.isEmpty()) {
				omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS,
						new Object[] { groupsJsonObject });
				result = exec.doCall(omc);
			}

			if (!productsNetbootJsonObject.isEmpty()) {
				omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_CREATE_OBJECTS,
						new Object[] { productsNetbootJsonObject });
				result = result && exec.doCall(omc);
			}
		}

		return result;
	}

	public boolean createClient(String hostname, String domainname, String depotId, String description,
			String inventorynumber, String notes, String ipaddress, String systemUUID, String macaddress,
			boolean shutdownInstall, boolean wanConfig, String[] groups, String productNetboot) {
		if (!userRolesConfigDataService.hasDepotPermission(depotId)) {
			return false;
		}

		boolean result = false;

		String newClientId = hostname + "." + domainname;

		Map<String, Object> hostItem = Utils.createNOMitem(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT);
		hostItem.put(HostInfo.HOSTNAME_KEY, newClientId);
		hostItem.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
		hostItem.put(HostInfo.CLIENT_NOTES_KEY, notes);
		hostItem.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUID);
		hostItem.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macaddress);
		hostItem.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipaddress);
		hostItem.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventorynumber);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CREATE_OBJECTS, new Object[] { hostItem });
		result = exec.doCall(omc);

		if (result) {
			result = updateGroupsForClient(groups, newClientId);
		}

		if (result && productNetboot != null && !productNetboot.isEmpty()) {
			Logging.info(this, "createClient productNetboot ", productNetboot);
			List<Map<String, Object>> jsonObjects = new ArrayList<>();
			Map<String, Object> itemProducts = Utils.createNOMitem("ProductOnClient");
			itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, productNetboot);
			itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
			itemProducts.put("clientId", newClientId);
			itemProducts.put(ProductState.KEY_ACTION_REQUEST, "setup");
			jsonObjects.add(itemProducts);
			omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_CREATE_OBJECTS, new Object[] { jsonObjects });
			result = exec.doCall(omc);
		}

		if (result) {
			if (depotId == null || depotId.isEmpty()) {
				depotId = hostInfoCollections.getConfigServer();
			}
			HostInfo hostInfo = new HostInfo();
			hostInfo.setValues(hostItem);
			hostInfo.setInDepot(depotId);
			hostInfo.setWanConfig(wanConfig);
			hostInfo.setShutdownInstall(shutdownInstall);
			hostInfoCollections.setLocalHostInfo(newClientId, depotId, hostInfo);

			Logging.info(this, " createClient hostInfo ", hostInfo);
		}

		return result;
	}

	private boolean updateGroupsForClient(String[] groups, String newClientId) {
		if (groups == null || groups.length == 0) {
			return true;
		}

		Logging.info(this, "createClient group ", Arrays.toString(groups));
		List<Map<String, Object>> jsonObjects = new ArrayList<>();
		for (String group : groups) {
			Map<String, Object> itemGroup = Utils.createNOMitem(Object2GroupEntry.TYPE_NAME);
			itemGroup.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			itemGroup.put(Object2GroupEntry.GROUP_ID_KEY, group);
			itemGroup.put(Object2GroupEntry.MEMBER_KEY, newClientId);
			jsonObjects.add(itemGroup);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS,
				new Object[] { jsonObjects });
		return exec.doCall(omc);
	}

	public boolean renameClient(String hostname, String newHostname) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_RENAME_OPSI_CLIENT,
				new String[] { hostname, newHostname });
		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());
		return exec.doCall(omc);
	}

	public void deleteClients(Collection<String> hostIds) {
		if (userRolesConfigDataService.isGlobalReadOnly() || hostIds.isEmpty()) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_DELETE, new Object[] { hostIds });
		exec.doCall(omc);

		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());
	}

	// executes all updates collected by setHostDescription ...
	public void updateHosts() {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return;
		}

		// checkHostPermission is done in updateHost

		if (hostUpdates == null) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_UPDATE_CLIENTS,
				new Object[] { hostUpdates.values() });

		if (exec.doCall(omc)) {
			hostUpdates.clear();
		}
	}

	private void updateHost(String hostId, String property, Object value) {
		if (hostUpdates == null) {
			hostUpdates = new HashMap<>();
		}

		Map<String, Object> hostUpdateMap = hostUpdates.get(hostId);

		if (hostUpdateMap == null) {
			hostUpdateMap = new HashMap<>();
		}

		hostUpdateMap.put("id", hostId);
		hostUpdateMap.put(property, value);

		hostUpdates.put(hostId, hostUpdateMap);
	}

	public void setHostDescription(String hostId, String description) {
		updateHost(hostId, HostInfo.CLIENT_DESCRIPTION_KEY, description);
	}

	public void setClientInventoryNumber(String hostId, String inventoryNumber) {
		updateHost(hostId, HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventoryNumber);
	}

	public void setClientOneTimePassword(String hostId, String oneTimePassword) {
		updateHost(hostId, HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY, oneTimePassword);
	}

	public void setHostNotes(String hostId, String notes) {
		updateHost(hostId, HostInfo.CLIENT_NOTES_KEY, notes);
	}

	public void setSystemUUID(String hostId, String uuid) {
		updateHost(hostId, HostInfo.CLIENT_SYSTEM_UUID_KEY, uuid);
	}

	public void setMacAddress(String hostId, String address) {
		updateHost(hostId, HostInfo.CLIENT_MAC_ADRESS_KEY, address);
	}

	public void setIpAddress(String hostId, String address) {
		updateHost(hostId, HostInfo.CLIENT_IP_ADDRESS_KEY, address);
	}

	public void setWanConfig(String hostId, boolean wanConfig) {
		updateHost(hostId, HostInfo.CLIENT_WAN_CONFIG_KEY, wanConfig);
	}

	public void setInstallOnShutdown(String hostId, boolean installOnShutdown) {
		updateHost(hostId, HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, installOnShutdown);
	}

	public List<Map<String, Object>> getOpsiHosts() {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		List<String> hostTypes = new ArrayList<>();
		hostTypes.add(HostInfo.HOST_TYPE_VALUE_OPSI_CONFIG_SERVER);
		hostTypes.add(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER);
		callFilter.put(HostInfo.HOST_TYPE_KEY, hostTypes);
		TimeCheck timer = new TimeCheck(this, "getOpsiHosts").start();
		Logging.notice(this, "host_getObjects");
		List<Map<String, Object>> opsiHosts = exec.getListOfMaps(
				new OpsiMethodCall(RPCMethodName.HOST_GET_OBJECTS, new Object[] { callAttributes, callFilter }));
		timer.stop();
		return opsiHosts;
	}

	public List<Map<String, Object>> getOpsiClients() {
		TimeCheck timer = new TimeCheck(this, "getOpsiClients").start();
		Logging.notice(this, "host_getClients");
		List<Map<String, Object>> opsiClients = exec
				.getListOfMaps(new OpsiMethodCall(RPCMethodName.HOST_GET_CLIENTS, new Object[0]));
		timer.stop();
		return opsiClients;
	}

	public List<String> getClientsWithOtherProductVersion(String productId, String productVersion,
			String packageVersion, boolean includeFailedInstallations) {
		List<String> result = new ArrayList<>();
		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put(OpsiPackage.DB_KEY_PRODUCT_ID, productId);
		callFilter.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> retrievedList = exec.getListOfMaps(omc);
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

	public Map<String, String> sessionInfo(List<String> clientIds) {
		Map<String, String> result = new HashMap<>();

		Object[] callParameters = new Object[] {};
		if (clientIds != null && !clientIds.isEmpty()) {
			callParameters = new Object[] { clientIds };
		}

		RPCMethodName methodname = RPCMethodName.HOST_CONTROL_GET_ACTIVE_SESSIONS;
		Map<String, Object> sessionInfos = exec.getResponses(exec
				.retrieveResponse(new OpsiMethodCall(methodname, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT)));
		for (Entry<String, Object> resultEntry : sessionInfos.entrySet()) {
			String value;

			if (resultEntry.getValue() instanceof String errorString) {
				value = Configed.getResourceValue("sessionInfo.noResponse") + ": " + errorString;
			} else if (resultEntry.getValue() instanceof List<?> sessionlist) {
				value = createSessionInfoForList(sessionlist);
			} else {
				Logging.warning(this, "resultEntry's value is neither a String nor a List");
				value = "";
			}

			result.put(resultEntry.getKey(), value);
		}

		return result;
	}

	private static String createSessionInfoForList(List<?> sessionlist) {
		StringBuilder value = new StringBuilder();
		for (Object element : sessionlist) {
			Map<String, Object> session = POJOReMapper.remap(element);

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
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_GET_MESSAGEBUS_CONNECTED_IDS, new Object[] {});
		return new HashSet<>(exec.getStringListResult(omc));
	}

	public void setHostValues(Map<String, Object> settings) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return;
		}

		exec.doCall(new OpsiMethodCall(RPCMethodName.HOST_UPDATE_OBJECTS, new Object[] { settings }));
	}

	public Map<String, Boolean> getHostDisplayFields() {
		retrieveHostDisplayFields();
		return cacheManager.getCachedData(CacheIdentifier.HOST_DISPLAY_FIELDS, Map.class);
	}

	public void retrieveHostDisplayFields() {
		if (cacheManager.isDataCached(CacheIdentifier.HOST_DISPLAY_FIELDS)) {
			return;
		}
		Map<String, List<Object>> serverPropertyMap = configDataService.getConfigDefaultValuesPD();
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
		for (String field : HostInfo.ORDERING_DISPLAY_FIELDS) {
			hostDisplayFields.put(field,
					configuredByService.indexOf(field) > -1 || userSavedDisplayFields.contains(field));
		}

		hostDisplayFields.put(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, true);
		cacheManager.setCachedData(CacheIdentifier.HOST_DISPLAY_FIELDS, hostDisplayFields);
	}

	private List<String> produceHostDisplayFields(List<String> givenList) {
		List<String> result = null;
		Map<String, ConfigOption> configOptions = configDataService.getConfigOptionsPD();
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

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_UPDATE_OBJECTS, new Object[] { item });

			exec.doCall(omc);
		} else {
			result = givenList;
		}

		return result;
	}
}
