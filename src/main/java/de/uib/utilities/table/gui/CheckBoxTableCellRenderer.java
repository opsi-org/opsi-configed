package de.uib.utilities.table.gui;

import javax.swing.*;
import java.awt.*;
import javax.swing.JTable;
import javax.swing.table.*;
import de.uib.configed.Globals;

public class CheckBoxTableCellRenderer
	extends JCheckBox
	implements TableCellRenderer 

{
	javax.swing.border.Border noFocusBorder;
	javax.swing.border.Border focusBorder;
	
	public CheckBoxTableCellRenderer() {
		super();
		setOpaque(true);
		setBorderPainted(true);
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}
	
	public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
												 boolean isSelected,
												 boolean hasFocus,
												 int row, int column) 
	{
	
		setForeground( Globals.lightBlack );
		
		if (isSelected) {
			setBackground( Globals.defaultTableCellSelectedBgColorNotEditable );
		}
		else if (row % 2 == 0)
		{
			setBackground( Globals.defaultTableCellBgColor2 );
		}
		else
		{
			setBackground( Globals.defaultTableCellBgColor1 );
		}
		
		/*
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		}
		else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		*/
	
		if (hasFocus) 
		{
			if (focusBorder == null) 
			{
				focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
			}
			setBorder(focusBorder);
		}
		else 
		{
			if (noFocusBorder == null) 
			{
				noFocusBorder = new javax.swing.border.EmptyBorder(1, 1, 1, 1);
			}
			setBorder(noFocusBorder);
		}
	
		setSelected(Boolean.TRUE.equals(value));
		return this;
	}
	
} 
