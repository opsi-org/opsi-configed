package de.uib.utilities.savedstates;

public abstract class AbstractSaveState {
	protected String key;
	protected Object defaultValue;
	protected SavedStates states;

	protected AbstractSaveState() {
	}

	protected AbstractSaveState(String key, Object defaultValue, SavedStates states) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.states = states;
		// states.addKey(key, ""); //for classifiedpropertiesstore
	}

	public void setDefaultValue(Object val) {
		defaultValue = val;
	}

	public void serialize(Object ob) {
		states.store();
		// we store every time when we add an object
	}

	public Object deserialize() {
		return null;
	}
}
