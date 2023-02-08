package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;

public class HostUpdate extends AbstractMapUpdate {
	public HostUpdate(AbstractPersistenceController persis, Map newdata) {
		super(persis, newdata);
	}

	@Override
	public void doCall() {
		Logging.debug(this, "doCall, newdata " + newdata);
		persis.setHostValues(newdata);
	}
}
