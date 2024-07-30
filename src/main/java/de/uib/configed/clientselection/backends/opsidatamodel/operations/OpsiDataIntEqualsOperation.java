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
import de.uib.configed.clientselection.operations.IntEqualsOperation;
import de.uib.utils.logging.Logging;

public class OpsiDataIntEqualsOperation extends IntEqualsOperation implements ExecutableOperation {
	private String map;
	private String key;
	private int data;

	public OpsiDataIntEqualsOperation(String map, String key, int data, AbstractSelectElement element) {
		super(element);
		this.map = map;
		this.key = key;
		this.data = data;
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		Map<String, Object> realMap = client.getMap(map);
		if (!realMap.containsKey(key) || realMap.get(key) == null) {
			Logging.debug(this, "key ", key, " not found!");
			return false;
		}

		Object realData = realMap.get(key);
		if (realData instanceof Integer integerData) {
			if (integerData == data) {
				return true;
			}
		} else {
			Logging.warning(this, "data is no Integer!");
		}
		return false;
	}
}
