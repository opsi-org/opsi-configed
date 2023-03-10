package de.uib.configed.clientselection.backends.opsidatamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;

import de.uib.configed.clientselection.AbstractBackend;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntEqualsOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntGreaterOrEqualOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntGreaterThanOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntLessOrEqualOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntLessThanOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataDateEqualsOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataDateGreaterOrEqualOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataDateGreaterThanOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataDateLessOrEqualOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataDateLessThanOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataGroupEqualsOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataHardwareOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataIntEqualsOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataIntGreaterOrEqualOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataIntGreaterThanOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataIntLessOrEqualOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataIntLessThanOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataSoftwareOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataStringEqualsOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataSuperGroupEqualsOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiDataSwAuditOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.operations.OpsiSoftwareEqualsOperation;
import de.uib.configed.clientselection.elements.DescriptionElement;
import de.uib.configed.clientselection.elements.GenericBigIntegerElement;
import de.uib.configed.clientselection.elements.GenericEnumElement;
import de.uib.configed.clientselection.elements.GenericIntegerElement;
import de.uib.configed.clientselection.elements.GenericTextElement;
import de.uib.configed.clientselection.elements.GroupElement;
import de.uib.configed.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.clientselection.elements.IPElement;
import de.uib.configed.clientselection.elements.NameElement;
import de.uib.configed.clientselection.elements.SoftwareActionProgressElement;
import de.uib.configed.clientselection.elements.SoftwareActionResultElement;
import de.uib.configed.clientselection.elements.SoftwareInstallationStatusElement;
import de.uib.configed.clientselection.elements.SoftwareLastActionElement;
import de.uib.configed.clientselection.elements.SoftwareModificationTimeElement;
import de.uib.configed.clientselection.elements.SoftwareNameElement;
import de.uib.configed.clientselection.elements.SoftwarePackageVersionElement;
import de.uib.configed.clientselection.elements.SoftwareRequestElement;
import de.uib.configed.clientselection.elements.SoftwareTargetConfigurationElement;
import de.uib.configed.clientselection.elements.SoftwareVersionElement;
import de.uib.configed.clientselection.elements.SwAuditArchitectureElement;
import de.uib.configed.clientselection.elements.SwAuditLanguageElement;
import de.uib.configed.clientselection.elements.SwAuditLicenseKeyElement;
import de.uib.configed.clientselection.elements.SwAuditNameElement;
import de.uib.configed.clientselection.elements.SwAuditSoftwareIdElement;
import de.uib.configed.clientselection.elements.SwAuditSubversionElement;
import de.uib.configed.clientselection.elements.SwAuditVersionElement;
import de.uib.configed.clientselection.operations.AndOperation;
import de.uib.configed.clientselection.operations.BigIntEqualsOperation;
import de.uib.configed.clientselection.operations.BigIntGreaterOrEqualOperation;
import de.uib.configed.clientselection.operations.BigIntGreaterThanOperation;
import de.uib.configed.clientselection.operations.BigIntLessOrEqualOperation;
import de.uib.configed.clientselection.operations.BigIntLessThanOperation;
import de.uib.configed.clientselection.operations.DateEqualsOperation;
import de.uib.configed.clientselection.operations.DateGreaterOrEqualOperation;
import de.uib.configed.clientselection.operations.DateGreaterThanOperation;
import de.uib.configed.clientselection.operations.DateLessOrEqualOperation;
import de.uib.configed.clientselection.operations.DateLessThanOperation;
import de.uib.configed.clientselection.operations.HardwareOperation;
import de.uib.configed.clientselection.operations.HostOperation;
import de.uib.configed.clientselection.operations.IntEqualsOperation;
import de.uib.configed.clientselection.operations.IntGreaterOrEqualOperation;
import de.uib.configed.clientselection.operations.IntGreaterThanOperation;
import de.uib.configed.clientselection.operations.IntLessOrEqualOperation;
import de.uib.configed.clientselection.operations.IntLessThanOperation;
import de.uib.configed.clientselection.operations.NotOperation;
import de.uib.configed.clientselection.operations.OrOperation;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.messages.Messages;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.logging.Logging;

public final class OpsiDataBackend extends AbstractBackend {
	// data which will be cached
	List<Client> clients;
	Map<String, HostInfo> clientMaps;

	// client -> groups with it
	Map<String, Set<String>> groups;

	// client -> all groups for which the client belongs to directly or by virtue of some path
	Map<String, Set<String>> superGroups;
	Map<String, List<Map<String, String>>> softwareMap;
	Map<String, List<SWAuditClientEntry>> swauditMap;
	List<Map<String, Object>> hardwareOnClient;
	Map<String, List<Map<String, Object>>> clientToHardware;

