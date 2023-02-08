package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;

public abstract class AbstractMapUpdate implements UpdateCommand {
	String objectId;
	Map newdata;

	AbstractPersistenceController persis;

	protected AbstractMapUpdate(AbstractPersistenceController persis, Map newdata) {
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
}
