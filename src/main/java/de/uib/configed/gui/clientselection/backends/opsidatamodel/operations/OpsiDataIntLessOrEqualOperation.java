/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.clientselection.backends.opsidatamodel.operations;

import java.util.Map;

import de.uib.configed.gui.clientselection.AbstractSelectElement;
import de.uib.configed.gui.clientselection.ExecutableOperation;
import de.uib.configed.gui.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.clientselection.operations.IntLessOrEqualOperation;
import de.uib.configed.share.logging.Logging;

public class OpsiDataIntLessOrEqualOperation extends IntLessOrEqualOperation implements ExecutableOperation {
	private String map;
	private String key;
	private int data;

	public OpsiDataIntLessOrEqualOperation(String map, String key, int data, AbstractSelectElement element) {
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
			return integerData <= data;
		} else {
			Logging.warning(this, "data is no Integer!");
		}
		return false;
	}
}
