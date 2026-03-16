/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataBigIntLessThanOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataBooleanEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataConnectionEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataDateEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataDateGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataDateGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataDateLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataDateLessThanOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataGroupEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataHardwareOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataIntEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataIntGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataIntGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataIntLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataIntLessThanOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataSoftwareOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataStringEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataSuperGroupEqualsOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiDataSwAuditOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations.OpsiSoftwareEqualsOperation;
import de.uib.configed.gui.features.clientselection.elements.ConnectionElement;
import de.uib.configed.gui.features.clientselection.elements.DescriptionElement;
import de.uib.configed.gui.features.clientselection.elements.GenericBigIntegerElement;
import de.uib.configed.gui.features.clientselection.elements.GenericBooleanElement;
import de.uib.configed.gui.features.clientselection.elements.GenericEnumElement;
import de.uib.configed.gui.features.clientselection.elements.GenericIntegerElement;
import de.uib.configed.gui.features.clientselection.elements.GenericTextElement;
import de.uib.configed.gui.features.clientselection.elements.GroupElement;
import de.uib.configed.gui.features.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.gui.features.clientselection.elements.IPElement;
import de.uib.configed.gui.features.clientselection.elements.NameElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareActionProgressElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareActionResultElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareInstallationStatusElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareLastActionElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareModificationTimeElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareNameElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwarePackageVersionElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareRequestElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareVersionElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditArchitectureElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditLanguageElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditLicenseKeyElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditNameElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditSoftwareIdElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditSubversionElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditVersionElement;
import de.uib.configed.gui.features.clientselection.operations.AndOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntEqualsOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntLessThanOperation;
import de.uib.configed.gui.features.clientselection.operations.BooleanEqualsOperation;
import de.uib.configed.gui.features.clientselection.operations.DateEqualsOperation;
import de.uib.configed.gui.features.clientselection.operations.DateGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.DateGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.operations.DateLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.DateLessThanOperation;
import de.uib.configed.gui.features.clientselection.operations.HardwareOperation;
import de.uib.configed.gui.features.clientselection.operations.HostOperation;
import de.uib.configed.gui.features.clientselection.operations.IntEqualsOperation;
import de.uib.configed.gui.features.clientselection.operations.IntGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.IntGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.operations.IntLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.IntLessThanOperation;
import de.uib.configed.gui.features.clientselection.operations.NotOperation;
import de.uib.configed.gui.features.clientselection.operations.OrOperation;
import de.uib.configed.gui.features.clientselection.operations.SoftwareOperation;
import de.uib.configed.gui.features.clientselection.operations.StringEqualsOperation;
import de.uib.configed.gui.features.clientselection.operations.SwAuditOperation;
import de.uib.configed.gui.features.hwinfopage.PanelHWSingleClientInfo;
import de.uib.configed.gui.messages.Messages;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.gui.type.SWAuditClientEntry;
import de.uib.configed.share.logging.Logging;

public final class OpsiDataBackend {
	/*
	* These variables tell you which data you have to fetch. E.g. if hasSoftware is
	* true, there is an software
	* operation and so you need to get the data about software.
	*/
	private boolean hasSoftware;
	private boolean hasHardware;
	private boolean hasSwAudit;
	private boolean reloadRequested;

	private Set<Class<?>> hardwareElements = Set.of(GenericTextElement.class, GenericIntegerElement.class,
			GenericBigIntegerElement.class, GenericEnumElement.class, GenericBooleanElement.class);

	// data which will be cached
	private Map<String, HostInfo> clientMaps;

	// client -> groups with it
	private Map<String, Set<String>> groups;

	// client -> all groups for which the client belongs to directly or by virtue of some path
	private Map<String, Set<String>> superGroups;
	private Map<String, List<Map<String, String>>> softwareMap;
	private Map<String, List<SWAuditClientEntry>> swauditMap;
	private Map<String, List<Map<String, Object>>> clientToHardware;

