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
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

/**
 * The SelectionManager is used by the gui to create the tree of operations.
 */
public class SelectionManager {
	private List<OperationWithStatus> groupWithStatusList;
	private boolean hasSoftware;
	private boolean hasHardware;
	private boolean hasSwAudit;
	private boolean isSerializedLoaded;
	private AbstractSelectOperation loadedSearch;

	private OpsiDataBackend backend;
	private OpsiDataSerializer serializer;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SelectionManager(String backend) {
		if (backend == null || backend.isEmpty()) {
			backend = "opsidata";
		}
		this.backend = new OpsiDataBackend();
		serializer = new OpsiDataSerializer(this);
		groupWithStatusList = new LinkedList<>();
	}

	/**
	 * Create a new SoftwareNameElement, with the existing product IDs as enum
	 * data.
	 */
	public SoftwareNameElement getNewSoftwareNameElement() {
		return new SoftwareNameElement(persistenceController.getProductDataService().getProductIdsPD());
	}

	/** Get the localized hardware list from the backend */
	public Map<String, List<AbstractSelectElement>> getLocalizedHardwareList() {
		return backend.getLocalizedHardwareList();
	}

	public OpsiDataBackend getBackend() {
		return backend;
	}

	/** Save this group operation in the manager for a later build. */
	public void addGroupOperation(String name, OperationWithStatus groupStatus,
			List<OperationWithStatus> operationsWithStatuses) {
		Logging.debug(this, "Adding group operation " + name + " with " + operationsWithStatuses.toString());

		List<AbstractSelectOperation> tmpList = new LinkedList<>();
		tmpList.add(build(operationsWithStatuses, new int[] { 0 }));
		Logging.debug(this, "addGroupOperation: " + name + " " + tmpList.size() + " " + tmpList.get(0));

		switch (name) {
		case "Software":
			groupStatus.setOperation(new SoftwareOperation(tmpList));
			break;

		case "Properties":
			groupStatus.setOperation(new PropertiesOperation(tmpList));
			break;

		case "SoftwareWithProperties":
			groupStatus.setOperation(new SoftwareWithPropertiesOperation(tmpList));
			break;

		case "Hardware":
			groupStatus.setOperation(new HardwareOperation(tmpList));
			break;

		case "SwAudit":
			groupStatus.setOperation(new SwAuditOperation(tmpList));
			break;

		case "Host":
			groupStatus.setOperation(new HostOperation(tmpList));
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
	private AbstractSelectOperation getTopOperation() {
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

		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();

		boolean withMySQL = persistenceController.getModuleDataService().canCallMySQLPD()
				&& persistenceController.getConfigDataService().getGlobalBooleanConfigValue(
						OpsiServiceNOMPersistenceController.KEY_SEARCH_BY_SQL,
						OpsiServiceNOMPersistenceController.DEFAULTVALUE_SEARCH_BY_SQL);

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
	private List<String> selectClientsSQL(AbstractSelectOperation operation) {
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
	private List<String> selectClientsLocal(AbstractSelectOperation operation) {
		ExecutableOperation selectOperation = backend.createExecutableOperation(operation);
		Logging.info(this, "selectClients, operation " + operation.getClassName());
		Logging.info(this, "" + ((AbstractSelectGroupOperation) operation).getChildOperations().size());
		return backend.checkClients(selectOperation, hasSoftware, hasHardware, hasSwAudit);
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
			Logging.debug("currentInput: " + currentInput.getOperation() + currentInput.getStatus()
					+ currentInput.isParenthesisOpen() + currentInput.isParenthesisClosed());
			if (currentInput.isParenthesisOpen()) {
				// so we don't go one step deeper next time here, too
				currentInput.setParenthesisOpen(false);
				AbstractSelectOperation operation = build(input, currentPos);
				Logging.debug("\n" + operation.printOperation(""));
				currentPos[0]--;
				currentInput = input.get(currentPos[0]);
				currentInput.setOperation(operation);
			}

			if (currentInput.getStatus() == ConnectionStatus.OR
					|| currentInput.getStatus() == ConnectionStatus.OR_NOT) {
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
			if (currentInput.isParenthesisClosed()) {
				currentInput.setParenthesisClose(false);
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
				result.getFirst().setParenthesisOpen(true);
				result.getLast().setParenthesisClose(true);
			}
		} else if (operation instanceof OrOperation && !((OrOperation) operation).getChildOperations().isEmpty()) {
			for (AbstractSelectOperation op : ((OrOperation) operation).getChildOperations()) {
				result.addAll(reverseBuild(op, false));
				if (result.getLast().getStatus() == ConnectionStatus.AND) {
					result.getLast().setStatus(ConnectionStatus.OR);
				} else {
					result.getLast().setStatus(ConnectionStatus.OR_NOT);
				}
			}

			if (result.getLast().getStatus() == ConnectionStatus.OR) {
				result.getLast().setStatus(ConnectionStatus.AND);
			} else {
				result.getLast().setStatus(ConnectionStatus.AND_NOT);
			}

			if (!isTopOperation) {
				result.getFirst().setParenthesisOpen(true);
				result.getLast().setParenthesisClose(true);
			}
		} else {
			result.add(reverseParseNot(operation, ConnectionStatus.AND));
			result.getLast().setParenthesisOpen(false);
			result.getLast().setParenthesisClose(false);
		}
		return result;
	}

	/* Add a NotOperation if necessary */
	private static AbstractSelectOperation parseNot(OperationWithStatus operation) {
		if (operation.getStatus() == ConnectionStatus.AND || operation.getStatus() == ConnectionStatus.OR) {
			return operation.getOperation();
		}

		List<AbstractSelectOperation> arg = new LinkedList<>();
		arg.add(operation.getOperation());

		return new NotOperation(arg);
	}

	/* See if there's a NotOperation and replace the status accordingly. */
	private static OperationWithStatus reverseParseNot(AbstractSelectOperation operation, ConnectionStatus status) {
		OperationWithStatus ows = new OperationWithStatus();
		if (operation instanceof NotOperation) {
			ows.setOperation(((NotOperation) operation).getChildOperations().get(0));
			if (status == ConnectionStatus.AND) {
				ows.setStatus(ConnectionStatus.AND_NOT);
			} else {
				ows.setStatus(ConnectionStatus.OR_NOT);
			}
		} else {
			ows.setOperation(operation);
			ows.setStatus(status);
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
}
