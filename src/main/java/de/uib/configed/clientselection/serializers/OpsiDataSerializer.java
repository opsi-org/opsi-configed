/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.serializers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectData.DataType;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.clientselection.elements.GroupElement;
import de.uib.configed.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.clientselection.operations.AndOperation;
import de.uib.configed.clientselection.operations.HardwareOperation;
import de.uib.configed.clientselection.operations.HostOperation;
import de.uib.configed.clientselection.operations.NotOperation;
import de.uib.configed.clientselection.operations.OrOperation;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.SavedSearch;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class OpsiDataSerializer {
	public static final int DATA_VERSION = 2;

	public static final String ELEMENT_NAME_GROUP = "GroupElement";
	public static final String ELEMENT_NAME_GROUP_WITH_SUBGROUPS = "GroupWithSubgroupsElement";
	public static final String ELEMENT_NAME_SOFTWARE_NAME_ELEMENT = "SoftwareNameElement";
	public static final String ELEMENT_NAME_GENERIC = "Generic";

	public static final String KEY_ELEMENT_NAME = "element";
	public static final String KEY_SUBELEMENT_NAME = "refinedElement";
	public static final String KEY_ELEMENT_PATH = "elementPath";
	public static final String KEY_OPERATION = "operation";
	public static final String KEY_DATA_TYPE = "dataType";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private SelectionManager manager;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private SelectData.DataType lastDataType;
	private Map<String, String> searches;
	private int searchDataVersion;

	public OpsiDataSerializer(SelectionManager manager) {
		this.manager = manager;
		searches = new HashMap<>();
		searchDataVersion = DATA_VERSION;
		objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
	}

	/**
	 * Save the given tree of operations under the given name. If the name
	 * already exists, overwrite it.
	 */
	public void save(AbstractSelectOperation topOperation, String name, String description) {
		Map<String, Object> data = produceData(topOperation);
		Logging.info(this, "save data ", data);
		saveData(name, description, data);
	}

	/**
	 * Get a list of the names of all saved searches.
	 */
	public Set<String> getSaved() {
		Set<String> set = new TreeSet<>();
		set.addAll(searches.keySet());
		set.addAll(persistenceController.getConfigDataService().getSavedSearchesPD().keySet());
		return set;
	}

	/**
	 * Remove a saved search from the server
	 */
	public void remove(String name) {
		searches.remove(name);
	}

	/**
	 * reproduce a search
	 */
	public AbstractSelectOperation deserialize(Map<String, Object> data) {
		if (data == null) {
			Logging.warning(this, "data in Serializer.deserialize is null");
			return null;
		}

		Logging.info(this, "deserialize data ", data);
		if (data.get(KEY_ELEMENT_PATH) != null) {
			Logging.info("deserialize, elementPath ", (List<String>) data.get(KEY_ELEMENT_PATH));
		}

		try {
			AbstractSelectOperation operation = getOperation(data, null);
			if (getSearchDataVersion() == 1) {
				operation = checkForHostGroup(operation);
			}
			return operation;
		} catch (Exception e) {
			Logging.error(e, "deserialize error for data ", data, " message ", e.getMessage());
			return null;
		}
	}

	/**
	 * reproduce a search from a serialization string
	 */

	public AbstractSelectOperation deserialize(String serialized) {
		Logging.info(this, "deserialize serialized ", serialized);
		AbstractSelectOperation result = null;

		Map<String, Object> data = decipher(serialized);
		result = deserialize(data);

		return result;
	}

	/**
	 * Get one search from searches map
	 */
	public AbstractSelectOperation load(String name) {
		Logging.info(this, "load ", name);
		Map<String, Object> data = getData(name);
		return deserialize(data);
	}

	/**
	 * produce map format of serializiation object
	 */
	private Map<String, Object> decipher(String serialization) {
		try {
			return parseAndExtractData(serialization);
		} catch (IOException originalEx) {
			Logging.warning(this,
					"Failed to parse JSON (probably old saved search). Possibly due to unquoted 'dataType' field. Retrying with fix. Original error: ",
					originalEx.getMessage());

			String fixed = serialization.replaceAll("(\"dataType\"\\s*:\\s*)(\\w+)", "$1\"$2\"");

			try {
				return parseAndExtractData(fixed);
			} catch (IOException retryEx) {
				Logging.error(this, retryEx, "Retry also failed when parsing fixed JSON. Original error: ",
						originalEx.getMessage(), " | Retry error: ", retryEx.getMessage());
				return new HashMap<>();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseAndExtractData(String json) throws IOException {
		Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
		});
		searchDataVersion = Integer.parseInt(String.valueOf(map.getOrDefault("version", 1)));
		Object data = map.get("data");
		return (data instanceof Map) ? (Map<String, Object>) data : new HashMap<>();
	}

	/** Get the data for the given saved search */
	private Map<String, Object> getData(String name) {
		// we take version from server and not the (possibly edited own version! )
		searches.put(name,
				persistenceController.getConfigDataService().getSavedSearchesPD().get(name).getSerialization());

		// controller.getSavedSearches().get(name)

		String serialization = searches.get(name);
		return decipher(serialization);
	}

	/** Save the search data with the given name. */
	private void saveData(String name, String description, Map<String, Object> data) {
		try {
			Map<String, Object> wrapper = new HashMap<>();
			wrapper.put("version", DATA_VERSION);
			wrapper.put("data", data);
			String jsonString = objectMapper.writeValueAsString(wrapper);

			Logging.info(this, name, ": ", jsonString);
			searches.put(name, jsonString);
			SavedSearch saveObj = new SavedSearch(name, jsonString, description);
			persistenceController.getConfigDataService().saveSearch(saveObj);
		} catch (IOException e) {
			Logging.error(this, e, e.getMessage());
		}
	}

	/** Get the data version of the currently loaded saved search */
	private int getSearchDataVersion() {
		return searchDataVersion;
	}

	public static String createJsonRecursive(Map<?, ?> objects) {
		try {
			return objectMapper.writeValueAsString(objects);
		} catch (IOException e) {
			Logging.error(OpsiDataSerializer.class, e, "Error serializing map to JSON: ", e.getMessage());
			return "{}";
		}
	}

	private void checkLastDataType(String value) {
		if (value == null || "null".equals(value)) {
			return;
		}

		switch (value) {
		// In old searches, we still have "EnumType", but this will now
		// due to refactoring be replaced by "TextType"
		case "TextType", "EnumType":
			lastDataType = DataType.TEXT_TYPE;
			break;

		case "IntegerType":
			lastDataType = DataType.INTEGER_TYPE;
			break;

		case "BigIntegerType":
			lastDataType = DataType.BIG_INTEGER_TYPE;
			break;

		case "DoubleType":
			lastDataType = DataType.DOUBLE_TYPE;
			break;

		case "DateType":
			lastDataType = DataType.DATE_TYPE;
			break;

		case "NoneType":
			lastDataType = DataType.NONE_TYPE;
			break;

		default:
			Logging.error(this, "dataType for ", value, " cannot be found...)");
			break;
		}
	}

	private static Object convertData(String data, DataType dataType) {
		if (data == null || dataType == null) {
			return null;
		}

		return switch (dataType) {
		case NONE_TYPE -> null;
		case TEXT_TYPE, DATE_TYPE -> data;
		case DOUBLE_TYPE -> Double.valueOf(data);
		case INTEGER_TYPE -> Integer.valueOf(data);
		case BIG_INTEGER_TYPE -> Long.valueOf(data);
		default -> throw new IllegalArgumentException("Type " + dataType + " not expected here");
		};
	}

	/*
	 * Create a SelectOperation from the given data. This function works
	 * recursively.
	 */
	private AbstractSelectOperation getOperation(Map<String, Object> data,
			Map<String, List<AbstractSelectElement>> hardware) throws Exception {
		Logging.info(this, "getOperation for map ", data, "; hardware ", hardware);

		String elementPathS = null;
		if (data.get(KEY_ELEMENT_PATH) != null) {
			elementPathS = ((List<String>) data.get(KEY_ELEMENT_PATH)).toString();
			Logging.info(this, "getOperation, elementPath in data ", elementPathS);
		}
		// Element
		AbstractSelectElement element = getSelectElement(data, hardware, elementPathS);

		// Children
		List<Map<String, Object>> childrenData = (List<Map<String, Object>>) data.get("children");
		List<AbstractSelectOperation> children = new LinkedList<>();
		if (childrenData != null) {
			for (Map<String, Object> child : childrenData) {
				children.add(getOperation(child, hardware));
			}
		}

		// Operation
		String operationName = (String) data.get(KEY_OPERATION);
		Logging.info(this, "getOperation Operation name: ", operationName);
		AbstractSelectOperation operation;

		if (getSearchDataVersion() == 1) {
			operation = parseOperationVersion1(operationName, element, children);
		} else {
			Class<?> operationClass = Class.forName("de.uib.configed.clientselection.operations." + operationName);
			Logging.info(this, "getOperation operationClass  ", operationClass.toString());
			if (element != null) {
				Logging.info(this, "getOperation element != null, element  ", element);
				operation = (AbstractSelectOperation) operationClass.getConstructors()[0].newInstance(element);
			} else if (children.size() == 1) {
				Class<?> list = Class.forName("de.uib.configed.clientselection.AbstractSelectOperation");
				Logging.info(this, "getOperation List name: ", list);
				operation = (AbstractSelectOperation) operationClass.getConstructor(list).newInstance(children.get(0));
			} else {
				Class<?> list = Class.forName("java.util.List");
				Logging.info(this, "getOperation List name: ", list);
				operation = (AbstractSelectOperation) operationClass.getConstructor(list).newInstance(children);
			}
		}

		Logging.info(this, "getOperation  ", operation);

		String dataTypeStr = (String) data.get(KEY_DATA_TYPE);
		checkLastDataType(dataTypeStr);
		Object realData = data.get("data");
		Logging.info(this, "getOperation realData ", realData);

		SelectData selectData = null;
		if (dataTypeStr != null && realData != null) {
			Object convertedData = convertData(realData.toString(), lastDataType);
			selectData = new SelectData(convertedData, lastDataType);
		}
		operation.setSelectData(selectData);

		return operation;
	}

	private AbstractSelectElement getSelectElement(Map<String, Object> data,
			Map<String, List<AbstractSelectElement>> hardware, String elementPathS) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		AbstractSelectElement element = null;
		String elementName = (String) data.get(KEY_ELEMENT_NAME);
		Logging.info(this, "Element name: ", elementName);

		if (elementName != null && !(elementName.isEmpty())) {
			String subelementName = (String) data.get(KEY_SUBELEMENT_NAME);

			List<String> elementPath = (List<String>) data.get(KEY_ELEMENT_PATH);

			element = switch (elementName) {
			case ELEMENT_NAME_SOFTWARE_NAME_ELEMENT -> manager.getNewSoftwareNameElement();
			case ELEMENT_NAME_GROUP_WITH_SUBGROUPS -> new GroupWithSubgroupsElement(
					persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]));
			case ELEMENT_NAME_GROUP -> getGroupElement(subelementName);
			default -> getDefaultElement(elementName, hardware, elementPath, elementPathS);
			};
		}

		if (element != null) {
			Logging.info(this, "getOperation element ", element, " class ", element.getClass(), " path ",
					element.getPath());
		}
		return element;
	}

	private AbstractSelectElement getDefaultElement(String elementName,
			Map<String, List<AbstractSelectElement>> hardware, List<String> elementPath, String elementPathS)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		if (elementName.startsWith(ELEMENT_NAME_GENERIC)) {
			return getGeneriSelectElement(elementName, elementPath, hardware, elementPathS);
		} else {
			return (AbstractSelectElement) Class.forName("de.uib.configed.clientselection.elements." + elementName)
					.getDeclaredConstructor().newInstance();
		}
	}

	private AbstractSelectElement getGeneriSelectElement(String elementName, List<String> elementPath,
			Map<String, List<AbstractSelectElement>> hardware, String elementPathS) {
		Logging.info(this, "getGeneriSelectElement elementName ", elementName, " elementPathS ", elementPathS);
		if (hardware == null) {
			hardware = manager.getBackend().getHardwareList();
		}
		Logging.info(this, "getOperation elementPath[0] ", elementPath.get(0));
		List<AbstractSelectElement> elements = hardware.get(elementPath.get(0));

		for (AbstractSelectElement possibleElement : elements) {
			Logging.info(this, "getOperation possibleElement.getClassName() ", possibleElement,
					" compare with elementName ", elementName, " or perhaps with elementPathS ", elementPathS);

			// originally, but is nonsense -------------------------------------------
			if (possibleElement.getClassName().equals(elementName)
					&& Arrays.toString(possibleElement.getPathArray()).equals(elementPathS)) {
				return possibleElement;
			}
		}

		return null;
	}

	private AbstractSelectElement getGroupElement(String subelementName) {
		Logging.info(this, "getGroupElement subelementName ", subelementName);
		if (subelementName != null && subelementName.equals(ELEMENT_NAME_GROUP_WITH_SUBGROUPS)) {
			return new GroupWithSubgroupsElement(
					persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]));
		} else {
			return new GroupElement(
					persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]));
		}
	}

	/* Create data from the operation recursively. */
	private Map<String, Object> produceData(AbstractSelectOperation operation) {
		Map<String, Object> map = new HashMap<>();
		AbstractSelectElement element = operation.getElement();
		if (element == null) {
			map.put(KEY_ELEMENT_NAME, null);
			map.put(KEY_ELEMENT_PATH, null);
		} else if (element instanceof GroupWithSubgroupsElement) {
			// producing compatibility for version without GroupWithSubgroupsElement

			map.put(KEY_ELEMENT_NAME, GroupElement.class.getSimpleName());
			map.put(KEY_SUBELEMENT_NAME, GroupWithSubgroupsElement.class.getSimpleName());
			map.put(KEY_ELEMENT_PATH, element.getPathArray());
		} else {
			map.put(KEY_ELEMENT_NAME, element.getClassName());
			map.put(KEY_ELEMENT_PATH, element.getPathArray());
		}

		map.put(KEY_OPERATION, operation.getClassName());
		if (operation.getSelectData() == null) {
			map.put(KEY_DATA_TYPE, null);
			map.put("data", null);
		} else {
			map.put(KEY_DATA_TYPE, operation.getSelectData().getType());
			map.put("data", operation.getSelectData().getData());
		}
		if (operation instanceof AbstractSelectGroupOperation abstractSelectGroupOperation) {
			List<Map<String, Object>> childData = new LinkedList<>();
			for (AbstractSelectOperation child : abstractSelectGroupOperation.getChildOperations()) {
				childData.add(produceData(child));
			}

			map.put("children", childData);
		} else {
			map.put("children", null);
		}
		Logging.info(this, "produced ", map);
		return map;
	}

	/* Parse the operations with the old (version 1) operation names */
	private AbstractSelectOperation parseOperationVersion1(String name, AbstractSelectElement element,
			List<AbstractSelectOperation> children) {
		Logging.info(this, "parseOperationVersion1");

		if (element != null) {
			for (AbstractSelectOperation operation : element.supportedOperations()) {
				if (operation.getOperationString().equals(name)) {
					return operation;
				}
			}
			throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
		}

		return switch (name) {
		case "Hardware" -> new HardwareOperation(children.get(0));
		case "Software" -> new SoftwareOperation(children.get(0));
		case "SwAudit" -> new SwAuditOperation(children.get(0));
		case "and" -> new AndOperation(children);
		case "or" -> new OrOperation(children);
		case "not" -> new NotOperation(children.get(0));
		default -> throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
		};
	}

	/*
	 * Needed for version 1 data. Adds HostOperations, as they didn't exist in
	 * version 1
	 */
	private static AbstractSelectOperation checkForHostGroup(AbstractSelectOperation operation) {
		if (!(operation instanceof AbstractSelectGroupOperation)) {
			Logging.debug("No group: ", operation.getClassName(), ", element path size: ",
					operation.getElement().getPathArray().length);
			if (operation.getElement().getPathArray().length == 1) {
				return new HostOperation(operation);
			} else {
				return operation;
			}
		}
		if (operation instanceof HardwareOperation || operation instanceof SoftwareOperation
				|| operation instanceof SwAuditOperation) {
			return operation;
		}

		if (!(operation instanceof AndOperation)) {
			return new HostOperation(operation);
		}

		AndOperation andOperation = (AndOperation) operation;
		AbstractSelectOperation notGroup = operation;
		while (notGroup instanceof AbstractSelectGroupOperation abstractSelectGroupOperation) {
			notGroup = abstractSelectGroupOperation.getChildOperations().get(0);
		}

		AbstractSelectOperation leftNotGroup = andOperation.getChildOperations().get(1);
		while (leftNotGroup instanceof AbstractSelectGroupOperation abstractSelectGroupOperation) {
			leftNotGroup = abstractSelectGroupOperation.getChildOperations().get(0);
		}

		if (notGroup.getElement().getPathArray().length != 1) {
			return operation;
		}

		if (notGroup.getElement().getPathArray().length == 1 && leftNotGroup.getElement().getPathArray().length == 1) {
			return new HostOperation(andOperation);
		}

		List<AbstractSelectOperation> ops = andOperation.getChildOperations();
		HostOperation host = new HostOperation(ops.get(0));
		ops.remove(0);
		ops.add(0, host);
		return new AndOperation(ops);
	}
}
