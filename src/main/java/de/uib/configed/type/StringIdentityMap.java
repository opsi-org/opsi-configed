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
