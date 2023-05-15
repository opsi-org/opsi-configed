package de.uib.messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.utilities.logging.Logging;

public final class Messages {

	public static final String APPNAME = "configed";
	private static final String BUNDLE_NAME = "de/uib/messages/configed";
	private static final String LOCALISATIONS_CONF = "valid_localisations.conf";

	private static List<LocaleRepresentation> existingLocales;
	private static List<String> existingLocalesNames;
	private static Map<String, String> localeInfo;
	private static Locale myLocale;
	public static ResourceBundle messagesBundle;
	public static ResourceBundle messagesEnBundle;
	private static final List<String> availableThemes = Arrays.asList("Light", "Dark");
	private static String selectedTheme = availableThemes.get(0);

	// private constructor to hide the implicit public one
	private Messages() {
	}

	private static String findSelectedLocale(String language, String country) {
		String result = null;
		List<String> myLocaleCharacteristics = new ArrayList<>();
		String characteristics = language + "_" + country;

		myLocaleCharacteristics.add(characteristics);
		if (existingLocalesNames.indexOf(characteristics) > -1) {
			result = characteristics;
		}

		characteristics = language;
		myLocaleCharacteristics.add(characteristics);
		if (result == null && existingLocalesNames.indexOf(characteristics) > -1) {
			result = characteristics;
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

		Logging.info("Messages, getSelectedLocale " + selectedLocaleString);

		return selectedLocaleString;
	}

	public static ResourceBundle getResource() {
		try {
			Logging.info("Messages, getResource from " + BUNDLE_NAME);

			messagesBundle = ResourceBundle.getBundle(BUNDLE_NAME, myLocale);

			Logging.debug("Messages messages " + messagesBundle);

		} catch (MissingResourceException ex) {
			messagesBundle = getResourceEN();
			Logging.notice("missing resource, using EN resources: " + ex);
		}
		return messagesBundle;
	}

	public static ResourceBundle getResourceEN() throws MissingResourceException {

		messagesEnBundle = ResourceBundle.getBundle(BUNDLE_NAME,
				new Locale.Builder().setLanguage("en").setRegion("US").build());

		List<String> myLocaleCharacteristicsEN = new ArrayList<>();
		myLocaleCharacteristicsEN.add("en_US");
		myLocaleCharacteristicsEN.add("en");
		return messagesEnBundle;
	}

	private static Locale giveLocale(String selection) {
		Logging.debug("Messages: selected locale " + myLocale + " by " + selection);
		return myLocale;
	}

	private static Locale produceLocale() {
		myLocale = Locale.getDefault();
		return giveLocale("default");
	}

	private static Locale produceLocale(String language) {
		myLocale = new Locale.Builder().setLanguage(language).build();
		return giveLocale("language " + language);
	}

	private static Locale produceLocale(String language, String country) {
		myLocale = new Locale.Builder().setLanguage(language).setRegion(country).build();
		return giveLocale("language " + language + ", country " + country);
	}

	private static Locale produceLocaleEnUS() {
		myLocale = new Locale.Builder().setLanguage("en").setRegion("US").build();
		return giveLocale("fallback (en_US)");
	}

	public static Locale getLocale() {
		return myLocale;
	}

	public static void setLocale(String characteristics) {
		Logging.debug("Messages, setLocale");
		Locale loc = null;
		if (characteristics != null && !characteristics.isEmpty()) {

			if (characteristics.length() == 5 && characteristics.indexOf('_') == 2) {
				try {
					loc = produceLocale(characteristics.substring(0, 2), characteristics.substring(3, 5));
					Logging.info("Locale " + loc.getLanguage() + "_" + loc.getCountry() + " set by param");
				} catch (Exception e) {
					Logging.info("Failed to set locale '" + characteristics + "': " + e);
				}
			} else if (characteristics.length() == 2) {
				try {
					loc = produceLocale(characteristics);
					Logging.info("Locale " + loc + " set by param");
				} catch (Exception e) {
					Logging.info("Failed to set locale '" + characteristics + "': " + e);
				}

			} else {
				Logging.info(
						"Bad format for locale, use <language>_<country> or <language>, each component consisting of two chars, or just a two char <language>");
			}

		}

		if (loc == null) {
			loc = produceLocale();
		}

		try {
			messagesBundle = getResource();
			messagesEnBundle = getResourceEN();
		} catch (MissingResourceException e) {
			Logging.info("Missing messages for locale EN: " + e);
		}
	}

	public static List<String> getLocaleNames() {
		if (existingLocalesNames == null) {
			getLocaleRepresentations();
		}

		return existingLocalesNames;
	}

	public static Map<String, String> getLocaleInfo() {
		if (localeInfo == null) {
			getLocaleRepresentations();
		}

		Logging.debug("Messages, getLocaleInfo " + localeInfo);

		return localeInfo;
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

	private static List<LocaleRepresentation> getLocaleRepresentations() {
		if (existingLocales != null) {
			return existingLocales;
		}

		ArrayList<LocaleRepresentation> existingLocales = new ArrayList<>();
		localeInfo = new TreeMap<>();

		InputStream stream = Messages.class.getResourceAsStream(LOCALISATIONS_CONF);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) != '#') {
					existingLocales.add(new LocaleRepresentation(line));
				}
				line = reader.readLine();
			}
		} catch (IOException ex) {
			Logging.warning("Messages exception on reading!", ex);
		}

		TreeSet<String> names = new TreeSet<>();
		for (LocaleRepresentation representer : existingLocales) {
			names.add(representer.getName());
			localeInfo.put(representer.getName(), representer.getIconName());
		}
		Logging.debug("Messages, existing names " + names);
		existingLocalesNames = new ArrayList<>(names);
		Logging.debug("Messages, existing locales " + existingLocales);
		Logging.debug("Messages, localeInfo  " + localeInfo);
		return existingLocales;
	}
}
