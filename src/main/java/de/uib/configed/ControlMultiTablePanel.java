/* 
 * Copyright (C) 2009 uib.de
 *
 */
 
package de.uib.configed;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.configed.Globals;

public abstract class ControlMultiTablePanel
{
	protected Vector<GenTableModel> tableModels 
	= new Vector<GenTableModel>();
	
	protected Vector<PanelGenEditTable> tablePanes 
	= new Vector<PanelGenEditTable>();

	protected de.uib.utilities.table.updates.TableUpdateCollection updateCollection
	= new de.uib.utilities.table.updates.TableUpdateCollection();

	public abstract TabClientAdapter getTabClient();
	
	public Vector<GenTableModel> getTableModels()
	{
		return tableModels;
	}
	
	public Vector<PanelGenEditTable> getTablePanes()
	{
		return tablePanes;
	}
	
	public abstract void init();
	
	/** called by the MultiTablePanel reset method
	* overwrite for the real content
	*/
	public void initializeVisualSettings()
	{
	}
	
	public void refreshTables()
	{
		Iterator iterM = tableModels.iterator();
		
		while (iterM.hasNext())
		{
			GenTableModel m = (GenTableModel) iterM.next();
			
			m.invalidate();
			m.reset();
		}
		
		Iterator iterP = tablePanes.iterator();
		
		while (iterP.hasNext())
		{
			PanelGenEditTable p = (PanelGenEditTable) iterP.next();
			
			p.setDataChanged(false);
		}
	}
	
	public boolean mayLeave()
	{
		//System.out.println(" ------------- check if may leave in " + this);
		boolean change = false;
		boolean result = false;
		
		Iterator iterP = tablePanes.iterator();
		
		
		while (!change && iterP.hasNext())
		{
			PanelGenEditTable p = (PanelGenEditTable) iterP.next();
			change = p.isDataChanged();
		}
		
		//System.out.println(" change " + true);
		
		if (change)
		{
			int returnedOption = JOptionPane.NO_OPTION;
			
			returnedOption = JOptionPane.showOptionDialog(	Globals.frame1, 
								configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.text"),
								configed.getResourceValue("ControlMultiTablePanel.NotSavedChanges.title"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,
								null, null, null);
								
			if (returnedOption == JOptionPane.YES_OPTION)
				result = true;
			
			Globals.frame1.setVisible(true);
		}
		else
			result = true;
		
		
		return result;
	}
	
}
	
	
	
	
	
