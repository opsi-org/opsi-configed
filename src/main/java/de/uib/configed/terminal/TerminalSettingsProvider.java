/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;

import javax.swing.KeyStroke;

import org.jetbrains.annotations.NotNull;

import com.jediterm.core.Platform;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import de.uib.configed.Configed;
import de.uib.messages.Messages;

public class TerminalSettingsProvider extends DefaultSettingsProvider {
	public static final int FONT_SIZE_MIN_LIMIT = 8;
	public static final int FONT_SIZE_MAX_LIMIT = 62;
	public static final TerminalColor BACKGROUND_COLOR_DARK = new TerminalColor(0, 0, 0);
	public static final TerminalColor FOREGROUND_COLOR_DARK = new TerminalColor(208, 208, 208);
	public static final TerminalColor BACKGROUND_COLOR_LIGHT = new TerminalColor(249, 249, 249);
	public static final TerminalColor FOREGROUND_COLOR_LIGHT = new TerminalColor(96, 96, 96);

	private static String theme = Messages.getSelectedTheme();
	private int fontSize = 12;

	@Override
	public @NotNull TerminalActionPresentation getOpenUrlActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.openAsUrl"),
				Collections.emptyList());
	}

	@Override
	public @NotNull TerminalActionPresentation getCopyActionPresentation() {
		KeyStroke keyStroke = Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK)
				: KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.copy"), keyStroke);
	}

	@Override
	public @NotNull TerminalActionPresentation getPasteActionPresentation() {
		KeyStroke keyStroke = Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_DOWN_MASK)
				: KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.paste"), keyStroke);
	}

	@Override
	public @NotNull TerminalActionPresentation getClearBufferActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.clearBuffer"),
				Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.META_DOWN_MASK)
						: KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
	}

	@Override
	public @NotNull TerminalActionPresentation getPageUpActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.pageUp"),
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_DOWN_MASK));
	}

	@Override
	public @NotNull TerminalActionPresentation getPageDownActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.pageDown"),
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.SHIFT_DOWN_MASK));
	}

	@Override
	public @NotNull TerminalActionPresentation getLineUpActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.lineUp"),
				Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.META_DOWN_MASK)
						: KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
	}

	@Override
	public @NotNull TerminalActionPresentation getLineDownActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.lineDown"),
				Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_DOWN_MASK)
						: KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));
	}

	@Override
	public @NotNull TerminalActionPresentation getFindActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.find"),
				Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK)
						: KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
	}

	@Override
	public @NotNull TerminalActionPresentation getSelectAllActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.popup.selectAll"),
				Collections.emptyList());
	}

	public @NotNull TerminalActionPresentation getNewWindowActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.menuBar.fileMenu.openNewWindow"),
				Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK)
						: KeyStroke.getKeyStroke(KeyEvent.VK_N,
								InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
	}

	public @NotNull TerminalActionPresentation getNewSessionActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.menuBar.fileMenu.openNewSession"),
				Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.META_DOWN_MASK)
						: KeyStroke.getKeyStroke(KeyEvent.VK_T,
								InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
	}

	public @NotNull TerminalActionPresentation getChangeSessionActionPresentation() {
		return new TerminalActionPresentation(Configed.getResourceValue("Terminal.menuBar.fileMenu.changeSession"),
				Platform.isMacOS() ? KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK)
						: KeyStroke.getKeyStroke(KeyEvent.VK_S,
								InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
	}

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
		if ("Light".equals(theme)) {
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

	public void setTerminalFontSize(int size) {
		fontSize = size;
	}

	public static void setTerminalTheme(String theme) {
		TerminalSettingsProvider.theme = theme;
	}

	public static String getTerminalTheme() {
		return theme;
	}
}
