package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class ConnectionStatusTableCellRenderer extends StandardTableCellRenderer {

	// These strings should be in an alphabetic order since the table will be sorted by these strings
	public static final String CONNECTED_BY_MESSAGEBUS = "connected_by_messagebus";
	public static final String REACHABLE = "reachable";
	public static final String NOT_REACHABLE = "unreachable";
	public static final String UNKNOWN = "unverified";

	private Icon messagebusIcon;
	private Icon trueIcon;
	private Icon falseIcon;

	public ConnectionStatusTableCellRenderer() {
		super();

		trueIcon = Globals.createImageIcon("images/new_network-connect2.png", "");
		falseIcon = Globals.createImageIcon("images/new_network-disconnect.png", "");
		messagebusIcon = Globals.createImageIcon("images/ok22.png", "");
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (!(c instanceof JLabel)) {

			Logging.warning(this, "component is not a JLabel, but it should be, it is" + c.getClass().toString());
			return c;
		}

		JLabel label = (JLabel) c;

		label.setText("");

		if (value == null) {
			label.setIcon(null);
		} else if (value instanceof String) {
			switch ((String) value) {
			case CONNECTED_BY_MESSAGEBUS:
				label.setIcon(messagebusIcon);
				break;

			case REACHABLE:
				label.setIcon(trueIcon);
				break;

			case NOT_REACHABLE:
				label.setIcon(falseIcon);
				break;

			case UNKNOWN:
				label.setIcon(null);
				break;

			default:
				Logging.warning(this, "unexpected value: " + value + "; set Icon null");
				label.setIcon(null);
				break;
			}
		} else {
			Logging.warning(this, "it's unexpected that value is not a string, but: " + value.getClass());
		}

		return c;
	}
}