	private List hwConfig;
	private List hwConfigLocalized;
	private Map<String, String> hwUiToOpsi;
	private Map<String, List> hwClassToValues;

	private AbstractPersistenceController controller;

	private static OpsiDataBackend instance;

	// we make a singleton in order to avoid data reloading
	public static OpsiDataBackend getInstance() {
		if (instance == null) {
			instance = new OpsiDataBackend();
		}

		return instance;
	}

	public static void renew() {
		instance = null;
	}

	private OpsiDataBackend() {
		controller = PersistenceControllerFactory.getPersistenceController();
		if (controller == null) {
			Logging.warning(this, "Warning, controller is null!");
		}
		getHardwareConfig();

	}

	@Override
	protected AbstractSelectOperation createOperation(AbstractSelectOperation operation) {
		Logging.info(this, "createOperation operation, data, element: " + operation.getClassName() + ", "
				+ operation.getData().toString() + ",  " + operation.getElement().getClassName());

		// Host
		AbstractSelectElement element = operation.getElement();
		String[] elementPath = element.getPathArray();
		Object data = operation.getData();
		String attributeTextHost = null;

		if (element instanceof NameElement) {
			attributeTextHost = HostInfo.HOSTNAME_KEY;
		} else if (element instanceof IPElement) {
			attributeTextHost = HostInfo.CLIENT_IP_ADDRESS_KEY;
		} else if (element instanceof DescriptionElement) {
			attributeTextHost = HostInfo.CLIENT_DESCRIPTION_KEY;
		}

		if (attributeTextHost != null) {
			if (operation instanceof StringEqualsOperation) {
				return new OpsiDataStringEqualsOperation(OpsiDataClient.HOSTINFO_MAP, attributeTextHost,
						(String) operation.getData(), element);
			}
			throw new IllegalArgumentException("Wrong operation for this element.");
		}

		if (element instanceof GroupElement && operation instanceof StringEqualsOperation) {
			return new OpsiDataGroupEqualsOperation((String) operation.getData(), element);
		}

		if (element instanceof GroupWithSubgroupsElement && operation instanceof StringEqualsOperation) {
			return new OpsiDataSuperGroupEqualsOperation((String) operation.getData(), element);
		}

		// Software
		String attributeTextSoftware = null;
		if (element instanceof SoftwareNameElement) {
			attributeTextSoftware = ProductState.KEY_PRODUCT_ID;
		} else if (element instanceof SoftwareVersionElement) {
			attributeTextSoftware = ProductState.KEY_PRODUCT_VERSION;
		} else if (element instanceof SoftwarePackageVersionElement) {
			attributeTextSoftware = ProductState.KEY_PACKAGE_VERSION;
		} else if (element instanceof SoftwareRequestElement) {
			attributeTextSoftware = ProductState.KEY_ACTION_REQUEST;
		} else if (element instanceof SoftwareTargetConfigurationElement) {
			attributeTextSoftware = ProductState.KEY_TARGET_CONFIGURATION;
		} else if (element instanceof SoftwareInstallationStatusElement) {
			attributeTextSoftware = ProductState.KEY_INSTALLATION_STATUS;
		} else if (element instanceof SoftwareActionProgressElement) {
			attributeTextSoftware = ProductState.KEY_ACTION_PROGRESS;
		} else if (element instanceof SoftwareActionResultElement) {
			attributeTextSoftware = ProductState.KEY_ACTION_RESULT;
		} else if (element instanceof SoftwareLastActionElement) {
			attributeTextSoftware = ProductState.KEY_LAST_ACTION;
		} else if (element instanceof SoftwareModificationTimeElement) {
			attributeTextSoftware = ProductState.KEY_LAST_STATE_CHANGE;
		}

		if (attributeTextSoftware != null) {
			if (operation instanceof StringEqualsOperation) {
				return new OpsiSoftwareEqualsOperation(attributeTextSoftware, (String) operation.getData(), element);
			}

			if (operation instanceof DateEqualsOperation) {
				return new OpsiDataDateEqualsOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			}

			if (operation instanceof DateLessThanOperation) {
				return new OpsiDataDateLessThanOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			}

			if (operation instanceof DateLessOrEqualOperation) {
				return new OpsiDataDateLessOrEqualOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			}

			if (operation instanceof DateGreaterThanOperation) {
				return new OpsiDataDateGreaterThanOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			}

			if (operation instanceof DateGreaterOrEqualOperation) {
				return new OpsiDataDateGreaterOrEqualOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			}

			throw new IllegalArgumentException("Wrong operation for this element.");
		}

		// this would need the package version to be an integer

		// return new OpsiDataIntEqualsOperation( OpsiDataClient.SOFTWARE_MAP,

		// return new OpsiDataIntLessThanOperation( OpsiDataClient.SOFTWARE_MAP,

		// return new OpsiDataIntGreaterThanOperation( OpsiDataClient.SOFTWARE_MAP,

		// SwAudit
		String swauditAttributeText = null;
		if (element instanceof SwAuditArchitectureElement) {
			swauditAttributeText = "architecture";
		} else if (element instanceof SwAuditLanguageElement) {
			swauditAttributeText = "language";
		} else if (element instanceof SwAuditLicenseKeyElement) {
			swauditAttributeText = "licenseKey";
		} else if (element instanceof SwAuditNameElement) {
			swauditAttributeText = "name";
		} else if (element instanceof SwAuditVersionElement) {
			swauditAttributeText = "version";
		} else if (element instanceof SwAuditSubversionElement) {
			swauditAttributeText = "subVersion";
		} else if (element instanceof SwAuditSoftwareIdElement) {
			swauditAttributeText = "windowsSoftwareID";
		}

		if (swauditAttributeText != null && operation instanceof StringEqualsOperation) {
			return new OpsiDataStringEqualsOperation(OpsiDataClient.SWAUDIT_MAP, swauditAttributeText,
					(String) operation.getData(), element);
		}

		// hardware
		if (element instanceof GenericTextElement || element instanceof GenericIntegerElement
				|| element instanceof GenericBigIntegerElement || element instanceof GenericEnumElement) {
			String map = hwUiToOpsi.get(elementPath[0]);
			String attr = getKey(elementPath);

			if (operation instanceof StringEqualsOperation) {
				return new OpsiDataStringEqualsOperation(map, attr, (String) data, element);
			}

			if (operation instanceof IntLessThanOperation) {
				return new OpsiDataIntLessThanOperation(map, attr, (Integer) data, element);
			}

			if (operation instanceof IntLessOrEqualOperation) {
				return new OpsiDataIntLessOrEqualOperation(map, attr, (Integer) data, element);
			}

			if (operation instanceof IntGreaterThanOperation) {
				return new OpsiDataIntGreaterThanOperation(map, attr, (Integer) data, element);
			}

			if (operation instanceof IntGreaterOrEqualOperation) {
				return new OpsiDataIntGreaterOrEqualOperation(map, attr, (Integer) data, element);
			}

			if (operation instanceof IntEqualsOperation) {
				return new OpsiDataIntEqualsOperation(map, attr, (Integer) data, element);
			}

			if (operation instanceof BigIntLessThanOperation) {
				return new OpsiDataBigIntLessThanOperation(map, attr, (Long) data, element);
			}

			if (operation instanceof BigIntLessOrEqualOperation) {
				return new OpsiDataBigIntLessOrEqualOperation(map, attr, (Long) data, element);
			}

			if (operation instanceof BigIntGreaterThanOperation) {
				return new OpsiDataBigIntGreaterThanOperation(map, attr, (Long) data, element);
			}

			if (operation instanceof BigIntGreaterOrEqualOperation) {
				return new OpsiDataBigIntGreaterOrEqualOperation(map, attr, (Long) data, element);
			}

			if (operation instanceof BigIntEqualsOperation) {
				return new OpsiDataBigIntEqualsOperation(map, attr, (Long) data, element);
			}
		}
		Logging.error("IllegalArgument: The operation " + operation + " was not found on " + element);
		throw new IllegalArgumentException("The operation " + operation + " was not found on " + element);
	}