	private List<Map<String, Object>> hwConfig;
	private List<Map<String, Object>> hwConfigLocalized;
	private Map<String, String> hwUiToOpsi;
	private Map<String, List<Map<String, Object>>> hwClassToValues;

	private Set<String> clientsConnectedByMessagebus;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public OpsiDataBackend() {
		getHardwareConfig();
	}

	/**
	 * Goes through the list of clients and filters them with operation. The
	 * boolean arguments give hints which data is needed.
	 */
	public Set<String> checkClients(ExecutableOperation operation, boolean hasSoftware, boolean hasHardware,
			boolean hasSwAudit) {
		Logging.debug(this, "Starting the filtering.. , operation ", operation);
		this.hasSoftware = hasSoftware;
		this.hasHardware = hasHardware;
		this.hasSwAudit = hasSwAudit;
		List<OpsiDataClient> clients = getClients();
		Logging.debug(this, "Number of clients to filter: ", clients.size());

		return clients.stream().filter(operation::doesMatch).map(OpsiDataClient::getId).collect(Collectors.toSet());
	}

	/**
	 * This function translates the operations tree with the root operation into
	 * an executable operation tree by replacing the non-executable operations
	 * with their backend-specific executable operations.
	 */
	public ExecutableOperation createExecutableOperation(AbstractSelectOperation operation) {
		Logging.debug(this, "createFromOperationData ", operation.getClassName());

		if (operation instanceof AbstractSelectGroupOperation groupOperation) {
			List<AbstractSelectOperation> children = new ArrayList<>();
			for (AbstractSelectOperation child : groupOperation.getChildOperations()) {
				children.add((AbstractSelectOperation) createExecutableOperation(child));
			}

			return (ExecutableOperation) createGroupOperation(groupOperation, children);
		} else {
			return (ExecutableOperation) createOperation(operation);
		}
	}

