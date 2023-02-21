/* 
 *
 * Entwickelt von uib, www.uib.de, 2011
 * @Author martina hammel
 *
 */
package de.uib.utilities.table;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.ColorTableCellRenderer;

public class TableCellRendererDate extends ColorTableCellRenderer {

	JLabel label = new JLabel();
	private java.text.DateFormat dateFormat;

	public TableCellRendererDate() {
		dateFormat = java.text.DateFormat.getDateInstance(Globals.DATE_FORMAT_STYLE_PATTERN);

		label.setText("");
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String selectedString = "";
		ImageIcon selectedIcon = null;

		if (value instanceof String && !((String) value).equals("")) {

			try {
				java.util.Date d = java.sql.Timestamp.valueOf((String) value);
				selectedString = dateFormat.format(d);
			} catch (Exception ex) {
				Logging.debug(this, " time format exception: " + ex);
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
