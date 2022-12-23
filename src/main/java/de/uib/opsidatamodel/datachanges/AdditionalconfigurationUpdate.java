package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;

public class AdditionalconfigurationUpdate implements UpdateCommand {
	String objectId;
	Map newdata;

	PersistenceController persis;

	public AdditionalconfigurationUpdate(PersistenceController persis, String objectId, Map newdata) {
		this.objectId = objectId;
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
		// logging.debug(this, "doCall, setting " + newdata + ", class " +
		// newdata.getClass());
		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {
			de.uib.configed.type.ConfigName2ConfigValue configState = (de.uib.configed.type.ConfigName2ConfigValue) newdata;
			// logging.debug(this, "retrieved property:" + configState.getRetrieved());
			persis.setAdditionalConfiguration(objectId, configState);
			// for opsi 4.0, this only collects the data
		}
	}
}
