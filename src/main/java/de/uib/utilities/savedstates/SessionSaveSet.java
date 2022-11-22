package de.uib.utilities.savedstates;

import java.util.Set;

public class SessionSaveSet<T> extends SaveState {
	Set<T> saveObject;

	public SessionSaveSet() {
	}

	public void serialize(Object ob) {
		if (ob == null)
			saveObject = null;
		else
			saveObject = (Set<T>) ob;
	}

	public Object deserialize() {
		return saveObject;
	}
}
