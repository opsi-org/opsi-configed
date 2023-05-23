package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.logging.Logging;

public class ConfigUpdate implements UpdateCommand {
	private Map newdata;

	private OpsiserviceNOMPersistenceController persis;

	public ConfigUpdate(OpsiserviceNOMPersistenceController persis, Map newdata) {
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (OpsiserviceNOMPersistenceController) obj;
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
