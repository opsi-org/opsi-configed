/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.UserPreferences;

public final class SplitPaneStateManager {
	public static final String CLIENT_INFO_SPLIT = "client_info";
	public static final String HARDWARE_SPLIT = "hardware";
	public static final String CLIENT_CONFIGURATION_SPLIT = "client_configuration";
	public static final String DEPOT_CONFIGURATION_SPLIT = "depot_configuration";
	public static final String PRODUCT_PROPERTIES_SPLIT = "product_properties";
	public static final String PRODUCT_SETTINGS_SPLIT = "product_settings";
	public static final String CLIENT_HOST_PARAMETERS_SPLIT = "client.host_parameteres";
	public static final String DEPOT_HOST_PARAMETERS_SPLIT = "depot.host_parameteres";
	public static final String SERVER_HOST_PARAMETERS_SPLIT = "server.host_parameteres";

	private SplitPaneStateManager() {
		// Hide constructor.
	}

	/**
	 * Registers a JSplitPane to automatically persist its divider location.
	 * 
	 * @param splitPane The split pane whose divider location should be
	 *                  persisted
	 * @param key       A unique key to store the divider location
	 */
	public static void registerSplitPane(JSplitPane splitPane, String key) {
		restoreDividerLocation(splitPane, key);

		PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
			int loc = splitPane.getDividerLocation();
			UserPreferences.set(buildDividerLocationKey(key), String.valueOf(loc));
		};
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, listener);
	}

	/**
	 * Restores the saved divider location of a {@link JSplitPane} from user
	 * preferences.
	 *
	 * @param splitPane The split pane whose divider location should be restored
	 * @param key       A unique key used to retrieve the stored divider
	 *                  location
	 */
	public static void restoreDividerLocation(JSplitPane splitPane, String key) {
		String value = UserPreferences.get(buildDividerLocationKey(key));
		if (value == null) {
			return;
		}

		try {
			int dividerLocation = Integer.parseInt(value);
			SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(dividerLocation));
		} catch (NumberFormatException ignored) {
			Logging.warning("Failed to convert string to numeric value " + value);
		}
	}

	private static String buildDividerLocationKey(String key) {
		return key + "." + UserPreferences.DIVIDER_LOCATION;
	}

}