	@Override
	protected AbstractSelectGroupOperation createGroupOperation(AbstractSelectGroupOperation operation,
			List<AbstractSelectOperation> operations) {
		if (operation instanceof AndOperation && operations.size() >= 2) {
			return new AndOperation(operations);
		}

		if (operation instanceof OrOperation && operations.size() >= 2) {
			return new OrOperation(operations);
		}

		if (operation instanceof NotOperation && operations.size() == 1) {
			return new NotOperation(operations.get(0));
		}

		if (operation instanceof SoftwareOperation && operations.size() == 1) {
			return new OpsiDataSoftwareOperation(operations.get(0));
		}

		if (operation instanceof SwAuditOperation && operations.size() == 1) {
			return new OpsiDataSwAuditOperation(operations.get(0));
		}

		if (operation instanceof HardwareOperation && operations.size() == 1) {
			return new OpsiDataHardwareOperation(operations.get(0));
		}

		if (operation instanceof HostOperation && operations.size() == 1) {
			return new HostOperation(operations.get(0));
		}

		Logging.error(this, "IllegalArgument: The group operation " + operation + " was not found with "
				+ operations.size() + " operations");
		throw new IllegalArgumentException(
				"The group operation " + operation + " was not found with " + operations.size() + " operations");

	}

	@Override
	public void setReloadRequested() {
		Logging.info(this, "setReloadRequested");
		super.setReloadRequested();
		clientMaps = null;
		groups = null;
		superGroups = null;
		softwareMap = null;
		controller.productDataRequestRefresh();

		swauditMap = null;
		controller.softwareAuditOnClientsRequestRefresh();

		hardwareOnClient = null;
		clientToHardware = null;

	}

