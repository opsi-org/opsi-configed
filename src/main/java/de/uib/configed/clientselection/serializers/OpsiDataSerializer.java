/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.serializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.uib.utilities.logging.Logging;

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

	private SelectionManager manager;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private JsonParser parser;
	private SelectData.DataType lastDataType;
	private Map<String, String> searches;
	private int searchDataVersion;

	public OpsiDataSerializer(SelectionManager manager) {
		this.manager = manager;
		searches = new HashMap<>();
		searchDataVersion = DATA_VERSION;
	}

	/**
	 * Save the given tree of operations under the given name. If the name
	 * already exists, overwrite it.
	 */
	public void save(AbstractSelectOperation topOperation, String name, String description) {
		Map<String, Object> data = produceData(topOperation);
		Logging.info(this, "save data " + data);
		saveData(name, description, data);
	}

	/**
	 * Get a list of the names of all saved searches.
	 */
	public List<String> getSaved() {
		Set<String> set = new HashSet<>();
		set.addAll(searches.keySet());
		set.addAll(persistenceController.getConfigDataService().getSavedSearchesPD().keySet());
		return new LinkedList<>(set);
	}

	/**
	 * Remove a saved search from the server
	 */
	public void remove(String name) {
		if (searches.containsKey(name)) {
			searches.remove(name);
		}
		// do something with the controller
	}

	public String getJson(AbstractSelectOperation topOperation) {
		Map<String, Object> data = produceData(topOperation);

		String jsonString;

		jsonString = "{ \"version\" : \"" + OpsiDataSerializer.DATA_VERSION + "\", \"data\" : ";
		jsonString += OpsiDataSerializer.createJsonRecursive(data);
		jsonString += " }";

		return jsonString;
	}

	/**
	 * reproduce a search
	 */
	public AbstractSelectOperation deserialize(Map<String, Object> data) {
		if (data == null) {
			Logging.warning(this, "data in Serializer.deserialize is null");
			return null;
		}

		Logging.info(this, "deserialize data " + data);
		if (data.get(KEY_ELEMENT_PATH) != null) {
			Logging.info("deserialize, elementPath " + Arrays.toString((String[]) data.get(KEY_ELEMENT_PATH)));
		}

		try {
			AbstractSelectOperation operation = getOperation(data, null);
			if (getSearchDataVersion() == 1) {
				operation = checkForHostGroup(operation);
			}
			return operation;
		} catch (Exception e) {
			Logging.error("deserialize error for data " + data + " message " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * reproduce a search from a serialization string
	 */

	public AbstractSelectOperation deserialize(String serialized) {
		Logging.info(this, "deserialize serialized " + serialized);
		AbstractSelectOperation result = null;

		Map<String, Object> data = decipher(serialized);
		result = deserialize(data);

		return result;
	}

	/**
	 * Get one search from searches map
	 */
	public AbstractSelectOperation load(String name) {
		Logging.info(this, "load " + name);

		Map<String, Object> data = getData(name);
		return deserialize(data);
	}

	/**
	 * produce map format of serializiation object
	 */
	private Map<String, Object> decipher(String serialization) {
		Map<String, Object> map = new HashMap<>();
		parser = new JsonParser(serialization);
		try {
			if (!parser.next() || parser.getPositionType() != JsonParser.PositionType.OBJECT_BEGIN) {
				return map;
			}
		} catch (IOException e) {
			Logging.error(this, e.getMessage(), e);
			return map;
		}
		map = parseObject();
		int version;
		if (!map.containsKey("version")) {
			version = 1;
		} else {
			version = Integer.valueOf((String) map.get("version"));
		}
		searchDataVersion = version;
		return (Map<String, Object>) map.get("data");
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
		String jsonString;

		jsonString = "{ \"version\" : \"" + DATA_VERSION + "\", \"data\" : ";
		jsonString += createJsonRecursive(data);
		jsonString += " }";

		Logging.info(this, name + ": " + jsonString);
		searches.put(name, jsonString);
		SavedSearch saveObj = new SavedSearch(name, jsonString, description);
		persistenceController.getConfigDataService().saveSearch(saveObj);
	}

	/** Get the data version of the currently loaded saved search */
	private int getSearchDataVersion() {
		return searchDataVersion;
	}

	private static String objectToString(Object object) {
		if (object == null) {
			return "null";
		}

		if (object instanceof String) {
			return "\"" + object + "\"";
		}

		if (object instanceof Integer) {
			return "\"" + object + "\"";
		}

		if (object instanceof Long) {
			return "\"" + object + "\"";
		}

		if (object instanceof SelectData.DataType) {
			return object.toString();
		}

		if (object instanceof String[]) {
			StringBuilder result = new StringBuilder("[ ");
			String[] data = (String[]) object;
			for (int i = 0; i < data.length - 1; i++) {
				result.append(objectToString(data[i]));
				result.append(", ");
			}
			return result + objectToString(data[data.length - 1]) + " ]";
		}
		throw new IllegalArgumentException("Unknown type");
	}

	public static String createJsonRecursive(Map<?, ?> objects) {
		StringBuilder builder = new StringBuilder(255);
		builder.append("{ ");
		builder.append("\"element\" : ");

		builder.append(objectToString(objects.get("element")));
		builder.append(", ");

		// compatibility with refinements
		if (objects.containsKey(KEY_SUBELEMENT_NAME)) {
			builder.append("\"");
			builder.append(KEY_SUBELEMENT_NAME);
			builder.append("\" : ");
			builder.append(objectToString(objects.get(KEY_SUBELEMENT_NAME)));
			builder.append(", ");
		}

		builder.append("\"elementPath\" : ");
		builder.append(objectToString(objects.get("elementPath")));
		builder.append(", \"operation\" : ");
		builder.append(objectToString(objects.get("operation")));
		builder.append(", \"dataType\" : ");
		builder.append(objectToString(objects.get("dataType")));
		builder.append(", \"data\" : ");
		builder.append(objectToString(objects.get("data")));
		builder.append(", \"children\" : ");
		List<?> children = (List<?>) objects.get("children");
		if (children == null) {
			builder.append("null");
		} else {
			builder.append("[ ");
			Iterator<?> childIterator = children.iterator();
			while (childIterator.hasNext()) {
				Object child = childIterator.next();
				if (child instanceof Map) {
					builder.append(createJsonRecursive((Map<?, ?>) child));

					if (childIterator.hasNext()) {
						builder.append(", ");
					}
				} else {
					Logging.warning("child is not a map, but " + child.getClass());
				}
			}
			builder.append(" ]");
		}
		builder.append(" }");

		return builder.toString();
	}

	private Map<String, Object> parseObject() {
		Map<String, Object> result = new HashMap<>();
		String name = null;

		try {
			while (parser.next()) {
				switch (parser.getPositionType()) {
				case OBJECT_BEGIN:
					addObjectToResult(result, name);
					break;
				case OBJECT_END:
					return result;
				case LIST_BEGIN:
					addListToResult(result, name);
					break;
				case JSON_NAME:
					name = parser.getValue();
					name = name.substring(1, name.length() - 1);
					Logging.debug(this, name);
					break;
				case JSON_VALUE:
					addValueToResult(result, name);
					break;
				default:
					throw new IllegalArgumentException("Type " + parser.getPositionType() + " not expected here");
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("IOException in parser", e);
		}

		throw new IllegalArgumentException("Reached EOF");
	}

	private Object parseList(String name) {
		List<Object> list = new LinkedList<>();
		boolean done = false;

		try {
			while (!done && parser.next()) {
				switch (parser.getPositionType()) {
				case LIST_END:
					done = true;
					break;
				case OBJECT_BEGIN:
					list.add(parseObject());
					break;
				case JSON_VALUE:
					list.add(stringToObject(parser.getValue(), ""));
					break;
				default:
					throw new IllegalArgumentException("Type " + parser.getPositionType() + " not expected here");
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("IOException in parser", e);
		}

		Logging.debug(this, "parseList " + list);

		if (!done) {
			throw new IllegalArgumentException("Unexpected EOF");
		}

		if ("elementPath".equals(name)) {
			return list.toArray(new String[0]);
		}

		return list;
	}

	private void addObjectToResult(Map<String, Object> result, String name) {
		if (name == null) {
			Logging.warning(this, "name is null, in case OBJECT_BEGIN");
		} else {
			result.put(name, parseObject());
		}
	}

	private void addListToResult(Map<String, Object> result, String name) {
		if (name == null) {
			Logging.warning(this, "name is null, in case LIST_BEGIN");
		} else {
			result.put(name, parseList(name));
		}
	}

	private void addValueToResult(Map<String, Object> result, String name) {
		if (name == null) {
			Logging.warning(this, "name is null, in case JSON_VALUE");
		} else {
			result.put(name, stringToObject(parser.getValue(), name));
		}
	}

	private Object stringToObject(String value, String name) {
		Logging.debug(this, "stringToObject: " + name);
		if ("null".equals(value)) {
			return null;
		}

		if ("data".equals(name)) {
			value = value.substring(1, value.length() - 1);
			switch (lastDataType) {
			case NONE_TYPE:
				return null;
			case TEXT_TYPE:
			case ENUM_TYPE:
				return value;
			case DOUBLE_TYPE:
				return Double.valueOf(value);
			case INTEGER_TYPE:
				return Integer.valueOf(value);
			case BIG_INTEGER_TYPE:
				return Long.valueOf(value);
			case DATE_TYPE:
				return value;
			default:
				throw new IllegalArgumentException("Type " + lastDataType + " not expected here");
			}
		}

		if (value.startsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}

		if ("dataType".equals(name)) {
			switch (value) {
			case "TextType":
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

			case "EnumType":
				lastDataType = DataType.ENUM_TYPE;
				break;

			case "DateType":
				lastDataType = DataType.DATE_TYPE;
				break;

			case "NoneType":
				lastDataType = DataType.NONE_TYPE;
				break;

			default:
				Logging.error(this, "dataType for " + value + " cannot be found...)");
				break;
			}

			Logging.info(this, "lastDataType is now " + lastDataType);

			return lastDataType;
		}

		throw new IllegalArgumentException(value + " was not expected here");
	}

	/*
	 * Create a SelectOperation from the given data. This function works
	 * recursively.
	 */
	private AbstractSelectOperation getOperation(Map<String, Object> data,
			Map<String, List<AbstractSelectElement>> hardware) throws Exception {
		Logging.info(this, "getOperation for map " + data + "; hardware " + hardware);

		String elementPathS = null;
		if (data.get(KEY_ELEMENT_PATH) != null) {
			elementPathS = Arrays.toString((String[]) data.get(KEY_ELEMENT_PATH));
			Logging.info(this, "getOperation, elementPath in data " + elementPathS);
		}
		// Element
		AbstractSelectElement element = null;
		String elementName = (String) data.get(KEY_ELEMENT_NAME);
		Logging.info(this, "Element name: " + elementName);

		if (elementName != null && !(elementName.isEmpty())) {
			String subelementName = (String) data.get(KEY_SUBELEMENT_NAME);

			String[] elementPath = (String[]) data.get(KEY_ELEMENT_PATH);

			if (elementName.equals(ELEMENT_NAME_SOFTWARE_NAME_ELEMENT)) {
				element = manager.getNewSoftwareNameElement();
			} else if (elementName.equals(ELEMENT_NAME_GROUP_WITH_SUBGROUPS)) {
				element = new GroupWithSubgroupsElement(
						persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]));
			} else if (elementName.equals(ELEMENT_NAME_GROUP)) {
				// constructing a compatibility with format without GroupWithSubgroupsElement
				if (subelementName != null && subelementName.equals(ELEMENT_NAME_GROUP_WITH_SUBGROUPS)) {
					element = new GroupWithSubgroupsElement(
							persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]));
				} else {
					element = new GroupElement(
							persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]));
				}
			} else if (elementName.startsWith(ELEMENT_NAME_GENERIC)) {
				if (hardware == null) {
					hardware = manager.getBackend().getHardwareList();
				}
				Logging.info(this, "getOperation elementPath[0] " + elementPath[0]);
				List<AbstractSelectElement> elements = hardware.get(elementPath[0]);

				for (AbstractSelectElement possibleElement : elements) {
					Logging.info(this,
							"getOperation possibleElement.getClassName() " + possibleElement
									+ " compare with elementName " + elementName + " or perhaps with elementPathS "
									+ elementPathS);

					// originally, but is nonsense -------------------------------------------
					if (possibleElement.getClassName().equals(elementName)
							&& Arrays.toString(possibleElement.getPathArray()).equals(elementPathS)) {
						element = possibleElement;
						break;
					}
				}
			} else {
				element = (AbstractSelectElement) Class
						.forName("de.uib.configed.clientselection.elements." + elementName).getDeclaredConstructor()
						.newInstance();
			}
		}

		if (element != null) {
			String elS = "" + element + " class " + element.getClass() + " path " + element.getPath();
			Logging.info(this, "getOperation element " + elS);
		}

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
		Logging.info(this, "getOperation Operation name: " + operationName);
		AbstractSelectOperation operation;

		if (getSearchDataVersion() == 1) {
			operation = parseOperationVersion1(operationName, element, children);
		} else {
			Class<?> operationClass = Class.forName("de.uib.configed.clientselection.operations." + operationName);
			Logging.info(this, "getOperation operationClass  " + operationClass.toString());
			if (element != null) {
				Logging.info(this, "getOperation element != null, element  " + element);
				operation = (AbstractSelectOperation) operationClass.getConstructors()[0].newInstance(element);
			} else {
				// GroupOperation

				Class<?> list = Class.forName("java.util.List");
				Logging.info(this, "getOperation List name: " + list.toString());
				operation = (AbstractSelectOperation) operationClass.getConstructor(list).newInstance(children);
			}
		}

		Logging.info(this, "getOperation  " + operation);

		// Data
		SelectData.DataType dataType = (SelectData.DataType) data.get(KEY_DATA_TYPE);
		Logging.info(this, "getOperation dataType " + dataType);
		Object realData = data.get("data");
		Logging.info(this, "getOperation realData " + realData);
		SelectData selectData;
		if (dataType == null || data == null) {
			selectData = null;
		} else {
			selectData = new SelectData(realData, dataType);
		}

		operation.setSelectData(selectData);

		return operation;
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
		if (operation instanceof AbstractSelectGroupOperation) {
			List<Map<String, Object>> childData = new LinkedList<>();
			for (AbstractSelectOperation child : ((AbstractSelectGroupOperation) operation).getChildOperations()) {
				childData.add(produceData(child));
			}

			map.put("children", childData);
		} else {
			map.put("children", null);
		}
		Logging.info(this, "produced " + map);
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
		if ("Hardware".equals(name)) {
			return new HardwareOperation(children.get(0));
		}

		if ("Software".equals(name)) {
			return new SoftwareOperation(children.get(0));
		}

		if ("SwAudit".equals(name)) {
			return new SwAuditOperation(children.get(0));
		}

		if ("and".equals(name)) {
			return new AndOperation(children);
		}

		if ("or".equals(name)) {
			return new OrOperation(children);
		}

		if ("not".equals(name)) {
			return new NotOperation(children.get(0));
		}

		throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
	}

	/*
	 * Needed for version 1 data. Adds HostOperations, as they didn't exist in
	 * version 1
	 */
	private static AbstractSelectOperation checkForHostGroup(AbstractSelectOperation operation) {
		if (!(operation instanceof AbstractSelectGroupOperation)) {
			Logging.debug("No group: " + operation.getClassName() + ", element path size: "
					+ operation.getElement().getPathArray().length);
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
		while (notGroup instanceof AbstractSelectGroupOperation) {
			notGroup = ((AbstractSelectGroupOperation) notGroup).getChildOperations().get(0);
		}

		AbstractSelectOperation leftNotGroup = andOperation.getChildOperations().get(1);
		while (leftNotGroup instanceof AbstractSelectGroupOperation) {
			leftNotGroup = ((AbstractSelectGroupOperation) leftNotGroup).getChildOperations().get(0);
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
