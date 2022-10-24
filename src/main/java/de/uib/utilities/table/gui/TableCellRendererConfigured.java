package de.uib.utilities.table.gui; 


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import de.uib.utilities.logging.*;

public class TableCellRendererConfigured extends DefaultTableCellRenderer
{     
	Font f;
	Color color;
	Color bg1;
	Color bg2; //for even line numbers
	Color selectionBackground;
	Color selectionEditingBackground;
	
	
	public TableCellRendererConfigured(Font f, Color c, Color bg1, Color bg2, Color selectionBackground, Color selectionEditingBackground)
	{
		super();
		this.f = f;
		this.color = c;
		this.bg1 = bg1;
		if (bg2 != null)
			this.bg2 = bg2;
		else
			this.bg2 = bg1;
		
		this.selectionBackground = selectionBackground;
		this.selectionEditingBackground = selectionEditingBackground;
	}
	
	
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,            // value to display
		boolean isSelected,      // is the cell selected
		boolean hasFocus,
		int row,
		int column)
	{
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	
		/*
		if (result instanceof JComponent) {
			JComponent jc = (JComponent)result;
			jc.setToolTipText("");
		}
		*/
		//CellColorizer.colorize(result, value.toString());
		
		if (f != null) 
			result.setFont(f);
		
		if (bg1 != null)
		{
			if (selectionEditingBackground != null && isSelected)
				result.setBackground(selectionEditingBackground);
			
			else
			{
				if (row % 2 == 0)
				{
					result.setBackground( bg2 );
				}
				else
					result.setBackground( bg1 );
			}
		}

		if (color != null) 
			result.setForeground(color);
		
		/*
		
		if (result instanceof JLabel)
		{
			if (row == currentRow)
			{
				result.setFont(result.getFont().deriveFont(Font.ITALIC) );
				logging.info(this, "row " + row + " emphasized ");
			}
		}
		
		*/
		
		return result;
	}
		
}
