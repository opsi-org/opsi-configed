package de.uib.messages;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;
import utils.ResourceBundleUtf8;

public class Messages {
	public static final String appname = "configed";
	private static String BUNDLE_NAME = "de/uib/messages/configed";
	private static String EXTRA_LOCALE = null;
	private static final String LOCALISATIONS_CONF = "valid_localisations.conf";
	private static Boolean UTF8_HACK = null;

	static List<LocaleRepresentation> existingLocales;
	static List<String> existingLocalesNames;
	static java.util.Map<String, String> localeInfo;
	static String selectedLocaleString;
	static Locale myLocale = null;
	public static ResourceBundle messages;
	public static ResourceBundle messagesEN;
	static List<String> myLocaleCharacteristics;
	static List<String> myLocaleCharacteristicsEN;

	private static String findSelectedLocale(String language, String country) {
		String result = null;
		myLocaleCharacteristics = new ArrayList<>();
		String characteristics = language + "_" + country;

		myLocaleCharacteristics.add(characteristics);
		if (existingLocalesNames.indexOf(characteristics) > -1)
			result = characteristics;

		characteristics = language;
		myLocaleCharacteristics.add(characteristics);
		if (result == null && (existingLocalesNames.indexOf(characteristics) > -1))
			result = characteristics;

		return result;
	}

	public static String getSelectedLocale() {
		if (EXTRA_LOCALE != null)
			selectedLocaleString = EXTRA_LOCALE;
		else
			selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());

		if (selectedLocaleString == null) {
			// not found, now try again for default locale
			produceLocale();
			selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());

