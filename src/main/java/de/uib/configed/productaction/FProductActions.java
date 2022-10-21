/* 
 * FProductActions 
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2013 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */
 
package de.uib.configed.productaction;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import utils.*;
import javax.swing.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.opsidatamodel.*;


public class FProductActions extends SecondaryFrame
{
	JPanel panelInstallOpsiPackage;
	JPanel imageActionPanel;
	JPanel panelCompleteWinProducts;
	
	JTextField fieldGroupname;
	JTextField fieldInvolvedClientsCount; 
	
	
	JComboBox comboSelectImage;
	
	
	PersistenceController persist;
	ConfigedMain main; 
	
	int firstLabelWidth = 150;
	int groupnameWidth = 300;
	
	public FProductActions(ConfigedMain main, PersistenceController persist, JFrame mainframe)
	{
		super();
		
		this.main =main; 
		this.persist = persist;
		
		define();
		setGlobals(Globals.getMap());
		setTitle(Globals.APPNAME + " " + configed.getResourceValue("FProductAction.title"));
		
	}
	
	
	@Override
	public void start()
	{
		super.start();
	}

	
	protected void define()
	{
		PanelInstallOpsiPackage panelInstallOpsiPackage = new PanelInstallOpsiPackage(main, persist, this);
		
		imageActionPanel = new JPanel();
		//defineImageActionPanel(imageActionPanel);
		
		PanelCompleteWinProducts panelCompleteWinProducts = new PanelCompleteWinProducts(main, persist, this);
		
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(panelInstallOpsiPackage, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize) 
			.addComponent(panelCompleteWinProducts, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
			.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(panelInstallOpsiPackage, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(panelCompleteWinProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		);
		
		Containership cs_all = new Containership (getContentPane());
		cs_all.doForAllContainedCompisOfClass 
		 ("setBackground", new Object[]{Globals.backLightBlue}, JPanel.class);
		 
		 cs_all.doForAllContainedCompisOfClass 
		 ("setBackground", new Object[]{Globals.backgroundLightGrey}, javax.swing.text.JTextComponent.class);
		
	}
	
	
	
	
	
	
	
}