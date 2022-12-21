package de.uib.opsidatamodel.datachanges;

import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;

public abstract class MapUpdate implements UpdateCommand {
	String objectId;
	Map newdata;

	PersistenceController persis;

	public MapUpdate(PersistenceController persis, Map newdata) {
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
	public abstract void doCall();
}
