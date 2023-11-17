/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messages;

import java.awt.event.ActionEvent;
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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.UserPreferences;

public final class Messages {
	public static final String APPNAME = "configed";
	private static final String BUNDLE_NAME = "de/uib/messages/opsi-configed";
	private static final String LOCALISATIONS_CONF = "valid_localisations.conf";

	private static Set<String> existingLocalesNames;
	private static String localeString;
	private static Locale myLocale;
	private static ResourceBundle messagesBundle;
	private static final List<String> availableThemes = Arrays.asList("Light", "Dark");
	private static String selectedTheme = availableThemes.get(0);

	// private constructor to hide the implicit public one
	private Messages() {
	}

	private static String findExistingLocale() {
		if (existingLocalesNames.contains(myLocale.toString())) {
			return myLocale.toString();
		} else if (existingLocalesNames.contains(myLocale.getLanguage())) {
			return myLocale.getLanguage();
		} else {
			return null;
		}
	}

	public static String getSelectedLocale() {
		return localeString;
	}

	private static void produceSelectedLocale() {
		produceLocaleRepresentations();

		String existingLocale = findExistingLocale();

		if (existingLocale == null) {
			// not found, now try again for default locale
			produceLocale();

			existingLocale = findExistingLocale();

			if (existingLocale == null) {
				// default locale not found, get english locale
				produceLocaleEnUS();

				existingLocale = findExistingLocale();
			}
		}

		if (existingLocale == null) {
			Logging.warning("Locale " + myLocale + " not existing...");
		}

		Logging.info("Selected locale: " + existingLocale);

		localeString = existingLocale;
	}

	private static void produceLocale() {
		myLocale = Locale.getDefault();
	}

	private static void produceLocale(String language) {
		myLocale = new Locale.Builder().setLanguage(language).build();
	}

	private static void produceLocale(String language, String country) {
		myLocale = new Locale.Builder().setLanguage(language).setRegion(country).build();
	}

	private static void produceLocaleEnUS() {
		produceLocale("en", "US");
	}

	public static Locale getLocale() {
		return myLocale;
	}

	public static void setLocale(String localeString) {
		Logging.debug("Messages setLocale: " + localeString);
		if (localeString != null && !localeString.isEmpty()) {
			if (localeString.length() == 5 && localeString.indexOf('_') == 2) {
				produceLocale(localeString.substring(0, 2), localeString.substring(3, 5));
			} else if (localeString.length() == 2) {
				produceLocale(localeString);
			} else {
				Logging.warning("Bad format for locale, use <language>_<country> or <language>"
						+ ", each component consisting of two chars, or just a two char <language>");
			}
		}

		if (myLocale == null) {
			produceLocale();
		}

		produceSelectedLocale();

		Logging.notice("Locale set to: " + myLocale);

		messagesBundle = ResourceBundle.getBundle(BUNDLE_NAME, myLocale);
	}

	public static Set<String> getLocaleNames() {
		produceLocaleRepresentations();

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

	private static void produceLocaleRepresentations() {
		if (existingLocalesNames != null) {
			return;
		}

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

	public static JMenu createJMenuLanguages(Runnable runnable) {
		JMenu jMenuLanguage = new JMenu(Configed.getResourceValue("MainFrame.jMenuFileChooseLanguage"));
		ButtonGroup groupLanguages = new ButtonGroup();

		String selectedLocale = Messages.getSelectedLocale();

		for (String locale : Messages.getLocaleNames()) {
			ImageIcon localeIcon = new ImageIcon(Messages.class.getResource(locale + ".png"));

			JMenuItem menuItem = new JRadioButtonMenuItem(locale, localeIcon);
			Logging.debug("Selected locale " + selectedLocale);
			menuItem.setSelected(selectedLocale.equals(locale));
			jMenuLanguage.add(menuItem);
			groupLanguages.add(menuItem);

			menuItem.addActionListener((ActionEvent e) -> {
				UserPreferences.set(UserPreferences.LANGUAGE, locale);
				Messages.setLocale(locale);
				Locale.setDefault(new Locale(locale));
				JComponent.setDefaultLocale(new Locale(locale));
				runnable.run();
			});
		}

		return jMenuLanguage;
	}
}
