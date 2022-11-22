package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.logging;

public class OpsiSoftwareEqualsOperation extends OpsiDataStringEqualsOperation // (implements ExecutableOperation)
{
	public OpsiSoftwareEqualsOperation(String key, String data, SelectElement element) {
		super(OpsiDataClient.SOFTWARE_MAP, key, data, element);
		logging.debug(this, "created  for key, data, element " + key + ", " + data + ", " + element);
	}

}
