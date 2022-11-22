package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.BigIntGreaterOrEqualOperation;
import de.uib.utilities.logging.logging;

public class OpsiDataBigIntGreaterOrEqualOperation extends BigIntGreaterOrEqualOperation
		implements ExecutableOperation {
	private String map;
	private String key;
	private long data;

	public OpsiDataBigIntGreaterOrEqualOperation(String map, String key, long data, SelectElement element) {
		super(element);
		this.map = map;
		this.key = key;
		this.data = data;
	}

	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		Map realMap = oClient.getMap(map);
		if (!realMap.containsKey(key) || realMap.get(key) == null) {
			logging.debug(this, "key " + key + " not found!");
			return false;
		}

		Object realData = realMap.get(key);
		if (realData instanceof Long) {
			if ((Long) realData >= data)
				return true;
		} else {
			if (realData instanceof Integer) {
				if ((Integer) realData >= data)
					return true;
			} else {
				logging.error(this, "data is no BigInteger!" + realData);
			}
		}
		return false;
	}
}