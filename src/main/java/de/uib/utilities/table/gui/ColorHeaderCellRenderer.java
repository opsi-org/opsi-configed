package de.uib.utilities.table.gui;

import java.awt.Component;
import javax.swing.*;
import javax.swing.table.*;
import de.uib.configed.Globals;

public class ColorHeaderCellRenderer extends DefaultTableCellRenderer
{
	private TableCellRenderer rend;
	
	public ColorHeaderCellRenderer(TableCellRenderer rend) {
		this.rend = rend;
	}
	
	
	//to override in subclasses for manipulation the value
	protected Object modifyValue(Object value)
	{
		return value;
	}
	
	
	
	@Override
	public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected,
		boolean hasFocus, int row, int column) 
	{
		Component cell = rend.getTableCellRendererComponent
					(table, modifyValue(value), isSelected, hasFocus, row, column);
		cell.setBackground(Globals.defaultTableHeaderBgColor);
		
			
		if (cell instanceof JComponent) {
				JComponent jc = (JComponent)cell;
				{
					if (value != null)
					{
						String val1 = "" + modifyValue(value);
						jc.setToolTipText( val1 );
					}
				}
			}
		
		return cell;
	}
}
	
	
