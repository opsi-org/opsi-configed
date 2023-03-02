package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.StringEqualsOperation;
import de.uib.utilities.logging.Logging;

public class OpsiDataStringEqualsOperation extends StringEqualsOperation implements ExecutableOperation {
	protected String map;
	protected String key;
	protected String data;
	protected String[] dataSplitted;
	protected boolean startsWith;
	protected boolean endsWith;

	public OpsiDataStringEqualsOperation(String map, String key, String data, AbstractSelectElement element) {
		super(element);
		Logging.debug(this, "OpsiDataStringEqualsOperation maptype, key, data: " + map + ", " + key + ", " + data);
		this.map = map;
		this.key = key;
		this.data = data.toLowerCase();
		if (data.contains("*")) {
			dataSplitted = (this.data).split("\\*");
			Logging.debug(this, "OpsiDataStringEqualsOperation " + dataSplitted.length);
		}
		startsWith = data.startsWith("*");
		endsWith = data.endsWith("*");

	}

	@Override
	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		Logging.debug(this, " (OpsiDataStringEqualsOperation) doesMatch client " + oClient);

		Map realMap = oClient.getMap(map);
		Logging.debug(this, "doesMatch,  we look into map for key " + key);
		if (!realMap.containsKey(key) || realMap.get(key) == null) {

			return false;
		}

		String realData = realMap.get(key).toString().toLowerCase();
		Logging.debug(this, " (OpsiDataStringEqualsOperation) doesMatch realData " + realData);
		return checkData(realData);
	}

	protected boolean checkData(final String realData) {

		String rData = realData.toLowerCase();

		// simple case: no '*'
		if (dataSplitted == null) {
			return rData.equals(data);
		}

		// the only chars are '*'
		else if (dataSplitted.length == 0) {
			return realData.length() > 0;
		} else {

			if (!startsWith && !rData.startsWith(dataSplitted[0])) {
				return false;
			}

			int index = 0;
			int i = 0;
			while (i < dataSplitted.length && index >= 0) {

				if (!dataSplitted[i].isEmpty()) {
					index = rData.indexOf(dataSplitted[i], index);
					if (index >= 0) {
						index += dataSplitted[i].length();
					}
				}

				i++;
			}
			return index >= 0 && (endsWith || rData.endsWith(dataSplitted[dataSplitted.length - 1]));
		}
	}
}
