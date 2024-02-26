/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.awt.Font;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Globals;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {

	private Map<String, Map<String, String>> groups;

	public ProductTreeRenderer(Map<String, Map<String, String>> groups) {
		this.groups = groups;

		super.setPreferredSize(Globals.LABEL_SIZE_OF_JTREE);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (((DefaultMutableTreeNode) value).getAllowsChildren()) {
			setFont(getFont().deriveFont(Font.PLAIN));
		} else {
			setFont(getFont().deriveFont(Font.BOLD));
		}

		if (value instanceof GroupNode && groups.containsKey(value.toString())) {
			setToolTipText(groups.get(value.toString()).get("description"));
		} else {
			setToolTipText(null);
		}

		return this;
	}
}
