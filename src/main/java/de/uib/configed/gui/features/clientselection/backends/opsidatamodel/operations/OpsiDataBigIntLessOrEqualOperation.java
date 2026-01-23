/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.features.clientselection.operations.BigIntLessOrEqualOperation;
import de.uib.configed.share.Utils;

public class OpsiDataBigIntLessOrEqualOperation extends BigIntLessOrEqualOperation implements ExecutableOperation {
	private String map;
	private String key;
	private long data;

	public OpsiDataBigIntLessOrEqualOperation(String map, String key, long data, AbstractSelectElement element) {
		super(element);
		this.map = map;
		this.key = key;
		this.data = data;
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		return Utils.compareNumeric(client.getMap(map).get(key), data, (a, b) -> a <= b);
	}
}
