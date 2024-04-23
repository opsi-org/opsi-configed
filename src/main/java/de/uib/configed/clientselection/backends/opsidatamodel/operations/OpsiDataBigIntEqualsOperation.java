/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.BigIntEqualsOperation;
import de.uib.utils.logging.Logging;

public class OpsiDataBigIntEqualsOperation extends BigIntEqualsOperation implements ExecutableOperation {
	private String map;
	private String key;
	private long data;

	public OpsiDataBigIntEqualsOperation(String map, String key, long data, AbstractSelectElement element) {
		super(element);
		this.map = map;
		this.key = key;
		this.data = data;
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		Object realData = client.getMap(map).get(key);
		if (realData instanceof Long) {
			if ((Long) realData == data) {
				return true;
			}
		} else if (realData instanceof Integer) {
			if ((Integer) realData == data) {
				return true;
			}
		} else {
			Logging.error(this, "data is no BigInteger!" + realData);
		}

		return false;
	}
}
