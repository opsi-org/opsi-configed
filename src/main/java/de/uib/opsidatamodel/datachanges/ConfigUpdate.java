package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.Logging;

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
		Logging.info(this, "doCall, setting class " + newdata.getClass() + ", the new data is " + newdata);

		persis.setConfig(newdata);
	}
}
