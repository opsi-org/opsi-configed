/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.serializers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.SelectData;
import de.uib.configed.gui.features.clientselection.SelectData.DataType;
import de.uib.configed.gui.features.clientselection.SelectionManager;
import de.uib.configed.gui.features.clientselection.elements.GroupElement;
import de.uib.configed.gui.features.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.gui.features.clientselection.operations.AndOperation;
import de.uib.configed.gui.features.clientselection.operations.HardwareOperation;
import de.uib.configed.gui.features.clientselection.operations.HostOperation;
import de.uib.configed.gui.features.clientselection.operations.NotOperation;
import de.uib.configed.gui.features.clientselection.operations.OrOperation;
import de.uib.configed.gui.features.clientselection.operations.SoftwareOperation;
import de.uib.configed.gui.features.clientselection.operations.SwAuditOperation;
import de.uib.configed.gui.type.SavedSearch;
import de.uib.configed.share.logging.Logging;

public class OpsiDataSerializer {
	public static final int DATA_VERSION = 2;
	private static final String DATATYPE_REGEX_STRING = "(\"dataType\"\\s*:\\s*)(\\w+)";
	private static final Pattern DATATYPE_REGEX = Pattern.compile(DATATYPE_REGEX_STRING,
			Pattern.UNICODE_CHARACTER_CLASS);

	public static final String ELEMENT_NAME_GROUP = "GroupElement";
	public static final String ELEMENT_NAME_GROUP_WITH_SUBGROUPS = "GroupWithSubgroupsElement";
	public static final String ELEMENT_NAME_SOFTWARE_NAME_ELEMENT = "SoftwareNameElement";
	public static final String ELEMENT_NAME_GENERIC = "Generic";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private SelectionManager manager;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private Map<String, String> searches;
	private int searchDataVersion;

	/**
	 * Represents a node in the operation tree for client selection.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private record OperationNode(String element, String refinedElement, List<String> elementPath, String operation,
			String dataType, Object data, List<OperationNode> children) {
	}

	/**
	 * Represents the data structure for a saved search, including version and
	 * the operation data.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private record SavedSearchData(int version, OperationNode data) {
	}

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
		OperationNode node = produceOperationNode(topOperation);
		Logging.info(this, "save OperationNode ", node);
		saveData(name, description, node);
	}

	/**
	 * Get a list of the names of all saved searches.
	 */
	public Set<String> getSaved() {
		Set<String> set = new TreeSet<>();
		set.addAll(searches.keySet());
		set.addAll(persistenceController.getDataServices().config.getSavedSearchesPD().keySet());
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
	public AbstractSelectOperation deserialize(OperationNode node) {
		if (node == null) {
			Logging.warning(this, "OperationNode in Serializer.deserialize is null");
			return null;
		}

		Logging.info(this, "deserialize OperationNode ", node);
		if (node.elementPath() != null) {
			Logging.info("deserialize, elementPath ", node.elementPath());
		}

		try {
			AbstractSelectOperation operation = getOperation(node, null);
			if (getSearchDataVersion() == 1) {
				operation = checkForHostGroup(operation);
			}
			return operation;
		} catch (Exception e) {
			Logging.error(e, "deserialize error for OperationNode ", node, " message ", e.getMessage());
			return null;
		}
	}

	/**
	 * reproduce a search from a serialization string
	 */
	public AbstractSelectOperation deserialize(String serialized) {
		Logging.info(this, "deserialize serialized ", serialized);
		AbstractSelectOperation result = null;

		OperationNode node = decipher(serialized);
		result = deserialize(node);

		return result;
	}

	/**
	 * Get one search from searches map
	 */
	public AbstractSelectOperation load(String name) {
		Logging.info(this, "load ", name);
		OperationNode node = getData(name);
		return deserialize(node);
	}

	/**
	 * Parse JSON string to OperationNode using SavedSearchData wrapper.
	 */
	private OperationNode decipher(String serialization) {
		try {
			return parseAndExtractNode(serialization);
		} catch (IOException originalEx) {
			Logging.warning(this, "Failed to parse JSON (probably old saved search).",
					" Possibly due to unquoted 'dataType' field. Retrying with fix. Original error: ",
					originalEx.getMessage());

			String fixed = DATATYPE_REGEX.matcher(serialization).replaceAll("$1\"$2\"");

			try {
				return parseAndExtractNode(fixed);
			} catch (IOException retryEx) {
				Logging.error(this, retryEx, "Retry also failed when parsing fixed JSON. Original error: ",
						originalEx.getMessage(), " | Retry error: ", retryEx.getMessage());
				return null;
			}
		}
	}

	private OperationNode parseAndExtractNode(String json) throws IOException {
		SavedSearchData data = objectMapper.readValue(json, SavedSearchData.class);
		searchDataVersion = data.version();
		return data.data();
	}

	/** Get the data for the given saved search */
	private OperationNode getData(String name) {
		// we take version from server and not the (possibly edited own version! )
		searches.put(name,
				persistenceController.getDataServices().config.getSavedSearchesPD().get(name).getSerialization());

		String serialization = searches.get(name);
		return decipher(serialization);
	}

	/** Save the search data with the given name. */
	private void saveData(String name, String description, OperationNode node) {
		try {
			SavedSearchData wrapper = new SavedSearchData(DATA_VERSION, node);
			String jsonString = objectMapper.writeValueAsString(wrapper);

			Logging.info(this, name, ": ", jsonString);
			searches.put(name, jsonString);
			SavedSearch saveObj = new SavedSearch(name, jsonString, description);
			persistenceController.getDataServices().config.saveSearch(saveObj);
		} catch (IOException e) {
			Logging.error(this, e, e.getMessage());
		}
	}

	/** Get the data version of the currently loaded saved search */
	private int getSearchDataVersion() {
		return searchDataVersion;
	}

	public static String createJsonRecursive(OperationNode node) {
		try {
			SavedSearchData wrapper = new SavedSearchData(DATA_VERSION, node);
			return objectMapper.writeValueAsString(wrapper);
		} catch (IOException e) {
			Logging.error(OpsiDataSerializer.class, e, "Error serializing OperationNode to JSON: ", e.getMessage());
			return "{}";
		}
	}

	private DataType getDataTypeFromString(String value) {
		if (value == null || "null".equals(value)) {
			return null;
		}

		return switch (value) {
		// In old searches, we still have "EnumType", but this will now
		// due to refactoring be replaced by "TextType"
		case "TextType", "EnumType" -> DataType.TEXT_TYPE;
		case "IntegerType" -> DataType.INTEGER_TYPE;
		case "BigIntegerType" -> DataType.BIG_INTEGER_TYPE;
		case "DoubleType" -> DataType.DOUBLE_TYPE;
		case "DateType" -> DataType.DATE_TYPE;
		case "BooleanType" -> DataType.BOOLEAN_TYPE;
		case "NoneType" -> DataType.NONE_TYPE;
		default -> {
			Logging.error(this, "dataType for ", value, " cannot be found...)");
			yield null;
		}
		};
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
		case BOOLEAN_TYPE -> Boolean.valueOf(data);
		default -> throw new IllegalArgumentException("Type " + dataType + " not expected here");
		};
	}

