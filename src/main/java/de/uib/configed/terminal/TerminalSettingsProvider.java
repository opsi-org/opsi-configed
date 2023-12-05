/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.Font;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import de.uib.messages.Messages;

public class TerminalSettingsProvider extends DefaultSettingsProvider {
	public static final int FONT_SIZE_MIN_LIMIT = 8;
	public static final int FONT_SIZE_MAX_LIMIT = 62;
	public static final TerminalColor BACKGROUND_COLOR_DARK = new TerminalColor(0, 0, 0);
	public static final TerminalColor FOREGROUND_COLOR_DARK = new TerminalColor(208, 208, 208);
	public static final TerminalColor BACKGROUND_COLOR_LIGHT = new TerminalColor(249, 249, 249);
	public static final TerminalColor FOREGROUND_COLOR_LIGHT = new TerminalColor(96, 96, 96);

	private static int fontSize = 12;
	private static String themeInUse = Messages.getSelectedTheme();

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
	public TextStyle getDefaultStyle() {
		TextStyle defaultStyle = new TextStyle(FOREGROUND_COLOR_DARK, BACKGROUND_COLOR_DARK);
		if ("Light".equals(themeInUse)) {
			defaultStyle = new TextStyle(FOREGROUND_COLOR_LIGHT, BACKGROUND_COLOR_LIGHT);
		}
		return defaultStyle;
	}

	@Override
	public ColorPalette getTerminalColorPalette() {
		return ColorPaletteImpl.DEFAULT_COLOR_PALETTE;
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

	public static void setTerminalTheme(String theme) {
		themeInUse = theme;
	}

	public static String getTerminalThemeInUse() {
		return themeInUse;
	}
}
