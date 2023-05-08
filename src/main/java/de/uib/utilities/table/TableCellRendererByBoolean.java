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

import de.uib.utilities.table.gui.ColorTableCellRenderer;

public class TableCellRendererByBoolean extends ColorTableCellRenderer {

	/**
	 * 
	 */
	private JLabel booleanString = new JLabel();

	protected boolean fontVariation;

	public TableCellRendererByBoolean() {
		booleanString.setText("");
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String selectedString = "";
		ImageIcon selectedIcon = null;

		if (value != null) {
			if (!value.toString().isEmpty()) {
				if (value instanceof Boolean) {
					if (Boolean.TRUE.equals(value)) {
						selectedString = "ja";
					} else {
						selectedString = "nein";
					}
				} else {
					selectedString = value.toString();
				}
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
