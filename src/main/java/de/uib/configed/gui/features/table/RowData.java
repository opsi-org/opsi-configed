/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
		NORMAL, MODIFIED, DELETED, NEW, MISSING_DATA
	}

	/**
	 * Helper to get a value safely.
	 */
	public <T> T getValue(String key, Class<T> type) {
		Object val = values.get(key);
		return val == null ? null : type.cast(val);
	}

	public static List<RowData> fromOriginalSnapshot(Iterable<Map<String, Object>> original, boolean isKeyValueTable,
			RowDiffStrategy diffStrategy) {
		return fromOriginalSnapshot(original, null, isKeyValueTable, diffStrategy);
	}

	// public static List<RowData> fromOriginalSnapshot(Iterable<Map<String, Object>> original, List<RowData> oldRows,
	// 		boolean isKeyValueTable, RowDiffStrategy diffStrategy) {
	// 	List<RowData> result = new ArrayList<>();

	// 	if (isKeyValueTable) {
	// 		for (Map<String, Object> map : original) {
	// 			for (Map.Entry<String, Object> entry : map.entrySet()) {
	// 				Optional<RowData> opOldData = Optional.empty();
	// 				if (oldRows != null) {
	// 					opOldData = oldRows.stream().filter(rowData -> rowData.getValues().equals(map)).findFirst();
	// 				}
	// 				String rowId = opOldData.isPresent() ? opOldData.get().getId() : UUID.randomUUID().toString();
	// 				RowState state = diffStrategy.getRowStyle(rowId, null, entry.getValue(),
	// 						opOldData.isPresent() ? opOldData.get().getValue("value", Object.class) : entry.getValue());
	// 				Logging.devel("state " + state + " rowId " + rowId);
	// 				RowData rowData = new RowData(rowId, Map.of("key", entry.getKey(), "value", entry.getValue()),
	// 						state);
	// 				result.add(rowData);
	// 			}
	// 		}
	// 		return result;
	// 	}

	// 	original.forEach((Map<String, Object> map) -> {
	// 		Optional<RowData> opOldData = Optional.empty();
	// 		if (oldRows != null) {
	// 			opOldData = oldRows.stream().filter(rowData -> rowData.getValues().equals(map)).findFirst();
	// 		}
	// 		RowData rowData = new RowData(
	// 				opOldData.isPresent() ? opOldData.get().getId() : UUID.randomUUID().toString(), map,
	// 				RowState.NORMAL);
	// 		result.add(rowData);
	// 	});
	// 	return result;
	// }

	public static List<RowData> fromOriginalSnapshot(Iterable<Map<String, Object>> original, List<RowData> oldRows,
			boolean isKeyValueTable, RowDiffStrategy diffStrategy) {
		List<RowData> result = new ArrayList<>();

		Map<String, RowData> oldRowCache = null;
		if (oldRows != null) {
			oldRowCache = new HashMap<>();
			for (RowData rowData : oldRows) {
				oldRowCache.put(rowData.getId(), rowData);
			}
		}

		if (isKeyValueTable) {
			for (Map<String, Object> map : original) {
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String key = entry.getKey();
					Object currentValue = entry.getValue();

					RowData oldRow = oldRowCache != null ? oldRowCache.get(key) : null;

					String rowId = oldRow != null ? oldRow.getId() : UUID.randomUUID().toString();

					Object originalValue = oldRow != null ? oldRow.getValue("value", Object.class) : null;

					RowState state = diffStrategy.getRowStyle(key, "value", currentValue, originalValue);

					RowData rowData = new RowData(rowId, Map.of("key", key, "value", currentValue), state);
					result.add(rowData);
				}
			}
			return result;
		}

		// Non-key-value table logic
		for (Map<String, Object> map : original) {
			// Find matching old row by ID if possible
			String potentialId = findExistingRowId(oldRowCache, map);

			RowData oldRow = (potentialId != null && oldRowCache != null) ? oldRowCache.get(potentialId) : null;

			RowData rowData = new RowData(oldRow != null ? oldRow.getId() : UUID.randomUUID().toString(), map,
					calculateNonKvState(diffStrategy, map, oldRow));
			result.add(rowData);
		}
		return result;
	}

	// Helper: Extract existing ID if map matches an old row
	private static String findExistingRowId(Map<String, RowData> oldRowCache, Map<String, Object> newMap) {
		if (oldRowCache == null)
			return null;

		// Try to match by some identifying field in the map (depends on your data structure)
		// For example, if every map has an "_id" field:
		if (newMap.containsKey("_id")) {
			String id = (String) newMap.get("_id");
			return oldRowCache.containsKey(id) ? id : null;
		}
		return null;
	}

	// Helper: Calculate state for non-KV tables
	private static RowState calculateNonKvState(RowDiffStrategy diffStrategy, Map<String, Object> currentMap,
			RowData oldRow) {
		if (oldRow == null)
			return RowState.NORMAL; // New rows start normal

		// Compare each column value
		for (String colKey : oldRow.getValues().keySet()) {
			Object currentValue = currentMap.getOrDefault(colKey, null);
			Object originalValue = oldRow.getValue(colKey, Object.class);

			if (!Objects.equals(currentValue, originalValue)) {
				return RowState.MODIFIED; // At least one change found
			}
		}
		return RowState.NORMAL;
	}
}
