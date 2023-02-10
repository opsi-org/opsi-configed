package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;

public class OpsiDataGroupEqualsOperation extends OpsiDataStringEqualsOperation {
	public OpsiDataGroupEqualsOperation(String data, AbstractSelectElement element) {
		super(OpsiDataClient.HOSTINFO_MAP, "", data, element);
	}

	@Override
	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		for (String obj : oClient.getGroups()) {
			String group = obj;
			if (checkData(group))
				return true;
		}
		return false;
	}
}