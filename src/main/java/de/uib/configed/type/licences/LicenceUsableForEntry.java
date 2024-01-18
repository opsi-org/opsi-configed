/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licences;

import java.util.HashMap;
import java.util.Map;

import de.uib.utilities.logging.Logging;
import utils.Utils;

public class LicenceUsableForEntry extends HashMap<String, String> {
	public static final String ID_KEY = "id";
	public static final String LICENCE_ID_KEY = "softwareLicenseId";
	public static final String LICENSE_POOL_ID_KEY = "licensePoolId";
	public static final String LICENCE_KEY_KEY = "licenseKey";

	public static LicenceUsableForEntry produceFrom(Map<String, Object> importedEntry) {
		LicenceUsableForEntry entry = new LicenceUsableForEntry();
		for (Entry<String, Object> imported : importedEntry.entrySet()) {
			entry.put(imported.getKey(), (String) imported.getValue());
		}
		if (importedEntry.get(LICENCE_ID_KEY) == null || importedEntry.get(LICENSE_POOL_ID_KEY) == null) {
			Logging.warning("LicenceUsableForEntry,  missing primary key in " + importedEntry);
		}

		String pseudokey = Utils.pseudokey(new String[] { entry.get(LICENCE_ID_KEY), entry.get(LICENSE_POOL_ID_KEY) });
		entry.put(ID_KEY, pseudokey);

		return entry;
	}

	public String getLicenceId() {
		return get(LICENCE_ID_KEY);
	}

	public String getLicencePoolId() {
		return get(LICENSE_POOL_ID_KEY);
	}
}
