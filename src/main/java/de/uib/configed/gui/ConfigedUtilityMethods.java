/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import de.uib.configed.gui.data.ListMerger;

/**
 * This class should only contain utility methods that are used for the configed
 * so this class should never be instantiated
 */
public final class ConfigedUtilityMethods {
	// Empty constructor to prevent instantiation of class
	private ConfigedUtilityMethods() {
	}

	public static int[] getTableColumnWidths(JTable table) {
		TableColumnModel columnModel = table.getColumnModel();
		int[] columnWidths = new int[columnModel.getColumnCount()];

		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			columnWidths[i] = columnModel.getColumn(i).getWidth();
		}

		return columnWidths;
	}

	// only has an effect if number of table columns not changed
	public static void setTableColumnWidths(JTable table, int[] columnWidths) {
		// Only do it if number of columns didn't change
		if (columnWidths.length == table.getColumnModel().getColumnCount()) {
			for (int i = 0; i < columnWidths.length; i++) {
				table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
				table.getColumnModel().getColumn(i).setWidth(columnWidths[i]);
			}
		}
	}

	public static Map<String, List<Object>> mergeMaps(List<Map<String, Object>> collection) {
		Map<String, List<Object>> mergedMap = new HashMap<>();
		if (collection == null || collection.isEmpty()) {
			return mergedMap;
		}

		Map<String, Object> mergeIn = collection.get(0);

		for (Entry<String, Object> entry : mergeIn.entrySet()) {
			List<?> value = (List<?>) entry.getValue();
			ListMerger merger = new ListMerger(value);
			mergedMap.put(entry.getKey(), merger);
		}

		// merge the other maps
		for (int i = 1; i < collection.size(); i++) {
			mergeIn = collection.get(i);

			for (Entry<String, Object> mergeInEntry : mergeIn.entrySet()) {
				List<?> value = (List<?>) mergeInEntry.getValue();

				if (mergedMap.get(mergeInEntry.getKey()) == null) {
					ListMerger merger = new ListMerger(value);
					merger.setHavingNoCommonValue();
					mergedMap.put(mergeInEntry.getKey(), merger);
				} else {
					ListMerger merger = (ListMerger) mergedMap.get(mergeInEntry.getKey());
					ListMerger mergedValue = merger.merge(value);
					mergedMap.put(mergeInEntry.getKey(), mergedValue);
				}
			}
		}

		return mergedMap;
	}

	public static void removeKeysStartingWith(Map<String, ? extends Object> m,
			Iterable<String> keystartersStrNotWanted) {
		for (String start : keystartersStrNotWanted) {
			m.keySet().removeIf(key -> key.startsWith(start));
		}
	}
}
