/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.features.clientselection.operations.SoftwareOperation;
import de.uib.configed.share.logging.Logging;

public class OpsiDataSoftwareOperation extends SoftwareOperation implements ExecutableOperation {
	private Map<String, Map<String, String>> productDefaultStates;
	private Set<String> productsWithDefaultValues;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public OpsiDataSoftwareOperation(AbstractSelectOperation operation) {
		super(operation);

		productDefaultStates = persistenceController.getProductDataService().getProductDefaultStatesPD();
		productsWithDefaultValues = new HashSet<>(productDefaultStates.keySet());
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		Logging.debug(this, "doesMatch starting");
		ExecutableOperation childOperation = (ExecutableOperation) getChildOperations().get(0);
		Logging.debug(this, " getChildOperations().get(0) check default product values, instance of ",
				childOperation.getClass());

		List<Map<String, String>> softwareSet = client.getSoftwareList();
		for (Map<String, String> value : softwareSet) {
			client.setCurrentSoftwareValue(value);
			if (childOperation.doesMatch(client)) {
				return true;
			}
		}

		Set<String> clientProductNames = new HashSet<>(client.getProductNames());

		// We go through 
		for (String product : productsWithDefaultValues) {
			if (!clientProductNames.contains(product)) {
				client.setCurrentSoftwareValue(productDefaultStates.get(product));
				if (childOperation.doesMatch(client)) {
					return true;
				}
			}
		}

		return false;
	}
}
