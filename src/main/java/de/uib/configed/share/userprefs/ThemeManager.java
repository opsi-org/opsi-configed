/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share.userprefs;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import de.uib.configed.app.Main;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public final class ThemeManager {
	public static final String THEME_LIGHT = "Light";
	public static final String THEME_DARK = "Dark";

	/**
	 * Path to the OpenSans font resource. IMPORTANT: If you change this font,
	 * you MUST also update references to the font name in 'default.css' (and
	 * any other relevant stylesheet) for JavaFX. The font name used in CSS must
	 * match the loaded font.
	 */
	private static final String FONT_PATH = "/fonts/OpenSans.ttf";
	private static final String BOLD_FONT_PATH = "/fonts/OpenSans-Bold.ttf";
	private static final float UI_FONT_SIZE = 13F;
	private static final int JAVAFX_FONT_SIZE = 13;
	private static final String THEMES_PACKAGE = "de.uib.configed.themes";

	private static final List<String> availableThemes = List.of(THEME_LIGHT, THEME_DARK);
	private static String selectedTheme = availableThemes.get(0);

	private ThemeManager() {
	}

	public static String getSelectedTheme() {
		return selectedTheme;
	}

	public static String getThemeTranslation(String theme) {
		return switch (theme) {
		case THEME_LIGHT -> Configed.getResourceValue("theme.light");
		case THEME_DARK -> Configed.getResourceValue("theme.dark");
		default -> {
			Logging.warning("Cannot find translation for theme ", theme);
			yield null;
		}
		};
	}

	public static void setThemeIcon(AbstractButton abstractButton, String theme) {
		switch (theme) {
		case THEME_LIGHT -> Icons.addIntellijIconToMenuItem(abstractButton, "lightTheme");
		case THEME_DARK -> Icons.addIntellijIconToMenuItem(abstractButton, "darkTheme");
		default -> Logging.warning("Cannot find translation for theme ", theme);
		}
	}

	public static List<String> getAvailableThemes() {
		return availableThemes;
	}

	public static void setTheme(String newTheme) {
		if (availableThemes.contains(newTheme)) {
			selectedTheme = newTheme;
		} else {
			Logging.warning("Failed to set theme that does not exist:", newTheme);
		}
	}

	public static void setOpsiLaf() {
		Logging.info("set look and feel ", getSelectedTheme());

		// Location of the theme property files - register them
		FlatLaf.registerCustomDefaultsSource(THEMES_PACKAGE);

		registerOpenSansFonts();

		switch (getSelectedTheme()) {
		case THEME_LIGHT -> FlatLightLaf.setup();
		case THEME_DARK -> FlatDarkLaf.setup();
		default -> Logging.warning("Tried to set theme in setOpsiLaf that does not exist: ", getSelectedTheme());
		}

		Globals.setTableColors();
	}

	private static void registerOpenSansFonts() {
		registerSwingFont();
		registerJavaFxFont(FONT_PATH);
		registerJavaFxFont(BOLD_FONT_PATH);
	}

	private static void registerSwingFont() {
		try (InputStream fontStream = Main.class.getResourceAsStream(FONT_PATH)) {
			if (fontStream == null) {
				Logging.error("OpenSans font resource not found for Swing!");
				return;
			}
			Font openSansFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(UI_FONT_SIZE);
			UIManager.put("defaultFont", openSansFont);
		} catch (IOException | FontFormatException e) {
			Logging.error(e, "Failed to load or parse OpenSans font for Swing (using system font)");
		}
	}

	private static void registerJavaFxFont(String fontPath) {
		try (InputStream fontStream = Main.class.getResourceAsStream(fontPath)) {
			if (fontStream == null) {
				Logging.error("OpenSans font resource not found for JavaFX!");
				return;
			}
			javafx.scene.text.Font.loadFont(fontStream, JAVAFX_FONT_SIZE);
		} catch (Exception e) {
			Logging.error(e, "Failed to load OpenSans font for JavaFX.");
		}
	}
}
