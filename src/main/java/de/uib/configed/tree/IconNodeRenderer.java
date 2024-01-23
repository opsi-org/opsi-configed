/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class IconNodeRenderer extends DefaultTreeCellRenderer {
	// Make width long enough so that it will be not too small for the whole text
	private static final int LABEL_WIDTH = 500;
	private static final int LABEL_HEIGHT = 20;

	public IconNodeRenderer() {
		super();

		super.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof IconNode) {
			String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

			setText(stringValue);

			// adaption to size of bold font??

			// Attention: must be a IconNode
			IconNode node = (IconNode) value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);
			node.setEnabled(enabled);

			if (leaf) {
				setIcon(node.getLeafIcon());
			} else if (expanded) {
				setIcon(node.getOpenIcon());
			} else {
				setIcon(node.getClosedIcon());
			}
		}

		return this;
	}
}
