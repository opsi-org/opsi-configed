package de.uib.utilities.table.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.configed.Globals;

public class TableCellRendererByIndex extends StandardTableCellRenderer {
	Map<String, String> mapOfStrings;
	Map<String, ImageIcon> mapOfImages;
	boolean showOnlyIcon = true;

	public TableCellRendererByIndex(Map<String, String> mapOfStringValues, String imagesBase) {
		this(mapOfStringValues, imagesBase, false, "");
	}

	public TableCellRendererByIndex(Map<String, String> mapOfStringValues, String imagesBase, boolean showOnlyIcon,
			String tooltipPrefix) {
		super(tooltipPrefix);
		this.showOnlyIcon = showOnlyIcon;
		mapOfStrings = mapOfStringValues;
		mapOfImages = new HashMap<String, ImageIcon>();
		// Load the item values
		{
			Iterator iter = mapOfStrings.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				String stringval = (String) entry.getValue();

				ImageIcon image = null;

				if (imagesBase != null) {
					if (key != null && stringval != null) {
						String imageFileString = imagesBase + "/" + key + ".png";
						// logging.info(this, "key " + key + ", image file " + imageFileString);

						image = de.uib.configed.Globals.createImageIcon(imageFileString, stringval);
						// logging.info(this, "image found " + (image != null));

						if (image == null)
						// try with gif
						{
							imageFileString = imagesBase + "/" + stringval + ".gif";
							// logging.info(this, " image file " + imageFileString);

							image = de.uib.configed.Globals.createImageIcon(imageFileString, stringval);
							// logging.info(this, "image found " + (image != null));
						}

						if (image != null)
							mapOfImages.put(key, image);
					}
				}
			}
		}

	}

	/*
	 * public void setBackgroundColor(Color c)
	 * {
	 * backgroundColor = c;
	 * }
	 */

	public Component getTableCellRendererComponent(JTable table, Object value, // value to display
			boolean isSelected, // is the cell selected
			boolean hasFocus, int row, int column) {
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		String selectedString = "";
		ImageIcon selectedIcon = null;

		if (value != null) {

			// logging.debug(this, " :1 value is " + value + " value class is " +
			// value.getClass());
			if (mapOfStrings != null)
				selectedString = mapOfStrings.get("" + value);
			// logging.debug(this, " :1 selectedString is " + selectedString);
			// logging.debug(this, " :2 value is " + value + " value class is " +
			// value.getClass());
			if (mapOfImages != null)
				selectedIcon = mapOfImages.get("" + value);

			/*
			 * if (value instanceof String)
			 * {
			 * try
			 * {
			 * selectedIndex = Integer.decode(((String)value).trim());
			 * }
			 * catch (Exception ex)
			 * { logging.debug(this, "TableCellRendererByIndex " + ex); }
			 * }
			 * else
			 * {
			 * try
			 * {
			 * selectedIndex = ((Integer)value).intValue();
			 * }
			 * catch (Exception ex)
			 * {
			 * logging.debug(this, "TableCellRendererByIndex " + ex);
			 * }
			 * }
			 */

		}

		if (result instanceof JLabel) {

			if (showOnlyIcon) {
				((JLabel) result).setText(null);
				((JLabel) result).setHorizontalAlignment(SwingConstants.CENTER);
			} else
				((JLabel) result).setText(selectedString);

			((JLabel) result).setIcon(selectedIcon);
			((JLabel) result).setToolTipText(
					Globals.fillStringToLength(tooltipPrefix + " " + selectedString + " ", FILL_LENGTH));
			// logging.debug("------ tooltip " + ((JLabel)result).getToolTipText());
		}

		// if (backgroundColor != null) result.setBackground (backgroundColor);
		// result.setForeground (lightBlack);

		// CellAlternatingColorizer.colorize(result, isSelected, (row % 2 == 0), true);

		return result;
	}
}
