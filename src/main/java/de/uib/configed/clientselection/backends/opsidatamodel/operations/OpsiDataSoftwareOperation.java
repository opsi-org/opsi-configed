package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class OpsiDataSoftwareOperation extends SoftwareOperation implements ExecutableOperation {
	private Map<String, Map<String, String>> productDefaultStates;
	private Set<String> productsWithDefaultValues;
	private de.uib.opsidatamodel.OpsiserviceNOMPersistenceController controller;

	public OpsiDataSoftwareOperation(AbstractSelectOperation operation) {
		super(operation);
		controller = PersistenceControllerFactory.getPersistenceController();
		if (controller == null) {
			Logging.warning(this, "Warning, controller is null!");
		}
		productDefaultStates = controller.getProductDefaultStates();
		productsWithDefaultValues = new TreeSet<>(productDefaultStates.keySet());
	}

	@Override
	public boolean doesMatch(Client client) {
		Logging.debug(this, "doesMatch starting");
		OpsiDataClient oClient = (OpsiDataClient) client;

		List<Map<String, String>> softwareSet = oClient.getSoftwareList();
		List<String> theProductNames = oClient.getProductNames();
		TreeSet<String> productsWithDefaultValuesClient = new TreeSet<>(productsWithDefaultValues);

		productsWithDefaultValuesClient.removeAll(theProductNames);

		for (Map<String, String> value : softwareSet) {
			if (value instanceof Map) {
				oClient.setCurrentSoftwareValue(value);
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
			oClient.setCurrentSoftwareValue(productDefaultStates.get(product));
			Logging.debug(this, " getChildOperations().get(0) check default product values, instance of "
					+ (getChildOperations().get(0)).getClass());
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {
				return true;
			}
		}

		return false;
	}
}
