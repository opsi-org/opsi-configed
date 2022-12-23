package de.uib.configed.clientselection.backends.opsidatamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uib.configed.clientselection.Backend;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectGroupOperation;
import de.uib.configed.clientselection.SelectOperation;
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
import de.uib.configed.type.HWAuditClientEntry;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.messages.Messages;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.logging.logging;

public class OpsiDataBackend extends Backend {
	// data which will be cached
	List<Client> clients;
	Map<String, HostInfo> clientMaps;
	Map<String, Set<String>> groups; // client -> groups with it
	Map<String, Set<String>> superGroups; // client -> all groups for which the client belongs to directly or by virtue
											// of some path
	Map softwareMap;
	Map<String, List<SWAuditClientEntry>> swauditMap;
	List<Map<String, Object>> hardwareOnClient;
	Map<String, List<Map<String, Object>>> clientToHardware;

	private List hwConfig;
	private List hwConfigLocalized;
	private Map<String, String> hwUiToOpsi;
	private Map<String, List> hwClassToValues;

	private PersistenceController controller;

	private static OpsiDataBackend instance;

	// we make a singleton in order to avoid data reloading
	public static OpsiDataBackend getInstance() {
		if (instance == null)
			instance = new OpsiDataBackend();

		return instance;
	}

	public static void renew() {
		instance = null;
	}

	private OpsiDataBackend() {
		controller = PersistenceControllerFactory.getPersistenceController();
		if (controller == null)
			logging.warning(this, "Warning, controller is null!");
		getHardwareConfig();
		
	}

	@Override
	protected SelectOperation createOperation(SelectOperation operation) {
		logging.info(this, "createOperation operation, data, element: " + operation.getClassName() + ", "
				+ operation.getData().toString() + ",  " + operation.getElement().getClassName());

		// Host
		SelectElement element = operation.getElement();
		String[] elementPath = element.getPathArray();
		Object data = operation.getData();
		String attributeTextHost = null;
		if (element instanceof NameElement)
			attributeTextHost = HostInfo.hostnameKEY; // "id", has been "hostId";
		else if (element instanceof IPElement)
			attributeTextHost = HostInfo.clientIpAddressKEY; // "ipAddress";
		else if (element instanceof DescriptionElement)
			attributeTextHost = HostInfo.clientDescriptionKEY; // "description";
		if (attributeTextHost != null) {
			if (operation instanceof StringEqualsOperation)
				return new OpsiDataStringEqualsOperation(OpsiDataClient.HOSTINFO_MAP, attributeTextHost,
						(String) operation.getData(), element);
			throw new IllegalArgumentException("Wrong operation for this element.");
		}

		if (element instanceof GroupElement && operation instanceof StringEqualsOperation)
			return new OpsiDataGroupEqualsOperation((String) operation.getData(), element);

		if (element instanceof GroupWithSubgroupsElement && operation instanceof StringEqualsOperation)
			return new OpsiDataSuperGroupEqualsOperation((String) operation.getData(), element);

		// Software
		String attributeTextSoftware = null;
		if (element instanceof SoftwareNameElement)
			attributeTextSoftware = ProductState.KEY_productId;
		else if (element instanceof SoftwareVersionElement)
			attributeTextSoftware = ProductState.KEY_productVersion;
		else if (element instanceof SoftwarePackageVersionElement)
			attributeTextSoftware = ProductState.KEY_packageVersion;
		else if (element instanceof SoftwareRequestElement)
			attributeTextSoftware = ProductState.KEY_actionRequest;
		else if (element instanceof SoftwareTargetConfigurationElement)
			attributeTextSoftware = ProductState.KEY_targetConfiguration;
		else if (element instanceof SoftwareInstallationStatusElement)
			attributeTextSoftware = ProductState.KEY_installationStatus;
		else if (element instanceof SoftwareActionProgressElement)
			attributeTextSoftware = ProductState.KEY_actionProgress;
		else if (element instanceof SoftwareActionResultElement)
			attributeTextSoftware = ProductState.KEY_actionResult;
		else if (element instanceof SoftwareLastActionElement)
			attributeTextSoftware = ProductState.KEY_lastAction;
		else if (element instanceof SoftwareModificationTimeElement)
			attributeTextSoftware = ProductState.KEY_lastStateChange; // "lastStateChange" ??;

		if (attributeTextSoftware != null) {
			if (operation instanceof StringEqualsOperation)
				return new OpsiSoftwareEqualsOperation(attributeTextSoftware, (String) operation.getData(), element);

			if (operation instanceof DateEqualsOperation)
				return new OpsiDataDateEqualsOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			if (operation instanceof DateLessThanOperation)
				return new OpsiDataDateLessThanOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			if (operation instanceof DateLessOrEqualOperation)
				return new OpsiDataDateLessOrEqualOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			if (operation instanceof DateGreaterThanOperation)
				return new OpsiDataDateGreaterThanOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);
			if (operation instanceof DateGreaterOrEqualOperation)
				return new OpsiDataDateGreaterOrEqualOperation(OpsiDataClient.SOFTWARE_MAP, attributeTextSoftware,
						(String) operation.getData(), element);

			throw new IllegalArgumentException("Wrong operation for this element.");
		}

