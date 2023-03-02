package de.uib.configed.terminal;

import java.awt.Color;
import java.awt.Font;

import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

public class TerminalSettingsProvider extends DefaultSettingsProvider {
	private static int fontSize = 12;

	protected static final Color[] COLORS = new Color[16];
	static {
		COLORS[0] = Color.decode("#d0d0d0");
		COLORS[1] = Color.decode("#d0d0d0");
		COLORS[2] = Color.decode("#ff0000");
		COLORS[3] = Color.decode("#33ff00");
		COLORS[4] = Color.decode("#ff0099");
		COLORS[5] = Color.decode("#0066ff");
		COLORS[6] = Color.decode("#cc00ff");
		COLORS[7] = Color.decode("#00ffff");
		COLORS[8] = Color.decode("#d0d0d0");
		COLORS[9] = Color.decode("#808080");
		COLORS[10] = Color.decode("#ff0000");
		COLORS[11] = Color.decode("#33ff00");
		COLORS[12] = Color.decode("#ff0099");
		COLORS[13] = Color.decode("#0066ff");
		COLORS[14] = Color.decode("#cc00ff");
		COLORS[15] = Color.decode("#000000");
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
	public ColorPalette getTerminalColorPalette() {
		return new ColorPalette() {
			@Override
			protected Color getBackgroundByColorIndex(int arg0) {
				return COLORS[arg0];
			}

			@Override
			protected Color getForegroundByColorIndex(int arg0) {
				return COLORS[arg0];
			}
		};
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

	public static void setTerminalLightTheme() {
		COLORS[0] = Color.decode("#606060");
		COLORS[1] = Color.decode("#606060");
		COLORS[2] = Color.decode("#ff0000");
		COLORS[3] = Color.decode("#33ff00");
		COLORS[4] = Color.decode("#ff0099");
		COLORS[5] = Color.decode("#0066ff");
		COLORS[6] = Color.decode("#cc00ff");
		COLORS[7] = Color.decode("#00ffff");
		COLORS[8] = Color.decode("#d0d0d0");
		COLORS[9] = Color.decode("#808080");
		COLORS[10] = Color.decode("#ff0000");
		COLORS[11] = Color.decode("#33ff00");
		COLORS[12] = Color.decode("#ff0099");
		COLORS[13] = Color.decode("#0066ff");
		COLORS[14] = Color.decode("#cc00ff");
		COLORS[15] = Color.decode("#ffffff");
	}

	public static void setTerminalDarkTheme() {
		COLORS[0] = Color.decode("#d0d0d0");
		COLORS[1] = Color.decode("#d0d0d0");
		COLORS[2] = Color.decode("#ff0000");
		COLORS[3] = Color.decode("#33ff00");
		COLORS[4] = Color.decode("#ff0099");
		COLORS[5] = Color.decode("#0066ff");
		COLORS[6] = Color.decode("#cc00ff");
		COLORS[7] = Color.decode("#00ffff");
		COLORS[8] = Color.decode("#d0d0d0");
		COLORS[9] = Color.decode("#808080");
		COLORS[10] = Color.decode("#ff0000");
		COLORS[11] = Color.decode("#33ff00");
		COLORS[12] = Color.decode("#ff0099");
		COLORS[13] = Color.decode("#0066ff");
		COLORS[14] = Color.decode("#cc00ff");
		COLORS[15] = Color.decode("#000000");
	}
}
