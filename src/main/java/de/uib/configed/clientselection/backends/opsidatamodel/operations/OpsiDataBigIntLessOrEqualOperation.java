package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.BigIntLessOrEqualOperation;
import de.uib.utilities.logging.Logging;

public class OpsiDataBigIntLessOrEqualOperation extends BigIntLessOrEqualOperation implements ExecutableOperation {
	private String map;
	private String key;
	private long data;

	public OpsiDataBigIntLessOrEqualOperation(String map, String key, long data, AbstractSelectElement element) {
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
		Logging.debug(this, realData.getClass().getCanonicalName());
		if (realData instanceof Long) {
			if ((Long) realData <= data) {
				return true;
			}
		} else {
			if (realData instanceof Integer) {
				if ((Integer) realData <= data) {
					return true;
				}
			} else {
				Logging.error(this, "data is no BigInteger!" + realData);
			}
		}
		return false;
	}
}