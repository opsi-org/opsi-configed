package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;

public class HostUpdate implements UpdateCommand {

	String objectId;
	Map<String, Object> newdata;

	AbstractPersistenceController persis;

	public HostUpdate(AbstractPersistenceController persis, Map<String, Object> newdata) {
		super();
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void doCall() {
		Logging.debug(this, "doCall, newdata " + newdata);
		persis.setHostValues(newdata);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (AbstractPersistenceController) obj;
	}

	@Override
	public Object getController() {
		return persis;
	}
}