		// this would need the package version to be an integer
		// if( element instanceof SoftwarePackageVersionElement )
		// {
		// if( operation instanceof IntEqualsOperation )
		// return new OpsiDataIntEqualsOperation( OpsiDataClient.SOFTWARE_MAP,
		
		// if( operation instanceof IntLessThanOperation )
		// return new OpsiDataIntLessThanOperation( OpsiDataClient.SOFTWARE_MAP,
		
		// if( operation instanceof IntGreaterThanOperation )
		// return new OpsiDataIntGreaterThanOperation( OpsiDataClient.SOFTWARE_MAP,
		
		// throw new IllegalArgumentException("Wrong operation for this element.");
		// }

		// SwAudit
		String swauditAttributeText = null;
		if (element instanceof SwAuditArchitectureElement)
			swauditAttributeText = "architecture";
		else if (element instanceof SwAuditLanguageElement)
			swauditAttributeText = "language";
		else if (element instanceof SwAuditLicenseKeyElement)
			swauditAttributeText = "licenseKey";
		else if (element instanceof SwAuditNameElement)
			swauditAttributeText = "name";
		else if (element instanceof SwAuditVersionElement)
			swauditAttributeText = "version";
		else if (element instanceof SwAuditSubversionElement)
			swauditAttributeText = "subVersion";
		else if (element instanceof SwAuditSoftwareIdElement)
			swauditAttributeText = "windowsSoftwareID";
		if (swauditAttributeText != null) {
			if (operation instanceof StringEqualsOperation)
				return new OpsiDataStringEqualsOperation(OpsiDataClient.SWAUDIT_MAP, swauditAttributeText,
						(String) operation.getData(), element);
		}

