/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.util.Collections;

import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

import de.uib.Main;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

public class IconNodeRendererClientTree extends IconNodeRenderer {
	private ConfigedMain configedMain;

	public IconNodeRendererClientTree(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		if (!Main.THEMES) {
			super.setOpaque(true);
			super.setForeground(Globals.LIGHT_BLACK);
			super.setTextSelectionColor(Globals.LIGHT_BLACK);
			super.setBackground(Globals.ICON_NODE_RENDERER_BACKGROUND_COLOR);
		}
		super.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (!Main.THEMES) {
			setBackground(Globals.PRIMARY_BACKGROUND_COLOR);
		}

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
					if (!Main.FONT) {
						setFont(Globals.DEFAULT_FONT_STANDARD_BOLD);
					}

					setIcon(node.getLeafIcon());
				} else {
					if (!Main.FONT) {
						setFont(Globals.DEFAULT_FONT);
					}
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

				if (configedMain.getActiveTreeNodes().containsKey(stringValue)) {
					if (!Main.FONT) {
						setFont(Globals.DEFAULT_FONT_STANDARD_BOLD);
					}
				} else {
					if (!Main.FONT) {
						setFont(Globals.DEFAULT_FONT);
					}
				}
			}

			if (tree.getLeadSelectionPath() != null && node.equals(tree.getLeadSelectionPath().getLastPathComponent())
					&& tree.hasFocus()) {
				setFont(getFont()
						.deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
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
