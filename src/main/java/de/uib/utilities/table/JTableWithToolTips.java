package de.uib.utilities.table;

/* 
*	Copyright uib (uib.de) 2008
*	Author Rupert Röder
*
*/
 
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class JTableWithToolTips extends JTable
{
	
	public JTableWithToolTips()
	{
		super();
	}
	
	public JTableWithToolTips(TableModel tm)
	{
		super(tm);
	}
	
	
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int colIndex) 
	{
		Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
		
		if (c != null && c instanceof JComponent) 
		{
			String valstr = "";
			
			if (c instanceof JLabel)
				valstr = ((JLabel) c).getText();
			else
			{
				Object val = getValueAt(rowIndex, colIndex);
				
				if (val instanceof Integer) 
					valstr = " " + val;
				else if (val instanceof String)
					valstr = (String) val;
			}
				
			JComponent jc = (JComponent)c;
			jc.setToolTipText(valstr);
								
		}
		return c;
	}
	
}
			
