/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.features.clientselection.operations.DateLessThanOperation;

public class OpsiDataDateLessThanOperation extends DateLessThanOperation implements ExecutableOperation {
	private AbstractOpsiDataDateMatcher matcher;

	public OpsiDataDateLessThanOperation(String map, String key, String data, AbstractSelectElement element) {
		super(element);

		matcher = new AbstractOpsiDataDateMatcher(map, key, data) {
			@Override
			protected boolean compare(java.sql.Date date, java.sql.Date realdate) {
				return realdate.before(date);
			}
		};
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		return matcher.doesMatch(client);
	}
}
