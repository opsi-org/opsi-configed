package de.uib.utilities.swing;

import java.awt.*;
import javax.swing.*;

//Version:
//Copyright:    Copyright (c) 2013 uib.de
//Author:       Rupert Röder



public class ProgressBarPainter implements Painter<JProgressBar> 
{
	private final Color color;

	public ProgressBarPainter(Color c1) {
		this.color = c1;
	}
	@Override
	public void paint(Graphics2D gd, JProgressBar t, int width, int height) {
		gd.setColor(color);
		gd.fillRect(0, 0, width, height);
	}
}
