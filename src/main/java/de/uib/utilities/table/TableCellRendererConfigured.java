package de.uib.utilities.table; 


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class TableCellRendererConfigured extends DefaultTableCellRenderer
{     
	Font f;
	Color c;
	Color bg;
	Color selectionBackground;
	
	public TableCellRendererConfigured(Font f, Color c, Color bg, Color selectionBackground)
	{
		super();
		this.f = f;
		this.c = c;
		this.bg = bg;
		this.selectionBackground = selectionBackground;
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
		if (bg != null)
		{
			if (selectionBackground != null && isSelected)
				result.setBackground(selectionBackground);
			
			else
				result.setBackground(bg);
		}

		if (c != null) 
			result.setForeground(c);
		
		return result;
	}
		
}
