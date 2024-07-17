/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licenses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.utils.datastructure.AbstractTableEntry;
import de.uib.utils.logging.Logging;

public class LicensepoolEntry extends AbstractTableEntry {
	public static final String ID_SERVICE_KEY = "licensePoolId";
	public static final String ID_KEY = "id";
	public static final String DESCRIPTION_KEY = "description";

	private static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(ID_SERVICE_KEY);
		KEYS.add(DESCRIPTION_KEY);
	}

	public LicensepoolEntry(Map<String, Object> entry) {
		super(entry);
		super.remap(ID_SERVICE_KEY, ID_KEY);
		super.remap(DESCRIPTION_KEY, DESCRIPTION_KEY);
	}

	@Override
	public String put(String key, String value) {
		if (KEYS.indexOf(key) <= -1) {
			Logging.error(this, "not valid key: ", key);
			return null;
		} else {
			return super.put(key, value);
		}
	}

	public String getLicensepoolId() {
		if (get(ID_SERVICE_KEY) != null) {
			return get(ID_SERVICE_KEY);
		}

		return get(ID_KEY);
	}
}
