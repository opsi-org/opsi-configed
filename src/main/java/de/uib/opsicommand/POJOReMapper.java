/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class POJOReMapper {
	private POJOReMapper() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T remap(Object obj) {
		return (T) obj;
	}

	public static boolean equalsNull(String ob) {
		return ob == null || "null".equalsIgnoreCase(ob);
	}

	public static Map<String, String> giveEmptyForNull(Map<String, Object> m) {
		Map<String, String> result = new HashMap<>();
		for (Entry<String, Object> entry : m.entrySet()) {
			if (entry.getValue() == null) {
				result.put(entry.getKey(), "");
			} else {
				result.put(entry.getKey(), "" + entry.getValue());
			}
		}

		return result;
	}
}
