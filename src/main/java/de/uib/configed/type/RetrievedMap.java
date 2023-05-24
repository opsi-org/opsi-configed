/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RetrievedMap extends HashMap<String, Object> {
	// pass the original map
	protected Map<String, Object> retrieved;
	protected Map<String, String> classnames;

	public RetrievedMap(Map<String, Object> retrieved) {
		super();
		this.retrieved = retrieved;
		classnames = new HashMap<>();
		build();
	}

	protected void build() {
		if (retrieved == null) {
			return;
		}

		Iterator<String> iter = retrieved.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Object value = retrieved.get(key);
			classnames.put(key, value.getClass().getName());
			put(key, value);
		}
	}

	public Map<String, Object> getRetrieved() {
		return retrieved;
	}

	public void rebuild() {
		build();
	}

	public Map<String, String> getClassnames() {
		return classnames;
	}

}
