package de.uib.configed.clientselection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataBackend;
import de.uib.configed.clientselection.elements.SoftwareNameElement;
import de.uib.configed.clientselection.operations.AndOperation;
import de.uib.configed.clientselection.operations.HardwareOperation;
import de.uib.configed.clientselection.operations.HostOperation;
import de.uib.configed.clientselection.operations.NotOperation;
import de.uib.configed.clientselection.operations.OrOperation;
import de.uib.configed.clientselection.operations.PropertiesOperation;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.configed.clientselection.operations.SoftwareWithPropertiesOperation;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.clientselection.serializers.OpsiDataSerializer;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.utilities.logging.logging;

/**
 * The SelectionManager is used by the gui to create the tree of operations.
 */
public class SelectionManager {
	public enum ConnectionStatus {
		AND, OR, AND_NOT, OR_NOT
	}

	private List<OperationWithStatus> groupWithStatusList;
	private boolean hasSoftware = false;
	private boolean hasHardware = false;
	private boolean hasSwAudit = false;
	private boolean isSerializedLoaded = false;
	private SelectOperation loadedSearch = null;

	private Backend backend;
	private de.uib.configed.clientselection.Serializer serializer;

	public SelectionManager(String backend) {
		if (backend == null || backend.isEmpty())
			backend = "opsidata";

		this.backend = OpsiDataBackend.getInstance();

		serializer = new OpsiDataSerializer(this);
		groupWithStatusList = new LinkedList<>();
	}

	/**
	 * Create a new SoftwareNameElement, with the existing product IDs as enum
	 * data.
	 */
	public SoftwareNameElement getNewSoftwareNameElement() {
		return new SoftwareNameElement(backend.getProductIDs());
	}

	/** Get the non-localized hardware list from the backend */
	public Map<String, List<SelectElement>> getHardwareList() {
		return backend.getHardwareList();
	}

	/** Get the localized hardware list from the backend */
	public Map<String, List<SelectElement>> getLocalizedHardwareList() {
		return backend.getLocalizedHardwareList();
	}

	public Backend getBackend() {
		return backend;
	}

	/**
	 * Next search, the manager should not use the loaded search data anymore.
	 */
	public void setChanged() {
		isSerializedLoaded = false;
	}

	/** Save this group operation in the manager for a later build. */
	public void addGroupOperation(String name, OperationWithStatus groupStatus,
			List<OperationWithStatus> operationsWithStatuses) {
		logging.debug(this, "Adding group operation " + name + " with " + operationsWithStatuses.toString());

		LinkedList<SelectOperation> tmpList = new LinkedList<>();
		tmpList.add(build(operationsWithStatuses, new int[] { 0 }));
		logging.debug(this, "addGroupOperation: " + name + " " + tmpList.size() + " " + tmpList.get(0));
		if (name.equals("Software"))
			groupStatus.operation = new SoftwareOperation(tmpList);
		else if (name.equals("Properties"))
			groupStatus.operation = new PropertiesOperation(tmpList);
		else if (name.equals("SoftwareWithProperties"))
			groupStatus.operation = new SoftwareWithPropertiesOperation(tmpList);
		else if (name.equals("Hardware"))
			groupStatus.operation = new HardwareOperation(tmpList);
		else if (name.equals("SwAudit"))
			groupStatus.operation = new SwAuditOperation(tmpList);
		else if (name.equals("Host"))
			groupStatus.operation = new HostOperation(tmpList);
		else
			throw new IllegalArgumentException(name + " is no valid group operation.");
		groupWithStatusList.add(groupStatus);

		if (name.equals("Software"))
			hasSoftware = true;
		if (name.equals("Hardware"))
			hasHardware = true;
		if (name.equals("SwAudit"))
			hasSwAudit = true;

	}

	/**
	 * Convert the operations into a list like it is used in the user interface
	 * class
	 */
	public List<OperationWithStatus> operationsAsList(SelectOperation top) {
		if (top == null)
			return groupWithStatusList;
		else
			return reverseBuild(top, true);
	}

	/** Clear all temporary data storage */
	public void clearOperations() {
		groupWithStatusList.clear();
		hasSoftware = false;
		hasHardware = false;
		hasSwAudit = false;
		isSerializedLoaded = false;
	}

	/** Get the top operation and build it before, if necessary. */
	public SelectOperation getTopOperation() {
		if (isSerializedLoaded)
			return loadedSearch;
		return build(groupWithStatusList, new int[] { 0 });
	}