	private void checkInitData() {
		Logging.info(this, "checkInitData ");

		// gets current data which should be in cache already

		// take always the current host infos

		clientMaps = controller.getHostInfoCollections().getMapOfPCInfoMaps();
		Logging.info(this, "client maps size " + clientMaps.size());

		if (groups == null || reloadRequested) {
			groups = controller.getFObject2Groups();
		}

		if (superGroups == null || reloadRequested) {
			superGroups = controller.getHostInfoCollections().getFNode2Treeparents();
		}

		String[] clientNames = clientMaps.keySet().toArray(new String[0]);

		if (hasSoftware) {
			softwareMap = controller.getMapOfProductStatesAndActions(clientNames);
			Logging.debug(this, "getClients softwareMap ");
		}

		swauditMap = getSwAuditOnClients();

		getHardwareConfig();

		Logging.debug(this, "getClients hasHardware " + hasHardware);
		if (hasHardware) {
			getHardwareOnClient(clientNames);
		} else {

			// dont use older data after a reload request
			hardwareOnClient = null;
		}

		reloadRequested = false;

	}

	@Override
	protected List<Client> getClients() {
		List<Client> clients = new LinkedList<>();

		checkInitData();

		Logging.info(this, "getClients hasSoftware " + hasSoftware);
		Logging.info(this, "getClients hasHardware " + hasHardware);
		Logging.info(this, "getClients hasSoftware " + hasSoftware);
		Logging.info(this, "getClients swauditMap != null  " + (swauditMap != null));

		for (Entry<String, HostInfo> clientEntry : clientMaps.entrySet()) {
			OpsiDataClient client = new OpsiDataClient(clientEntry.getKey());
			client.setInfoMap(clientEntry.getValue().getMap());
			if (hasHardware) {
				client.setHardwareInfo(clientToHardware.get(clientEntry.getKey()));
			}

			if (groups.containsKey(clientEntry.getKey())) {
				client.setGroups(groups.get(clientEntry.getKey()));
			}

			if (superGroups.containsKey(clientEntry.getKey())) {
				client.setSuperGroups(superGroups.get(clientEntry.getKey()));
			}

			if (hasSoftware && softwareMap.containsKey(clientEntry.getKey())
					&& softwareMap.get(clientEntry.getKey()) instanceof List) {
				client.setOpsiProductList(softwareMap.get(clientEntry.getKey()));
			}

			if (swauditMap != null && swauditMap.containsKey(clientEntry.getKey())) {
				client.setSwAuditList(swauditMap.get(clientEntry.getKey()));
			}

			clients.add(client);
		}
		return clients;
	}

	@Override
	public List<String> getGroups() {
		return controller.getHostGroupIds();
	}

	@Override
	public NavigableSet<String> getProductIDs() {
		return controller.getProductIds();
	}

	@Override
	public Map<String, List<AbstractSelectElement>> getHardwareList() {
		Map<String, List<AbstractSelectElement>> result = new HashMap<>();

		for (int i = 0; i < hwConfig.size(); i++) {
			Map hardwareMap = (Map) hwConfig.get(i);
			Map hardwareMapLocalized = (Map) hwConfigLocalized.get(i);
			String hardwareName = (String) ((Map) hardwareMap.get("Class")).get("UI");
			String hardwareNameLocalized = (String) ((Map) hardwareMapLocalized.get("Class")).get("UI");
			List<AbstractSelectElement> elementList = new LinkedList<>();
			List values = (List) hardwareMap.get("Values");
			List valuesLocalized = (List) hardwareMapLocalized.get("Values");
			for (int j = 0; j < values.size(); j++) {
				Map valuesMap = (Map) values.get(j);
				String type = (String) valuesMap.get("Type");
				String name = (String) valuesMap.get("UI");
				String localizedName = (String) ((Map) valuesLocalized.get(j)).get("UI");
				if (type.equals("int") || type.equals("tinyint")) {
					elementList.add(new GenericIntegerElement(new String[] { hardwareName, name },
							hardwareNameLocalized, localizedName));
				} else if (type.equals("bigint")) {
					elementList.add(new GenericBigIntegerElement(new String[] { hardwareName, name },
							hardwareNameLocalized, localizedName));
				} else {
					elementList.add(new GenericTextElement(new String[] { hardwareName, name }, hardwareNameLocalized,
							localizedName));
				}
			}
			result.put(hardwareName, elementList);

			Logging.debug(this, "" + elementList);
		}
		return result;
	}

