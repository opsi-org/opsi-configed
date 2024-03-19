/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.savedstates;

import java.util.prefs.Preferences;

public final class UserPreferences {
	public static final String THEME = "theme";
	public static final String LANGUAGE = "language";
	public static final String OTP = "otp";

	private static Preferences prefs = Preferences.userNodeForPackage(UserPreferences.class);

	private UserPreferences() {
	}

	public static void set(String key, String value) {
		prefs.put(key, value);
	}

	public static String get(String key) {
		return prefs.get(key, "");
	}

	public static void setBoolean(String key, Boolean value) {
		prefs.putBoolean(key, value);
	}

	public static Boolean getBoolean(String key) {
		return prefs.getBoolean(key, false);
	}

	public static void remove(String key) {
		prefs.remove(key);
	}
}
