/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import javax.swing.UIManager;

import com.jediterm.core.Color;
import com.jediterm.terminal.emulator.ColorPalette;

public class ColorPaletteImpl extends ColorPalette {
	private static final Color[] DEFAULT_COLORS = new Color[16];
	static {
		// Black
		DEFAULT_COLORS[0] = new Color(UIManager.getColor("Terminal.foreground").getRGB());
		// Red
		DEFAULT_COLORS[1] = new Color(0xff0000);
		// Green
		DEFAULT_COLORS[2] = new Color(0x33ff00);
		// Yellow
		DEFAULT_COLORS[3] = new Color(0xf3e207);
		// Blue
		DEFAULT_COLORS[4] = new Color(0x0066ff);
		// Magenta
		DEFAULT_COLORS[5] = new Color(0xe197f4);
		// Cyan
		DEFAULT_COLORS[6] = new Color(0x00ffff);
		// White
		DEFAULT_COLORS[7] = new Color(0xd0d0d0);

		// Bright versions of the ISO colors

		// Black
		DEFAULT_COLORS[8] = new Color(0x808080);
		// Red
		DEFAULT_COLORS[9] = new Color(0xff0000);
		// Green
		DEFAULT_COLORS[10] = new Color(0x33ff00);
		// Yellow
		DEFAULT_COLORS[11] = new Color(0xf3e207);
		// Blue
		DEFAULT_COLORS[12] = new Color(0x0066ff);
		// Magenta
		DEFAULT_COLORS[13] = new Color(0xe197f4);
		// Cyan
		DEFAULT_COLORS[14] = new Color(0x00ffff);
		// White
		DEFAULT_COLORS[15] = new Color(UIManager.getColor("Terminal.background").getRGB());
	}

	private Color[] colors;

	public ColorPaletteImpl(Color[] colors) {
		this.colors = colors;
	}

	public static ColorPalette getDefaultColorPalette() {
		Color[] defaultColors = DEFAULT_COLORS.clone();
		defaultColors[0] = new Color(UIManager.getColor("Terminal.foreground").getRGB());
		defaultColors[15] = new Color(UIManager.getColor("Terminal.background").getRGB());
		return new ColorPaletteImpl(defaultColors);
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
