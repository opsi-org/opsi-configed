/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.BigIntLessThanOperation;
import de.uib.utilities.logging.Logging;

public class OpsiDataBigIntLessThanOperation extends BigIntLessThanOperation implements ExecutableOperation {
	private String map;
	private String key;
	private long data;

	public OpsiDataBigIntLessThanOperation(String map, String key, long data, AbstractSelectElement element) {
		super(element);
		this.map = map;
		this.key = key;
		this.data = data;
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		Map<String, Object> realMap = client.getMap(map);
		if (!realMap.containsKey(key) || realMap.get(key) == null) {
			Logging.debug(this, "key " + key + " not found!");
			return false;
		}

		Object realData = realMap.get(key);
		Logging.debug(this, realData.getClass().getCanonicalName());
		if (realData instanceof Long) {
			if ((Long) realData < data) {
				return true;
			}
		} else {
			if (realData instanceof Integer) {
				if ((Integer) realData < data) {
					return true;
				}
			} else {
				Logging.error(this, "data is no BigInteger!" + realData);
			}
		}
		return false;
	}
}
