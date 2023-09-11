/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.licences.LicencePoolXOpsiProduct;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import utils.Utils;

/**
 * Provides methods for retrieving data from the server using RPC methods,
 * without storing the retrieved data internally.
 * <p>
 * For retrieving persistent data (i.e. data that is stored internally using
 * {@link CacheManager}) use {@link PersistentDataRetriever}.
 */
public class VolatileDataRetriever {
	AbstractExecutioner exec;
	PersistentDataRetriever persistentDataRetriever;
	OpsiServiceNOMPersistenceController persistenceController;

	public VolatileDataRetriever(AbstractExecutioner exec, PersistentDataRetriever persistentDataRetriever,
			OpsiServiceNOMPersistenceController persistenceController) {
		this.exec = exec;
		this.persistentDataRetriever = persistentDataRetriever;
		this.persistenceController = persistenceController;
	}

	/**
	 * Execute the python-opsi command {@code SSHCommand_getObjects}.
	 *
	 * @return list of commands available for executing with SSH
	 */
	public List<Map<String, Object>> retrieveCommandList() {
		Logging.info(this, "retrieveCommandList ");
		List<Map<String, Object>> sshCommands = exec
				.getListOfMaps(new OpsiMethodCall(RPCMethodName.SSH_COMMAND_GET_OBJECTS, new Object[] {}));
		Logging.debug(this, "retrieveCommandList commands " + sshCommands);
		return sshCommands;
	}

	/**
	 * Retrieve available backends.
	 * <p>
	 * This methods is only a viable option for servers/depots, that has opsi
	 * 4.2 or lower; Due to the RPC method deprecation in opsi 4.3.
	 * 
	 * @return available backends
	 * @deprecated since opsi 4.3
	 */
	public String getBackendInfos() {
		String bgColor0 = "#dedeff";
		String bgColor1 = "#ffffff";
		String bgColor = "";

		String titleSize = "14px";
		String fontSizeBig = "10px";
		String fontSizeSmall = "8px";

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GET_BACKEND_INFOS_LIST_OF_HASHES, new String[] {});
		List<Object> list = exec.getListResult(omc);
		Map<String, List<Map<String, Object>>> backends = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> listEntry = exec.getMapFromItem(list.get(i));
			String backendName = "UNKNOWN";

			if (listEntry.containsKey("name")) {
				backendName = (String) listEntry.get("name");
			}

			if (!backends.containsKey(backendName)) {
				backends.put(backendName, new ArrayList<>());
			}

