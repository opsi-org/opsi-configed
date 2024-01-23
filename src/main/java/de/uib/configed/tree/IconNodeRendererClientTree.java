/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JTree;

import de.uib.configed.ConfigedMain;
import utils.Utils;

public class IconNodeRendererClientTree extends IconNodeRenderer {
	private ConfigedMain configedMain;

	private ImageIcon iconClient = Utils.createImageIcon("images/client_small.png", "client");
	private ImageIcon nonSelectedIconClient = Utils.createImageIcon("images/client_small_unselected.png", "client");

	private ImageIcon groupUnselectedIcon = Utils.createImageIcon("images/group_small_unselected.png",
			"group unselected");
	private ImageIcon groupSelected = Utils.createImageIcon("images/group_small.png", "client");
	private ImageIcon group1SelectedIcon = Utils.createImageIcon("images/group_small_1selected.png", "group 1selected");

	public IconNodeRendererClientTree(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof IconNode) {
			String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

			setText(stringValue);
			setToolTipText(((IconNode) value).getToolTipText());

			// Attention: must be a IconNode
			IconNode node = (IconNode) value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);

			node.setEnabled(enabled);

			if (!node.getAllowsChildren()) {
				// client
				if (sel) {
					setIcon(iconClient);
				} else {
					setIcon(nonSelectedIconClient);
				}
			} else {
				// group
				if (sel) {
					setIcon(groupSelected);
				} else if (configedMain.getActiveParents().contains(stringValue)) {
					setIcon(group1SelectedIcon);
				} else {
					setIcon(groupUnselectedIcon);
				}
			}

			setComponentOrientation(tree.getComponentOrientation());
		}

		if (hasFocus) {
			setFont(getFont()
					.deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
		} else {
			setFont(getFont().deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, -1)));
		}

		return this;
	}
}
