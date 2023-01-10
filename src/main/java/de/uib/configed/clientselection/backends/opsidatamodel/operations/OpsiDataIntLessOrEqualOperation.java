package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.IntLessOrEqualOperation;
import de.uib.utilities.logging.Logging;

public class OpsiDataIntLessOrEqualOperation extends IntLessOrEqualOperation implements ExecutableOperation {
	private String map;
	private String key;
	private int data;

	public OpsiDataIntLessOrEqualOperation(String map, String key, int data, SelectElement element) {
		super(element);
		this.map = map;
		this.key = key;
		this.data = data;
	}

	@Override
	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		Map realMap = oClient.getMap(map);
		if (!realMap.containsKey(key) || realMap.get(key) == null) {
			Logging.debug(this, "key " + key + " not found!");
			return false;
		}

		Object realData = realMap.get(key);
		if (realData instanceof Integer) {
			if ((Integer) realData <= data)
				return true;
		} else {
			Logging.warning(this, "data is no Integer!");
		}
		return false;
	}
}