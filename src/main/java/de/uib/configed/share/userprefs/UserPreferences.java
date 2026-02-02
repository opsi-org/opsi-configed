/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share.userprefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public final class UserPreferences {
	public static final String THEME = "theme";
	public static final String LANGUAGE = "language";
	public static final String OTP = "otp";
	public static final String CLIENTS_TABLE_DISPLAY_FIELDS = "clients_table_display_fields";
	public static final String LOCALBOOT_TABLE_DISPLAY_FIELDS = "localboot_table_display_fields";
	public static final String NETBOOT_TABLE_DISPLAY_FIELDS = "netboot_table_display_fields";
	public static final String CLIENTS_TABLE_COLUMN_WIDTHS = "clients_table.column_widths";
	public static final String WINDOW_BOUNDS = "window_bounds";
	public static final String WINDOW_STATE = "window_state";

	private static Properties properties = new Properties();
	private static File propertiesFile = new File(
			Utils.getSavedStatesDefaultLocation() + File.separator + "userprefs.properties");

	static {
		if (!propertiesFile.exists()) {
			try {
				if (!propertiesFile.createNewFile()) {
					Logging.warning("error creating saved states file");
				} else {
					Logging.info("migrating user preferences");
					migrateUserPreferences();
				}
			} catch (IOException e) {
				Logging.warning(e, "error creating saved states file");
			}
		}

		try (FileInputStream in = new FileInputStream(propertiesFile)) {
			properties.load(in);
		} catch (FileNotFoundException e) {
			Logging.warning(e, "saved states file not found");
		} catch (IOException e) {
			Logging.warning(e, "error loading saved states file");
		}
	}

	private UserPreferences() {
	}

	public static void migrateUserPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(UserPreferences.class);
		Properties props = new Properties();

		try {
			String[] keys = prefs.keys();
			for (String key : keys) {
				String value = prefs.get(key, "");
				props.setProperty(key, value);
			}

			try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
				props.store(out, "migrated user preferences");
			}

			Logging.info("migration successful!");
			deleteOldUserPreferences(prefs);
		} catch (IOException ioe) {
			Logging.error("error occured while migrating user preferences " + ioe);
		} catch (BackingStoreException bse) {
			Logging.error("error occured while contacting backing store " + bse);
		}
	}

	private static void deleteOldUserPreferences(Preferences oldPreferences) {
		try {
			Logging.info("deleting old user preferences");
			oldPreferences.removeNode();
			oldPreferences.flush();
			Logging.info("succesfully deleted old user preferences");
		} catch (BackingStoreException bse) {
			Logging.error("error occured while contacting backing store " + bse);
		}
	}

	public static void set(String key, String value) {
		properties.put(key, value);
		store();
	}

	public static String get(String key) {
		return properties.getProperty(key, "");
	}

	public static String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public static void setBoolean(String key, Boolean value) {
		properties.put(key, Boolean.toString(value));
		store();
	}

	public static Boolean getBoolean(String key) {
		return Boolean.parseBoolean(properties.getProperty(key, Boolean.FALSE.toString()));
	}

	public static void remove(String key) {
		properties.remove(key);
		store();
	}

	public static void store() {
		store(null);
	}

	public static void store(String comments) {
		try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
			properties.store(out, comments);
		} catch (FileNotFoundException e) {
			Logging.warning(e, "saved states file not found");
		} catch (IOException e) {
			Logging.warning(e, "error storing saved states file");
		}
	}
}
