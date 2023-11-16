/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import de.uib.utilities.logging.Logging;

public final class Messages {
	public static final String APPNAME = "configed";
	private static final String BUNDLE_NAME = "de/uib/messages/opsi-configed";
	private static final String LOCALISATIONS_CONF = "valid_localisations.conf";

	private static Set<String> existingLocalesNames;
	private static Locale myLocale;
	private static ResourceBundle messagesBundle;
	private static final List<String> availableThemes = Arrays.asList("Light", "Dark");
	private static String selectedTheme = availableThemes.get(0);

	// private constructor to hide the implicit public one
	private Messages() {
	}

	private static String findSelectedLocale(String language, String country) {

		String languageRegion = language + "_" + country;

		String result;
		if (existingLocalesNames.contains(languageRegion)) {
			result = languageRegion;
		} else if (existingLocalesNames.contains(language)) {
			result = language;
		} else {
			result = null;
		}

		return result;
	}

	public static String getSelectedLocale() {
		String selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());

		if (selectedLocaleString == null) {
			// not found, now try again for default locale
			produceLocale();
			selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());

			if (selectedLocaleString == null) {
				// default locale not found
				produceLocaleEnUS();
				selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());
			}
		}

		Logging.info("Selected locale: " + selectedLocaleString);

		return selectedLocaleString;
	}

	private static void produceLocale() {
		myLocale = Locale.getDefault();
	}

	private static Locale produceLocale(String language) {
		myLocale = new Locale.Builder().setLanguage(language).build();
		return myLocale;
	}

	private static Locale produceLocale(String language, String country) {
		myLocale = new Locale.Builder().setLanguage(language).setRegion(country).build();
		return myLocale;
	}

	private static Locale produceLocaleEnUS() {
		return produceLocale("en", "US");
	}

	public static Locale getLocale() {
		return myLocale;
	}

	public static void setLocale(String characteristics) {
		Logging.debug("Messages setLocale: " + characteristics);
		Locale loc = null;
		if (characteristics != null && !characteristics.isEmpty()) {
			if (characteristics.length() == 5 && characteristics.indexOf('_') == 2) {
				loc = produceLocale(characteristics.substring(0, 2), characteristics.substring(3, 5));
				Logging.info("Locale " + loc.getLanguage() + "_" + loc.getCountry() + " set by param");
			} else if (characteristics.length() == 2) {
				loc = produceLocale(characteristics);
				Logging.info("Locale " + loc + " set by param");
			} else {
				Logging.warning("Bad format for locale, use <language>_<country> or <language>"
						+ ", each component consisting of two chars, or just a two char <language>");
			}
		}

		if (loc == null) {
			produceLocale();
		}
		Logging.notice("Locale set to: " + myLocale);

		messagesBundle = ResourceBundle.getBundle(BUNDLE_NAME, myLocale);
	}

	public static Set<String> getLocaleNames() {
		if (existingLocalesNames == null) {
			getLocaleRepresentations();
		}

		return existingLocalesNames;
	}

	public static String getSelectedTheme() {
		return selectedTheme;
	}

	public static List<String> getAvailableThemes() {
		return List.copyOf(availableThemes);
	}

	public static void setTheme(String newTheme) {
		if (availableThemes.contains(newTheme)) {
			selectedTheme = newTheme;
		} else {
			Logging.warning("Failing to set theme that does not exist: " + newTheme);
		}
	}

	private static void getLocaleRepresentations() {

		TreeSet<String> names = new TreeSet<>();

		InputStream stream = Messages.class.getResourceAsStream(LOCALISATIONS_CONF);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		try {
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				if (!line.isEmpty() && line.charAt(0) != '#') {
					names.add(line);
				}
				line = reader.readLine();
			}
		} catch (IOException ex) {
			Logging.warning("Messages exception on reading!", ex);
		}

		Logging.debug("Messages, existing names " + names);
		existingLocalesNames = names;
	}

	public static ResourceBundle getResourceBundle() {
		return messagesBundle;
	}
}
