package de.uib.configed.type;

import java.util.HashMap;

public class StringIdentityMap extends HashMap<String, String> {
	public StringIdentityMap(java.util.List<String> keys) {
		super();
		if (keys != null) {
			for (String key : keys) {
				put(key, key);
			}
		}
	}

}
