package de.uib.configed.gui.helper;

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
import javax.swing.*;
import de.uib.configed.gui.*;
import de.uib.configed.guidata.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.logging.*;


import de.uib.opsidatamodel.*;


public class PropertiesTableCellRenderer extends ColorTableCellRenderer
{
	public Component getTableCellRendererComponent(
	JTable table,
	Object value,            // value to display
	boolean isSelected,      // is the cell selected
	boolean hasFocus,
	int row,
	int column)
	{
		//logging.debug(this, "getTableCellRendererComponent");
		
		Object sValue = formatList(value);
		Component result = super.getTableCellRendererComponent(table, sValue, isSelected, hasFocus, row, column);
		
		if (column == 1)
			merge_colorize (result, value);
		
		
		return result;
	}	
	
	private Object formatList(Object value)
	{
		//logging.debug(this, "formatList " + value);
		Object result = value;
		if (value != null)
		{
			String s =  value.toString();
			if (s.length() >= 2 && s.charAt(0) == '[' && s. charAt(s.length()-1) == ']') 
			{
				result = s.substring(1, s.length()-1);
			}
		}
		//logging.debug(this, "formatList produced " + result);
		
		return result;
	}
		
	
	protected void merge_colorize (Component comp, Object value)
	{
		//logging.debug(this, "merge_colorize  value " +value);
		//logging.debug(this, "class of value " + value.getClass().getName());
		
		if //( value instanceof ListMerger && !((ListMerger)value).hasCommonValue() )
			(value == ListMerger.NO_COMMON_VALUE)
		{
			//logging.debug(this," ++++++ take colors for NO_COMMON_VALUE"); 
			comp.setBackground(ListMerger.noCommonValueBackcolor) ;
			comp.setForeground(ListMerger.noCommonValueTextcolor) ;
		}

	}

}    


