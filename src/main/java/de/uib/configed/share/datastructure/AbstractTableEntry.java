/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share.datastructure;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractTableEntry extends LinkedHashMap<String, Object> {
	// very similar to RelationElement
	// static values and methods (resp. methods based on static values)
	// must be defined in subclasses

	// object values and methods
	protected Map<String, ? extends Object> entryRetrieved;

	protected AbstractTableEntry(Map<String, ? extends Object> entry) {
		entryRetrieved = entry;
	}

	protected void remap(String key, String keyRetrieved) {
		if (entryRetrieved.get(keyRetrieved) != null) {
			put(key, entryRetrieved.get(keyRetrieved));
		} else {
			put(key, "");
		}
	}
}
