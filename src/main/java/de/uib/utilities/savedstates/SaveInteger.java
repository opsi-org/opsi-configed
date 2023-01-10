package de.uib.utilities.savedstates;

import de.uib.utilities.logging.Logging;

public class SaveInteger extends SaveState {
	public SaveInteger(String key, Object defaultValue, SavedStates states) {
		super(key, defaultValue, states);
		if (!(defaultValue instanceof Integer))
			Logging.error("default value must be Integer");
	}

	public void serialize(final Integer value, Integer minValue) {
		if (minValue == null || value == null)
			serialize(value);
		else {
			int val0 = value;
			if (val0 < minValue)
				val0 = minValue;
			serialize(val0);
		}
	}

	@Override
	public void serialize(Object value) {
		states.setProperty(key, value.toString());
		states.store();
	}

	@Override
	public String deserialize() {
		Logging.info(this, "deserialize states" + states);
		Logging.info(this, "deserialize  getProperty " + states.getProperty(key, defaultValue.toString()));
		return states.getProperty(key, defaultValue.toString());
	}

	public Integer deserializeAsInt() {
		Integer result = null;
		try {
			result = Integer.valueOf(deserialize());
		} catch (Exception ex) {
			Logging.warning(this, "deserializeAsInt error " + ex);
		}
		if (result == null)
			result = (Integer) defaultValue;

		return result;
	}

}
