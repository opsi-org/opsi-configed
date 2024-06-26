/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.configed.Configed;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class ConnectionStatusTableCellRenderer extends ColorTableCellRenderer {
	// These strings should be in an alphabetic order since the table will be sorted by these strings
	public static final String CONNECTED_BY_MESSAGEBUS = "connected_by_messagebus";
	public static final String REACHABLE = "reachable";
	public static final String NOT_REACHABLE = "unreachable";
	public static final String UNKNOWN = "unverified";

	private static final Icon messagebusIcon = Utils.createImageIcon("bootstrap/check_green.png", "");
	private static final Icon trueIcon = Utils.createImageIcon("images/new_network-connect2.png", "");
	private static final Icon falseIcon = Utils.createImageIcon("images/new_network-disconnect.png", "");

	public ConnectionStatusTableCellRenderer() {
		super();

		super.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		setText(null);

		if (value instanceof String string) {
			switch (string) {
			case CONNECTED_BY_MESSAGEBUS:
				setIcon(messagebusIcon);
				setToolTipText(Configed.getResourceValue("ConnectionStatusTableCellRenderer.connected.tooltip"));
				break;

			case REACHABLE:
				setIcon(trueIcon);
				setToolTipText(Configed.getResourceValue("ConnectionStatusTableCellRenderer.reachable.tooltip"));
				break;

			case NOT_REACHABLE:
				setIcon(falseIcon);
				setToolTipText(Configed.getResourceValue("ConnectionStatusTableCellRenderer.notReachable.tooltip"));
				break;

			case UNKNOWN:
				setIcon(null);
				setToolTipText(Configed.getResourceValue("ConnectionStatusTableCellRenderer.notConnected.tooltip"));
				break;

			default:
				Logging.warning(this, "unexpected value: " + value + "; set Icon null");
				setIcon(null);
				setToolTipText(null);
				break;
			}
		} else {
			Logging.warning(this, "it's unexpected that value is not a string, but: " + value.getClass());
			setIcon(null);
			setToolTipText(null);
		}

		return this;
	}
}
