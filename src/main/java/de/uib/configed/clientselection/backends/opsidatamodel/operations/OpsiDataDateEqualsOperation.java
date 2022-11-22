package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.operations.DateEqualsOperation;

public class OpsiDataDateEqualsOperation extends DateEqualsOperation implements ExecutableOperation {
	private OpsiDataDateMatcher matcher;

	public OpsiDataDateEqualsOperation(String map, String key, String data, SelectElement element) {
		super(element);

		matcher = new OpsiDataDateMatcher(map, key, data, element) {
			@Override
			protected boolean compare(java.sql.Date date, java.sql.Date realdate) {
				return date.equals(realdate);
			}
		};
	}

	public boolean doesMatch(Client client) {
		return matcher.doesMatch(client);
	}
}