	private AbstractSelectOperation createOperation(AbstractSelectOperation operation) {
		Logging.info(this, "createOperation operation, data, element: ", operation.getClassName(), ", ",
				operation.getData(), ",  ", operation.getElement().getClassName());

		// Host
		AbstractSelectElement element = operation.getElement();
		String attributeTextHost = switch (element) {
		case NameElement _ -> HostInfo.HOSTNAME_KEY;
		case IPElement _ -> HostInfo.CLIENT_IP_ADDRESS_KEY;
		case DescriptionElement _ -> HostInfo.CLIENT_DESCRIPTION_KEY;
		default -> null;
		};

		if (attributeTextHost != null) {
			if (operation instanceof StringEqualsOperation) {
				return new OpsiDataStringEqualsOperation(OpsiDataClient.HOSTINFO_MAP, attributeTextHost,
						(String) operation.getData(), element);
			}
			throw new IllegalArgumentException("Wrong operation for this element.");
		}

		switch (element) {
		case ConnectionElement ce -> {
			return new OpsiDataConnectionEqualsOperation((String) operation.getData(), ce);
		}
		case GroupElement ge when operation instanceof StringEqualsOperation -> {
			return new OpsiDataGroupEqualsOperation((String) operation.getData(), ge);
		}
		case GroupWithSubgroupsElement gse when operation instanceof StringEqualsOperation -> {
			return new OpsiDataSuperGroupEqualsOperation((String) operation.getData(), gse);
		}
		default -> {
			// continue
		}
		}

		// Software
		String attributeTextSoftware = switch (element) {
		case SoftwareNameElement _ -> ProductState.KEY_PRODUCT_ID;
		case SoftwareVersionElement _ -> ProductState.KEY_PRODUCT_VERSION;
		case SoftwarePackageVersionElement _ -> ProductState.KEY_PACKAGE_VERSION;
		case SoftwareRequestElement _ -> ProductState.KEY_ACTION_REQUEST;
		case SoftwareInstallationStatusElement _ -> ProductState.KEY_INSTALLATION_STATUS;
		case SoftwareActionProgressElement _ -> ProductState.KEY_ACTION_PROGRESS;
		case SoftwareActionResultElement _ -> ProductState.KEY_ACTION_RESULT;
		case SoftwareLastActionElement _ -> ProductState.KEY_LAST_ACTION;
		case SoftwareModificationTimeElement _ -> ProductState.KEY_LAST_STATE_CHANGE;
		default -> null;
		};

		if (attributeTextSoftware != null) {
			return switch (operation) {
			case StringEqualsOperation _ -> new OpsiSoftwareEqualsOperation(attributeTextSoftware,
					(String) operation.getData(), element);
			case DateEqualsOperation _ -> new OpsiDataDateEqualsOperation(OpsiDataClient.SOFTWARE_MAP,
					attributeTextSoftware, (String) operation.getData(), element);
			case DateLessThanOperation _ -> new OpsiDataDateLessThanOperation(OpsiDataClient.SOFTWARE_MAP,
					attributeTextSoftware, (String) operation.getData(), element);
			case DateLessOrEqualOperation _ -> new OpsiDataDateLessOrEqualOperation(OpsiDataClient.SOFTWARE_MAP,
					attributeTextSoftware, (String) operation.getData(), element);
			case DateGreaterThanOperation _ -> new OpsiDataDateGreaterThanOperation(OpsiDataClient.SOFTWARE_MAP,
					attributeTextSoftware, (String) operation.getData(), element);
			case DateGreaterOrEqualOperation _ -> new OpsiDataDateGreaterOrEqualOperation(OpsiDataClient.SOFTWARE_MAP,
					attributeTextSoftware, (String) operation.getData(), element);
			default -> throw new IllegalArgumentException("Wrong operation for this element.");
			};
		}

		// SwAudit
		String swauditAttributeText = switch (element) {
		case SwAuditArchitectureElement _ -> "architecture";
		case SwAuditLanguageElement _ -> "language";
		case SwAuditLicenseKeyElement _ -> "licenseKey";
		case SwAuditNameElement _ -> "name";
		case SwAuditVersionElement _ -> "version";
		case SwAuditSubversionElement _ -> "subVersion";
		case SwAuditSoftwareIdElement _ -> "windowsSoftwareID";
		default -> null;
		};

		if (swauditAttributeText != null && operation instanceof StringEqualsOperation) {
			return new OpsiDataStringEqualsOperation(OpsiDataClient.SWAUDIT_MAP, swauditAttributeText,
					(String) operation.getData(), element);
		}

		String[] elementPath = element.getPathArray();
		Object data = operation.getData();

		// hardware
		if (hardwareElements.contains(element.getClass())) {
			String map = hwUiToOpsi.get(elementPath[0]);
			String attr = getKey(elementPath);

			return switch (operation) {
			case StringEqualsOperation _ -> new OpsiDataStringEqualsOperation(map, attr, (String) data, element);
			case IntLessThanOperation _ -> new OpsiDataIntLessThanOperation(map, attr, (Integer) data, element);
			case IntLessOrEqualOperation _ -> new OpsiDataIntLessOrEqualOperation(map, attr, (Integer) data, element);
			case IntGreaterThanOperation _ -> new OpsiDataIntGreaterThanOperation(map, attr, (Integer) data, element);
			case IntGreaterOrEqualOperation _ -> new OpsiDataIntGreaterOrEqualOperation(map, attr, (Integer) data,
					element);
			case IntEqualsOperation _ -> new OpsiDataIntEqualsOperation(map, attr, (Integer) data, element);
			case BigIntLessThanOperation _ -> new OpsiDataBigIntLessThanOperation(map, attr, (Long) data, element);
			case BigIntLessOrEqualOperation _ -> new OpsiDataBigIntLessOrEqualOperation(map, attr, (Long) data,
					element);
			case BigIntGreaterThanOperation _ -> new OpsiDataBigIntGreaterThanOperation(map, attr, (Long) data,
					element);
			case BigIntGreaterOrEqualOperation _ -> new OpsiDataBigIntGreaterOrEqualOperation(map, attr, (Long) data,
					element);
			case BigIntEqualsOperation _ -> new OpsiDataBigIntEqualsOperation(map, attr, (Long) data, element);
			case BooleanEqualsOperation _ -> new OpsiDataBooleanEqualsOperation(map, attr, (Boolean) data, element);
			default -> {
				// do nothing here
				Logging.error("IllegalArgument: The operation ", operation, " was not found on ", element);
				throw new IllegalArgumentException("The operation " + operation + " was not found on " + element);
			}
			};
		}
		Logging.error("IllegalArgument: The operation ", operation, " was not found on ", element);
		throw new IllegalArgumentException("The operation " + operation + " was not found on " + element);
	}

