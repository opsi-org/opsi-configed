/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class to persist TableFilterState across UI reloads. Use a unique key
 * per table/panel to store and retrieve state.
 */
public final class FilterStateManager {
	private static final Map<FilterKey, TableFilterState> filterStates = new EnumMap<>(FilterKey.class);

	private FilterStateManager() {
		// Prevent instantiation
	}

	/**
	 * Save the filter state for a given key.
	 * 
	 * @param key   Unique identifier for the table/panel (e.g.,
	 *              FilterKey.DEPOT_PRODUCTS)
	 * @param state The TableFilterState to save
	 */
	public static void saveFilterState(FilterKey key, TableFilterState state) {
		filterStates.put(key, state);
	}

	/**
	 * Retrieve the filter state for a given key.
	 * 
	 * @param key Unique identifier for the table/panel
	 * @return The saved TableFilterState, or null if none exists
	 */
	public static TableFilterState getFilterState(FilterKey key) {
		return filterStates.get(key);
	}

	/**
	 * Remove the filter state for a given key.
	 * 
	 * @param key Unique identifier for the table/panel
	 */
	public static void removeFilterState(FilterKey key) {
		filterStates.remove(key);
	}

	/**
	 * Clear all saved filter states.
	 */
	public static void clear() {
		filterStates.clear();
	}
}
