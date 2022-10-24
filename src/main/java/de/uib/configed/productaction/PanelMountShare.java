/* 
 * PanelMountShare
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

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import utils.*;
import javax.swing.*;
import de.uib.messages.Messages;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.NameProducer;
import de.uib.opsidatamodel.*;


public class PanelMountShare extends JPanel
{
	static ArrayList<PanelMountShare> instances = new ArrayList<PanelMountShare>(); 
	
	final int firstLabelWidth = Globals.firstLabelWidth;
	
	PersistenceController persist;
	ConfigedMain main;
	JFrame rootFrame;
	
	
	JButton buttonMountShare;
	JLabel mountShareLabel;
	JLabel mountShareDescriptionLabel;
	
	
	final boolean isWindows;
	
	boolean smbMounted;
	
	int leftBound = -1;
	
	NameProducer np;
	
	public PanelMountShare(NameProducer np, ConfigedMain main, JFrame root)
	{
		this(np, main, root, -1);
	}
	
	public PanelMountShare(NameProducer np, ConfigedMain main, JFrame root, int leftBound)
	{
		instances.add(this);
		this.main =main; 
		this.rootFrame = root;
		this.np = np;
		this.leftBound = leftBound;
		
		isWindows = de.uib.utilities.Globals.isWindows();
		smbMounted = false;
		
		initComponents();
		defineLayout();
	}
	
	
	private void initComponents()
	{
		
		buttonMountShare  = new JButton("",  Globals.createImageIcon("images/windows16.png", "" ));
		buttonMountShare.setSelectedIcon( Globals.createImageIcon("images/windows16.png", "" ) );
		buttonMountShare.setPreferredSize(Globals.graphicButtonDimension);
		if (isWindows) buttonMountShare.setToolTipText(configed.getResourceValue("PanelMountShare.mountShareDescription") + " " + np.produceName()) ;
		
		buttonMountShare.setEnabled(isWindows);
		
		
		buttonMountShare.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					callMountShare();
				}
			}
		);
	}
	
	private void defineLayout()
	{
		
		//mountShareLabel = new JLabel( configed.getResourceValue("PanelMountShare.mountShare" )  + " " + np.produceName() );
		mountShareLabel = new JLabel("");
		
		mountShareDescriptionLabel = new JLabel( configed.getResourceValue("PanelMountShare.mountShareResult0" )   + " " + np.getDefaultName() );
		//if (!isWindows)
		//	mountShareDescriptionLabel.setForeground(Globals.greyed);
		
		
		checkConnectionToShare();
		//mountShareDescriptionLabel.setVisible(isWindows);
		
		JPanel panel = this;
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		int hFirstGap = Globals.hFirstGap;
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup( layout.createParallelGroup(GroupLayout.Alignment.CENTER) 
				.addComponent(mountShareLabel, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(buttonMountShare, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(mountShareDescriptionLabel, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				)
			)
		;
		
		if (leftBound >= 0)
		{
			layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(mountShareLabel, 0, 0, 0)
					.addGap(leftBound, leftBound, leftBound) 
					.addComponent(buttonMountShare,  de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
					.addGap(hFirstGap, hFirstGap, hFirstGap)
					.addComponent( mountShareDescriptionLabel, Globals.buttonWidth *2 ,  Globals.buttonWidth*3,  Short.MAX_VALUE)
					.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize )
				)
			);
		}
		else
		{
			
			layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addGap(hFirstGap, hFirstGap,  hFirstGap)
					.addComponent(mountShareLabel, firstLabelWidth, firstLabelWidth, firstLabelWidth)
					.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize ) 
					.addComponent(buttonMountShare, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					.addGap(hFirstGap, hFirstGap, hFirstGap)
					.addComponent( mountShareDescriptionLabel, Globals.buttonWidth *2 ,  Globals.buttonWidth*2,  Short.MAX_VALUE)
					.addGap(5, 5, 5)
					.addGap(Globals.hGapSize, Globals.hGapSize*3,  Short.MAX_VALUE)
				)
			);
		}
	}

	public void mount(boolean mounted)
	{
		smbMounted = mounted;
		if (!mounted)
		{
			callMountShare();
		}
		
		setShareMountedInfo(smbMounted);
	}
		
	
	private void callMountShare()
	{
		if (!isWindows)
			return;
		
		String call;
		call = "explorer.exe " + " \"" + np.produceName() +  "\"";
		//call = "explorer.exe " + " \"" +  fieldServerPath.getText() + "\"";
		//call = "cmd.exe /c start \"\" \"" + fileName +FileUtils "\"";
		logging.info(this, "windows call: " + call);
		
		
		try
		{
			Runtime.getRuntime().exec(call);
		 } 
		 catch (IOException ioex) 
		 {
			logging.error("io-Error: " + ioex, ioex);
		 } 
		 catch (Exception ex) 
		{
			logging.error("general error on starting net mount " + ex, ex);
		}
		
		mountShareDescriptionLabel.setText( configed.getResourceValue("PanelMountShare.mountShareResult1")   + " " + np.produceName() );
		
		checkConnectionToShare(240);
		
		
		
		
		
		
		//rootFrame.toFront(); //of no use, since we probably do not wait for the runtime
	}
	
	private static void checkAllConnections()
	{
		for (PanelMountShare panel : instances)
		{
			panel.checkConnectionToShare();
		}
	}
	
	
	private void setShareMountedInfo(boolean mounted)
	{
		if (mounted)
			mountShareDescriptionLabel.setText( configed.getResourceValue("PanelMountShare.mountShareResult2")   + " " + np.produceName());
		else
			mountShareDescriptionLabel.setText( configed.getResourceValue("PanelMountShare.mountShareResult0") );
		
	}

	
	protected boolean checkConnectionToShare()
	{
		boolean found = false;
		
		if (np.produceName() == null || np.produceName().equals(""))
		{
			logging.info(this, "checkConnectionToShare no filename " + np.produceName());
		}
		else
		{
			File f = new File(np.produceName());
			if (!f.exists()) 
			{
				logging.info(this, "checkConnectionToShare no existing filename " + np.produceName());
			}
			else
			{
				found = f.isDirectory();
				if (!found)
				{
					logging.info(this, "checkConnectionToShare no directory " + np.produceName());
				}
			}
		}	
		
		setShareMountedInfo(found);
		
		if (!smbMounted && found)
			initialMount();
		
		smbMounted = found;;
		
		return smbMounted;
	}
	
	//for overriding
	protected void initialMount()
	{
	}
	
	protected void checkConnectionToShare(final int seconds)
	{
		new Thread(){
				public void run()
				{
					int i = 0;
					
					//Icon saveIcon = buttonMountShare.getIcon();
					while (!smbMounted && i < seconds)
					{
						//buttonMountShare.setPressedIcon(Globals.waitingButtonIcon);
						
						try{
							logging.debug(this, "trying to find dir, count " + i);
							sleep(1000);
							i++;
							checkConnectionToShare();
							rootFrame.toFront();
						}
						catch(Exception ex)
						{
							logging.debug(this, "Exception " + ex);
						}
					}
					
					
					if (smbMounted)
					{	
						checkAllConnections();
					}
				}
			}
		.start();
	}
	
	
	/*
	class JButtonFlashing extends JButton
	{
		JButtonFlashing(Icon icon)
		{
			super("", icon);
		}
		
		final JButton theButton = this;
		
		private boolean flashing = false;
	
		private int maxDeciSeconds = 0;
		
		public void setFlash(boolean b)
		{
			setFlash(b, 0);
		}
		
		public void setFlash(boolean b, int maxDeciSeconds)
		{
			this.maxDeciSeconds = maxDeciSeconds;
			
			if (flashing == b)
				return;
			
			else
				flashing = b;
			
			
			//we changed the value and act correspondently
			if (flashing)
			{
				new Thread(){
					public void run()
					{
						int i = 0;
						
						while (flashing)
						{
							
							try{
								logging.info(this, "flashThread, count " + i);
								sleep(100);
								i++;
								if (i % 10 == 0)
									theButton.setBackground(Color.white);
								else if (i % 5 == 0)
									theButton.setBackground(Color.blue);
								
							}
							catch(Exception ex)
							{
							}
						}
					}
				}.start();
				
			}
			
		}
	}
	*/
		
}
	

