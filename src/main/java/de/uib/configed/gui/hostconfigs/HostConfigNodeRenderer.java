/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Configed;

public class HostConfigNodeRenderer extends DefaultTreeCellRenderer {
	private static final int LABEL_WIDTH = 300;
	private static final int LABEL_HEIGHT = 22;

	private Map<String, String> tooltips;

	public HostConfigNodeRenderer() {
		super.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
	}

	public void setTooltips(Map<String, String> tooltips) {
		this.tooltips = tooltips;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof DefaultMutableTreeNode) {
			String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

			if ("".equals(stringValue)) {
				setText(Configed.getResourceValue("HostConfigNodeRenderer.mainNode"));
			} else {
				setText(stringValue);
			}

			if (tooltips != null && tooltips.get(stringValue) != null && !tooltips.get(stringValue).isEmpty()) {
				setToolTipText(Configed.getResourceValue(tooltips.get(stringValue)));
			} else {
				setToolTipText(null);
			}

			if (row == 0) {
				setFont(getFont().deriveFont(Font.BOLD));
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
			}
		}

		return this;
	}
}
