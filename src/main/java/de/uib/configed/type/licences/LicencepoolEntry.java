package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.TableEntry;

public class LicencepoolEntry extends TableEntry {

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
		assert KEYS.indexOf(key) > -1 : "not valid key " + key;

		if (KEYS.indexOf(key) > -1) {
			return super.put(key, value);
		}

		return null;

	}

	public LicencepoolEntry(Map<String, Object> entry) {
		super(entry);
		remap(ID_SERVICE_KEY, ID_KEY);
		remap(DESCRIPTION_KEY, DESCRIPTION_KEY);
	}

	public String getLicencepoolId() {
		if (get(ID_SERVICE_KEY) != null)
			return get(ID_SERVICE_KEY);

		return get(ID_KEY);
	}

}
