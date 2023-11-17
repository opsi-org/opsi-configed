/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.Font;

import com.jediterm.core.Color;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import de.uib.messages.Messages;
import de.uib.utilities.logging.Logging;

public class TerminalSettingsProvider extends DefaultSettingsProvider {
	public static final int FONT_SIZE_MIN_LIMIT = 8;
	public static final int FONT_SIZE_MAX_LIMIT = 62;
	public static final int FONT_SIZE_SCALING_FACTOR = 2;

	private static int fontSize = 12;
	private static final Color[] DARK_COLORS = new Color[16];
	static {
		// Black
		DARK_COLORS[0] = new Color(255, 255, 255);
		// Red
		DARK_COLORS[1] = new Color(255, 0, 0);
		// Green
		DARK_COLORS[2] = new Color(51, 255, 0);
		// Yellow
		DARK_COLORS[3] = new Color(255, 0, 153);
		// Blue
		DARK_COLORS[4] = new Color(0, 102, 255);
		// Magenta
		DARK_COLORS[5] = new Color(204, 0, 255);
		// Cyan
		DARK_COLORS[6] = new Color(0, 255, 255);
		// White
		DARK_COLORS[7] = new Color(208, 208, 208);

		// Bright versions of the ISO colors

		// Black
		DARK_COLORS[8] = new Color(128, 128, 128);
		// Red
		DARK_COLORS[9] = new Color(255, 0, 0);
		// Green
		DARK_COLORS[10] = new Color(51, 255, 0);
		// Yellow
		DARK_COLORS[11] = new Color(255, 0, 153);
		// Blue
		DARK_COLORS[12] = new Color(0, 102, 255);
		// Magenta
		DARK_COLORS[13] = new Color(204, 0, 255);
		// Cyan
		DARK_COLORS[14] = new Color(0, 255, 255);
		// White
		DARK_COLORS[15] = new Color(0, 0, 0);
	}
	private static final Color[] LIGHT_COLORS = new Color[16];
	static {
		// Black
		LIGHT_COLORS[0] = new Color(0, 0, 0);
		// Red
		LIGHT_COLORS[1] = new Color(255, 0, 0);
		// Green
		LIGHT_COLORS[2] = new Color(51, 255, 0);
		// Yellow
		LIGHT_COLORS[3] = new Color(255, 0, 153);
		// Blue
		LIGHT_COLORS[4] = new Color(0, 102, 255);
		// Magenta
		LIGHT_COLORS[5] = new Color(204, 0, 255);
		// Cyan
		LIGHT_COLORS[6] = new Color(0, 255, 255);
		// White
		LIGHT_COLORS[7] = new Color(208, 208, 208);

		// Bright versions of the ISO colors

		// Black
		LIGHT_COLORS[8] = new Color(128, 128, 128);
		// Red
		LIGHT_COLORS[9] = new Color(255, 0, 0);
		// Green
		LIGHT_COLORS[10] = new Color(51, 255, 0);
		// Yellow
		LIGHT_COLORS[11] = new Color(255, 0, 153);
		// Blue
		LIGHT_COLORS[12] = new Color(0, 102, 255);
		// Magenta
		LIGHT_COLORS[13] = new Color(204, 0, 255);
		// Cyan
		LIGHT_COLORS[14] = new Color(0, 255, 255);
		// White
		LIGHT_COLORS[15] = new Color(255, 255, 255);
	}

	private static String themeInUse = Messages.getSelectedTheme();
	private static MyColorPalette colorPalette = getColorPalette(themeInUse);

	@Override
	public Font getTerminalFont() {
		return new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
	}

	@Override
	public float getTerminalFontSize() {
		return fontSize;
	}

	@Override
	public boolean useInverseSelectionColor() {
		return true;
	}

	@Override
	public ColorPalette getTerminalColorPalette() {
		return colorPalette;
	}

	@Override
	public boolean useAntialiasing() {
		return true;
	}

	@Override
	public boolean audibleBell() {
		return true;
	}

	@Override
	public boolean scrollToBottomOnTyping() {
		return true;
	}

	public static void setTerminalFontSize(int size) {
		fontSize = size;
	}

	private static MyColorPalette getColorPalette(String theme) {
		switch (theme) {
		case Messages.THEME_LIGHT:
			return new MyColorPalette(LIGHT_COLORS);

		case Messages.THEME_DARK:
			return new MyColorPalette(DARK_COLORS);

		default:
			Logging.warning("Could not find MyColorPalette for theme " + theme);
			return new MyColorPalette(DARK_COLORS);
		}
	}

	public static void setTerminalTheme(String theme) {
		themeInUse = theme;
		colorPalette = getColorPalette(themeInUse);
	}

	public static String getTerminalThemeInUse() {
		return themeInUse;
	}

	private static class MyColorPalette extends ColorPalette {
		private Color[] colors;

		public MyColorPalette(Color[] colors) {
			this.colors = colors;
		}

		@Override
		public Color getForegroundByColorIndex(int colorIndex) {
			return colors[colorIndex];
		}

		@Override
		public Color getBackgroundByColorIndex(int colorIndex) {
			return colors[colorIndex];
		}
	}
}
