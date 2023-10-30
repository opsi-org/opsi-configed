/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.Logging;

public class OpsiDataSuperGroupEqualsOperation extends OpsiDataStringEqualsOperation {
	private static boolean issuedTreeError;

	public OpsiDataSuperGroupEqualsOperation(String data, AbstractSelectElement element) {
		super(OpsiDataClient.HOSTINFO_MAP, "", data, element);
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		if (client.getSuperGroups() == null) {
			if (!issuedTreeError) {
				Logging.debug(
						"Selection by tree structure not possible in headless mode, please remove this selection criterion.");
				Logging.debug("( The tree is built by the visual component.) ");
				issuedTreeError = true;
			}
			return false;
		}

		for (String group : client.getSuperGroups()) {
			if (checkData(group)) {
				return true;
			}
		}
		return false;
	}
}
