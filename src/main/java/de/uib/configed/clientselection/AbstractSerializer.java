package de.uib.configed.clientselection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.clientselection.elements.GroupElement;
import de.uib.configed.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.clientselection.operations.AndOperation;
import de.uib.configed.clientselection.operations.HardwareOperation;
import de.uib.configed.clientselection.operations.HostOperation;
import de.uib.configed.clientselection.operations.NotOperation;
import de.uib.configed.clientselection.operations.OrOperation;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.clientselection.serializers.OpsiDataSerializer;
import de.uib.configed.clientselection.serializers.WrongVersionException;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.utilities.logging.Logging;

/**
 * A serializer is able to save and load searches.
 */
public abstract class AbstractSerializer {
	public static final String ELEMENT_NAME_GROUP = "GroupElement";
	public static final String ELEMENT_NAME_GROUP_WITH_SUBGROUPS = "GroupWithSubgroupsElement";
	public static final String ELEMENT_NAME_SOFTWARE_NAME_ELEMENT = "SoftwareNameElement";
	public static final String ELEMENT_NAME_GENERIC = "Generic";

	public static final String KEY_ELEMENT_NAME = "element";
	public static final String KEY_SUBELEMENT_NAME = "refinedElement";
	public static final String KEY_ELEMENT_PATH = "elementPath";
	public static final String KEY_OPERATION = "operation";
	public static final String KEY_DATA_TYPE = "dataType";

	protected SelectionManager manager;

	protected AbstractSerializer(SelectionManager manager) {
		this.manager = manager;
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
	public abstract List<String> getSaved();

	/**
	 * Get the saved searches map
	 */
	public abstract SavedSearches getSavedSearches();

	public String getJson(AbstractSelectOperation topOperation) {
		Map<String, Object> data = produceData(topOperation);

		String jsonString;
		try {
			jsonString = "{ \"version\" : \"" + OpsiDataSerializer.DATA_VERSION + "\", \"data\" : ";
			jsonString += OpsiDataSerializer.createJsonRecursive(data);
			jsonString += " }";
		} catch (IllegalArgumentException e) {
			Logging.error(this, "Saving failed: " + e.getMessage(), e);
			return null;
		}

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

		try {
			Map<String, Object> data = decipher(serialized);
			result = deserialize(data);
		} catch (Exception e) {
			Logging.error("deserialize error " + e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Get one search from searches map
	 */
	public AbstractSelectOperation load(String name) {
		Logging.info(this, "load " + name);
		try {
			Map<String, Object> data = getData(name);
			return deserialize(data);
		}

		catch (Exception e) {
			Logging.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Remove a saved search from the server
	 */
	public abstract void remove(String name);

	/** Get the data for the given saved search */
	protected abstract Map<String, Object> getData(String name) throws WrongVersionException;

	/**
	 * produce map format of serializiation object
	 */
	protected abstract Map<String, Object> decipher(String serialization) throws WrongVersionException;

	/** Save the search data with the given name. */
	protected abstract void saveData(String name, String description, Map<String, Object> data);

	/** Get the data version of the currently loaded saved search */
	protected abstract int getSearchDataVersion();

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
			}

			else if (elementName.equals(ELEMENT_NAME_GROUP_WITH_SUBGROUPS)) {
				element = new GroupWithSubgroupsElement(manager.getBackend().getGroups().toArray(new String[0]));
			}

			else if (elementName.equals(ELEMENT_NAME_GROUP)) {
				// constructing a compatibility with format without GroupWithSubgroupsElement
				if (subelementName != null && subelementName.equals(ELEMENT_NAME_GROUP_WITH_SUBGROUPS)) {
					element = new GroupWithSubgroupsElement(manager.getBackend().getGroups().toArray(new String[0]));
				} else {
					element = new GroupElement(manager.getBackend().getGroups().toArray(new String[0]));
				}
			}

			else if (elementName.startsWith(ELEMENT_NAME_GENERIC)) {
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
		LinkedList<AbstractSelectOperation> children = new LinkedList<>();
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
			} else // GroupOperation
			{
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
		} else if (element instanceof GroupWithSubgroupsElement)
		// producing compatibility for version without GroupWithSubgroupsElement
		{
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
		if (name.equals("Hardware")) {
			return new HardwareOperation(children);
		}

		if (name.equals("Software")) {
			return new SoftwareOperation(children);
		}

		if (name.equals("SwAudit")) {
			return new SwAuditOperation(children);
		}

		if (name.equals("and")) {
			return new AndOperation(children);
		}

		if (name.equals("or")) {
			return new OrOperation(children);
		}

		if (name.equals("not")) {
			return new NotOperation(children);
		}

		throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
	}

	/*
	 * Needed for version 1 data. Adds HostOperations, as they didn't exist in
	 * version 1
	 */
	private AbstractSelectOperation checkForHostGroup(AbstractSelectOperation operation) {
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
