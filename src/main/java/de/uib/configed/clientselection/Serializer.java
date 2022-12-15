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
import de.uib.utilities.logging.logging;

/**
 * A serializer is able to save and load searches.
 */
public abstract class Serializer {
	public static final String elementNameGroup = "GroupElement";
	public static final String elementNameGroupWithSubgroups = "GroupWithSubgroupsElement";
	public static final String elementNameSoftwareNameElement = "SoftwareNameElement";
	public static final String elementNameGeneric = "Generic";

	public static final String keyElementName = "element";
	public static final String keySubelementName = "refinedElement";
	public static final String keyElementPath = "elementPath";
	public static final String keyOperation = "operation";
	public static final String keyDataType = "dataType";

	protected SelectionManager manager;

	protected Serializer(SelectionManager manager) {
		this.manager = manager;
	}

	/**
	 * Save the given tree of operations under the given name. If the name
	 * already exists, overwrite it.
	 */
	public void save(SelectOperation topOperation, String name, String description) {
		Map<String, Object> data = produceData(topOperation);
		logging.info(this, "save data " + data);
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

	public String getJson(SelectOperation topOperation) {
		Map<String, Object> data = produceData(topOperation);

		String jsonString;
		try {
			jsonString = "{ \"version\" : \"" + OpsiDataSerializer.dataVersion + "\", \"data\" : ";
			jsonString += OpsiDataSerializer.createJsonRecursive(data);
			jsonString += " }";
		} catch (IllegalArgumentException e) {
			logging.error(this, "Saving failed: " + e.getMessage(), e);
			return null;
		}

		return jsonString;
	}

	/**
	 * reproduce a search
	 */
	public SelectOperation deserialize(Map<String, Object> data) {
		if (data == null)
			logging.warning(this, "data in Serializer.deserialize is null");

		logging.info(this, "deserialize data " + data);
		if (data.get("elementPath") != null) {
			logging.info("deserialize, elementPath " + Arrays.toString((String[]) data.get("elementPath")));
		}

		try {
			SelectOperation operation = getOperation(data, null);
			if (getSearchDataVersion() == 1) {
				operation = checkForHostGroup(operation);
			}
			return operation;
		} catch (Exception e) {
			logging.error("deserialize error for data " + data + " message " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * reproduce a search from a serialization string
	 */

	public SelectOperation deserialize(String serialized) {
		logging.info(this, "deserialize serialized " + serialized);
		SelectOperation result = null;

		try {
			Map<String, Object> data = decipher(serialized);
			result = deserialize(data);
		} catch (Exception e) {
			logging.error("deserialize error " + e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Get one search from searches map
	 */
	public SelectOperation load(String name) {
		logging.info(this, "load " + name);
		try {
			Map<String, Object> data = getData(name);
			return deserialize(data);
		}

		catch (Exception e) {
			logging.error(e.getMessage(), e);
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
	 * Care for finding a SelectElement
	 * private SelectElement produceSelectElementFromPath
	 * {
	 * 
	 * 
	 * (SelectElement)
	 * Class.forName("de.uib.configed.clientselection.elements."+elementName).
	 * newInstance();
	 * 
	 */

	/*
	 * Create a SelectOperation from the given data. This function works
	 * recursively.
	 */
	private SelectOperation getOperation(Map<String, Object> data, Map<String, List<SelectElement>> hardware)
			throws Exception {
		logging.info(this, "getOperation for map " + data + "; hardware " + hardware);

		String elementPathS = null;
		if (data.get(keyElementPath) != null) {
			elementPathS = Arrays.toString((String[]) data.get(keyElementPath));
			logging.info(this, "getOperation, elementPath in data " + elementPathS);
		}
		// Element
		SelectElement element = null;
		String elementName = (String) data.get(keyElementName);
		logging.info(this, "Element name: " + elementName);

		if (elementName != null && !(elementName.isEmpty())) {
			String subelementName = (String) data.get(keySubelementName);

			String[] elementPath = (String[]) data.get(keyElementPath);

			if (elementName.equals(elementNameSoftwareNameElement))
				element = manager.getNewSoftwareNameElement();

			else if (elementName.equals(elementNameGroupWithSubgroups)) {
				element = new GroupWithSubgroupsElement(manager.getBackend().getGroups().toArray(new String[0]));
			}

			else if (elementName.equals(elementNameGroup)) {
				// constructing a compatibility with format without GroupWithSubgroupsElement
				if (subelementName != null && subelementName.equals(elementNameGroupWithSubgroups))
					element = new GroupWithSubgroupsElement(manager.getBackend().getGroups().toArray(new String[0]));
				else
					element = new GroupElement(manager.getBackend().getGroups().toArray(new String[0]));
			}

			else if (elementName.startsWith(elementNameGeneric)) {
				if (hardware == null) {
					hardware = manager.getBackend().getHardwareList();
				}
				logging.info(this, "getOperation elementPath[0] " + elementPath[0]);
				List<SelectElement> elements = hardware.get(elementPath[0]);
				// logging.info(this, "getOperation hardware " + hardware);
				// logging.info(this, "getOperation hardware elements " + elements );

				for (SelectElement possibleElement : elements) {
					logging.info(this,
							"getOperation possibleElement.getClassName() " + possibleElement
									+ " compare with elementName " + elementName + " or perhaps with elementPathS "
									+ elementPathS);

					// if( possibleElement.getClassName().equals( elementName ) )
					// originally, but is nonsense -------------------------------------------
					if (possibleElement.getClassName().equals(elementName)
							&& Arrays.toString(possibleElement.getPathArray()).equals(elementPathS)) {
						element = possibleElement;
						break;
					}
				}
			} else {
				element = (SelectElement) Class.forName("de.uib.configed.clientselection.elements." + elementName)
						.newInstance();
			}
		}

		if (element != null) {
			String elS = "" + element + " class " + element.getClass() + " path " + element.getPath();
			logging.info(this, "getOperation element " + elS);
		}

		// Children
		List<Map<String, Object>> childrenData = (List<Map<String, Object>>) data.get("children");
		LinkedList<SelectOperation> children = new LinkedList<SelectOperation>();
		if (childrenData != null)
			for (Map<String, Object> child : childrenData)
				children.add(getOperation(child, hardware));

		// Operation
		String operationName = (String) data.get(keyOperation);
		logging.info(this, "getOperation Operation name: " + operationName);
		SelectOperation operation;

		if (getSearchDataVersion() == 1) {
			operation = parseOperationVersion1(operationName, element, children);
		} else {
			Class operationClass = Class.forName("de.uib.configed.clientselection.operations." + operationName);
			logging.info(this, "getOperation operationClass  " + operationClass.toString());
			if (element != null) {
				logging.info(this, "getOperation element != null, element  " + element);
				operation = (SelectOperation) operationClass.getConstructors()[0].newInstance(element);
			} else // GroupOperation
			{
				Class list = Class.forName("java.util.List");
				logging.info(this, "getOperation List name: " + list.toString());
				operation = (SelectOperation) operationClass.getConstructor(list).newInstance(children);
			}
		}

		logging.info(this, "getOperation  " + operation);

		// Data
		SelectData.DataType dataType = (SelectData.DataType) data.get(keyDataType);
		logging.info(this, "getOperation dataType " + dataType);
		Object realData = data.get("data");
		logging.info(this, "getOperation realData " + realData);
		SelectData selectData;
		if (dataType == null || data == null)
			selectData = null;
		else
			selectData = new SelectData(realData, dataType);
		operation.setSelectData(selectData);

		return operation;
	}

	/* Create data from the operation recursively. */
	private Map<String, Object> produceData(SelectOperation operation) {
		Map<String, Object> map = new HashMap<String, Object>();
		SelectElement element = operation.getElement();
		if (element == null) {
			map.put(keyElementName, null);
			map.put(keyElementPath, null);
		} else if (element instanceof GroupWithSubgroupsElement)
		// producing compatibility for version without GroupWithSubgroupsElement
		{
			map.put(keyElementName, GroupElement.class.getSimpleName());
			map.put(keySubelementName, GroupWithSubgroupsElement.class.getSimpleName());
			map.put(keyElementPath, element.getPathArray());

		} else {
			map.put(keyElementName, element.getClassName());
			map.put(keyElementPath, element.getPathArray());
		}

		map.put(keyOperation, operation.getClassName());
		if (operation.getSelectData() == null) {
			map.put(keyDataType, null);
			map.put("data", null);
		} else {
			map.put(keyDataType, operation.getSelectData().getType());
			map.put("data", operation.getSelectData().getData());
		}
		if (operation instanceof SelectGroupOperation) {
			List<Map<String, Object>> childData = new LinkedList<Map<String, Object>>();
			for (SelectOperation child : ((SelectGroupOperation) operation).getChildOperations())
				childData.add(produceData(child));
			map.put("children", childData);
		} else {
			map.put("children", null);
		}
		logging.info(this, "produced " + map);
		return map;
	}

	/* Parse the operations with the old (version 1) operation names */
	private SelectOperation parseOperationVersion1(String name, SelectElement element, List<SelectOperation> children) {
		logging.info(this, "parseOperationVersion1");

		if (element != null) {
			for (SelectOperation operation : element.supportedOperations()) {
				if (operation.getOperationString().equals(name))
					return operation;
			}
			throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
		}
		if (name.equals("Hardware"))
			return new HardwareOperation(children);
		if (name.equals("Software"))
			return new SoftwareOperation(children);
		if (name.equals("SwAudit"))
			return new SwAuditOperation(children);
		if (name.equals("and"))
			return new AndOperation(children);
		if (name.equals("or"))
			return new OrOperation(children);
		if (name.equals("not"))
			return new NotOperation(children);
		throw new IllegalArgumentException("While parsing ver 1 saved search: " + name);
	}

	/*
	 * Needed for version 1 data. Adds HostOperations, as they didn't exist in
	 * version 1
	 */
	private SelectOperation checkForHostGroup(SelectOperation operation) {
		if (!(operation instanceof SelectGroupOperation)) {
			logging.debug("No group: " + operation.getClassName() + ", element path size: "
					+ operation.getElement().getPathArray().length);
			if (operation.getElement().getPathArray().length == 1)
				return new HostOperation(operation);
			else
				return operation;
		}
		if (operation instanceof HardwareOperation || operation instanceof SoftwareOperation
				|| operation instanceof SwAuditOperation)
			return operation;
		if (!(operation instanceof AndOperation))
			return new HostOperation(operation);
		AndOperation andOperation = (AndOperation) operation;
		SelectOperation notGroup = operation;
		while (notGroup instanceof SelectGroupOperation)
			notGroup = ((SelectGroupOperation) notGroup).getChildOperations().get(0);
		SelectOperation leftNotGroup = andOperation.getChildOperations().get(1);
		while (leftNotGroup instanceof SelectGroupOperation)
			leftNotGroup = ((SelectGroupOperation) leftNotGroup).getChildOperations().get(0);
		if (notGroup.getElement().getPathArray().length != 1)
			return operation;
		if (notGroup.getElement().getPathArray().length == 1 && leftNotGroup.getElement().getPathArray().length == 1)
			return new HostOperation(andOperation);
		List<SelectOperation> ops = andOperation.getChildOperations();
		HostOperation host = new HostOperation(ops.get(0));
		ops.remove(0);
		ops.add(0, host);
		return new AndOperation(ops);
	}
}
