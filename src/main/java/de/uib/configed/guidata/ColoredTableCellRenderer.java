/**
	ColoredTableCellRenderer.java
	
	utility class for PanelProductSettings
	
*/
	
	

package de.uib.configed.guidata;

import de.uib.configed.Globals;
import java.awt.*;
import javax.swing.*;

public class ColoredTableCellRenderer extends de.uib.utilities.table.gui.StandardTableCellRenderer
{
	
	public ColoredTableCellRenderer(String tooltipPrefix)
	{
		super(tooltipPrefix);
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
		
		
		if ( value != null && (value.equals(InstallationStateTableModel.CONFLICTstring)) )
		{
			c.setBackground(Globals.backgroundGrey); //result.setForeground (lightBlack);
			c.setForeground(Globals.backgroundGrey);
		}
		
		//de.uib.utilities.swing.CellAlternatingColorizer.colorize(c, isSelected, (row % 2 == 0), true);
		
		return c;
		}
}
