package de.uib.configed.gui;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2010 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import de.uib.configed.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.datapanel.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.opsidatamodel.datachanges.*;

public class PanelHostProperties extends JPanel
	implements ItemListener
{
	//delegate
	protected AbstractEditMapPanel editMapPanel;
	protected JLabel label;
	protected JComboBox combo;
	protected Map<String, Map<String, Object>> multipleMaps;
	protected UpdateCollection updateCollection;
	
	public PanelHostProperties()
	{
		buildPanel();
	}

	protected void buildPanel()
	{
		label = new JLabel (configed.getResourceValue("MainFrame.jLabel_Config") );
		combo = new JComboBox();
		combo.setVisible(false);
		combo.addItemListener(this);
		de.uib.configed.gui.helper.PropertiesTableCellRenderer cellRenderer
			= new de.uib.configed.gui.helper.PropertiesTableCellRenderer();
		logging.info(this, "buildPanel, produce editMapPanel");
		editMapPanel = new EditMapPanelX(cellRenderer, false, false); //true, true); 
		((EditMapPanelX) editMapPanel).setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName().toString()));
		editMapPanel.setShowToolTip(false);
		
		JPanel header =new JPanel();
		
		GroupLayout headerLayout = new GroupLayout(header);
		header.setLayout(headerLayout);
		
		headerLayout.setHorizontalGroup(
			headerLayout.createSequentialGroup()
				.addGap(10)
				.addComponent(label, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(10)
				.addComponent(combo, 200, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(10)
			)
		;
	
		headerLayout.setVerticalGroup(
			headerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(label)
				.addComponent(combo)
			)
		;
		
		GroupLayout planeLayout = new GroupLayout( this );
		this.setLayout( planeLayout );
		
		planeLayout.setHorizontalGroup(
			planeLayout.createSequentialGroup()
			.addGap(20)
			.addGroup(planeLayout.createParallelGroup()
				.addComponent( header, GroupLayout.Alignment.CENTER )
				.addComponent( editMapPanel )
				//.addComponent( editMapPanelStrings )
			)
			.addGap(20)
		);
		
		planeLayout.setVerticalGroup(
			planeLayout.createSequentialGroup()
			.addGap(20)
			.addComponent( header,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5)
			.addComponent( editMapPanel, de.uib.configed.Globals.lineHeight * 2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE )
			//.addComponent( editMapPanelStrings, de.uib.configed.Globals.lineHeight * 2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE )
			.addGap(20)
		);
	}
	
	
	public void initMultipleHostsEditing(
		String labeltext,
		ComboBoxModel comboModel,
		Map<String, Map<String, Object>> multipleMaps,
		UpdateCollection updateCollection,
		Set<String> keysOfReadOnlyEntries
		)
	{
		label.setText(labeltext);
		activateCombo(comboModel);
		
		//editMapPanel.setEditableMap(null, null);
		
		logging.debug(this, "initMultipleHosts " 
			+ " configs  " +  (multipleMaps)
			//+ " configOptions  " + (configOptions)
			);
		
		this.updateCollection = updateCollection;
		
		this.multipleMaps = multipleMaps;
		editMapPanel.setUpdateCollection(updateCollection);
		editMapPanel.setReadOnlyEntries(keysOfReadOnlyEntries);
		
		if (comboModel != null && comboModel.getSize() > 0)
		{
			setMap((String) comboModel.getElementAt(0));
		}
	}

	

	//delegated methods
	public void registerDataChangedObserver( DataChangedObserver o )
	{
		editMapPanel.registerDataChangedObserver(o);
	}
	
	
	public void activateCombo(ComboBoxModel model)
	{
		if (model != null) combo.setModel(model);
		combo.setEnabled( (model != null) );
		combo.setVisible( (model != null) );
	}
	
	protected Map<String, ListCellOptions> deriveOptionsMap(Map m)
	{
		Map<String, ListCellOptions> result = new HashMap<String, ListCellOptions>();
		
		for (Object key : m.keySet())
		{
			
			ListCellOptions cellOptions = null;
			
			if ( (m.get(key)) instanceof java.lang.Boolean )
				cellOptions = DefaultListCellOptions.getNewBooleanListCellOptions();
			
			else
				cellOptions = DefaultListCellOptions.getNewEmptyListCellOptions();
				
			logging.debug(this, "cellOptions: " + cellOptions);
			
			result.put((String) key, cellOptions);
		}
		return result;
		
	}
	
	
	protected void setMap(String selectedItem)
	{
		ArrayList editedMaps = new ArrayList(1);
		editedMaps.add(multipleMaps.get(selectedItem));
		logging.debug(this, "setMap " + multipleMaps.get(selectedItem)); 
		editMapPanel.setEditableMap(
			multipleMaps.get(selectedItem), 
			deriveOptionsMap(multipleMaps.get(selectedItem))
		);
		editMapPanel.setStoreData(editedMaps);
	}
	
	
	//item listener
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			setMap((String) combo.getSelectedItem());
		}
	}
		
}
