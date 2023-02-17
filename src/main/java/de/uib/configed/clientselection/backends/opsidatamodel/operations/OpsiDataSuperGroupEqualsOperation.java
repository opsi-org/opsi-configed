package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.Logging;

public class OpsiDataSuperGroupEqualsOperation extends OpsiDataStringEqualsOperation {
	private static boolean issuedTreeError = false;

	public OpsiDataSuperGroupEqualsOperation(String data, AbstractSelectElement element) {
		super(OpsiDataClient.HOSTINFO_MAP, "", data, element);
	}

	@Override
	public boolean doesMatch(Client client) {

		OpsiDataClient oClient = (OpsiDataClient) client;
		if (oClient.getSuperGroups() == null) {
			if (!issuedTreeError) {
				Logging.debug(
						"Selection by tree structure not possible in headless mode, please remove this selection criterion.");
				Logging.debug("( The tree is built by the visual component.) ");
				issuedTreeError = true;
			}
			return false;
		}

		for (String group : oClient.getSuperGroups()) {
			if (checkData(group)) {
				return true;
			}
		}
		return false;
	}
}