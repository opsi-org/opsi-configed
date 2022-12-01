package de.uib.utilities.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/*
 * 
 * Author: Rupert Röder, uib 2011
 *
 */

import de.uib.configed.Globals;

public class ColoredListCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		CellAlternatingColorizer.colorize(c, isSelected, (index % 2 == 0), true);

		/*
		 * Color background;
		 * Color foreground;
		 * 
		 * if (isSelected)
		 * {
		 * background = Globals.nimbusSelectionBackground;
		 * foreground = Color.WHITE;
		 * }
		 * else
		 * {
		 * background = Globals.nimbusBackground;
		 * foreground = de.uib.configed.Globals.nimbusSelectionBackground;
		 * //Color.black;
		 * };
		 * 
		 * c.setBackground(background);
		 * c.setForeground(foreground);
		 */
		c.setFont(Globals.defaultFont);
		// logging.debug("component active");

		return c;
	}
}