	private AbstractSelectGroupOperation createGroupOperation(AbstractSelectGroupOperation operation,
			List<AbstractSelectOperation> operations) {
		int size = operations.size();

		return switch (operation) {
		case AndOperation _ when size >= 2 -> new AndOperation(operations);
		case OrOperation _ when size >= 2 -> new OrOperation(operations);
		case NotOperation _ when size == 1 -> new NotOperation(operations.get(0));
		case SoftwareOperation _ when size == 1 -> new OpsiDataSoftwareOperation(operations.get(0));
		case SwAuditOperation _ when size == 1 -> new OpsiDataSwAuditOperation(operations.get(0));
		case HardwareOperation _ when size == 1 -> new OpsiDataHardwareOperation(operations.get(0));
		case HostOperation _ when size == 1 -> new HostOperation(operations.get(0));
		default -> unsupportedOperation(operation, size);
		};
	}

	private AbstractSelectGroupOperation unsupportedOperation(AbstractSelectGroupOperation operation, int size) {
		String errorMessage = "The operation " + operation + " with " + size + " operations is not supported.";
		Logging.error(this, errorMessage);
		throw new IllegalArgumentException(errorMessage);
	}

	public void setReloadRequested() {
		Logging.info(this, "setReloadRequested");
		reloadRequested = true;

		clientMaps = null;
		groups = null;
		superGroups = null;
		softwareMap = null;
		persistenceController.reloadData(ReloadEvent.PRODUCT_DATA_RELOAD.toString());

		swauditMap = null;
		persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());

