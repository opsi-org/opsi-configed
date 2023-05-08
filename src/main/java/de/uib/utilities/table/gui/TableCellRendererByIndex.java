package de.uib.utilities.table.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.configed.Globals;

public class TableCellRendererByIndex extends StandardTableCellRenderer {
	Map<String, String> mapOfStrings;
	Map<String, ImageIcon> mapOfImages;
	private boolean showOnlyIcon = true;

	public TableCellRendererByIndex(Map<String, String> mapOfStringValues, String imagesBase) {
		this(mapOfStringValues, imagesBase, false, "");
	}

	public TableCellRendererByIndex(Map<String, String> mapOfStringValues, String imagesBase, boolean showOnlyIcon,
			String tooltipPrefix) {
		super(tooltipPrefix);
		this.showOnlyIcon = showOnlyIcon;
		mapOfStrings = mapOfStringValues;
		mapOfImages = new HashMap<>();
		// Load the item values

		Iterator<Entry<String, String>> iter = mapOfStrings.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String stringval = entry.getValue();

			ImageIcon image = null;

			if (imagesBase != null && key != null && stringval != null) {
				String imageFileString = imagesBase + "/" + key + ".png";

				image = Globals.createImageIcon(imageFileString, stringval);

				if (image == null) {
					// try with gif

					imageFileString = imagesBase + "/" + stringval + ".gif";

					image = Globals.createImageIcon(imageFileString, stringval);

				}

				if (image != null) {
					mapOfImages.put(key, image);
				}

			}
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String selectedString = "";
		ImageIcon selectedIcon = null;

		if (value != null) {

			if (mapOfStrings != null) {
				selectedString = mapOfStrings.get("" + value);
			}

			if (mapOfImages != null) {
				selectedIcon = mapOfImages.get("" + value);
			}

		}

		if (result instanceof JLabel) {

			if (showOnlyIcon) {
				((JLabel) result).setText(null);
				((JLabel) result).setHorizontalAlignment(SwingConstants.CENTER);
			} else {
				((JLabel) result).setText(selectedString);
			}

			((JLabel) result).setIcon(selectedIcon);
			((JLabel) result).setToolTipText(
					Globals.fillStringToLength(tooltipPrefix + " " + selectedString + " ", FILL_LENGTH));

		}

		return result;
	}
}
