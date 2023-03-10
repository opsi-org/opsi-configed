/* 
 *
 * Entwickelt von uib, www.uib.de, 2012
 * @Author rupert roeder
 *
 */
package de.uib.utilities.table;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.ColorTableCellRenderer;

public class TableCellRendererCurrency extends ColorTableCellRenderer {
	JLabel label = new JLabel();
	private java.text.NumberFormat decimalFormat;

	public TableCellRendererCurrency() {
		label.setText("");
		decimalFormat = NumberFormat.getCurrencyInstance();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, // value to display
			boolean isSelected, // is the cell selected
			boolean hasFocus, int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String selectedString = "";
		ImageIcon selectedIcon = null;

		if (value instanceof String && !((String) value).equals("")) {

			try {
				double number = Double.parseDouble((String) value);
				selectedString = decimalFormat.format(number);
			} catch (Exception ex) {
				Logging.warning(this, " format exception", ex);
			}

		} else {
			selectedString = "";
		}

		if (result instanceof JLabel) {
			((JLabel) result).setText(selectedString);
			((JLabel) result).setIcon(selectedIcon);
			((JLabel) result).setToolTipText(selectedString);

		}

		return result;
	}

}
