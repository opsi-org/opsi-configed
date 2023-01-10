package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class LicenceUsableForEntry extends HashMap<String, String> {
	/*
	 * desc SOFTWARE_LICENSE_TO_LICENSE_POOL
	 * | Field | Type | Null | Key | Default | Extra
	 * | softwareLicenseId | varchar(100) | NO | PRI | NULL |
	 * | licensePoolId | varchar(100) | NO | PRI | NULL |
	 * | licenseKey | varchar(100) | NO |
	 * 
	 */

	public static final String ID_KEY = "id";
	public static final String LICENCE_ID_KEY = "softwareLicenseId";
	public static final String LICENSE_POOL_ID_KEY = "licensePoolId";
	public static final String LICENCE_KEY_KEY = "licenseKey";

	private static List<String> KEYS;
	static {
		KEYS = new ArrayList<>();
		KEYS.add(LICENCE_ID_KEY);
		KEYS.add(LICENSE_POOL_ID_KEY);
		KEYS.add(LICENCE_KEY_KEY);
	}

	public static LicenceUsableForEntry produceFrom(Map<String, Object> importedEntry) {
		LicenceUsableForEntry entry = new LicenceUsableForEntry();
		for (String key : importedEntry.keySet()) {
			entry.put(key, (String) importedEntry.get(key));
		}
		if (importedEntry.get(LICENCE_ID_KEY) == null || importedEntry.get(LICENSE_POOL_ID_KEY) == null)
			Logging.warning("LicenceUsableForEntry,  missing primary key in " + importedEntry);

		String pseudokey = Globals
				.pseudokey(new String[] { entry.get(LICENCE_ID_KEY), entry.get(LICENSE_POOL_ID_KEY) });

		entry.put(ID_KEY, pseudokey);

		return entry;

	}

	public String getId() {
		return get(ID_KEY);
	}

	public String getLicenceId() {
		return get(LICENCE_ID_KEY);
	}

	public String getLicencePoolId() {
		return get(LICENSE_POOL_ID_KEY);
	}

}
