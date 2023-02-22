package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.Logging;

public class OpsiSoftwareEqualsOperation extends OpsiDataStringEqualsOperation {
	public OpsiSoftwareEqualsOperation(String key, String data, AbstractSelectElement element) {
		super(OpsiDataClient.SOFTWARE_MAP, key, data, element);
		Logging.debug(this, "created  for key, data, element " + key + ", " + data + ", " + element);
	}

}
