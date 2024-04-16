/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class OpsiDataSoftwareOperation extends SoftwareOperation implements ExecutableOperation {
	private Map<String, Map<String, String>> productDefaultStates;
	private Set<String> productsWithDefaultValues;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public OpsiDataSoftwareOperation(AbstractSelectOperation operation) {
		super(operation);

		productDefaultStates = persistenceController.getProductDataService().getProductDefaultStatesPD();
		productsWithDefaultValues = new TreeSet<>(productDefaultStates.keySet());
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		Logging.debug(this, "doesMatch starting");

		List<Map<String, String>> softwareSet = client.getSoftwareList();
		List<String> theProductNames = client.getProductNames();
		TreeSet<String> productsWithDefaultValuesClient = new TreeSet<>(productsWithDefaultValues);

		productsWithDefaultValuesClient.removeAll(theProductNames);

		for (Map<String, String> value : softwareSet) {
			if (value instanceof Map) {
				client.setCurrentSoftwareValue(value);
				Logging.debug(this,
						" getChildOperations().get(0) instance of " + (getChildOperations().get(0)).getClass());
				if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {
					return true;
				}
			} else {
				Logging.error(this, "Software map returned bad value (not a Map)");
			}
		}

		for (String product : productsWithDefaultValuesClient) {
			client.setCurrentSoftwareValue(productDefaultStates.get(product));
			Logging.debug(this, " getChildOperations().get(0) check default product values, instance of "
					+ (getChildOperations().get(0)).getClass());
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {
				return true;
			}
		}

		return false;
	}
}
