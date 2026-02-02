/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import de.uib.configed.share.userprefs.UserPreferences;

public final class WindowsPositionManager {
	private static final Pattern WINDOW_BOUNDS_PATTERN = Pattern.compile("\\d+,\\d+,\\d+,\\d+");
	public static final String LOGVIEWER = "logviewer";
	public static final String MAIN_WINDOW = "main_window";

	private WindowsPositionManager() {
		// Hide constructor.
	}

	public static void saveWindowProperties(Window window, String windowId) {
		if (window == null || windowId == null) {
			throw new IllegalArgumentException("Window and window id can't be null");
		}

		Rectangle b = window.getBounds();
		String bounds = b.x + "," + b.y + "," + b.width + "," + b.height;
		UserPreferences.set(windowId + "." + UserPreferences.WINDOW_BOUNDS, bounds);

		if (window instanceof Frame frame) {
			int state = frame.getExtendedState();
			UserPreferences.set(windowId + "." + UserPreferences.WINDOW_STATE, Integer.toString(state));
		}
	}

	public static void loadWindowProperties(Window window, String windowId) {
		if (window == null || windowId == null) {
			throw new IllegalArgumentException("Window and window id can't be null");
		}

		Rectangle bounds = getWindowBounds(windowId);

		if (bounds != null) {
			window.setBounds(bounds);

			if (window instanceof JFrame frame) {
				int state = Integer.parseInt(UserPreferences.get(windowId + "." + UserPreferences.WINDOW_STATE,
						Integer.toString(Frame.NORMAL)));
				if ((state & Frame.ICONIFIED) == 0) {
					frame.setExtendedState(state);
				}
			}
		}
	}

	public static boolean isOnAnyScreen(Rectangle bounds) {
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
			if (bounds != null && screenBounds.intersects(bounds)) {
				return true;
			}
		}
		return false;
	}

	public static void centerDialogOnWindowScreen(Component component) {
		Rectangle screenBounds = getScreenBoundsForLocation(getWindowBounds(MAIN_WINDOW));

		int dialogWidth = component.getWidth();
		int dialogHeight = component.getHeight();

		int x = screenBounds.x + (screenBounds.width - dialogWidth) / 2;
		int y = screenBounds.y + (screenBounds.height - dialogHeight) / 2;

		component.setLocation(x, y);
	}

	private static Rectangle getScreenBoundsForLocation(Rectangle bounds) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();

		for (GraphicsDevice device : devices) {
			Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
			if (screenBounds.intersects(bounds)) {
				return screenBounds;
			}
		}

		// Fallback to primary screen
		return ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
	}

	public static Rectangle getWindowBounds(String windowId) {
		String boundsStr = UserPreferences.get(windowId + "." + UserPreferences.WINDOW_BOUNDS);
		Rectangle bounds = null;

		if (boundsStr != null && WINDOW_BOUNDS_PATTERN.matcher(boundsStr).matches()) {
			String[] parts = boundsStr.split(",");
			bounds = new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
					Integer.parseInt(parts[3]));
		}
		return bounds;
	}
}
