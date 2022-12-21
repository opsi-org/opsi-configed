package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

public class ConfigUpdate implements UpdateCommand {
	String objectId;
	Map newdata;

	PersistenceController persis;

	public ConfigUpdate(PersistenceController persis, Map newdata) {
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (PersistenceController) obj;
	}

	@Override
	public Object getController() {
		return persis;
	}

	@Override
	public void doCall() {
		logging.info(this, "doCall, setting class " + newdata.getClass() + ", the new data is " + newdata);
		// logging.debug(this, "retrieved property:" + config.getRetrieved());
		persis.setConfig(newdata);
	}
}
