/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * The core data structure representing a single row in the generic table. It
 * holds the values, the visual state (for diffing), and the row ID.
 */
@Value
@With
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

	public static List<RowData> fromOriginalSnapshot(Iterable<Map<String, Object>> original,
			RowDiffStrategy diffStrategy) {
		return fromOriginalSnapshot(original, null, diffStrategy);
	}

	public static List<RowData> fromOriginalSnapshot(Iterable<Map<String, Object>> original, List<RowData> oldRows,
			RowDiffStrategy diffStrategy) {
		List<RowData> result = new ArrayList<>();

		original.forEach((Map<String, Object> map) -> {
			RowData rowData = produceRowData(map, oldRows, diffStrategy);
			result.add(rowData);
		});
		return result;
	}

	private static RowData produceRowData(Map<String, Object> map, List<RowData> oldRows,
			RowDiffStrategy diffStrategy) {
		Optional<RowData> opOldData = Optional.empty();
		if (oldRows != null) {
			opOldData = oldRows.stream().filter(rowData -> rowData.getValues().equals(map)).findFirst();
		}

		RowData rowData = opOldData.isPresent() ? opOldData.get()
				: new RowData(UUID.randomUUID().toString(), map, RowState.NORMAL);
		RowState state = diffStrategy != null ? diffStrategy.getRowStyle(rowData, null, map, rowData.getValues())
				: RowState.NORMAL;

		return rowData.withState(state);
	}

	public static List<RowData> fromOriginalSnapshotKeyValueTable(Iterable<Map<String, Object>> original,
			RowDiffStrategy diffStrategy) {
		return fromOriginalSnapshotKeyValueTable(original, null, diffStrategy);
	}

	public static List<RowData> fromOriginalSnapshotKeyValueTable(Iterable<Map<String, Object>> original,
			Collection<RowData> oldRows, RowDiffStrategy diffStrategy) {
		List<RowData> result = new ArrayList<>();

		for (Map<String, Object> map : original) {
			Optional<RowData> opOldData = Optional.empty();
			if (oldRows != null) {
				opOldData = oldRows.stream().filter(rowData -> rowData.getValues().equals(map)).findFirst();
			}
			String rowId = opOldData.isPresent() ? opOldData.get().getId() : UUID.randomUUID().toString();
			RowData rowData = new RowData(rowId, map, RowState.NORMAL);
			RowState state = diffStrategy.getRowStyle(rowData, "value", map.get("value"),
					opOldData.isPresent() ? opOldData.get().getValue("value", Object.class) : map.get("value"));
			result.add(rowData.withState(state));
		}
		return result;
	}
}
