package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.operations.DateLessThanOperation;

public class OpsiDataDateLessThanOperation extends DateLessThanOperation implements ExecutableOperation {
	private OpsiDataDateMatcher matcher;

	public OpsiDataDateLessThanOperation(String map, String key, String data, SelectElement element) {
		super(element);

		matcher = new OpsiDataDateMatcher(map, key, data) {
			@Override
			protected boolean compare(java.sql.Date date, java.sql.Date realdate) {
				return realdate.before(date);
			}
		};
	}

	@Override
	public boolean doesMatch(Client client) {
		return matcher.doesMatch(client);
	}
}
