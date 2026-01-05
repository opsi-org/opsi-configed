/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.features.clientselection.operations.BooleanEqualsOperation;
import de.uib.configed.share.logging.Logging;

public class OpsiDataBooleanEqualsOperation extends BooleanEqualsOperation implements ExecutableOperation {
	private String map;
	private String key;
	private boolean data;

	public OpsiDataBooleanEqualsOperation(String map, String key, boolean data,
			de.uib.configed.gui.features.clientselection.AbstractSelectElement element) {
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

		Object realData = client.getMap(map).get(key);
		if (realData instanceof Integer integerData) {
			return integerData.equals(1) == data;
		} else {
			Logging.warning(this, "data is no Boolean!");
		}
		return false;
	}
}