	/*
	 * Create a SelectOperation from the given OperationNode. This function works
	 * recursively.
	 */
	private AbstractSelectOperation getOperation(OperationNode node, Map<String, List<AbstractSelectElement>> hardware)
			throws Exception {
		Logging.info(this, "getOperation for node ", node, "; hardware ", hardware);

		String elementPathS = extractElementPath(node);
		AbstractSelectElement element = getSelectElement(node, hardware, elementPathS);

		List<AbstractSelectOperation> children = buildChildOperations(node, hardware);

		String operationName = node.operation();
		Logging.info(this, "getOperation Operation name: ", operationName);

		AbstractSelectOperation operation = createOperation(operationName, element, children);

		Logging.info(this, "getOperation  ", operation);

		attachSelectData(node, operation);

		return operation;
	}

	private String extractElementPath(OperationNode node) {
		if (node.elementPath() != null) {
			String elementPathS = node.elementPath().toString();
			Logging.info(this, "getOperation, elementPath in node ", elementPathS);
			return elementPathS;
		}
		return null;
	}

	private List<AbstractSelectOperation> buildChildOperations(OperationNode node,
			Map<String, List<AbstractSelectElement>> hardware) throws Exception {
		List<OperationNode> childrenData = node.children();
		List<AbstractSelectOperation> children = new ArrayList<>();
		if (childrenData != null) {
			for (OperationNode child : childrenData) {
				children.add(getOperation(child, hardware));
			}
		}
		return children;
	}

	@SuppressWarnings("java:S112")
	private AbstractSelectOperation createOperation(String operationName, AbstractSelectElement element,
			List<AbstractSelectOperation> children) throws Exception {
		if (getSearchDataVersion() == 1) {
			return parseOperationVersion1(operationName, element, children);
		}

		Class<?> operationClass = Class
				.forName("de.uib.configed.gui.features.clientselection.operations." + operationName);
		Logging.info(this, "createOperation operationClass  ", operationClass.toString());
		AbstractSelectOperation op = null;

		if (element != null) {
			Logging.info(this, "createOperation element != null, element  ", element);
			op = (AbstractSelectOperation) operationClass.getConstructors()[0].newInstance(element);
		} else if (children.size() == 1) {
			Logging.info(this, "createOperation has one children  ", children.get(0));
			op = (AbstractSelectOperation) operationClass.getConstructor(AbstractSelectOperation.class)
					.newInstance(children.get(0));
		} else {
			Logging.info(this, "createOperation element == null - probably has more than one chlidren  ",
					children.size());
			op = (AbstractSelectOperation) operationClass.getConstructor(List.class).newInstance(children);
		}

		return op;
	}

