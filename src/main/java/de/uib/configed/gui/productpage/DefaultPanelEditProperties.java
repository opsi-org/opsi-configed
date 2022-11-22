package de.uib.configed.gui.productpage;

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

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.type.*;
import de.uib.opsidatamodel.datachanges.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;
import org.jdesktop.swingx.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.list.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.datapanel.*;
import de.uib.utilities.logging.*;

import de.uib.configed.guidata.*;
import de.uib.configed.gui.helper.*;
import de.uib.opsidatamodel.productstate.*;

public abstract class DefaultPanelEditProperties extends JXPanel
{
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
	
	public DefaultPanelEditProperties( ConfigedMain mainController, 
		de.uib.utilities.datapanel.AbstractEditMapPanel productPropertiesPanel
		)
	{
		super();
		this.mainController = mainController;
		this.productPropertiesPanel = productPropertiesPanel;
		//initComponents();
		setBackgroundPainter(new org.jdesktop.swingx.painter.AbstractPainter()
			{
				@Override
				public void doPaint( Graphics2D g, Object obj, int width, int height)
				{
					g.setPaint(de.uib.configed.Globals.backLightBlue);
					g.fillRect(0,0,width, height);
				}
			}
		);
	}
	
	public abstract JPanel getTitlePanel();
	public abstract void setTitlePanelActivated(boolean actived);
	
	public JLabel getDepotLabel() {
		return null;
	}
}
