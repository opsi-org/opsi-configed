/**
	ColoredTableCellRendererByIndex.java
	
	utility class for PanelProductSettings
	
*/

package de.uib.configed.guidata;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.JTable;

import de.uib.configed.Globals;

public class ColoredTableCellRendererByIndex extends de.uib.utilities.table.gui.TableCellRendererByIndex {

	Map<String, Color> mapOfTextColors;

	public ColoredTableCellRendererByIndex(Map<String, String> mapOfStringValues, String imagesBase,
			boolean showOnlyIcon, String tooltipPrefix) {
		this(null, mapOfStringValues, imagesBase, showOnlyIcon, tooltipPrefix);
	}

	public ColoredTableCellRendererByIndex(Map<String, Color> mapOfTextColors, Map<String, String> mapOfStringValues,
			String imagesBase, boolean showOnlyIcon, String tooltipPrefix) {
		super(mapOfStringValues, imagesBase, showOnlyIcon, tooltipPrefix);
		this.mapOfTextColors = mapOfTextColors;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, // value to display
			boolean isSelected, // is the cell selected
			boolean hasFocus, int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (value == null)
			return c;

		if (value.equals(InstallationStateTableModel.CONFLICT_STRING)) {
			c.setBackground(Globals.BACKGROUND_COLOR_4);
			c.setForeground(Globals.BACKGROUND_COLOR_4);
		} else {
			if (mapOfTextColors != null && value instanceof String) {
				Color textcolor = mapOfTextColors.get(value);
				if (textcolor != null) {
					if (textcolor.equals(Globals.INVISIBLE))
						c.setForeground(c.getBackground());
					else
						c.setForeground(textcolor);
				}
			}
		}

		return c;
	}
}
