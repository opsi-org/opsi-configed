package de.uib.utilities.savedstates;

import java.util.prefs.Preferences;

public final class UserPreferences {
	public static final String THEME = "theme";
	public static final String LANGUAGE = "language";

	private static Preferences prefs = Preferences.userNodeForPackage(UserPreferences.class);

	private UserPreferences() {
	}

	public static void set(String key, String value) {
		prefs.put(key, value);
	}

	public static String get(String key) {
		return prefs.get(key, "");
	}

	public static void remove(String key) {
		prefs.remove(key);
	}
}
