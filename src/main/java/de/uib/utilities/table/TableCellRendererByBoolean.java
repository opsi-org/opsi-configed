/* 
 *
 * Entwickelt von uib, www.uib.de, 2011
 * @Author martina hammel
 *
 */
package de.uib.utilities.table;
import java.awt.Color;
import java.awt.Component;
//import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*; 
//import de.uib.utilities.swing.CellAlternatingColorizer;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
 
public class TableCellRendererByBoolean extends ColorTableCellRenderer {

	/**
	 * 
	 */
	JLabel booleanString = new JLabel();

	protected boolean fontVariation; 
	
    public TableCellRendererByBoolean () {
        booleanString.setText("");
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
				
			
				if (value != null) {
					if (!value.toString().equals("")) {
						if (value instanceof Boolean)
						{
							if ( (Boolean) value )
								selectedString = "ja";
							else
								selectedString = "nein";
						}
						else
							selectedString = value.toString();
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