		clientToHardware = null;
		persistenceController.reloadData(ReloadEvent.DEPOT_CHANGE_RELOAD.toString());
	}

	private void checkInitData() {
		Logging.info(this, "checkInitData ");

		// gets current data which should be in cache already

		// take always the current host infos

		clientMaps = persistenceController.getDataServices().hostInfoCollections.getMapOfPCInfoMaps();
		Logging.info(this, "client maps size ", clientMaps.size());

		if (groups == null || reloadRequested) {
			groups = persistenceController.getDataServices().group.getFObject2GroupsPD();
		}

		if (superGroups == null || reloadRequested) {
			superGroups = persistenceController.getDataServices().hostInfoCollections.getFNode2TreeparentsPD();
		}

		if (clientsConnectedByMessagebus == null || reloadRequested) {
			clientsConnectedByMessagebus = persistenceController.getDataServices().host.getMessagebusConnectedClients();
		}

		Set<String> clientNames = clientMaps.keySet();

		if (hasSoftware) {
			softwareMap = persistenceController.getDataServices().product.getMapOfProductStatesAndActions(clientNames);
			Logging.debug(this, "getClients softwareMap ");
		}

		if (hasSwAudit) {
			swauditMap = persistenceController.getDataServices().software.getSoftwareAuditOnClients(clientNames);
		}

		Logging.debug(this, "getClients hasHardware ", hasHardware);
		if (hasHardware) {
			getHardwareConfig();
			clientToHardware = persistenceController.getDataServices().hardware.getHardwareAuditOnClients(clientNames);
		}

		reloadRequested = false;
	}

	private List<OpsiDataClient> getClients() {
		List<OpsiDataClient> clients = new ArrayList<>();

		checkInitData();

		Logging.info(this, "getClients hasSoftware ", hasSoftware);
		Logging.info(this, "getClients hasHardware ", hasHardware);
		Logging.info(this, "getClients hasSoftware ", hasSoftware);
		Logging.info(this, "getClients swauditMap != null  ", swauditMap != null);

		for (Entry<String, HostInfo> clientEntry : clientMaps.entrySet()) {
			OpsiDataClient client = new OpsiDataClient(clientEntry.getKey());
			client.setConnectedByMessagebus(clientsConnectedByMessagebus);
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

	public Map<String, List<AbstractSelectElement>> getLocalizedHardwareList() {
		return getHardwareList(true);
	}

	public Map<String, List<AbstractSelectElement>> getHardwareList() {
		return getHardwareList(false);
	}

	private Map<String, List<AbstractSelectElement>> getHardwareList(boolean localized) {
		Map<String, List<AbstractSelectElement>> result = new HashMap<>();

		for (int i = 0; i < hwConfig.size(); i++) {
			Map<String, Object> hardwareMap = hwConfig.get(i);
			Map<String, Object> hardwareMapLocalized = hwConfigLocalized.get(i);
			String hardwareName = (String) Map.class.cast(hardwareMap.get("Class")).get("UI");
			String hardwareNameLocalized = (String) Map.class.cast(hardwareMapLocalized.get("Class")).get("UI");
			List<AbstractSelectElement> elementList = new ArrayList<>();
			List<Map<String, Object>> values = POJOReMapper.remap(hardwareMap.get("Values"));
			List<Map<String, Object>> valuesLocalized = POJOReMapper.remap(hardwareMapLocalized.get("Values"));
			for (int j = 0; j < values.size(); j++) {
				Map<String, Object> valuesMap = values.get(j);
				String type = (String) valuesMap.get("Type");
				String name = (String) valuesMap.get("UI");
				String localizedName = (String) valuesLocalized.get(j).get("UI");
				if (PanelHWSingleClientInfo.BOOLEAN_VALUES.contains(valuesMap.get("Opsi"))) {
					elementList.add(new GenericBooleanElement(new String[] { hardwareName, name },
							hardwareNameLocalized, localizedName));
				} else if ("int".equals(type) || "tinyint".equals(type)) {
					elementList.add(new GenericIntegerElement(new String[] { hardwareName, name },
							hardwareNameLocalized, localizedName));
				} else if ("bigint".equals(type)) {
					elementList.add(new GenericBigIntegerElement(new String[] { hardwareName, name },
							hardwareNameLocalized, localizedName));
				} else {
					elementList.add(new GenericTextElement(new String[] { hardwareName, name }, hardwareNameLocalized,
							localizedName));
				}
			}
			if (localized) {
				result.put(hardwareNameLocalized, elementList);
			} else {
				result.put(hardwareName, elementList);
			}

			Logging.debug(this, "", elementList);
		}
		return result;
	}

	private String getKey(String[] elementPath) {
		Logging.debug(this, elementPath[0]);
		List<Map<String, Object>> values = hwClassToValues.get(hwUiToOpsi.get(elementPath[0]));
		if (values != null) {
			for (Map<String, Object> valueMap : values) {
				if (elementPath[1].equals(valueMap.get("UI"))) {
					return (String) valueMap.get("Opsi");
				}
			}
		}
		Logging.error(this, "Element not found: ", Arrays.toString(elementPath));
		return "";
	}

	private void getHardwareConfig() {
		String locale = Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry();
		Logging.debug(this, locale);
		hwConfig = persistenceController.getDataServices().hardware.getOpsiHWAuditConfPD("en_");
		hwConfigLocalized = persistenceController.getDataServices().hardware.getOpsiHWAuditConfPD(locale);
		Logging.debug(this, "", hwConfig);
		hwUiToOpsi = new HashMap<>();
		hwClassToValues = new HashMap<>();

		for (Map<String, Object> hardwareMap : hwConfig) {
			String hardwareName = (String) Map.class.cast(hardwareMap.get("Class")).get("UI");
			String hardwareOpsi = (String) Map.class.cast(hardwareMap.get("Class")).get("Opsi");
			List<Map<String, Object>> values = POJOReMapper.remap(hardwareMap.get("Values"));
			hwUiToOpsi.put(hardwareName, hardwareOpsi);
			hwClassToValues.put(hardwareOpsi, values);
		}
	}
}