			backends.get(backendName).add(listEntry);
		}

		StringBuilder buf = new StringBuilder("");
		buf.append("<table border='0' cellspacing='0' cellpadding='0'>\n");

		Iterator<String> backendIterator = backends.keySet().iterator();
		while (backendIterator.hasNext()) {
			String backendName = backendIterator.next();

			buf.append("<tr><td bgcolor='#fbeca5' color='#000000'  width='100%'  colspan='3'  align='left'>");
			buf.append("<font size='" + titleSize + "'><b>" + backendName + "</b></font></td></tr>");

			List<Map<String, Object>> backendEntries = backends.get(backendName);

			for (int i = 0; i < backendEntries.size(); i++) {
				Map<String, Object> listEntry = backendEntries.get(i);

				Iterator<String> eIt = listEntry.keySet().iterator();

				boolean entryIsEven = false;

				while (eIt.hasNext()) {
					String key = eIt.next();
					if ("name".equals(key)) {
						continue;
					}

					entryIsEven = !entryIsEven;
					if (entryIsEven) {
						bgColor = bgColor0;
					} else {
						bgColor = bgColor1;
					}

					Object value = listEntry.get(key);
					buf.append("<tr height='8px'>");
					buf.append("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
							+ fontSizeBig + "'>" + key + "</font></td>");

					if ("config".equals(key)) {
						buf.append("<td colspan='2'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>&nbsp;</font></td>");
						buf.append("</tr>");

						Map<String, Object> configItems = exec.getMapFromItem(value);

						if (!configItems.isEmpty()) {
							Iterator<String> configItemsIterator = configItems.keySet().iterator();

							while (configItemsIterator.hasNext()) {
								String configKey = configItemsIterator.next();

								Object jO = configItems.get(configKey);

								String configVal = "";

								configVal = jO.toString();

								buf.append("<td bgcolor='" + bgColor + "'>&nbsp;</td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configKey
										+ "</font></td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configVal
										+ "</font></td>");
								buf.append("</tr>");
							}
						}
					} else {
						buf.append("<td width='300px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>" + value + "</font></td>");
						buf.append("</tr>");
					}
				}
				buf.append("<tr height='10px'><td bgcolor='" + bgColor + "' colspan='3'></td></tr>");
			}

			buf.append(
					"<tr><td bgcolor='#ffffff' color='#000000' width='100%' height='30px' colspan='3'>&nbsp;</td></tr>");
		}

		buf.append("</table>\n");

		return buf.toString();
	}

	/**
	 * Collects the common property values of some product for a client
	 * collection; Needed for local imaging handling.
	 * 
	 * @param clients  collection of clients
	 * @param product  for which to collect property values
	 * @param property from which to collect values
	 */
	public List<String> getCommonProductPropertyValues(List<String> clients, String product, String property) {
		Logging.info(this, "getCommonProductPropertyValues for product, property, clients " + product + ", " + property
				+ "  -- " + clients);
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("objectId", clients);
		callFilter.put("productId", product);
		callFilter.put("propertyId", property);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> properties = exec.getListOfMaps(omc);
		Set<String> resultSet = new HashSet<>();
		boolean starting = true;
		for (Map<String, Object> map : properties) {
			List<?> valueList = (List<?>) map.get("values");
			Set<String> values = new HashSet<>();
			for (int i = 0; i < valueList.size(); i++) {
				values.add((String) valueList.get(i));
			}

			if (starting) {
				resultSet = values;
				starting = false;
			} else {
				resultSet.retainAll(values);
			}
		}
		Logging.info(this, "getCommonProductPropertyValues " + resultSet);
		return new ArrayList<>(resultSet);
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
		if (!ServerFacade.isOpsi43()) {
			return new HashSet<>();
		}

		Logging.info(this, "get clients connected with messagebus");
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_GET_MESSAGEBUS_CONNECTED_IDS, new Object[] {});
		return new HashSet<>(exec.getStringListResult(omc));
	}

	public List<Map<String, Object>> getProductInfos(String clientId) {
		return new ArrayList<>(getProductInfos(new HashSet<>(), clientId));
	}

	public List<Map<String, Object>> getProductInfos(Set<String> productIds, String clientId) {
		String[] callAttributes = new String[] {};
		HashMap<String, Object> callFilter = new HashMap<>();
		if (!productIds.isEmpty()) {
			callFilter.put(OpsiPackage.DB_KEY_PRODUCT_ID, productIds);
		}
		callFilter.put("clientId", clientId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		return new ArrayList<>(exec.getListOfMaps(omc));
	}

	public Map<String, List<Map<String, String>>> getMapOfNetbootProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfNetbootProductStatesAndActions for : " + Arrays.toString(clientIds));
		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}

		String[] callAttributes = new String[0];
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		callFilter.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
		List<Map<String, Object>> productOnClients = exec.getListOfMaps(new OpsiMethodCall(
				RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS, new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {

			String client = (String) m.get("clientId");
			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(POJOReMapper.giveEmptyForNull(m), true));

		}
		return result;
	}

	public Map<String, List<Map<String, String>>> getMapOfLocalbootProductStatesAndActions(String[] clientIds,
			String[] attributes) {
		Logging.debug(this, "getMapOfLocalbootProductStatesAndActions for : " + Arrays.toString(clientIds));

		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}

		String[] callAttributes = attributes;
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		callFilter.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);

		RPCMethodName methodName = ServerFacade.isOpsi43() && attributes.length != 0
				? RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS_WITH_SEQUENCE
				: RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS;
		List<Map<String, Object>> productOnClients = exec
				.getListOfMaps(new OpsiMethodCall(methodName, new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		for (Map<String, Object> m : productOnClients) {
			String client = (String) m.get("clientId");
			List<Map<String, String>> states1Client = result.computeIfAbsent(client, arg -> new ArrayList<>());
			Map<String, String> aState = new ProductState(POJOReMapper.giveEmptyForNull(m), true);
			states1Client.add(aState);
		}

		return result;
	}

	public Map<String, List<Map<String, String>>> getMapOfProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfProductStatesAndActions for : " + Arrays.toString(clientIds));
		if (clientIds == null || clientIds.length == 0) {
			return new HashMap<>();
		}
		return getProductStatesNOM(clientIds);
	}

	public Map<String, List<Map<String, String>>> getProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", Arrays.asList(clientIds));
		List<Map<String, Object>> productOnClients = exec.getListOfMaps(new OpsiMethodCall(
				RPCMethodName.PRODUCT_ON_CLIENT_GET_OBJECTS, new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {
			String client = (String) m.get("clientId");

			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(POJOReMapper.giveEmptyForNull(m), true));
		}
		return result;
	}

	public List<Map<String, Object>> getAllProducts() {
		String callReturnType = "dict";
		Map<String, String> callFilter = new HashMap<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_DEPOT_GET_IDENTS,
				new Object[] { callReturnType, callFilter });
		return exec.getListOfMaps(omc);
	}

	public Map<String, List<Map<String, Object>>> getHardwareInfo(String clientId) {
		if (clientId == null) {
			return new HashMap<>();
		}

		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("hostId", clientId);

		List<Map<String, Object>> hardwareInfos = exec.getListOfMaps(new OpsiMethodCall(
				RPCMethodName.AUDIT_HARDWARE_ON_HOST_GET_OBJECTS, new Object[] { callAttributes, callFilter }));

		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime scanTime = LocalDateTime.parse("2000-01-01 00:00:00", timeFormatter);
		Map<String, List<Map<String, Object>>> result = new HashMap<>();
		for (Map<String, Object> hardwareInfo : hardwareInfos) {
			if (result.containsKey(hardwareInfo.get("hardwareClass"))) {
				List<Map<String, Object>> hardwareClassInfos = result.get(hardwareInfo.get("hardwareClass"));
				hardwareClassInfos.add(hardwareInfo);
			} else {
				List<Map<String, Object>> hardwareClassInfos = new ArrayList<>();
				hardwareClassInfos.add(hardwareInfo);
				result.put((String) hardwareInfo.get("hardwareClass"), hardwareClassInfos);
			}
			Object lastSeenStr = hardwareInfo.get("lastseen");
			LocalDateTime lastSeen = scanTime;
			if (lastSeenStr != null) {
				lastSeen = LocalDateTime.parse(lastSeenStr.toString(), timeFormatter);
			}
			if (scanTime.compareTo(lastSeen) < 0) {
				scanTime = lastSeen;
			}
		}

		List<Map<String, Object>> scanProperties = new ArrayList<>();
		Map<String, Object> scanProperty = new HashMap<>();
		scanProperty.put("scantime", scanTime.format(timeFormatter));
		scanProperties.add(scanProperty);
		result.put("SCANPROPERTIES", scanProperties);

		if (result.size() > 1) {
			return result;
		}

		return new HashMap<>();
	}

	public Map<String, String> sessionInfo(String[] clientIds) {
		Map<String, String> result = new HashMap<>();

		Object[] callParameters = new Object[] {};
		if (clientIds != null && clientIds.length > 0) {
			callParameters = new Object[] { clientIds };
		}

		RPCMethodName methodname = RPCMethodName.HOST_CONTROL_GET_ACTIVE_SESSIONS;
		Map<String, Object> result0 = exec.getResponses(exec
				.retrieveResponse(new OpsiMethodCall(methodname, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT)));
		for (Entry<String, Object> resultEntry : result0.entrySet()) {
			StringBuilder value = new StringBuilder();

			if (resultEntry.getValue() instanceof String) {
				String errorStr = (String) resultEntry.getValue();
				value = new StringBuilder("no response");
				if (errorStr.indexOf("Opsi timeout") > -1) {
					int i = errorStr.indexOf("(");
					if (i > -1) {
						value.append("   " + errorStr.substring(i + 1, errorStr.length() - 1));
					} else {
						value.append(" (opsi timeout)");
					}
				} else if (errorStr.indexOf(methodname.toString()) > -1) {
					value.append("  (" + methodname + " not valid)");
				} else if (errorStr.indexOf("Name or service not known") > -1) {
					value.append(" (name or service not known)");
				} else {
					Logging.notice(this, "unexpected output occured in session Info");
				}
			} else if (resultEntry.getValue() instanceof List) {
				List<?> sessionlist = (List<?>) resultEntry.getValue();
				for (Object element : sessionlist) {
					Map<String, Object> session = POJOReMapper.remap(element, new TypeReference<Map<String, Object>>() {
					});

					String username = "" + session.get("UserName");
					String logondomain = "" + session.get("LogonDomain");

					if (!value.toString().isEmpty()) {
						value.append("; ");
					}

					value.append(username + " (" + logondomain + "\\" + username + ")");
				}
			} else {
				Logging.warning(this, "resultEntry's value is neither a String nor a List");
			}

			result.put(resultEntry.getKey(), value.toString());
		}

		return result;
	}

	public Map<String, List<SWAuditClientEntry>> getClient2Software(List<String> clients) {
		return retrieveSoftwareAuditOnClients(clients);
	}

	public Map<String, List<SWAuditClientEntry>> retrieveSoftwareAuditOnClients(final List<String> clients) {
		Map<String, List<SWAuditClientEntry>> client2software = new HashMap<>();
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Utils.usedMemory());
		Logging.info(this, "retrieveSoftwareAuditOnClients clients cound: " + clients.size());

		final int STEP_SIZE = 100;

		while (!clients.isEmpty()) {
			List<String> clientListForCall = new ArrayList<>();

			for (int i = 0; i < STEP_SIZE && i < clients.size(); i++) {
				clientListForCall.add(clients.get(i));
			}

			clients.removeAll(clientListForCall);

			Logging.info(this, "retrieveSoftwareAuditOnClients, start a request");

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();
			callFilter.put("state", 1);
			if (clients != null) {
				callFilter.put("clientId", clientListForCall);
			}

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_ON_CLIENT_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> softwareAuditOnClients = exec.getListOfMaps(omc);

			Logging.info(this,
					"retrieveSoftwareAuditOnClients, finished a request, map size " + softwareAuditOnClients.size());

			for (String clientId : clientListForCall) {
				client2software.put(clientId, new LinkedList<>());
			}

			for (Map<String, Object> item : softwareAuditOnClients) {
				SWAuditClientEntry clientEntry = new SWAuditClientEntry(item);
				String clientId = clientEntry.getClientId();

				if (clientId != null) {
					List<SWAuditClientEntry> entries = client2software.get(clientId);
					entries.add(clientEntry);
				}
			}

			Logging.info(this, "retrieveSoftwareAuditOnClients client2software ");
		}

		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Utils.usedMemory());
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Utils.usedMemory());

		return client2software;
	}

	public boolean areDepotsSynchronous(Iterable<String> depots) {
		String lastIdent = null;
		for (String depot : depots) {
			List<String> productIdents = new ArrayList<>();
			String callReturnType = "dict";
			Map<String, String> callFilter = new HashMap<>();
			callFilter.put("depotId", depot);
			List<Map<String, Object>> products = exec.getListOfMaps(new OpsiMethodCall(
					RPCMethodName.PRODUCT_ON_DEPOT_GET_IDENTS, new Object[] { callReturnType, callFilter }));
			for (Map<String, Object> product : products) {
				productIdents.add(product.get("productId") + ";" + product.get("productVersion") + ";"
						+ product.get("packageVersion"));
			}
			Collections.sort(productIdents);
			String ident = String.join("|", productIdents);
			if (lastIdent != null && !ident.equals(lastIdent)) {
				return false;
			}
			lastIdent = ident;
		}
		return true;
	}

	public List<String> getClientsWithOtherProductVersion(String productId, String productVersion,
			String packageVersion, boolean includeFailedInstallations) {
		List<String> result = new ArrayList<>();
		String[] callAttributes = new String[] {};
		HashMap<String, String> callFilter = new HashMap<>();
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
				Logging.debug("getClientsWithOtherProductVersion hit " + m);
				result.add(client);
			}
		}
		Logging.info(this, "getClientsWithOtherProductVersion globally " + result.size());
		return result;
	}

	public List<Map<String, Object>> hostRead() {
		String[] callAttributes = new String[] {};
		Map<?, ?> callFilter = new HashMap<>();
		TimeCheck timer = new TimeCheck(this, "HOST_read").start();
		Logging.notice(this, "host_getObjects");
		List<Map<String, Object>> opsiHosts = exec.getListOfMaps(
				new OpsiMethodCall(RPCMethodName.HOST_GET_OBJECTS, new Object[] { callAttributes, callFilter }));
		timer.stop();
		return opsiHosts;
	}

	public String getOpsiCACert() {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GET_OPSI_CA_CERT, new Object[0]);
		return exec.getStringResult(omc);
	}

	public Map<String, Map<String, Object>> getDepotPropertiesForPermittedDepots() {
		Map<String, Map<String, Object>> depotProperties = persistentDataRetriever.getHostInfoCollections()
				.getAllDepots();
		LinkedHashMap<String, Map<String, Object>> depotPropertiesForPermittedDepots = new LinkedHashMap<>();

		String configServer = persistentDataRetriever.getHostInfoCollections().getConfigServer();
		if (persistenceController.hasDepotPermission(configServer)) {
			depotPropertiesForPermittedDepots.put(configServer, depotProperties.get(configServer));
		}

		for (Entry<String, Map<String, Object>> depotProperty : depotProperties.entrySet()) {
			if (!depotProperty.getKey().equals(configServer)
					&& persistenceController.hasDepotPermission(depotProperty.getKey())) {
				depotPropertiesForPermittedDepots.put(depotProperty.getKey(), depotProperty.getValue());
			}
		}

		return depotPropertiesForPermittedDepots;
	}

	public Map<String, Integer> getInstalledOsOverview() {
		Logging.info(this, "getInstalledOsOverview");
		Map<String, Object> producedLicencingInfo = retrieveProducedLicensingInfo();
		return POJOReMapper.remap(producedLicencingInfo.get("client_numbers"),
				new TypeReference<Map<String, Integer>>() {
				});
	}

	public List<Map<String, Object>> getModules() {
		Logging.info(this, "getModules");
		Map<String, Object> producedLicencingInfo = retrieveProducedLicensingInfo();
		return POJOReMapper.remap(producedLicencingInfo.get("licenses"),
				new TypeReference<List<Map<String, Object>>>() {
				});
	}

	private Map<String, Object> retrieveProducedLicensingInfo() {
		Map<String, Object> producedLicencingInfo;
		if (persistentDataRetriever.isOpsiUserAdmin()
				&& persistentDataRetriever.getOpsiLicencingInfoOpsiAdmin() != null) {
			producedLicencingInfo = POJOReMapper.remap(
					persistentDataRetriever.getOpsiLicencingInfoOpsiAdmin().get("result"),
					new TypeReference<Map<String, Object>>() {
					});
		} else {
			producedLicencingInfo = persistentDataRetriever.getOpsiLicencingInfoNoOpsiAdmin();
		}
		return producedLicencingInfo;
	}

	public List<String> getDomains() {
		List<String> result = new ArrayList<>();

		Map<String, List<Object>> configDefaultValues = persistentDataRetriever.getConfigDefaultValues();
		if (configDefaultValues.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY) == null) {
			Logging.info(this,
					"no values found for   " + OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY);
		} else {
			Logging.info(this, "getDomains "
					+ configDefaultValues.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY));

			HashMap<String, Integer> numberedValues = new HashMap<>();
			TreeSet<String> orderedValues = new TreeSet<>();
			TreeSet<String> unorderedValues = new TreeSet<>();

			for (Object item : configDefaultValues
					.get(OpsiServiceNOMPersistenceController.CONFIGED_GIVEN_DOMAINS_KEY)) {
				String entry = (String) item;
				int p = entry.indexOf(":");
				if (p == -1 || p == 0) {
					unorderedValues.add(entry);
				} else if (p > 0) {
					// the only regular case
					int orderNumber = -1;
					try {
						orderNumber = Integer.valueOf(entry.substring(0, p));
						String value = entry.substring(p + 1);
						if (numberedValues.get(value) == null || orderNumber < numberedValues.get(value)) {
							orderedValues.add(entry);
							numberedValues.put(value, orderNumber);
						}
					} catch (NumberFormatException x) {
						Logging.warning(this, "illegal order format for domain entry: " + entry);
						unorderedValues.add(entry);
					}
				} else {
					Logging.warning(this, "p has unexpected value " + p);
				}
			}

			for (String entry : orderedValues) {
				int p = entry.indexOf(":");
				result.add(entry.substring(p + 1));
			}

			unorderedValues.removeAll(result);

			for (String entry : unorderedValues) {
				result.add(entry);
			}
		}

		Logging.info(this, "getDomains " + result);
		return result;
	}

	// without internal caching; legacy license method
	public Map<String, Map<String, Object>> getRelationsSoftwareL2LPool() {
		Map<String, Map<String, Object>> rowsSoftwareL2LPool = new HashMap<>();
		if (persistentDataRetriever.isWithLicenceManagement()) {
			List<String> callAttributes = new ArrayList<>();
			Map<String, Object> callFilter = new HashMap<>();
			List<Map<String, Object>> softwareL2LPools = exec
					.getListOfMaps(new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_GET_OBJECTS,
							new Object[] { callAttributes, callFilter }));

			for (Map<String, Object> softwareL2LPool : softwareL2LPools) {
				softwareL2LPool.remove("ident");
				softwareL2LPool.remove("type");

				rowsSoftwareL2LPool
						.put(Utils.pseudokey(new String[] { (String) softwareL2LPool.get("softwareLicenseId"),
								(String) softwareL2LPool.get("licensePoolId") }), softwareL2LPool);
			}
		}
		return rowsSoftwareL2LPool;
	}

	// without internal caching
	public Map<String, Map<String, String>> getRelationsProductId2LPool() {
		HashMap<String, Map<String, String>> rowsLicencePoolXOpsiProduct = new HashMap<>();

		if (persistentDataRetriever.isWithLicenceManagement()) {
			//persistentDataRetriever.licencePoolXOpsiProductRequestRefresh();
			persistentDataRetriever.getLicencePoolXOpsiProduct();
			Logging.info(this,
					"licencePoolXOpsiProduct size " + persistentDataRetriever.getLicencePoolXOpsiProduct().size());

			for (StringValuedRelationElement element : persistentDataRetriever.getLicencePoolXOpsiProduct()) {
				rowsLicencePoolXOpsiProduct
						.put(Utils.pseudokey(new String[] { element.get(LicencePoolXOpsiProduct.LICENCE_POOL_KEY),
								element.get(LicencePoolXOpsiProduct.PRODUCT_ID_KEY) }), element);
			}
		}

		Logging.info(this, "rowsLicencePoolXOpsiProduct size " + rowsLicencePoolXOpsiProduct.size());

		return rowsLicencePoolXOpsiProduct;
	}

	public boolean isHealthDataAlreadyLoaded() {
		return persistentDataRetriever.checkHealth() != null;
	}

	public List<Map<String, Object>> retrieveHealthDetails(String checkId) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> data : persistentDataRetriever.checkHealth()) {
			if (((String) data.get("check_id")).equals(checkId)) {
				result = POJOReMapper.remap(data.get("partial_results"),
						new TypeReference<List<Map<String, Object>>>() {
						});
				break;
			}
		}
		return result;
	}
}
