package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;

public class AdditionalconfigurationUpdate implements UpdateCommand {
	private String objectId;
	private Map<?, ?> newdata;

	private AbstractPersistenceController persis;

	public AdditionalconfigurationUpdate(AbstractPersistenceController persis, String objectId, Map<?, ?> newdata) {
		this.objectId = objectId;
		this.newdata = newdata;
		setController(persis);
	}

	@Override
	public void setController(Object obj) {
		this.persis = (AbstractPersistenceController) obj;
	}

	@Override
	public Object getController() {
		return persis;
	}

	@Override
	public void doCall() {

		if (newdata instanceof de.uib.configed.type.ConfigName2ConfigValue) {
			de.uib.configed.type.ConfigName2ConfigValue configState = (de.uib.configed.type.ConfigName2ConfigValue) newdata;

			persis.setAdditionalConfiguration(objectId, configState);
			// for opsi 4.0, this only collects the data
		}
	}
}
