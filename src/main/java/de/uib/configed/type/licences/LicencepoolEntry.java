package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.TableEntry;

public class LicencepoolEntry extends TableEntry {

	public static final String idSERVICEKEY = "licensePoolId";
	public static final String idKEY = "id";
	public static final String descriptionKEY = "description";

	private static List<String> KEYS;
	static {
		KEYS = new ArrayList<String>();
		KEYS.add(idSERVICEKEY);
		KEYS.add(descriptionKEY);
	}

	static {
		new HashMap<String, String>();
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

	public LicencepoolEntry(Map entry) {
		super(entry);
		remap(idSERVICEKEY, idKEY);
		remap(descriptionKEY, descriptionKEY);
	}

	public String getLicencepoolId() {
		if (get(idSERVICEKEY) != null)
			return get(idSERVICEKEY);

		return get(idKEY);
	}

}
