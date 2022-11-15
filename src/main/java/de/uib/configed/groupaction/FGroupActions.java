/* 
 * FGroupActions 
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2013 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */
 
package de.uib.configed.groupaction;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import utils.*;
import javax.swing.*;
import de.uib.messages.Messages;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.configed.type.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.opsidatamodel.*;


public class FGroupActions extends SecondaryFrame
{
	JPanel topPanel;
	JPanel imageActionPanel;
	
	JTextField fieldGroupname;
	JTextField fieldInvolvedClientsCount; 
	
	
	JComboBox comboSelectImage;
	
	java.util.List<String> associatedClients; 
	
	PersistenceController persist;
	ConfigedMain main; 
	
	int hFirstGap = Globals.hFirstGap;
	
	int firstLabelWidth =  Globals.buttonWidth; //Globals.firstLabelWidth;
	
	
	public FGroupActions(ConfigedMain main, PersistenceController persist, JFrame mainframe)
	{
		super();
		
		this.main =main; 
		this.persist = persist;
		
		define();
		reload();
		setGlobals(Globals.getMap());
		setTitle(Globals.APPNAME + " " + configed.getResourceValue("FGroupAction.title"));
		
	}
	
	protected void setGroupLabelling(String label, String clientCount)
	{
		fieldGroupname.setText(label);
		fieldInvolvedClientsCount.setText(clientCount);
	}
	
	@Override
	public void start()
	{
		super.start();
		reload();
	}
	
	
	
	protected void setImages()
	{
		Vector<String> imagesCollection  = new Vector<String>();
		//imagesCollection.add("");
		
		imagesCollection.addAll(new TreeSet<String>(
				persist.getCommonProductPropertyValues(
					associatedClients,
					persist.localImageRestoreProductKey,
					persist.localImagesListPropertyKey
					)
				)
		);
		
		comboSelectImage.setModel( new DefaultComboBoxModel( imagesCollection ) );
	}
	
	private void reload()
	{
		setGroupLabelling(
			main.getActivatedGroupModel().getLabel(),
			"" + main.getActivatedGroupModel().getNumberOfClients()
			);
		
		associatedClients = new ArrayList<String> ( main.getActivatedGroupModel().getAssociatedClients() );
		setImages();
	}
	
	protected void replay()
	{
		logging.debug(this, "replay " + comboSelectImage.getSelectedItem() );
		
		if (comboSelectImage.getSelectedItem() == null)
			return;
		
		String image = (String) comboSelectImage.getSelectedItem();
		
		
		ArrayList<String> values = new ArrayList<String>();
		values.add(image); //selected from common product property values
			
			
		WaitCursor waitCursor = new WaitCursor( this );
		
		persist.setCommonProductPropertyValue(
			main.getActivatedGroupModel().getAssociatedClients(),
			persist.localImageRestoreProductKey,
			persist.localImageToRestorePropertyKey,
			values
		);
			
		
		Map<String, String> changedValues = new HashMap<String, String>();
		changedValues.put(de.uib.opsidatamodel.productstate.ProductState.KEY_actionRequest, "setup");
		//ActionRequest.getLabel(ActionRequest.SETUP);
		
		persist.updateProductOnClients(
			//associatedClients,
			main.getActivatedGroupModel().getAssociatedClients(),
			persist.localImageRestoreProductKey,
			OpsiPackage.TYPE_NETBOOT,
			changedValues);
			
			
		waitCursor.stop();
		//if (comboSelectImage
		//String selectedImage = comboSelectImage
	}	
	
	
	protected void define()
	{
		topPanel = new JPanel();
		//topPanel.setBorder( Globals.createPanelBorder() );
		
		defineTopPanel( topPanel );
		
		imageActionPanel = new JPanel();
		imageActionPanel.setBorder( Globals.createPanelBorder() );
		
		defineImageActionPanel(imageActionPanel);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(topPanel, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize) 
			.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(topPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		);
		
		Containership cs_all = new Containership (getContentPane());
		cs_all.doForAllContainedCompisOfClass 
		 ("setBackground", new Object[]{Globals.backLightBlue}, JPanel.class);
		 
		 cs_all.doForAllContainedCompisOfClass 
		 ("setBackground", new Object[]{Globals.backgroundLightGrey}, javax.swing.text.JTextComponent.class);
		
	}
	
	
	
	
	private void defineImageActionPanel(JPanel panel)
	{
		JLabel labelCombo = new JLabel(configed.getResourceValue("FGroupAction.existingImages"));
		//labelCombo.setPreferredSize(new Dimension(200, Globals.lineHeight));
		comboSelectImage = new JComboBox();
		//comboSelectImage.setPreferredSize(new Dimension(200, Globals.lineHeight));
		
		
		JLabel topicLabel =  new JLabel(configed.getResourceValue("FGroupAction.replayImage"));
		
		
		JButton buttonSetup = new JButton(configed.getResourceValue("FGroupAction.buttonSetup"));
		buttonSetup.setToolTipText(configed.getResourceValue("FGroupAction.buttonSetup.tooltip"));
		
		buttonSetup.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					//logging.info(this, "actionPerformed");
					replay();
				}
			}
		);
		
		IconButton buttonReload = new IconButton(configed.getResourceValue("FGroupAction.buttonReload"),
			"images/reload16.png",
			"images/reload16_over.png", 
			"images/reload16_disabled.png",
			true
			);