	public List<String> selectClients() {
		PersistenceController controller = PersistenceControllerFactory.getPersistenceController();
		boolean withMySQL = controller.isWithMySQL() && controller.getGlobalBooleanConfigValue(
				PersistenceController.KEY_SEARCH_BY_SQL, PersistenceController.DEFAULTVALUE_SEARCH_BY_SQL);

		if (withMySQL) {
			long startTime = System.nanoTime();
			List<String> l = selectClientsSQL(controller);
			logging.notice(this, "select Clients with MySQL " + ((System.nanoTime() - startTime) / 1000000));
			return l;
		} else {
			long startTime = System.nanoTime();
			List<String> l = selectClientsLocal();
			logging.notice(this, "select Clients without ;MySQL " + ((System.nanoTime() - startTime) / 1000000));
			return l;
		}
	}

	// Filter the clients and get the matching clients back with MySQL backend
	public List<String> selectClientsSQL(PersistenceController controller) {

		SelectOperation operation = getTopOperation();
		String json = serializer.getJson(operation);

		ArrayList<String> clientsSelected = new ArrayList<>();

		try {

			BackendMySQL backendMySQL = new BackendMySQL(controller);
			List<String> list = backendMySQL.getClientListFromJSONString(json);

			for (int i = 0; i < list.size(); i++) {
				clientsSelected.add(list.get(i));
			}

		} catch (Exception e) {
			for (int i = 0; i < 100; i++)
				logging.error(this, "EXCEPTION");
		}

		return clientsSelected;
	}

	// Filter the clients and get the matching clients back with old backend
	public List<String> selectClientsLocal() {
		SelectOperation operation = getTopOperation();
		if (operation == null) {
			logging.info(this, "Nothing selected");
			return null;
		} else {
			logging.info("\n" + operation.printOperation(""));
		}

		ExecutableOperation selectOperation = backend.createExecutableOperation(operation);
		logging.info(this, "selectClients, operation " + operation.getClassName());
		logging.info(this, "" + ((SelectGroupOperation) operation).getChildOperations().size());
		return backend.checkClients(selectOperation, hasSoftware, hasHardware, hasSwAudit);
	}

	public void saveSearch(String name) {
		saveSearch(name, "");
	}

	/** Save the current operation tree with the serializer */
	public void saveSearch(String name, String description) {
		logging.debug(this, "saveSearch " + name);
		SelectOperation operation = getTopOperation();
		if (operation == null)
			logging.debug(this, "Nothing selected");
		else
			serializer.save(operation, name, description);
	}

	public List<String> getSavedSearchesNames() {
		return serializer.getSaved();
	}

	public SavedSearches getSavedSearches() {
		return serializer.getSavedSearches();
	}

	/**
	 * Sets the given serialized search. It will replace the current operation
	 * tree.
	 */
	private void setSearch(SelectOperation search) {
		loadedSearch = search;
		isSerializedLoaded = true;
		checkForGroupSearches(getTopOperation());
		groupWithStatusList = reverseBuild(getTopOperation(), true);
	}

	/**
	 * Sets the given serialized search. It will replace the current operation
	 * tree.
	 */
	public void setSearch(String serialized) {
		logging.debug(this, "setSearch " + serialized);
		clearOperations();
		setSearch(serializer.deserialize(serialized));
	}

	/** Load the given search. It will replace the current operation tree. */
	public void loadSearch(String name) {
		logging.info(this, "loadSearch " + name);
		clearOperations();
		if (name == null || name.isEmpty()) {
			isSerializedLoaded = false;
			return;
		}
		logging.info(this, "setSearch " + name);
		setSearch(serializer.load(name));
	}

	public void removeSearch(String name) {
		serializer.remove(name);
	}

