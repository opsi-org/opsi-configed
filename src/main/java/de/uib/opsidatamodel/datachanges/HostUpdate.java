package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

public class HostUpdate extends MapUpdate {
	public HostUpdate(PersistenceController persis, Map newdata) {
		super(persis, newdata);
	}

	@Override
	public void doCall() {
		logging.debug(this, "doCall, newdata " + newdata);
		persis.setHostValues(newdata);
	}
}
