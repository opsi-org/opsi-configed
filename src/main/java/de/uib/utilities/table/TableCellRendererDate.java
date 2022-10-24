/* 
 *
 * Entwickelt von uib, www.uib.de, 2011
 * @Author martina hammel
 *
 */
package de.uib.utilities.table;
import java.awt.Component;
import javax.swing.*; 
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
 
public class TableCellRendererDate extends ColorTableCellRenderer 
{

	JLabel label = new JLabel();
	private java.text.DateFormat dateFormat;

    public TableCellRendererDate () 
    {
    	dateFormat = java.text.DateFormat.getDateInstance(Globals.dateFormatStylePattern);//DateFormat.LONG);
		
        label.setText("");
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
				
				String selectedString = "";
				ImageIcon selectedIcon = null;
				
				if (value != null && value instanceof String && !((String)value).equals(""))
				{
					
					try
					{
						java.util.Date d = java.sql.Timestamp.valueOf((String) value);
						selectedString = dateFormat.format(d);
					}
					catch (Exception ex)
					{
						logging.debug(this, " time format exception: " + ex);
					}			
		
				} else {
					selectedString = "";
				}
				
				if (result instanceof JLabel) {
					((JLabel)result).setText(selectedString);
					((JLabel)result).setIcon(selectedIcon);
					((JLabel)result).setToolTipText(selectedString);
					//((JLabel)result).setHorizontalAlignment(CENTER);
				}
				
				//CellAlternatingColorizer.colorize(result, isSelected, (row % 2 == 0) , true);
				
				return result;
			}

}
