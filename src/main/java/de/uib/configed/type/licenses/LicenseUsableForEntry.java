/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licenses;

import java.util.HashMap;
import java.util.Map;

import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class LicenseUsableForEntry extends HashMap<String, String> {
	public static final String ID_KEY = "id";
	public static final String LICENSE_ID_KEY = "softwareLicenseId";
	public static final String LICENSE_POOL_ID_KEY = "licensePoolId";
	public static final String LICENSE_KEY_KEY = "licenseKey";

	public static LicenseUsableForEntry produceFrom(Map<String, Object> importedEntry) {
		LicenseUsableForEntry entry = new LicenseUsableForEntry();
		for (Entry<String, Object> imported : importedEntry.entrySet()) {
			entry.put(imported.getKey(), (String) imported.getValue());
		}
		if (importedEntry.get(LICENSE_ID_KEY) == null || importedEntry.get(LICENSE_POOL_ID_KEY) == null) {
			Logging.warning("LicenseUsableForEntry,  missing primary key in " + importedEntry);
		}

		String pseudokey = Utils.pseudokey(new String[] { entry.get(LICENSE_ID_KEY), entry.get(LICENSE_POOL_ID_KEY) });
		entry.put(ID_KEY, pseudokey);

		return entry;
	}

	public String getLicenseId() {
		return get(LICENSE_ID_KEY);
	}

	public String getLicensePoolId() {
		return get(LICENSE_POOL_ID_KEY);
	}
}