		// hardware
		if (element instanceof GenericTextElement || element instanceof GenericIntegerElement
				|| element instanceof GenericBigIntegerElement || element instanceof GenericEnumElement) {
			String map = hwUiToOpsi.get(elementPath[0]);
			String attr = getKey(elementPath);
			if (operation instanceof StringEqualsOperation)
				return new OpsiDataStringEqualsOperation(map, attr, (String) data, element);
			if (operation instanceof IntLessThanOperation)
				return new OpsiDataIntLessThanOperation(map, attr, (Integer) data, element);
			if (operation instanceof IntLessOrEqualOperation)
				return new OpsiDataIntLessOrEqualOperation(map, attr, (Integer) data, element);
			if (operation instanceof IntGreaterThanOperation)
				return new OpsiDataIntGreaterThanOperation(map, attr, (Integer) data, element);
			if (operation instanceof IntGreaterOrEqualOperation)
				return new OpsiDataIntGreaterOrEqualOperation(map, attr, (Integer) data, element);
			if (operation instanceof IntEqualsOperation)
				return new OpsiDataIntEqualsOperation(map, attr, (Integer) data, element);
			if (operation instanceof BigIntLessThanOperation)
				return new OpsiDataBigIntLessThanOperation(map, attr, (Long) data, element);
			if (operation instanceof BigIntLessOrEqualOperation)
				return new OpsiDataBigIntLessOrEqualOperation(map, attr, (Long) data, element);
			if (operation instanceof BigIntGreaterThanOperation)
				return new OpsiDataBigIntGreaterThanOperation(map, attr, (Long) data, element);
			if (operation instanceof BigIntGreaterOrEqualOperation)
				return new OpsiDataBigIntGreaterOrEqualOperation(map, attr, (Long) data, element);
			if (operation instanceof BigIntEqualsOperation)
				return new OpsiDataBigIntEqualsOperation(map, attr, (Long) data, element);
		}
		logging.error("IllegalArgument: The operation " + operation + " was not found on " + element);
		throw new IllegalArgumentException("The operation " + operation + " was not found on " + element);
	}

	@Override
	protected SelectGroupOperation createGroupOperation(SelectGroupOperation operation,
			List<SelectOperation> operations) {
		/*
		 * if (operation.equals("Software"))
		 * {
		 * logging.info(this, "IllegalArgument: The group operation " +operation);
		 * System.exit(0);
		 * }
		 */
		if (operation instanceof AndOperation && operations.size() >= 2)
			return new AndOperation(operations);
		if (operation instanceof OrOperation && operations.size() >= 2)
			return new OrOperation(operations);
		if (operation instanceof NotOperation && operations.size() == 1)
			return new NotOperation(operations.get(0));
		// if( operation.equals(de.uib.opsidatamodel.OpsiProduct.NAME) &&
		// operations.size() == 1 )
		if (operation instanceof SoftwareOperation && operations.size() == 1)
			return new OpsiDataSoftwareOperation(operations.get(0));
		if (operation instanceof SwAuditOperation && operations.size() == 1)
			return new OpsiDataSwAuditOperation(operations.get(0));
		if (operation instanceof HardwareOperation && operations.size() == 1)
			return new OpsiDataHardwareOperation(operations.get(0));
		if (operation instanceof HostOperation && operations.size() == 1)
			return new HostOperation(operations.get(0));

		logging.error(this, "IllegalArgument: The group operation " + operation + " was not found with "
				+ operations.size() + " operations");
		throw new IllegalArgumentException(
				"The group operation " + operation + " was not found with " + operations.size() + " operations");

	}

	@Override
	public void setReloadRequested() {
		logging.info(this, "setReloadRequested");
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
		System.gc();
		

	}

	private void checkInitData() {
		logging.info(this, "checkInitData ");

		// gets current data which should be in cache already
		// reloadRequested " + reloadRequested);

		// if (clientMaps == null || reloadRequested)
		// take always the current host infos
		{
			clientMaps = controller.getHostInfoCollections().getMapOfPCInfoMaps();
			logging.info(this, "client maps size " + clientMaps.size());
		}
		

		if (groups == null || reloadRequested) {
			groups = controller.getFObject2Groups();
		}

		if (superGroups == null || reloadRequested) {

			superGroups = controller.getHostInfoCollections().getFNode2Treeparents();
		}

		String[] clientNames = clientMaps.keySet().toArray(new String[0]);

		if (hasSoftware) {
			// if (reloadRequested || softwareMap == null)
			{
				softwareMap = controller.getMapOfProductStatesAndActions(clientNames);
				logging.debug(this, "getClients softwareMap ");
				
			}
		}
		// else
		

		// if ( reloadRequested || swauditMap == null )
		{
			swauditMap = getSwAuditOnClients();
		}
		// else
		

		// if ( reloadRequested || hwConfig == null || hwConfigLocalized == null ||
		// hwUiToOpsi == null || hwClassToValues == null )
		getHardwareConfig();

		logging.debug(this, "getClients hasHardware " + hasHardware);
		if (hasHardware) {
			// if (reloadRequested || hardwareOnClient == null || clientToHardware == null )
			getHardwareOnClient(clientNames);
		} else
			hardwareOnClient = null; // dont use older data after a reload request

		reloadRequested = false;

	}

	@Override
	protected List<Client> getClients() {
		List<Client> clients = new LinkedList<>();

		checkInitData();

		logging.info(this, "getClients hasSoftware " + hasSoftware);
		logging.info(this, "getClients hasHardware " + hasHardware);
		logging.info(this, "getClients hasSoftware " + hasSoftware);
		logging.info(this, "getClients swauditMap != null  " + (swauditMap != null));
		/*
		 * if (swauditMap != null)
		 * logging.info(this, "getClients swauditMap.keySet()   " +
		 * (swauditMap.keySet()) );
		 */

		

		for (String clientName : clientMaps.keySet()) {
			OpsiDataClient client = new OpsiDataClient(clientName);
			client.setInfoMap(clientMaps.get(clientName).getMap());
			if (hasHardware)
				client.setHardwareInfo(clientToHardware.get(clientName));
			if (groups.containsKey(clientName))
				client.setGroups(groups.get(clientName));

			if (superGroups.containsKey(clientName))
				client.setSuperGroups(superGroups.get(clientName));

			if (hasSoftware && softwareMap.containsKey(clientName) && softwareMap.get(clientName) instanceof List)
				client.setOpsiProductList((List) softwareMap.get(clientName));
			if (swauditMap != null && swauditMap.containsKey(clientName))
				client.setSwAuditList(swauditMap.get(clientName));
			clients.add(client);
		}
		return clients;
	}

	@Override
	public List<String> getGroups() {
		return controller.getHostGroupIds();
	}

	@Override
	public TreeSet<String> getProductIDs() {
		return controller.getProductIds();
	}

	@Override
	public Map<String, List<SelectElement>> getHardwareList() {
		Map<String, List<SelectElement>> result = new HashMap<>();

		for (int i = 0; i < hwConfig.size(); i++) {
			Map hardwareMap = (Map) hwConfig.get(i);
			Map hardwareMapLocalized = (Map) hwConfigLocalized.get(i);
			String hardwareName = (String) ((Map) hardwareMap.get("Class")).get("UI");
			String hardwareNameLocalized = (String) ((Map) hardwareMapLocalized.get("Class")).get("UI");
			List<SelectElement> elementList = new LinkedList<>();
			List values = (List) hardwareMap.get("Values");
			List valuesLocalized = (List) hardwareMapLocalized.get("Values");
			for (int j = 0; j < values.size(); j++) {
				Map valuesMap = (Map) values.get(j);
				String type = (String) valuesMap.get("Type");
				String name = (String) valuesMap.get("UI");
				String localizedName = (String) ((Map) valuesLocalized.get(j)).get("UI");
				if (type.equals("int") || type.equals("tinyint"))
					elementList.add(new GenericIntegerElement(new String[] { hardwareName, name },
							new String[] { hardwareNameLocalized, localizedName }));
				else if (type.equals("bigint"))
					elementList.add(new GenericBigIntegerElement(new String[] { hardwareName, name },
							new String[] { hardwareNameLocalized, localizedName }));
				else
					elementList.add(new GenericTextElement(new String[] { hardwareName, name },
							new String[] { hardwareNameLocalized, localizedName }));
			}
			result.put(hardwareName, elementList);
			
			logging.debug(this, "" + elementList);
		}
		return result;
	}

	@Override
	public Map<String, List<SelectElement>> getLocalizedHardwareList() {
		Map<String, List<SelectElement>> result = new HashMap<>();

		for (int i = 0; i < hwConfig.size(); i++) {
			Map hardwareMap = (Map) hwConfig.get(i);
			Map hardwareMapLocalized = (Map) hwConfigLocalized.get(i);
			String hardwareName = (String) ((Map) hardwareMap.get("Class")).get("UI");
			String hardwareNameLocalized = (String) ((Map) hardwareMapLocalized.get("Class")).get("UI");
			List<SelectElement> elementList = new LinkedList<>();
			List values = (List) hardwareMap.get("Values");
			List valuesLocalized = (List) hardwareMapLocalized.get("Values");
			for (int j = 0; j < values.size(); j++) {
				Map valuesMap = (Map) values.get(j);
				String type = (String) valuesMap.get("Type");
				String name = (String) valuesMap.get("UI");
				String localizedName = (String) ((Map) valuesLocalized.get(j)).get("UI");
				if (type.equals("int") || type.equals("tinyint"))
					elementList.add(new GenericIntegerElement(new String[] { hardwareName, name },
							new String[] { hardwareNameLocalized, localizedName }));
				else if (type.equals("bigint"))
					elementList.add(new GenericBigIntegerElement(new String[] { hardwareName, name },
							new String[] { hardwareNameLocalized, localizedName }));
				else
					elementList.add(new GenericTextElement(new String[] { hardwareName, name },
							new String[] { hardwareNameLocalized, localizedName }));
			}
			result.put(hardwareNameLocalized, elementList);
			
			logging.debug(this, "" + elementList);
		}
		return result;
	}

	private String getKey(String[] elementPath) {
		logging.debug(this, elementPath[0]);
		List values = hwClassToValues.get(hwUiToOpsi.get(elementPath[0]));
		if (values != null) {
			for (Object value : values) {
				Map valueMap = (Map) value;
				if (elementPath[1].equals((String) valueMap.get("UI")))
					return (String) valueMap.get("Opsi");
			}
		}
		logging.error(this, "Element not found: " + Arrays.toString(elementPath));
		return "";
	}

	private void getHardwareOnClient(String[] clientNames) {
		hardwareOnClient = controller.getHardwareOnClient();
		clientToHardware = new HashMap<>();
		for (int i = 0; i < clientNames.length; i++)
			clientToHardware.put(clientNames[i], new LinkedList<>());
		for (Map<String, Object> map : hardwareOnClient) {
			String name = (String) map.get(HWAuditClientEntry.hostKEY);
			if (!clientToHardware.containsKey(name)) {
				logging.debug(this, "Non-client hostid: " + name);
				continue;
			}
			clientToHardware.get(name).add(map);
		}
	}

	private Map<String, List<SWAuditClientEntry>> getSwAuditOnClients() {
		Map<String, List<SWAuditClientEntry>> result = new HashMap<>();
		if (!hasSwAudit)
			return result;

		controller.fillClient2Software(new ArrayList<>(clientMaps.keySet()));
		result = controller.getClient2Software();
		/*
		 * for( Object obj: controller.getSoftwareAuditOnClients() )
		 * {
		 * if( !(obj instanceof Map) )
		 * {
		 * logging.warning(this, "SwAudit element is no map: " + obj + " " +
		 * obj.getClass() );
		 * continue;
		 * }
		 * 
		 * Map map = (Map) obj;
		 * String name = (String) map.get("clientId");
		 * if( !result.containsKey(name) )
		 * result.put( name, new LinkedList<>() );
		 * result.get(name).add(map);
		 * }
		 */
		return result;
	}

	private void getHardwareConfig() {
		String locale = Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry();
		logging.debug(this, locale);
		hwConfig = controller.getOpsiHWAuditConf("en_");
		hwConfigLocalized = controller.getOpsiHWAuditConf(locale);
		logging.debug(this, "" + hwConfig);
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