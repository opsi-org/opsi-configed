package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JProgressBar;
import javax.swing.Painter;

import de.uib.configed.ConfigedMain;

//Version:
//Copyright:    Copyright (c) 2013 uib.de
//Author:       Rupert Röder

public class ProgressBarPainter implements Painter<JProgressBar> {
	private final Color color;

	public ProgressBarPainter(Color c1) {
		this.color = c1;
	}

	@Override
	public void paint(Graphics2D gd, JProgressBar t, int width, int height) {
		if (!ConfigedMain.THEMES) {
			gd.setColor(color);
			gd.fillRect(0, 0, width, height);
		}
	}
}
