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
	public static final String LOCALBOOT_PRODUCT_SETTINGS_SPLIT = "localboot.product_settings";
	public static final String NETBOOT_PRODUCT_SETTINGS_SPLIT = "netboot.product_settings";
	public static final String CLIENT_HOST_PARAMETERS_SPLIT = "client.host_parameteres";
	public static final String DEPOT_HOST_PARAMETERS_SPLIT = "depot.host_parameteres";
	public static final String SERVER_HOST_PARAMETERS_SPLIT = "server.host_parameteres";

	private SplitPaneStateManager() {
		// Hide constructor.
	}

	/**
	 * Registers a JSplitPane to automatically persist its divider location.
	 * <p>
	 * The divider location is stored as a proportional value between
	 * {@code 0.0} and {@code 1.0}, relative to the current size of the split
	 * pane.
	 * 
	 * @param splitPane The split pane whose divider location should be
	 *                  persisted
	 * @param key       A unique key to store the divider location
	 */
	public static void registerSplitPane(JSplitPane splitPane, String key) {
		restoreDividerLocation(splitPane, key);

		PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
			int loc = splitPane.getDividerLocation();
			int totalSize = splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? splitPane.getWidth()
					: splitPane.getHeight();
			double proportion = (double) loc / totalSize;
			UserPreferences.set(buildDividerLocationKey(key), String.valueOf(proportion));
		};
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, listener);
	}

	/**
	 * Restores the divider location of the specified {@link JSplitPane} from
	 * user preferences.
	 * <p>
	 * If a saved value exists for the given key, it will be applied. Otherwise
	 * the divider location remains unchanged.
	 *
	 * @param splitPane the split pane whose divider location should be restored
	 * @param key       a unique identifier used to look up the stored divider
	 *                  location
	 */
	public static void restoreDividerLocation(JSplitPane splitPane, String key) {
		restoreDividerLocation(splitPane, key, null);
	}

	/**
	 * Restores the divider location of the specified {@link JSplitPane} from
	 * user preferences.
	 * <p>
	 * The stored value is expected to be a floating-point number representing
	 * the proportional divider location (between {@code 0.0} and {@code 1.0}).
	 * <p>
	 * If no value is stored for the given key, the provided
	 * {@code defaultValue} will be used. If both the stored value and the
	 * default value are {@code null}, the divider location is left unchanged.
	 *
	 * @param splitPane    the split pane whose divider location should be
	 *                     restored
	 * @param key          a unique identifier used to look up the stored
	 *                     divider location
	 * @param defaultValue the divider location to use when no stored value
	 *                     exists; may be {@code null}
	 */
	public static void restoreDividerLocation(JSplitPane splitPane, String key, Float defaultValue) {
		String value = UserPreferences.get(buildDividerLocationKey(key));

		Float dividerLocation = null;

		if (value != null && !value.isEmpty()) {
			try {
				dividerLocation = Float.parseFloat(value);
			} catch (NumberFormatException e) {
				Logging.warning("Invalid divider location '" + value + "'");
			}
		} else {
			dividerLocation = defaultValue;
		}

		if (dividerLocation != null) {
			float location = dividerLocation;
			SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(location));
		}
	}

	private static String buildDividerLocationKey(String key) {
		return key + "." + UserPreferences.DIVIDER_LOCATION;
	}

}
