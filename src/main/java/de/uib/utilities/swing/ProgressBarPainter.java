/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JProgressBar;
import javax.swing.Painter;

import de.uib.Main;

public class ProgressBarPainter implements Painter<JProgressBar> {
	private final Color color;

	public ProgressBarPainter(Color c1) {
		this.color = c1;
	}

	@Override
	public void paint(Graphics2D gd, JProgressBar t, int width, int height) {
		if (!Main.THEMES) {
			gd.setColor(color);
			gd.fillRect(0, 0, width, height);
		}
	}
}
