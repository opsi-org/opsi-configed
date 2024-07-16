/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.userprefs;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public final class ThemeManager {
	public static final String THEME_LIGHT = "Light";
	public static final String THEME_DARK = "Dark";

	private static final List<String> availableThemes = Arrays.asList(THEME_LIGHT, THEME_DARK);
	private static String selectedTheme = availableThemes.get(0);

	private ThemeManager() {
	}

	public static String getSelectedTheme() {
		return selectedTheme;
	}

	public static String getThemeTranslation(String theme) {
		switch (theme) {
		case THEME_LIGHT:
			return Configed.getResourceValue("theme.light");

		case THEME_DARK:
			return Configed.getResourceValue("theme.dark");

		default:
			Logging.warning("Cannot find translation for theme ", theme);
			return null;
		}
	}

	public static void setThemeIcon(AbstractButton abstractButton, String theme) {
		switch (theme) {
		case THEME_LIGHT:
			Utils.addIntellijIconToMenuItem(abstractButton, "lightTheme");
			break;

		case THEME_DARK:
			Utils.addIntellijIconToMenuItem(abstractButton, "darkTheme");
			break;

		default:
			Logging.warning("Cannot find translation for theme ", theme);
		}
	}

	public static List<String> getAvailableThemes() {
		return availableThemes;
	}

	public static void setTheme(String newTheme) {
		if (availableThemes.contains(newTheme)) {
			selectedTheme = newTheme;
		} else {
			Logging.warning("Failing to set theme that does not exist: ", newTheme);
		}
	}

	public static void setOpsiLaf() {
		Logging.info("set look and feel " + getSelectedTheme());

		// Location of the theme property files - register them
		FlatLaf.registerCustomDefaultsSource("de.uib.configed.themes");

		registerOpenSansFont();

		switch (getSelectedTheme()) {
		case THEME_LIGHT:
			FlatLightLaf.setup();
			break;

		case THEME_DARK:
			FlatDarkLaf.setup();
			break;

		default:
			Logging.warning("tried to set theme in setOpsiLaf that does not exist: ", getSelectedTheme());
			break;
		}

		if (SystemInfo.isLinux) {
			// enable custom window decorations
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		}

		Globals.setTableColors();
	}

	private static void registerOpenSansFont() {
		try (InputStream fontStream = Main.class.getResourceAsStream("/fonts/OpenSans.ttf")) {
			Font openSansFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
			openSansFont = openSansFont.deriveFont(13F);
			UIManager.put("defaultFont", openSansFont);
		} catch (IOException e) {
			Logging.error(e, "Failed to retrieve font from resources (using font chosen by the system)");
		} catch (FontFormatException e) {
			Logging.error(e, "Font is faulty");
		}
	}
}
