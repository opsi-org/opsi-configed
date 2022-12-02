package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.logging;

public class OpsiDataSuperGroupEqualsOperation extends OpsiDataStringEqualsOperation {
	private static boolean issuedTreeError = false;

	public OpsiDataSuperGroupEqualsOperation(String data, SelectElement element) {
		super(OpsiDataClient.HOSTINFO_MAP, "", data, element);
	}

	@Override
	public boolean doesMatch(Client client) {
		// logging.debug( " ------------ ");
		// logging.debug( " client " + client );
		OpsiDataClient oClient = (OpsiDataClient) client;
		if (oClient.getSuperGroups() == null) {
			if (!issuedTreeError) {
				logging.debug(
						"Selection by tree structure not possible in headless mode, please remove this selection criterion.");
				logging.debug("( The tree is built by the visual component.) ");
				issuedTreeError = true;
			}
			return false;
		}

		for (Object obj : oClient.getSuperGroups()) {
			String group = (String) obj;
			if (checkData(group))
				return true;
		}
		return false;
	}
}