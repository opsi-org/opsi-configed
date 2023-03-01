package de.uib.configed.terminal;

import java.awt.Color;
import java.awt.Font;

import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

public class TerminalSettingsProvider extends DefaultSettingsProvider {
	protected static final Color[] COLORS = new Color[16];
	static {
		COLORS[0] = Color.decode("#ffffff");
		COLORS[1] = Color.decode("#ffffff");
		COLORS[2] = Color.decode("#ffffff");
		COLORS[3] = Color.decode("#ffffff");
		COLORS[4] = Color.decode("#ffffff");
		COLORS[5] = Color.decode("#FF8C42");
		COLORS[6] = Color.decode("#FBB13C");
		COLORS[7] = Color.decode("#D81159");
		COLORS[8] = Color.decode("#E0777D");
		COLORS[9] = Color.decode("#8E3B46");
		COLORS[10] = Color.decode("#A2AD59");
		COLORS[11] = Color.decode("#92140C");
		COLORS[12] = Color.decode("#253237");
		COLORS[13] = Color.decode("#5C6B73");
		COLORS[14] = Color.decode("#4C2719");
		COLORS[15] = Color.decode("#000000");
	}

	@Override
	public Font getTerminalFont() {
		return new Font(Font.MONOSPACED, Font.PLAIN, 12);
	}

	@Override
	public float getTerminalFontSize() {
		return 12.0F;
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

	public static void setTerminalForegroundColor(Color color) {
		COLORS[0] = color;
	}

	public static void setTerminalBackgroundColor(Color color) {
		COLORS[15] = color;
	}
}
