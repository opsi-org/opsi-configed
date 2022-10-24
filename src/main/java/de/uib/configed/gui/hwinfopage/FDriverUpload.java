package de.uib.configed.gui.hwinfopage;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import de.uib.configed.gui.*;
import de.uib.configed.*;
import de.uib.configed.tree.*;
import de.uib.opsidatamodel.*;
//import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;

public class FDriverUpload extends SecondaryFrame
{
	
	PanelDriverUpload panelDriverUpload;
	
		
	PersistenceController persist;
	ConfigedMain main; 
	MainFrame mainframe;
	
	/*
	public FDriverUpload()
	{
		define();
		setGlobals(Globals.getMap());
		setTitle(Globals.APPNAME + " " + configed.getResourceValue("FDriverUpload.title"));
		
	}
	*/
	
	public FDriverUpload(ConfigedMain main, PersistenceController persist, MainFrame mainframe)
	{
		super();
		
		this.main =main; 
		this.mainframe = mainframe;
		this.persist = persist;
		
		define();
		setGlobals(Globals.getMap());
		setTitle(Globals.APPNAME + " " + configed.getResourceValue("FDriverUpload.title"));
		
	}
	
	@Override
	public void start()
	{
		super.start();
	}

	
	protected void define()
	{
		panelDriverUpload  = new PanelDriverUpload(main, persist, this);
		
		//main, persist, this);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
			.addComponent(panelDriverUpload, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize) 
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(panelDriverUpload, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		);
		
		Containership cs_all = new Containership (getContentPane());
		cs_all.doForAllContainedCompisOfClass 
		 ("setBackground", new Object[]{Globals.backLightBlue}, JPanel.class);
		 
		 cs_all.doForAllContainedCompisOfClass 
		 ("setBackground", new Object[]{Globals.backgroundLightGrey}, javax.swing.text.JTextComponent.class);
		
	}
	
	public void setUploadParameters(String byAuditPath)
	{
		panelDriverUpload.setByAuditPath(byAuditPath);
		
		logging.info(this, " setUploadParameters "  + main.getSelectedClients()[0]);
		
		if (main.getSelectedClients() != null && main.getSelectedClients().length == 1)
			panelDriverUpload.setClientName(main.getSelectedClients()[0]);
		else 
			panelDriverUpload.setClientName("");
		
		panelDriverUpload.setDepot(main.getConfigserver());
	}
		
}
