/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.HashMap;
import java.util.List;

public class StringIdentityMap extends HashMap<String, String> {
	public StringIdentityMap(List<String> keys) {
		super();

		if (keys != null) {
			for (String key : keys) {
				super.put(key, key);
			}
		}
	}

}