			if (selectedLocaleString == null) {
				// default locale not found
				produceLocale_enUS();
				selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());
			}
		}

		logging.info("Messages, getSelectedLocale " + selectedLocaleString);

		return selectedLocaleString;
	}

	private static int lastIntIndex(String s) {
		if (s == null)
			return -1;

		StringBuffer allowed = new StringBuffer();

		for (int j = 0; j < 10; j++)
			allowed.append("" + j);
		allowed.append(".");

		int i = 0;
		boolean goOn = true;

		while (goOn && i < s.length()) {
			if (allowed.indexOf("" + s.charAt(i)) >= 0) {
				i++;
			} else {
				goOn = false;
			}
		}
		return i;
	}

	private static void checkUTF8() {
		if (UTF8_HACK == null) {
			String javaVersionOnlyNumbers0 = System.getProperty("java.version");
			// test String javaVersionOnlyNumbers0 = "14-ea";
			logging.debug("java version " + javaVersionOnlyNumbers0);
			String javaVersionOnlyNumbers = javaVersionOnlyNumbers0.substring(0, lastIntIndex(javaVersionOnlyNumbers0));

			if (javaVersionOnlyNumbers.length() < javaVersionOnlyNumbers0.length())
				logging.debug("shortened to " + javaVersionOnlyNumbers);

			Integer differenceToJava9 = Globals.compareDottedNumberStrings("9", javaVersionOnlyNumbers);
			logging.debug(" version difference to java 9 is: " + differenceToJava9);
			UTF8_HACK = (differenceToJava9 > 0);
			logging.debug(" we will use the UTF8 hack " + UTF8_HACK);
		}
	}

	public static ResourceBundle getResource() throws MissingResourceException {
		checkUTF8();

		try {
			logging.info("Messages, getResource from " + BUNDLE_NAME);
			if (UTF8_HACK)
				messages = ResourceBundleUtf8.getBundle(BUNDLE_NAME, myLocale);
			else
				messages = ResourceBundle.getBundle(BUNDLE_NAME, myLocale);

			logging.debug("Messages messages " + messages);

		} catch (MissingResourceException ex) {
			messages = getResourceEN();
		}
		return messages;
	}

	public static ResourceBundle getResourceEN() throws MissingResourceException {
		checkUTF8();

		if (UTF8_HACK)
			messagesEN = ResourceBundleUtf8.getBundle(BUNDLE_NAME, new Locale("en", "US"));
		else
			messagesEN = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("en", "US"));
		myLocaleCharacteristicsEN = new ArrayList<>();
		myLocaleCharacteristicsEN.add("en_US");
		myLocaleCharacteristicsEN.add("en");
		return messagesEN;
	}

	private static Locale giveLocale(String selection) {
		logging.debug("Messages: selected locale " + myLocale + " by " + selection);
		return myLocale;
	}

	private static Locale produceLocale() {
		myLocale = Locale.getDefault();
		return giveLocale("default");
	}

	private static Locale produceLocale(String language) {
		myLocale = new Locale(language);
		return giveLocale("language " + language);
	}

	private static Locale produceLocale(String language, String country) {
		myLocale = new Locale(language, country);
		return giveLocale("language " + language + ", country " + country);
	}

	private static Locale produceLocale_enUS() {
		myLocale = new Locale("en", "US");
		return giveLocale("fallback (en_US)");
	}

	public static Locale getLocale() {
		return myLocale;
	}

	public static Locale setLocale(String characteristics) {
		logging.debug("Messages, setLocale");
		Locale loc = null;
		if (characteristics != null && !characteristics.equals("")) {
			// logging.info("Locale is: " + characteristics + ">");
			if (characteristics.length() == 5 && characteristics.indexOf('_') == 2) {
				try {
					loc = produceLocale(characteristics.substring(0, 2), characteristics.substring(3, 5));
					logging.info("Locale " + loc.getLanguage() + "_" + loc.getCountry() + " set by param");
				} catch (Exception e) {
					logging.info("Failed to set locale '" + characteristics + "': " + e);
				}
			}

			else if (characteristics.length() == 2) {
				try {
					loc = produceLocale(characteristics);
					logging.info("Locale " + loc + " set by param");
				} catch (Exception e) {
					logging.info("Failed to set locale '" + characteristics + "': " + e);
				}

			} else {
				logging.info(
						"Bad format for locale, use <language>_<country> or <language>, each component consisting of two chars, or just a two char <language>");
			}

		}

		if (loc == null)
			loc = produceLocale();

		try {
			messages = getResource();
			messagesEN = getResourceEN();
		} catch (MissingResourceException e) {
			logging.info("Missing messages for locale EN");
		}

		return loc;
	}

	public static List<String> getLocaleNames() {
		if (existingLocalesNames == null)
			getLocaleRepresentations();

		return existingLocalesNames;
	}

	public static Map<String, String> getLocaleInfo() {
		if (localeInfo == null)
			getLocaleRepresentations();

		logging.debug("Messages, getLocaleInfo " + localeInfo);

		return localeInfo;
	}

	private static List<LocaleRepresentation> getLocaleRepresentations() {
		if (existingLocales != null)
			return existingLocales;

		ArrayList<LocaleRepresentation> existingLocales = new ArrayList<>();
		localeInfo = new TreeMap<>();

		InputStream stream = de.uib.messages.Messages.class.getResourceAsStream(LOCALISATIONS_CONF);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) != '#')
					existingLocales.add(new LocaleRepresentation(line));
				line = reader.readLine();
			}
		} catch (Exception ex) {
			logging.warning("Messages exception on reading: " + ex);
		}

		TreeSet<String> names = new TreeSet<>();
		for (LocaleRepresentation representer : existingLocales) {
			names.add(representer.getName());
			localeInfo.put(representer.getName(), representer.getIconName());
		}
		logging.debug("Messages, existing names " + names);
		existingLocalesNames = new ArrayList<>(names);
		logging.debug("Messages, existing locales " + existingLocales);
		logging.debug("Messages, localeInfo  " + localeInfo);
		return existingLocales;
	}

	/*
	 * does not work in applet context
	 * public static List<String> getLocales()
	 * {
	 * if (existingLocales != null)
	 * return existingLocales;
	 * 
	 * existingLocales = new ArrayList<>();
	 * 
	 * try
	 * {
	 * URI uri = de.uib.messages.Messages.class.getResource(".").toURI();
	 * File messagesDir = new File( uri );
	 * logging.debug("Messages:, dir " + messagesDir);
	 * //logging.debug("Messages:, messagesDir isDirectory " +
	 * messagesDir.isDirectory());
	 * 
	 * final class PropertiesFilenameFilter implements FilenameFilter{
	 * public boolean accept(File dir, String name)
	 * {
	 * return name.endsWith("." + FILE_TYPE);
	 * }
	 * };
	 * PropertiesFilenameFilter filter = new PropertiesFilenameFilter();
	 * 
	 * 
	 * 
	 * //logging.debug("Messages: filter " + filter);
	 * 
	 * if (messagesDir.isDirectory())
	 * {
	 * String[] filenames = messagesDir.list(filter);
	 * for (int i = 0; i < filenames.length; i++)
	 * {
	 * String s = filenames[i];
	 * s = s.substring(prefix.length());
	 * s = s.substring(0, s.lastIndexOf("."));
	 * existingLocales.add(s);
	 * }
	 * }
	 * }
	 * catch(Exception ex)
	 * {
	 * logging.warning("Messages:, getLocales error: " + ex);
	 * }
	 * 
	 * 
	 * existingLocales = new ArrayList<>(new TreeSet<>(existingLocales));
	 * //logging.info("Messages:, getLocales: " + existingLocales);
	 * 
	 * return existingLocales;
	 * }
	 */
}
