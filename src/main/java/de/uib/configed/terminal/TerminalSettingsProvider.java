package de.uib.configed.terminal;

import java.awt.Color;
import java.awt.Font;

import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

public class TerminalSettingsProvider extends DefaultSettingsProvider {
	private static int fontSize = 12;
	private static final Color[] DARK_COLORS = new Color[16];
	static {
		// Black
		DARK_COLORS[0] = Color.decode("#ffffff");
		// Red
		DARK_COLORS[1] = Color.decode("#ff0000");
		// Green
		DARK_COLORS[2] = Color.decode("#33ff00");
		// Yellow
		DARK_COLORS[3] = Color.decode("#ff0099");
		// Blue
		DARK_COLORS[4] = Color.decode("#0066ff");
		// Magenta
		DARK_COLORS[5] = Color.decode("#cc00ff");
		// Cyan
		DARK_COLORS[6] = Color.decode("#00ffff");
		// White
		DARK_COLORS[7] = Color.decode("#d0d0d0");

		// Bright versions of the ISO colors

		// Black
		DARK_COLORS[8] = Color.decode("#808080");
		// Red
		DARK_COLORS[9] = Color.decode("#ff0000");
		// Green
		DARK_COLORS[10] = Color.decode("#33ff00");
		// Yellow
		DARK_COLORS[11] = Color.decode("#ff0099");
		// Blue
		DARK_COLORS[12] = Color.decode("#0066ff");
		// Magenta
		DARK_COLORS[13] = Color.decode("#cc00ff");
		// Cyan
		DARK_COLORS[14] = Color.decode("#00ffff");
		// White
		DARK_COLORS[15] = Color.decode("#000000");
	}
	private static final Color[] LIGHT_COLORS = new Color[16];
	static {
		// Black
		LIGHT_COLORS[0] = Color.decode("#000000");
		// Red
		LIGHT_COLORS[1] = Color.decode("#ff0000");
		// Green
		LIGHT_COLORS[2] = Color.decode("#33ff00");
		// Yellow
		LIGHT_COLORS[3] = Color.decode("#ff0099");
		// Blue
		LIGHT_COLORS[4] = Color.decode("#0066ff");
		// Magenta
		LIGHT_COLORS[5] = Color.decode("#cc00ff");
		// Cyan
		LIGHT_COLORS[6] = Color.decode("#00ffff");
		// White
		LIGHT_COLORS[7] = Color.decode("#d0d0d0");

		// Bright versions of the ISO colors

		// Black
		LIGHT_COLORS[8] = Color.decode("#808080");
		// Red
		LIGHT_COLORS[9] = Color.decode("#ff0000");
		// Green
		LIGHT_COLORS[10] = Color.decode("#33ff00");
		// Yellow
		LIGHT_COLORS[11] = Color.decode("#ff0099");
		// Blue
		LIGHT_COLORS[12] = Color.decode("#0066ff");
		// Magenta
		LIGHT_COLORS[13] = Color.decode("#cc00ff");
		// Cyan
		LIGHT_COLORS[14] = Color.decode("#00ffff");
		// White
		LIGHT_COLORS[15] = Color.decode("#ffffff");
	}

	private static MyColorPalette colorPalette = new MyColorPalette(DARK_COLORS);

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

	public static void setTerminalLightTheme() {
		colorPalette = new MyColorPalette(LIGHT_COLORS);
	}

	public static void setTerminalDarkTheme() {
		colorPalette = new MyColorPalette(DARK_COLORS);
	}

	private static class MyColorPalette extends ColorPalette {
		private Color[] colors;

		public MyColorPalette(Color[] colors) {
			this.colors = colors.clone();
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
