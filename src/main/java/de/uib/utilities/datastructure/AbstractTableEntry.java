package de.uib.utilities.datastructure;

import java.util.LinkedHashMap;
import java.util.Map;

import de.uib.utilities.logging.Logging;

public abstract class AbstractTableEntry extends LinkedHashMap<String, String> {
	// very similar to RelationElement
	// static values and methods (resp. methods based on static values)
	// must be defined in subclasses

	protected AbstractTableEntry(Map entry) {
		pureEntry = entry;
		entryRetrieved = entry;
	}

	// object values and methods
	protected Map pureEntry;
	protected Map<String, String> entryRetrieved;

	protected void remap(String key)
	// if key are identically named
	{
		remap(key, key);
	}

	protected void remap(String key, String keyRetrieved) {
		remap(key, keyRetrieved, true);
	}

	protected void remap(String key, String keyRetrieved, boolean replaceNull) {

		try {
			if (entryRetrieved.get(keyRetrieved) != null) {
				put(key, entryRetrieved.get(keyRetrieved));
			} else {
				if (replaceNull) {
					put(key, "");
				}
			}
		} catch (Exception ex) {
			Logging.debug(this, "remap keyRetrieved, exception " + ex);
			Logging.debug(this, "remap keyRetrieved " + keyRetrieved + ", value " + pureEntry.get(keyRetrieved)
					+ ", class " + pureEntry.get(keyRetrieved).getClass());
			put(key, "");
		}
	}
}
