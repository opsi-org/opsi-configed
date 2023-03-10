package de.uib.configed.gui.productpage;

import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.AbstractPainter;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2014 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

public abstract class AbstractPanelEditProperties extends JXPanel {
	protected de.uib.utilities.datapanel.AbstractEditMapPanel productPropertiesPanel;

	protected int minLabelVSize = 0;
	protected int minTableVSize = 40;
	protected int minGapVSize = 2;
	protected int minVSize = 10;
	protected int prefVSize = 20;
	protected int vGapSize = 5;
	protected int hGapSize = 2;
	protected int minHSize = 50;
	protected int prefHSize = 80;

	protected ConfigedMain mainController;

	protected String productEdited;

	protected AbstractPanelEditProperties(ConfigedMain mainController,
			de.uib.utilities.datapanel.AbstractEditMapPanel productPropertiesPanel) {
		super();
		this.mainController = mainController;
		this.productPropertiesPanel = productPropertiesPanel;

		setBackgroundPainter(new AbstractPainter<AbstractPanelEditProperties>() {
			@Override
			public void doPaint(Graphics2D g, AbstractPanelEditProperties obj, int width, int height) {
				g.setPaint(Globals.BACKGROUND_COLOR_7);
				g.fillRect(0, 0, width, height);
			}
		});
	}

	public abstract JPanel getTitlePanel();

	public abstract void setTitlePanelActivated(boolean actived);

	public JLabel getDepotLabel() {
		return null;
	}
}
