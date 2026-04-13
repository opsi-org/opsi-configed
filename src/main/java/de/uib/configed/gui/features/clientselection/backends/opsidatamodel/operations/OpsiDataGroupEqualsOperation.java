/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;

public class OpsiDataGroupEqualsOperation extends OpsiDataStringEqualsOperation {
	public OpsiDataGroupEqualsOperation(String data, AbstractSelectElement element) {
		super(OpsiDataClient.HOSTINFO_MAP, "", data, element);
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		for (String obj : client.getGroups()) {
			String group = obj;
			if (checkData(group)) {
				return true;
			}
		}
		return false;
	}
}
