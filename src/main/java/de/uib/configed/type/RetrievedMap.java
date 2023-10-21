/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.HashMap;
import java.util.Map;

public class RetrievedMap extends HashMap<String, Object> {
	// pass the original map
	protected Map<String, Object> retrieved;

	public RetrievedMap(Map<String, Object> retrieved) {
		super();
		this.retrieved = retrieved;
		build();
	}

	protected RetrievedMap() {
	}

	protected void build() {
		if (retrieved != null) {
			putAll(retrieved);
		}
	}

	public Map<String, Object> getRetrieved() {
		return retrieved;
	}

	public void rebuild() {
		build();
	}
}
