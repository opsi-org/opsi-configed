package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.operations.DateLessOrEqualOperation;

public class OpsiDataDateLessOrEqualOperation extends DateLessOrEqualOperation implements ExecutableOperation {
	private AbstractOpsiDataDateMatcher matcher;

	public OpsiDataDateLessOrEqualOperation(String map, String key, String data, AbstractSelectElement element) {
		super(element);

		matcher = new AbstractOpsiDataDateMatcher(map, key, data) {
			@Override
			protected boolean compare(java.sql.Date date, java.sql.Date realdate) {
				return date.equals(realdate) || realdate.before(date);
			}
		};
	}

	@Override
	public boolean doesMatch(Client client) {
		return matcher.doesMatch(client);
	}
}