	private void attachSelectData(OperationNode node, AbstractSelectOperation operation) {
		String dataTypeStr = node.dataType();
		DataType dataType = getDataTypeFromString(dataTypeStr);
		Object realData = node.data();
		Logging.info(this, "getOperation realData ", realData);

		SelectData selectData = null;
		if (dataTypeStr != null && realData != null) {
			Object convertedData = convertData(realData.toString(), dataType);
			selectData = new SelectData(convertedData, dataType);
		}
		operation.setSelectData(selectData);
	}

	private AbstractSelectElement getSelectElement(OperationNode node,
			Map<String, List<AbstractSelectElement>> hardware, String elementPathS) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		AbstractSelectElement element = null;
		String elementName = node.element();
		Logging.info(this, "Element name: ", elementName);

		if (elementName != null && !(elementName.isEmpty())) {
			String subelementName = node.refinedElement();

			List<String> elementPath = node.elementPath();

			element = switch (elementName) {
			case ELEMENT_NAME_SOFTWARE_NAME_ELEMENT -> manager.getNewSoftwareNameElement();
			case ELEMENT_NAME_GROUP_WITH_SUBGROUPS -> new GroupWithSubgroupsElement(
					persistenceController.getDataServices().group.getHostGroupIds().toArray(new String[0]));
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
			return (AbstractSelectElement) Class
					.forName("de.uib.configed.gui.features.clientselection.elements." + elementName)
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
					persistenceController.getDataServices().group.getHostGroupIds().toArray(new String[0]));
		} else {
			return new GroupElement(
					persistenceController.getDataServices().group.getHostGroupIds().toArray(new String[0]));
		}
	}

	/* Create OperationNode from the operation recursively. */
	private static OperationNode produceOperationNode(AbstractSelectOperation operation) {
		AbstractSelectElement element = operation.getElement();
		String elementName = null;
		String refinedElement = null;
		List<String> elementPath = null;

		if (element == null) {
			elementName = null;
			elementPath = null;
		} else if (element instanceof GroupWithSubgroupsElement) {
			// producing compatibility for version without GroupWithSubgroupsElement
			elementName = GroupElement.class.getSimpleName();
			refinedElement = GroupWithSubgroupsElement.class.getSimpleName();
			elementPath = List.of(element.getPathArray());
		} else {
			elementName = element.getClassName();
			elementPath = List.of(element.getPathArray());
		}

		String operationName = operation.getClassName();
		String dataType = operation.getSelectData() == null ? null : operation.getSelectData().getType().toString();
		Object data = operation.getSelectData() == null ? null : operation.getSelectData().getData();

		List<OperationNode> children = null;
		if (operation instanceof AbstractSelectGroupOperation abstractSelectGroupOperation) {
			children = new ArrayList<>();
			for (AbstractSelectOperation child : abstractSelectGroupOperation.getChildOperations()) {
				children.add(produceOperationNode(child));
			}
		}

		return new OperationNode(elementName, refinedElement, elementPath, operationName, dataType, data, children);
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
		return switch (operation) {
		case AndOperation andOperation -> handleAndOperation(andOperation);
		case AbstractSelectGroupOperation groupOp when isSpecialGroupOperation(groupOp) -> groupOp;
		case AbstractSelectGroupOperation groupOp when !(groupOp instanceof AndOperation) -> new HostOperation(groupOp);
		default -> {
			Logging.debug("No group: ", operation.getClassName(), ", element path size: ",
					operation.getElement().getPathArray().length);
			yield (operation.getElement().getPathArray().length == 1) ? new HostOperation(operation) : operation;
		}
		};
	}

	private static boolean isSpecialGroupOperation(AbstractSelectOperation operation) {
		return operation instanceof HardwareOperation || operation instanceof SoftwareOperation
				|| operation instanceof SwAuditOperation;
	}

	private static AbstractSelectOperation handleAndOperation(AndOperation andOperation) {
		AbstractSelectOperation notGroup = unwrapGroup(andOperation);

		int notGroupPathLen = notGroup.getElement().getPathArray().length;

		if (notGroupPathLen != 1) {
			return andOperation;
		}

		AbstractSelectOperation leftNotGroup = unwrapGroup(andOperation.getChildOperations().get(1));
		int leftNotGroupPathLen = leftNotGroup.getElement().getPathArray().length;
		if (notGroupPathLen == 1 && leftNotGroupPathLen == 1) {
			return new HostOperation(andOperation);
		}

		List<AbstractSelectOperation> ops = new ArrayList<>(andOperation.getChildOperations());
		ops.set(0, new HostOperation(ops.get(0)));
		return new AndOperation(ops);
	}

	private static AbstractSelectOperation unwrapGroup(AbstractSelectOperation operation) {
		while (operation instanceof AbstractSelectGroupOperation groupOp) {
			operation = groupOp.getChildOperations().get(0);
		}
		return operation;
	}
}
