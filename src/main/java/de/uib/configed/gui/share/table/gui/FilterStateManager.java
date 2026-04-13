/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
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

	/**
	 * Enum for identifying filter state keys in FilterStateManager.
	 */
	public enum FilterKey {
		DEPOT_PRODUCT_PROPERTIES_TABLE, CLIENT_TABLE, LOCALBOOT_PRODUCTS_TABLE, NETBOOT_PRODUCTS_TABLE,
		LICENSE_POOL_ENTER_TABLE, LICENSE_POOL_USAGE_TABLE, LICENSE_POOL_POOLS_TABLE, LICENSE_KEYS_EDIT_TABLE,
		LICENSE_KEYS_ENTER_TABLE, LICENSE_SOFTWARE_TABLE, LICENSE_CONTRACTS_EDIT_TABLE, LICENSE_CONTRACTS_ENTER_TABLE,
		LICENSE_USAGE_TABLE, LICENSE_RECONCILIATION_TABLE, LICENSES_STATISTICS_TABLE,
		LICENSE_PRODUCT_ID_TO_LICENSE_POOL_TABLE, LICENSE_REGISTERED_SOFTWARE_TABLE
	}

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
