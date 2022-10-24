package de.uib.utilities.swing;

import java.util.*;
import de.uib.configed.*;
import javax.swing.*;
import java.awt.*;


public class XCellEditor extends DefaultCellEditor
{
	
	public XCellEditor(JTextField textfield)
	{	
		super(textfield);
	}
	
	public XCellEditor(JComboBox combo)
	{	
		super(combo);
	}
	
	
	public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column)
    {
    	
    		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    		
		Color background;
		Color foreground;
		
		if (isSelected) 
		{
			background = Globals.nimbusSelectionBackground;
			foreground = Color.WHITE;
		}
		else 
		{
			background = Globals.nimbusBackground;
			foreground = Color.black;
		};
		
			c.setBackground(background);
			c.setForeground(foreground);
			
		
		
		//System.out.println("XCellEditor active");	
		return c;
	}
	
}
