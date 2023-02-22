package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.AbstractTableEntry;
import de.uib.utilities.logging.Logging;

public class LicencepoolEntry extends AbstractTableEntry {

	public static final String ID_SERVICE_KEY = "licensePoolId";
	public static final String ID_KEY = "id";
	public static final String DESCRIPTION_KEY = "description";

	private static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(ID_SERVICE_KEY);
		KEYS.add(DESCRIPTION_KEY);
	}

	public static List<String> getKeys() {
		return KEYS;
	}

	@Override
	public String put(String key, String value) {
		if (KEYS.indexOf(key) <= -1) {
			Logging.error(this, "not valid key: " + key);
			return null;
		} else {
			return super.put(key, value);
		}
	}

	public LicencepoolEntry(Map<String, Object> entry) {
		super(entry);
		remap(ID_SERVICE_KEY, ID_KEY);
		remap(DESCRIPTION_KEY, DESCRIPTION_KEY);
	}

	public String getLicencepoolId() {
		if (get(ID_SERVICE_KEY) != null) {
			return get(ID_SERVICE_KEY);
		}

		return get(ID_KEY);
	}

}
