/**
	ColoredTableCellRendererByIndex.java
	
	utility class for PanelProductSettings
	
*/
	
	
package de.uib.configed.guidata;

import java.awt.*; 
import java.util.*;
import javax.swing.*;


import de.uib.configed.Globals;

public class ColoredTableCellRendererByIndex extends de.uib.utilities.table.gui.TableCellRendererByIndex
{
	
	Map<String, Color> mapOfTextColors; 
	
	public ColoredTableCellRendererByIndex(Map<String,String> mapOfStringValues, String imagesBase, boolean showOnlyIcon, String tooltipPrefix)
	{
		this(null, mapOfStringValues, imagesBase, showOnlyIcon, tooltipPrefix);
	}
	
	public ColoredTableCellRendererByIndex(Map<String, Color> mapOfTextColors, Map<String,String> mapOfStringValues, String imagesBase, boolean showOnlyIcon, String tooltipPrefix)
	{
		super(mapOfStringValues, imagesBase, showOnlyIcon, tooltipPrefix);
		this.mapOfTextColors = mapOfTextColors;
	}
		
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,            // value to display
		boolean isSelected,      // is the cell selected
		boolean hasFocus,
		int row,
		int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if (value == null)
			return c;
		
		if (value.equals(InstallationStateTableModel.CONFLICTstring))
		{
			c.setBackground(Globals.backgroundGrey); //result.setForeground (lightBlack);
			c.setForeground(Globals.backgroundGrey);
		}
		else 
		{
			if (mapOfTextColors != null && value instanceof String)
			{
				Color textcolor = mapOfTextColors.get((String) value);
				if (textcolor != null)
				{
					if (textcolor.equals(Globals.INVISIBLE))
						c.setForeground(c.getBackground());
					else
						c.setForeground(textcolor);
				}
			}
		}
		
		return c;
	}
}
