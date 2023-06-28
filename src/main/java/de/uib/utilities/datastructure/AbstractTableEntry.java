/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.datastructure;

import java.util.LinkedHashMap;
import java.util.Map;

import de.uib.utilities.logging.Logging;

public abstract class AbstractTableEntry extends LinkedHashMap<String, String> {
	// very similar to RelationElement
	// static values and methods (resp. methods based on static values)
	// must be defined in subclasses

	// object values and methods
	protected Map<String, String> entryRetrieved;

	protected AbstractTableEntry(Map entry) {
		entryRetrieved = entry;
	}

	protected void remap(String key, String keyRetrieved) {
		try {
			if (entryRetrieved.get(keyRetrieved) != null) {
				put(key, entryRetrieved.get(keyRetrieved));
			} else {
				put(key, "");
			}
		} catch (Exception ex) {
			Logging.debug(this, "remap keyRetrieved, exception " + ex);

			put(key, "");
		}
	}
}
