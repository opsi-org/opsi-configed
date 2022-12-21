package de.uib.utilities.savedstates;

import java.util.Set;

public class SessionSaveSet<T> extends SaveState {
	Set<T> saveObject;

	public SessionSaveSet() {
	}

	@Override
	public void serialize(Object ob) {
		if (ob == null)
			saveObject = null;
		else
			saveObject = (Set<T>) ob;
	}

	@Override
	public Object deserialize() {
		return saveObject;
	}
}
