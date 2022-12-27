package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.operations.DateGreaterOrEqualOperation;

public class OpsiDataDateGreaterOrEqualOperation extends DateGreaterOrEqualOperation implements ExecutableOperation {
	private OpsiDataDateMatcher matcher;

	public OpsiDataDateGreaterOrEqualOperation(String map, String key, String data, SelectElement element) {
		super(element);

		matcher = new OpsiDataDateMatcher(map, key, data) {
			@Override
			protected boolean compare(java.sql.Date date, java.sql.Date realdate) {

				return realdate.equals(date) || realdate.after(date);
			}
		};
	}

	@Override
	public boolean doesMatch(Client client) {
		return matcher.doesMatch(client);
	}
}
