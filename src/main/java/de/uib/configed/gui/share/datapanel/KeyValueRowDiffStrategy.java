/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.util.Map;

import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.features.table.RowData.RowState;
import de.uib.configed.gui.features.table.RowDiffStrategy;
import de.uib.configed.share.logging.Logging;

public class KeyValueRowDiffStrategy implements RowDiffStrategy {
	private Map<String, Object> defaultsMap;
	private Map<String, Object> originalMap;
	private boolean pinnedProperty;

	public KeyValueRowDiffStrategy(Map<String, Object> defaultsMap, Map<String, Object> originalMap,
			boolean pinnedProperty) {
		this.defaultsMap = defaultsMap;
		this.originalMap = originalMap;
		this.pinnedProperty = pinnedProperty;
	}

	@Override
	public RowState getRowStyle(RowData rowData, String colKey, Object currentValue, Object originalValue) {
		if (defaultsMap == null) {
			Logging.warning(this, "no default values available, defaultsMap is null");
			return RowState.NORMAL;
		}

		String key = null;
		if (rowData != null) {
			key = rowData.getValue("key", String.class);
			if (pinnedProperty) {
				currentValue = rowData.getValue("value", Object.class);
			}
		}

		Logging.devel(this, "row data", rowData, "key", key, "current value", currentValue, "defautls map", defaultsMap,
				"original map", originalMap);
		RowState rowState;
		Object defaultValue;
		if ((defaultValue = defaultsMap.get(key)) == null) {
			Logging.warning(this, "no default Value found");
			Logging.devel(this, "no default Value found", key);
			rowState = RowState.MISSING_DATA;
		} else if (!defaultValue.equals(currentValue) || (originalMap != null && originalMap.containsKey(key))) {
			rowState = RowState.MODIFIED;
		} else {
			rowState = RowState.NORMAL;
		}

		return rowState;
	}
}