	@Override
	public Map<String, List<AbstractSelectElement>> getLocalizedHardwareList() {
		Map<String, List<AbstractSelectElement>> result = new HashMap<>();

		for (int i = 0; i < hwConfig.size(); i++) {
			Map hardwareMap = (Map) hwConfig.get(i);
			Map hardwareMapLocalized = (Map) hwConfigLocalized.get(i);
			String hardwareName = (String) ((Map) hardwareMap.get("Class")).get("UI");
			String hardwareNameLocalized = (String) ((Map) hardwareMapLocalized.get("Class")).get("UI");
			List<AbstractSelectElement> elementList = new LinkedList<>();
			List values = (List) hardwareMap.get("Values");
			List valuesLocalized = (List) hardwareMapLocalized.get("Values");
			for (int j = 0; j < values.size(); j++) {
				Map valuesMap = (Map) values.get(j);
				String type = (String) valuesMap.get("Type");
				String name = (String) valuesMap.get("UI");
				String localizedName = (String) ((Map) valuesLocalized.get(j)).get("UI");
				if (type.equals("int") || type.equals("tinyint")) {
					elementList.add(new GenericIntegerElement(new String[] { hardwareName, name },
							hardwareNameLocalized, localizedName));
				} else if (type.equals("bigint")) {
					elementList.add(new GenericBigIntegerElement(new String[] { hardwareName, name },
							hardwareNameLocalized, localizedName));
				} else {
					elementList.add(new GenericTextElement(new String[] { hardwareName, name }, hardwareNameLocalized,
							localizedName));
				}
			}
			result.put(hardwareNameLocalized, elementList);

			Logging.debug(this, "" + elementList);
		}
		return result;
	}

	private String getKey(String[] elementPath) {
		Logging.debug(this, elementPath[0]);
		List values = hwClassToValues.get(hwUiToOpsi.get(elementPath[0]));
		if (values != null) {
			for (Object value : values) {
				Map valueMap = (Map) value;
				if (elementPath[1].equals(valueMap.get("UI"))) {
					return (String) valueMap.get("Opsi");
				}
			}
		}
		Logging.error(this, "Element not found: " + Arrays.toString(elementPath));
		return "";
	}

	private void getHardwareOnClient(String[] clientNames) {
		hardwareOnClient = controller.getHardwareOnClient();
		clientToHardware = new HashMap<>();
		for (int i = 0; i < clientNames.length; i++) {
			clientToHardware.put(clientNames[i], new LinkedList<>());
		}
		for (Map<String, Object> map : hardwareOnClient) {
			String name = (String) map.get("hostId");
			if (!clientToHardware.containsKey(name)) {
				Logging.debug(this, "Non-client hostid: " + name);
				continue;
			}
			clientToHardware.get(name).add(map);
		}
	}

	private Map<String, List<SWAuditClientEntry>> getSwAuditOnClients() {
		Map<String, List<SWAuditClientEntry>> result = new HashMap<>();
		if (!hasSwAudit) {
			return result;
		}

		controller.fillClient2Software(new ArrayList<>(clientMaps.keySet()));
		result = controller.getClient2Software();

		return result;
	}

	private void getHardwareConfig() {
		String locale = Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry();
		Logging.debug(this, locale);
		hwConfig = controller.getOpsiHWAuditConf("en_");
		hwConfigLocalized = controller.getOpsiHWAuditConf(locale);
		Logging.debug(this, "" + hwConfig);
		hwUiToOpsi = new HashMap<>();
		hwClassToValues = new HashMap<>();

		for (Object obj : hwConfig) {
			Map hardwareMap = (Map) obj;
			String hardwareName = (String) ((Map) hardwareMap.get("Class")).get("UI");
			String hardwareOpsi = (String) ((Map) hardwareMap.get("Class")).get("Opsi");
			List values = (List) hardwareMap.get("Values");
			hwUiToOpsi.put(hardwareName, hardwareOpsi);
			hwClassToValues.put(hardwareOpsi, values);
		}
	}
}