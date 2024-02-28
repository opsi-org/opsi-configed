/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Globals;
import utils.Utils;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {
	private Map<String, Map<String, String>> groups;

	private ImageIcon productIcon = Utils.getThemeIconPNG("bootstrap/product", "client");
	private ImageIcon productSelectedIcon = Utils.getThemeIconPNG("bootstrap/product_selected", "client");

	private ImageIcon groupIcon = Utils.getThemeIconPNG("bootstrap/group", "group unselected");

	// TODO find a way to get all the productgroups that contain a selected product and they should have this icon
	private ImageIcon groupContainsSelectedIcon = Utils.getThemeIconPNG("bootstrap/group_selected", "group selected");

	public ProductTreeRenderer(Map<String, Map<String, String>> groups) {
		this.groups = groups;

		super.setPreferredSize(Globals.LABEL_SIZE_OF_JTREE);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (((DefaultMutableTreeNode) value).getAllowsChildren()) {
			setIcon(groupIcon);
		} else {
			if (sel) {
				setIcon(productSelectedIcon);
			} else {
				setIcon(productIcon);
			}
		}

		if (value instanceof GroupNode && groups.containsKey(value.toString())) {
			setToolTipText(groups.get(value.toString()).get("description"));
		} else {
			setToolTipText(null);
		}

		return this;
	}
}
