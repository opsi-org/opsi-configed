package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.utilities.logging.logging;

public class OpsiDataSoftwareOperation extends SoftwareOperation implements ExecutableOperation {
	protected Map<String, Map<String, String>> productDefaultStates;
	protected Set<String> productsWithDefaultValues;
	protected de.uib.opsidatamodel.PersistenceController controller;

	public OpsiDataSoftwareOperation(SelectOperation operation) {
		super(operation);
		controller = de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController();
		if (controller == null)
			logging.warning(this, "Warning, controller is null!");
		productDefaultStates = controller.getProductDefaultStates();
		productsWithDefaultValues = new TreeSet<>(productDefaultStates.keySet());
	}

	@Override
	public boolean doesMatch(Client client) {
		logging.debug(this, "doesMatch starting");
		OpsiDataClient oClient = (OpsiDataClient) client;
		
		List softwareSet = oClient.getSoftwareList();
		List<String> theProductNames = oClient.getProductNames();
		TreeSet<String> productsWithDefaultValues_client = new TreeSet<>(productsWithDefaultValues);
		
		// logging.debug(this, "doesMatch productsWithDefaultValues_client " +
		
		
		productsWithDefaultValues_client.removeAll(theProductNames);
		// logging.debug(this, "doesMatch productsWithDefaultValues " +
		

		

		for (Object value : softwareSet) {
			if (value instanceof Map) {
				oClient.setCurrentSoftwareValue((Map) value);
				logging.debug(this,
						" getChildOperations().get(0) instance of " + (getChildOperations().get(0)).getClass());
				if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client))
					return true;
			} else {
				logging.error(this, "Software map returned bad value (not a Map)");
			}
		}

		for (String product : productsWithDefaultValues_client) {
			oClient.setCurrentSoftwareValue(productDefaultStates.get(product));
			logging.debug(this, " getChildOperations().get(0) check default product values, instance of "
					+ (getChildOperations().get(0)).getClass());
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client))
				return true;
		}

		return false;
	}
}
