/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
	public static final String LOCALBOOT_PRODUCT_INFO_SPLIT = "localboot.product_info";
	public static final String NETBOOT_PRODUCT_SETTINGS_SPLIT = "netboot.product_settings";
	public static final String NETBOOT_PRODUCT_INFO_SPLIT = "netboot.product_info";
	public static final String CLIENT_HOST_PARAMETERS_SPLIT = "client.host_parameteres";
	public static final String DEPOT_HOST_PARAMETERS_SPLIT = "depot.host_parameteres";
	public static final String DEPOT_PRODUCT_PROPERTIES_SPLIT = "depot.product_properties";
	public static final String DEPOT_PRODUCT_INFO_SPLIT = "depot.product_info";
	public static final String SERVER_HOST_PARAMETERS_SPLIT = "server.host_parameteres";
	public static final String REMOTE_CONTROL_SPLIT = "remote_control";

	private static final String CLIENT_PROPERTY_ADJUSTING = "SplitPane.adjusting";

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
		registerSplitPane(splitPane, key, null);
	}

	/**
	 * Registers a JSplitPane to automatically persist its divider location.
	 * <p>
	 * The divider location is stored as a proportional value between
	 * {@code 0.0} and {@code 1.0}, relative to the current size of the split
	 * pane.
	 * 
	 * @param splitPane    The split pane whose divider location should be
	 *                     persisted
	 * @param key          A unique key to store the divider location
	 * @param defaultValue the divider location to use when no stored value
	 *                     exists; may be {@code null}. Can be {@link Integer}
	 *                     (pixel position) or {@link Float} (proportion).
	 */
	public static void registerSplitPane(JSplitPane splitPane, String key, Number defaultValue) {
		Runnable restore = new Runnable() {
			@Override
			public void run() {
				if (getTotalSize(splitPane) > 0) {
					resolveDividerLocation(splitPane, key, defaultValue);
				} else {
					SwingUtilities.invokeLater(this);
				}
			}
		};

		splitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				restore();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				restore();
			}

			private void restore() {
				if (splitPane.isShowing() && getTotalSize(splitPane) > 0) {
					restore.run();
					addPropertyChangeListener(key, splitPane);
					splitPane.removeComponentListener(this);
				}
			}
		});

		SwingUtilities.invokeLater(restore);
	}

	/**
	 * Restores the divider location of the given {@link JSplitPane} from
	 * persisted user preferences.
	 * <p>
	 * The divider location is stored as a proportional value (0.0–1.0) and is
	 * applied relative to the current size of the split pane.
	 * </p>
	 * 
	 * @param splitPane the split pane whose divider location should be restored
	 * @param key       the unique key used to retrieve the stored divider
	 *                  position
	 */
	public static void restoreDividerLocation(JSplitPane splitPane, String key) {
		Double dividerLocation = loadStoredLocation(key);
		applyDividerLocation(splitPane, dividerLocation);
	}

	private static void resolveDividerLocation(JSplitPane splitPane, String key, Number defaultVal) {
		Double dividerLocation = loadStoredLocation(key);

		if (dividerLocation == null) {
			dividerLocation = calculateDefaultLocation(splitPane, key, defaultVal);
		}

		applyDividerLocation(splitPane, dividerLocation);
	}

	private static Double loadStoredLocation(String key) {
		String storedValue = UserPreferences.get(buildDividerLocationKey(key));

		if (storedValue == null || storedValue.isEmpty()) {
			return null;
		}

		try {
			Double parsed = Double.parseDouble(storedValue);
			if (isValidProportion(parsed)) {
				return parsed;
			} else {
				Logging.warning("Stored value out of bounds: " + parsed + ". Using default.");
			}
		} catch (NumberFormatException e) {
			Logging.warning("Invalid stored value: " + storedValue);
		}

		return null;
	}

	private static Double calculateDefaultLocation(JSplitPane splitPane, String key, Number defaultVal) {
		if (defaultVal == null) {
			return null;
		}

		Integer totalSize = getTotalSize(splitPane);
		if (totalSize == null || totalSize <= 0) {
			return null;
		}

		Double proportion = convertToProportion(defaultVal, totalSize);

		if (proportion != null && isValidProportion(proportion)) {
			UserPreferences.set(buildDividerLocationKey(key), String.valueOf(proportion));
			Logging.info("Persisted default proportion: " + proportion + " for key: " + key);
		}

		return proportion;
	}

	private static Double convertToProportion(Number defaultVal, Integer totalSize) {
		if (defaultVal instanceof Integer val) {
			return convertPixelToProportionSize(val, totalSize);
		} else {
			return defaultVal.doubleValue();
		}
	}

	private static boolean isValidProportion(Double value) {
		return value != null && value >= 0.0F && value <= 1.0F;
	}

	private static void applyDividerLocation(JSplitPane splitPane, Double dividerLocation) {
		if (dividerLocation == null) {
			return;
		}

		splitPane.putClientProperty(CLIENT_PROPERTY_ADJUSTING, Boolean.TRUE);
		splitPane.setDividerLocation(dividerLocation);
		splitPane.putClientProperty(CLIENT_PROPERTY_ADJUSTING, Boolean.FALSE);
	}

	private static void addPropertyChangeListener(String key, JSplitPane splitPane) {
		PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
			if (Boolean.TRUE.equals(splitPane.getClientProperty(CLIENT_PROPERTY_ADJUSTING))) {
				return;
			}

			storeDividerLocation(key, splitPane);
		};

		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, listener);
	}

	private static void storeDividerLocation(String key, JSplitPane splitPane) {
		Integer totalSize = getTotalSize(splitPane);
		if (totalSize > 0) {
			int loc = splitPane.getDividerLocation();
			Double proportion = convertPixelToProportionSize(loc, totalSize);

			if (proportion != null && proportion >= 0.0F && proportion <= 1.0F) {
				UserPreferences.set(buildDividerLocationKey(key), String.valueOf(proportion));
			}
		}
	}

	private static Double convertPixelToProportionSize(Integer pixelSize, Integer totalSize) {
		if (pixelSize == null || totalSize == null || totalSize == 0) {
			return null;
		}
		double proportion = (double) pixelSize / totalSize;
		if (proportion < 0.0 || proportion > 1.0 || Double.isInfinite(proportion) || Double.isNaN(proportion)) {
			Logging.warning("Invalid proportion calculated: " + proportion);
			return null;
		}
		return Math.round(proportion * 100.0) / 100.0;
	}

	private static int getTotalSize(JSplitPane splitPane) {
		return splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? splitPane.getWidth() : splitPane.getHeight();
	}

	private static String buildDividerLocationKey(String key) {
		return key + "." + UserPreferences.DIVIDER_LOCATION;
	}
}
