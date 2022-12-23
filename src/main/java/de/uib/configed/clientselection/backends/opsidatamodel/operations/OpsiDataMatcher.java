package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.SelectElement;

import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.logging;

public abstract class OpsiDataMatcher {
	protected String map;
	protected String key;
	protected String data;

	public OpsiDataMatcher(String map, String key, String data, SelectElement element) {
		logging.debug(this, "created:  maptype, key, data: " + map + ", " + key + ", " + data);

		this.map = map;
		this.key = key;
		this.data = data;
	}

	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		logging.debug(this, "doesMatch client " + oClient);
		
		Map realMap = oClient.getMap(map);
		
		if (!realMap.containsKey(key) || realMap.get(key) == null) {
			
			return false;
		}

		String realData = realMap.get(key).toString();
		
		return checkData(realData);
	}

	protected abstract boolean checkData(final String realdata);
}
