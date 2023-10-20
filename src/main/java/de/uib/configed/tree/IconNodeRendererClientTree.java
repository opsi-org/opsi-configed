/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;

import de.uib.configed.ConfigedMain;

public class IconNodeRendererClientTree extends IconNodeRenderer {
	private ConfigedMain configedMain;

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
				if (configedMain.getActiveTreeNodes().containsKey(stringValue)) {
					setIcon(node.getLeafIcon());
				} else {
					setIcon(node.getNonSelectedLeafIcon());
				}
			} else {
				// group

				String visualText = modify(stringValue);

				setText(visualText);

				// default,will be changed, if clients are childs
				setIcon(node.getClosedIcon());

				if (configedMain.getActiveParents().contains(stringValue)) {
					setIcon(node.getEmphasizedIcon());
				}
			}

			if (tree.getLeadSelectionPath() != null && node.equals(tree.getLeadSelectionPath().getLastPathComponent())
					&& tree.hasFocus()) {
				setFont(getFont().deriveFont(Font.UNDERLINE));
			} else {
				setFont(getFont().deriveFont(Font.NORMAL));
			}

			setComponentOrientation(tree.getComponentOrientation());
		}

		return this;
	}

	private static String modify(final String in) {
		if (in == null) {
			return null;
		}

		int l = in.length();
		int i = l - 1;
		while (i > 0 && in.charAt(i) == '_') {
			i--;
		}

		if (i == l - 1) {
			return in;
		}

		return in.substring(0, i + 1);
	}
}
