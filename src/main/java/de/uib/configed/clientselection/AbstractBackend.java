package de.uib.configed.clientselection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import de.uib.utilities.logging.Logging;

/**
 * Each backend represents a data source, which can be used find out which
 * clients should be selected. It also creates all operations, as the
 * implementation may differ between the data sources.
 */
public abstract class AbstractBackend {

	/*
	 * These variables tell you which data you have to fetch. E.g. if hasSoftware is
	 * true, there is an software
	 * operation and so you need to get the data about software.
	 */
	protected boolean hasSoftware;
	protected boolean hasHardware;
	protected boolean hasSwAudit;
	protected boolean reloadRequested;

	/**
	 * Goes through the list of clients and filters them with operation. The
	 * boolean arguments give hints which data is needed.
	 */
	public List<String> checkClients(ExecutableOperation operation, boolean hasSoftware, boolean hasHardware,
			boolean hasSwAudit) {
		Logging.debug(this, "Starting the filtering.. , operation " + operation);
		this.hasSoftware = hasSoftware;
		this.hasHardware = hasHardware;
		this.hasSwAudit = hasSwAudit;
		List<Client> clients = getClients();
		Logging.debug(this, "Number of clients to filter: " + clients.size());

		List<String> matchingClients = new LinkedList<>();
		for (Client client : clients) {

			if (operation.doesMatch(client)) {

				matchingClients.add(client.getId());
			}
		}
		return matchingClients;
	}

	/**
	 * This function translates the operations tree with the root operation into
	 * an executable operation tree by replacing the non-executable operations
	 * with their backend-specific executable operations.
	 */
	public ExecutableOperation createExecutableOperation(AbstractSelectOperation operation) {
		Logging.debug(this, "createFromOperationData " + operation.getClassName());

		if (operation instanceof AbstractSelectGroupOperation) {
			AbstractSelectGroupOperation groupOperation = (AbstractSelectGroupOperation) operation;
			List<AbstractSelectOperation> children = new LinkedList<>();
			for (AbstractSelectOperation child : groupOperation.getChildOperations())
				children.add((AbstractSelectOperation) createExecutableOperation(child));
			return (ExecutableOperation) createGroupOperation(groupOperation, children);
		} else {
			return (ExecutableOperation) createOperation(operation);
		}
	}

	/**
	 * sets the property
	 */
	public void setReloadRequested() {
		reloadRequested = true;
	}

	/**
	 * Create a backend specific executable operation based on this operation.
	 */
	protected abstract AbstractSelectOperation createOperation(AbstractSelectOperation operation);

	/**
	 * Creates a backend specific executable operation based on this group
	 * operation and the list of backend specific children.
	 */
	protected abstract AbstractSelectGroupOperation createGroupOperation(AbstractSelectGroupOperation operation,
			List<AbstractSelectOperation> operations);

	/**
	 * Get a list of all clients. These will be filtered later.
	 */
	protected abstract List<Client> getClients();

	/**
	 * Get a list of all groups of this opsi installation.
	 */
	public abstract List<String> getGroups();

	/**
	 * Get a list of product IDs of the opsi products.
	 */
	public abstract NavigableSet<String> getProductIDs();

	/**
	 * Get a map, with the hardware as key and a list of properties as value.
	 * The key is in english.
	 */
	public abstract Map<String, List<AbstractSelectElement>> getHardwareList();

	/**
	 * Get a map, with the hardware as key and a list of properties as value.
	 * The key is localized.
	 */
	public abstract Map<String, List<AbstractSelectElement>> getLocalizedHardwareList();
}