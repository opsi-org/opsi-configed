/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.gui.clientselection.AbstractSelectElement;
import de.uib.configed.gui.clientselection.ExecutableOperation;
import de.uib.configed.gui.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.clientselection.operations.DateEqualsOperation;

public class OpsiDataDateEqualsOperation extends DateEqualsOperation implements ExecutableOperation {
	private AbstractOpsiDataDateMatcher matcher;

	public OpsiDataDateEqualsOperation(String map, String key, String data, AbstractSelectElement element) {
		super(element);

		matcher = new AbstractOpsiDataDateMatcher(map, key, data) {
			@Override
			protected boolean compare(java.sql.Date date, java.sql.Date realdate) {
				return date.equals(realdate);
			}
		};
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		return matcher.doesMatch(client);
	}
}
