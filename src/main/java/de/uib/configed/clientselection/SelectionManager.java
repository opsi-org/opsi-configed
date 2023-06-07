/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

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
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.utilities.logging.Logging;

/**
 * The SelectionManager is used by the gui to create the tree of operations.
 */
public class SelectionManager {
	public enum ConnectionStatus {
		AND, OR, AND_NOT, OR_NOT
	}

	private List<OperationWithStatus> groupWithStatusList;
	private boolean hasSoftware;
	private boolean hasHardware;
	private boolean hasSwAudit;
	private boolean isSerializedLoaded;
	private AbstractSelectOperation loadedSearch;

	private OpsiDataBackend backend;
	private OpsiDataSerializer serializer;

	public SelectionManager(String backend) {
		if (backend == null || backend.isEmpty()) {
			backend = "opsidata";
		}

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
	public Map<String, List<AbstractSelectElement>> getHardwareList() {
		return backend.getHardwareList();
	}

	/** Get the localized hardware list from the backend */
	public Map<String, List<AbstractSelectElement>> getLocalizedHardwareList() {
		return backend.getLocalizedHardwareList();
	}

	public OpsiDataBackend getBackend() {
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
		Logging.debug(this, "Adding group operation " + name + " with " + operationsWithStatuses.toString());

		LinkedList<AbstractSelectOperation> tmpList = new LinkedList<>();
		tmpList.add(build(operationsWithStatuses, new int[] { 0 }));
		Logging.debug(this, "addGroupOperation: " + name + " " + tmpList.size() + " " + tmpList.get(0));

		switch (name) {
		case "Software":
			groupStatus.operation = new SoftwareOperation(tmpList);
			break;

		case "Properties":
			groupStatus.operation = new PropertiesOperation(tmpList);
			break;

		case "SoftwareWithProperties":
			groupStatus.operation = new SoftwareWithPropertiesOperation(tmpList);
			break;

		case "Hardware":
			groupStatus.operation = new HardwareOperation(tmpList);
			break;

		case "SwAudit":
			groupStatus.operation = new SwAuditOperation(tmpList);
			break;

		case "Host":
			groupStatus.operation = new HostOperation(tmpList);
			break;

		default:
			throw new IllegalArgumentException(name + " is no valid group operation.");

		}

		groupWithStatusList.add(groupStatus);

		if ("Software".equals(name)) {
			hasSoftware = true;
		}

		if ("Hardware".equals(name)) {
			hasHardware = true;
		}

		if ("SwAudit".equals(name)) {
			hasSwAudit = true;
		}
	}

	/**
	 * Convert the operations into a list like it is used in the user interface
	 * class
	 */
	public List<OperationWithStatus> operationsAsList(AbstractSelectOperation top) {
		if (top == null) {
			return groupWithStatusList;
		} else {
			return reverseBuild(top, true);
		}
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
	public AbstractSelectOperation getTopOperation() {
		if (isSerializedLoaded) {
			return loadedSearch;
		}
		return build(groupWithStatusList, new int[] { 0 });
	}

	public List<String> selectClients() {

		AbstractSelectOperation operation = getTopOperation();
		if (operation == null) {
			Logging.info(this, "Nothing selected");
			return new ArrayList<>();
		} else {
			Logging.info("\n" + operation.printOperation(""));
		}

		OpsiserviceNOMPersistenceController controller = PersistenceControllerFactory.getPersistenceController();

		boolean withMySQL = controller.isWithMySQL()
				&& controller.getGlobalBooleanConfigValue(OpsiserviceNOMPersistenceController.KEY_SEARCH_BY_SQL,
						OpsiserviceNOMPersistenceController.DEFAULTVALUE_SEARCH_BY_SQL);

		if (withMySQL) {
			long startTime = System.nanoTime();
			List<String> l = selectClientsSQL(operation);
			Logging.notice(this, "select Clients with MySQL " + ((System.nanoTime() - startTime) / 1_000_000));
			return l;
		} else {
			long startTime = System.nanoTime();
			List<String> l = selectClientsLocal(operation);
			Logging.notice(this, "select Clients without MySQL " + ((System.nanoTime() - startTime) / 1_000_000));
			return l;
		}
	}

	// Filter the clients and get the matching clients back with MySQL backend
	public List<String> selectClientsSQL(AbstractSelectOperation operation) {
		String json = serializer.getJson(operation);
		Logging.info(this, "in selectClientsSQL gotten json-string: " + json);
		List<String> clientsSelected = new ArrayList<>();

		BackendMySQL backendMySQL = new BackendMySQL();
		List<String> list = backendMySQL.getClientListFromJSONString(json);

		for (int i = 0; i < list.size(); i++) {
			clientsSelected.add(list.get(i));
		}

		return clientsSelected;
	}

	// Filter the clients and get the matching clients back with old backend,
	// it should be checked before if operation is null
	public List<String> selectClientsLocal(AbstractSelectOperation operation) {
		ExecutableOperation selectOperation = backend.createExecutableOperation(operation);
		Logging.info(this, "selectClients, operation " + operation.getClassName());
		Logging.info(this, "" + ((AbstractSelectGroupOperation) operation).getChildOperations().size());
		return backend.checkClients(selectOperation, hasSoftware, hasHardware, hasSwAudit);
	}

	public void saveSearch(String name) {
		saveSearch(name, "");
	}

	/** Save the current operation tree with the serializer */
	public void saveSearch(String name, String description) {
		Logging.debug(this, "saveSearch " + name);
		AbstractSelectOperation operation = getTopOperation();
		if (operation == null) {
			Logging.debug(this, "Nothing selected");
		} else {
			serializer.save(operation, name, description);
		}
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
	private void setSearch(AbstractSelectOperation search) {
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
		Logging.debug(this, "setSearch " + serialized);
		clearOperations();
		setSearch(serializer.deserialize(serialized));
	}

	/** Load the given search. It will replace the current operation tree. */
	public void loadSearch(String name) {
		Logging.info(this, "loadSearch " + name);
		clearOperations();
		if (name == null || name.isEmpty()) {
			isSerializedLoaded = false;
			return;
		}
		Logging.info(this, "setSearch " + name);
		setSearch(serializer.load(name));
	}

	public void removeSearch(String name) {
		serializer.remove(name);
	}

	/* Build a operation tree from the temporary data given by the UI */
	private AbstractSelectOperation build(List<OperationWithStatus> input, int[] currentPos) {
		Logging.debug(this, "build counter: " + currentPos[0]);
		Logging.debug(this, "input size: " + input.size());
		if (input.isEmpty()) {
			return null;
		}

		List<AbstractSelectOperation> orConnections = new ArrayList<>();
		List<AbstractSelectOperation> andConnections = new ArrayList<>();
		boolean currentAnd = false;

		while (currentPos[0] < input.size()) {
			OperationWithStatus currentInput = input.get(currentPos[0]);
			Logging.debug("Position: " + currentPos[0]);
			Logging.debug("currentInput: " + currentInput.operation + currentInput.status + currentInput.parenthesisOpen
					+ currentInput.parenthesisClose);
			if (currentInput.parenthesisOpen) {

				// so we don't go one step deeper next time here, too
				currentInput.parenthesisOpen = false;
				AbstractSelectOperation operation = build(input, currentPos);
				Logging.debug("\n" + operation.printOperation(""));
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

		Logging.debug(this, "After break: " + currentPos[0]);

		if (andConnections.size() == 1) {
			orConnections.add(andConnections.get(0));
		} else if (!andConnections.isEmpty()) {
			orConnections.add(new AndOperation(andConnections));
		} else {
			// continue because there is nothing to do
		}

		if (orConnections.size() == 1) {
			return orConnections.get(0);
		}

		return new OrOperation(orConnections);
	}

	/*
	 * A reverse build, to be able to show a saved search in the UI. If
	 * isTopOperation == true,
	 * there are no parentheses around the whole list
	 */
	private static List<OperationWithStatus> reverseBuild(AbstractSelectOperation operation, boolean isTopOperation) {
		LinkedList<OperationWithStatus> result = new LinkedList<>();
		if (operation instanceof AndOperation) {
			for (AbstractSelectOperation op : ((AndOperation) operation).getChildOperations()) {
				result.addAll(reverseBuild(op, false));
			}

			if (!isTopOperation) {
				result.getFirst().parenthesisOpen = true;
				result.getLast().parenthesisClose = true;
			}
		} else if (operation instanceof OrOperation && !((OrOperation) operation).getChildOperations().isEmpty()) {
			for (AbstractSelectOperation op : ((OrOperation) operation).getChildOperations()) {
				result.addAll(reverseBuild(op, false));
				if (result.getLast().status == ConnectionStatus.AND) {
					result.getLast().status = ConnectionStatus.OR;
				} else {
					result.getLast().status = ConnectionStatus.OR_NOT;
				}
			}

			if (result.getLast().status == ConnectionStatus.OR) {
				result.getLast().status = ConnectionStatus.AND;
			} else {
				result.getLast().status = ConnectionStatus.AND_NOT;
			}

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
	private static AbstractSelectOperation parseNot(OperationWithStatus operation) {
		if (operation.status == ConnectionStatus.AND || operation.status == ConnectionStatus.OR) {
			return operation.operation;
		}

		LinkedList<AbstractSelectOperation> arg = new LinkedList<>();
		arg.add(operation.operation);

		return new NotOperation(arg);
	}

	/* See if there's a NotOperation and replace the status accordingly. */
	private static OperationWithStatus reverseParseNot(AbstractSelectOperation operation, ConnectionStatus status) {
		OperationWithStatus ows = new OperationWithStatus();
		if (operation instanceof NotOperation) {
			ows.operation = ((NotOperation) operation).getChildOperations().get(0);
			if (status == ConnectionStatus.AND) {
				ows.status = ConnectionStatus.AND_NOT;
			} else {
				ows.status = ConnectionStatus.OR_NOT;
			}
		} else {
			ows.operation = operation;
			ows.status = status;
		}
		return ows;
	}

	/* Check if there are any operations on some data groups. */
	private void checkForGroupSearches(AbstractSelectOperation operation) {
		if (hasHardware && hasSoftware && hasSwAudit) {
			return;
		}

		if (operation instanceof SoftwareOperation) {
			hasSoftware = true;
		} else if (operation instanceof HardwareOperation) {
			hasHardware = true;
		} else if (operation instanceof SwAuditOperation) {
			hasSwAudit = true;
		} else {
			// nothing to do for other operations
		}

		if (operation instanceof AbstractSelectGroupOperation) {
			for (AbstractSelectOperation child : ((AbstractSelectGroupOperation) operation).getChildOperations()) {
				checkForGroupSearches(child);
			}
		}
	}

	public static class OperationWithStatus {
		public AbstractSelectOperation operation;
		public ConnectionStatus status;
		public boolean parenthesisOpen;
		public boolean parenthesisClose;
	}
}
