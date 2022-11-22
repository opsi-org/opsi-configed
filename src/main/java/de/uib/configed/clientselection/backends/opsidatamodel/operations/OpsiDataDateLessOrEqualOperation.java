package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.operations.DateLessOrEqualOperation;

public class OpsiDataDateLessOrEqualOperation extends DateLessOrEqualOperation implements ExecutableOperation {
	private OpsiDataDateMatcher matcher;

	public OpsiDataDateLessOrEqualOperation(String map, String key, String data, SelectElement element) {
		super(element);

		matcher = new OpsiDataDateMatcher(map, key, data, element) {
			@Override
			protected boolean compare(java.sql.Date date, java.sql.Date realdate) {
				return date.equals(realdate) || realdate.before(date);
			}
		};
	}

	public boolean doesMatch(Client client) {
		return matcher.doesMatch(client);
	}
}
