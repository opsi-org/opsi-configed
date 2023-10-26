/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Configed;

public class HostConfigNodeRenderer extends DefaultTreeCellRenderer {
	private static final int LABEL_WIDTH = 300;
	private static final int LABEL_HEIGHT = 22;

	public HostConfigNodeRenderer() {
		super();

		super.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof SimpleIconNode) {

			String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

			if ("".equals(stringValue)) {
				setText(Configed.getResourceValue("HostConfigNodeRenderer.mainNode"));
			} else {
				setText(stringValue);
			}

			setToolTipText(((SimpleIconNode) value).getToolTipText());

			// adaption to size of bold font??

			// Attention: must be a SimpleIconNode
			SimpleIconNode node = (SimpleIconNode) value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);
			node.setEnabled(enabled);

			if (row == 0) {
				setFont(getFont().deriveFont(Font.BOLD));
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
			}

			if (leaf) {
				setIcon(node.getLeafIcon());
			} else {
				if (expanded) {
					setIcon(node.getOpenIcon());
				} else {
					setIcon(node.getClosedIcon());
				}
			}

			if (!sel) {
				setIcon(node.getNonSelectedIcon());
			}

			setComponentOrientation(tree.getComponentOrientation());
		}

		return this;
	}
}
