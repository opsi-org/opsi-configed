/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

/**
 * The core data structure representing a single row in the generic table. It
 * holds the values, the visual state (for diffing), and the row ID.
 */
@Value
@Builder(toBuilder = true)
public class RowData {
	private final String id;
	private final Map<String, Object> values;
	private final RowState state;

	/**
	 * Enum representing the lifecycle state of a row.
	 */
	public enum RowState {
		NORMAL, MODIFIED, DELETED, NEW
	}

	/**
	 * Helper to get a value safely.
	 */
	public <T> T getValue(String key, Class<T> type) {
		Object val = values.get(key);
		return val == null ? null : type.cast(val);
	}

	public static List<RowData> fromOriginalSnapshot(Iterable<Map<String, Object>> original) {
		List<RowData> result = new ArrayList<>();
		original.forEach((Map<String, Object> map) -> {
			RowData rowData = new RowData(UUID.randomUUID().toString(), map, RowState.NORMAL);
			result.add(rowData);
		});
		return result;
	}
}