//buttonReload.setPreferredSize(new Dimension(60, 40));
		//buttonReload.setBackground(de.uib.utilities.Globals.backgroundLightGrey);
		
		buttonReload.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					//logging.info(this, "actionPerformed");
					reload();
				}
			}
        			
        );
		
        
        GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(Globals.vGapSize, Globals.vGapSize*3, Globals.vGapSize * 4)
			.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize * 2)
			.addGroup( layout.createParallelGroup(GroupLayout.Alignment.CENTER) 
				.addComponent(labelCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSelectImage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonSetup, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(buttonReload, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
			)
			.addGap(Globals.vGapSize, Globals.vGapSize * 3, Globals.vGapSize * 4)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGap(Globals.hGapSize, Globals.hGapSize*2,  Short.MAX_VALUE)
				.addComponent(topicLabel,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.hGapSize, Globals.hGapSize*2, Short.MAX_VALUE)
			)
			
			.addGroup(layout.createSequentialGroup()
				.addGap(Globals.hGapSize, Globals.hFirstGap, Globals.hFirstGap) 
				.addComponent(labelCombo, firstLabelWidth, firstLabelWidth, firstLabelWidth)
				.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize )
				.addComponent(comboSelectImage, GroupLayout.PREFERRED_SIZE, Globals.buttonWidth * 2, Short.MAX_VALUE)
				.addGap(Globals.hGapSize * 2, Globals.hGapSize * 4, Globals.hGapSize * 4)
				.addComponent(buttonSetup, Globals.buttonWidth, Globals.buttonWidth, Globals.buttonWidth)
				.addGap( Globals.hGapSize * 2, Globals.hGapSize * 2,  Globals.hGapSize * 2)
				.addComponent(buttonReload,de.uib.utilities.Globals.iconWidth, de.uib.utilities.Globals.iconWidth, de.uib.utilities.Globals.iconWidth)
				.addGap(Globals.hGapSize, Globals.hFirstGap, Short.MAX_VALUE)
			)
//////////////////////////////////////////////////////////////////////
		);
        
        		
        
		/*
		imageActionPanel = new PanelLinedComponents(new JComponent[]{
			new JLabel(" "),
			labelCombo,
			new JLabel(" "),
			comboSelectImage,
			new JLabel(" "),
			buttonSetup,
			new JLabel(" "),
			new JLabel(" "),
			buttonReload
		});
		*/
		
		
		
		/*
		persist.getCommonProductPropertyValues(
			new ArrayList<String> (
				main.getActivatedGroupModel().getAssociatedClients()
			),
			persist.localImageRestoreProductKey,
			persist.localImagesListPropertyKey
			);
		
		
		
		//set common property
		ArrayList<String> values = new ArrayList<String>();
		values.add("win2000"); //selected from common product property values
			
		persist.setCommonProductPropertyValue(
			main.getActivatedGroupModel().getAssociatedClients(),
			persist.localImageRestoreProductKey,
			persist.localImageToRestorePropertyKey,
			values
		);
			
		
		//set to update
			
		
		Map<String, String> changedValues = new HashMap<String, String>();
		changedValues.put(de.uib.opsidatamodel.productstate.ProductState.KEY_actionRequest, "setup");
		//ActionRequest.getLabel(ActionRequest.SETUP);
		
		persist.updateProductOnClients( 
			main.getActivatedGroupModel().getAssociatedClients(),
			persist.localImageRestoreProductKey,
			OpsiPackage.TYPE_NETBOOT,
			changedValues);
			
		*/
			
			
	}
	
	
	
	
	private void defineTopPanel(JPanel panel)
	{
		JLabel groupNameLabel =  new JLabel( configed.getResourceValue("FGroupAction.groupname"));
		//JLabel clientsLabel = new JLabel("clients");
		JLabel clientsCountLabel = new JLabel( configed.getResourceValue("FGroupAction.clientcounter"));
		
		fieldGroupname = new JTextField();
		fieldGroupname.setPreferredSize(Globals.counterfieldDimension);
		fieldGroupname.setEditable(false);
		
		fieldInvolvedClientsCount = new JTextField("");
		fieldInvolvedClientsCount.setPreferredSize(Globals.counterfieldDimension);
		fieldInvolvedClientsCount.setEditable(false);
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize * 2)
			.addGroup( layout.createParallelGroup(GroupLayout.Alignment.CENTER) 
				.addComponent(groupNameLabel, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(fieldGroupname, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(clientsCountLabel, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(fieldInvolvedClientsCount, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
			)
			.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize * 2)
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGap(Globals.hGapSize, Globals.hFirstGap, Globals.hFirstGap) 
			.addComponent(groupNameLabel, firstLabelWidth, firstLabelWidth, firstLabelWidth)
			.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
			.addComponent(fieldGroupname, Globals.buttonWidth,Globals.buttonWidth, Short.MAX_VALUE)
			.addGap(Globals.hGapSize, Globals.hGapSize*2, Globals.hGapSize * 2)
			.addComponent(clientsCountLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize * 2)
			.addComponent(fieldInvolvedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize * 2)
		);
		
	}
	
}