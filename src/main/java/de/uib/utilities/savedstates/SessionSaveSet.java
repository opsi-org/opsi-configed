package de.uib.utilities.savedstates;

import java.util.Set;

public class SessionSaveSet<T> extends AbstractSaveState {
	Set<T> saveObject;

	@Override
	public void serialize(Object ob) {
		if (ob == null)
			saveObject = null;
		else
			saveObject = (Set<T>) ob;
	}

	@Override
	public Set<T> deserialize() {
		return saveObject;
	}
}