	/* Build a operation tree from the temporary data given by the UI */
	private SelectOperation build(List<OperationWithStatus> input, int[] currentPos) {
		logging.debug(this, "build counter: " + currentPos[0]);
		logging.debug(this, "input size: " + input.size());
		if (input.isEmpty())
			return null;

		LinkedList<SelectOperation> orConnections = new LinkedList<>();
		LinkedList<SelectOperation> andConnections = new LinkedList<>();
		boolean currentAnd = false;

		while (currentPos[0] < input.size()) {
			OperationWithStatus currentInput = input.get(currentPos[0]);
			logging.debug("Position: " + currentPos[0]);
			logging.debug("currentInput: " + currentInput.operation + currentInput.status + currentInput.parenthesisOpen
					+ currentInput.parenthesisClose);
			if (currentInput.parenthesisOpen) {
				currentInput.parenthesisOpen = false; // so we don't go one step deeper next time here, too
				SelectOperation operation = build(input, currentPos);
				logging.debug("\n" + operation.printOperation(""));
				currentPos[0]--;
				currentInput = input.get(currentPos[0]);
				currentInput.operation = operation;
			}
			if (currentInput.status == ConnectionStatus.OR || currentInput.status == ConnectionStatus.OR_NOT) {
				if (!currentAnd) {
					orConnections.add(parseNot(currentInput));
				} else {
					andConnections.add(parseNot(currentInput));
					orConnections.add(new AndOperation(andConnections));
					andConnections.clear();
				}
				currentAnd = false;
			} else {
				andConnections.add(parseNot(currentInput));
				currentAnd = true;
			}
			currentPos[0]++;
			if (currentInput.parenthesisClose) {
				currentInput.parenthesisClose = false;
				break;
			}
		}
		logging.debug(this, "After break: " + currentPos[0]);
		if (andConnections.size() == 1)
			orConnections.add(andConnections.get(0));
		else if (!andConnections.isEmpty())
			orConnections.add(new AndOperation(andConnections));

		if (orConnections.size() == 1)
			return orConnections.get(0);

		return new OrOperation(orConnections);
	}

	/*
	 * A reverse build, to be able to show a saved search in the UI. If
	 * isTopOperation == true,
	 * there are no parentheses around the whole list
	 */
	private List<OperationWithStatus> reverseBuild(SelectOperation operation, boolean isTopOperation) {
		LinkedList<OperationWithStatus> result = new LinkedList<>();
		if (operation instanceof AndOperation) {
			for (SelectOperation op : ((AndOperation) operation).getChildOperations()) {
				result.addAll(reverseBuild(op, false));
			}
			if (!isTopOperation) {
				result.getFirst().parenthesisOpen = true;
				result.getLast().parenthesisClose = true;
			}
		} else if (operation instanceof OrOperation && !((OrOperation) operation).getChildOperations().isEmpty()) {
			for (SelectOperation op : ((OrOperation) operation).getChildOperations()) {
				result.addAll(reverseBuild(op, false));
				if (result.getLast().status == ConnectionStatus.AND)
					result.getLast().status = ConnectionStatus.OR;
				else
					result.getLast().status = ConnectionStatus.OR_NOT;
			}
			if (result.getLast().status == ConnectionStatus.OR)
				result.getLast().status = ConnectionStatus.AND;
			else
				result.getLast().status = ConnectionStatus.AND_NOT;
			if (!isTopOperation) {
				result.getFirst().parenthesisOpen = true;
				result.getLast().parenthesisClose = true;
			}
		} else {
			result.add(reverseParseNot(operation, ConnectionStatus.AND));
			result.getLast().parenthesisOpen = false;
			result.getLast().parenthesisClose = false;
		}
		return result;
	}

	/* Add a NotOperation if necessary */
	private SelectOperation parseNot(OperationWithStatus operation) {
		if (operation.status == ConnectionStatus.AND || operation.status == ConnectionStatus.OR)
			return operation.operation;

		LinkedList<SelectOperation> arg = new LinkedList<>();
		arg.add(operation.operation);

		return new NotOperation(arg);
	}

	/* See if there's a NotOperation and replace the status accordingly. */
	private OperationWithStatus reverseParseNot(SelectOperation operation, ConnectionStatus status) {
		OperationWithStatus ows = new OperationWithStatus();
		if (operation instanceof NotOperation) {
			ows.operation = ((NotOperation) operation).getChildOperations().get(0);
			if (status == ConnectionStatus.AND)
				ows.status = ConnectionStatus.AND_NOT;
			else
				ows.status = ConnectionStatus.OR_NOT;
		} else {
			ows.operation = operation;
			ows.status = status;
		}
		return ows;
	}

	/* Check if there are any operations on some data groups. */
	private void checkForGroupSearches(SelectOperation operation) {
		if (hasHardware && hasSoftware && hasSwAudit)
			return;
		if (operation instanceof SoftwareOperation)
			hasSoftware = true;
		else if (operation instanceof HardwareOperation)
			hasHardware = true;
		else if (operation instanceof SwAuditOperation)
			hasSwAudit = true;
		if (operation instanceof SelectGroupOperation)
			for (SelectOperation child : ((SelectGroupOperation) operation).getChildOperations())
				checkForGroupSearches(child);
	}

	public static class OperationWithStatus {
		public SelectOperation operation;
		public ConnectionStatus status;
		public boolean parenthesisOpen;
		public boolean parenthesisClose;
	}
}
