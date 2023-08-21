/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.Main;
import de.uib.configed.Globals;

public class SimpleIconNodeRenderer extends DefaultTreeCellRenderer {
	private static final int LABEL_WIDTH = 300;
	private static final int LABEL_HEIGHT = 22;

	private Font emphasized;
	private Font standard;

	public SimpleIconNodeRenderer() {
		super();

		if (!Main.THEMES) {
			super.setOpaque(true);
		}

		standard = Globals.DEFAULT_FONT_BIG;

		emphasized = Globals.DEFAULT_FONT_STANDARD_BOLD;

		if (!Main.FONT) {
			super.setFont(standard);
		}
		if (!Main.THEMES) {
			super.setForeground(Globals.LIGHT_BLACK);
			super.setTextSelectionColor(Globals.LIGHT_BLACK);
			super.setBackground(Globals.SIMPLE_ICON_NODE_RENDERER_BACKGROUND_COLOR);
		}
		super.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
		super.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof SimpleIconNode) {

			String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

			setText(stringValue);
			setToolTipText(((SimpleIconNode) value).getToolTipText());

			// adaption to size of bold font??

			// Attention: must be a SimpleIconNode
			SimpleIconNode node = (SimpleIconNode) value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);
			node.setEnabled(enabled);

			if (row == 0) {
				if (!Main.FONT) {
					setFont(emphasized);
				}
			} else {
				if (!Main.FONT) {
					setFont(standard);
				}
			}

			if (!Main.THEMES) {
				// assuming that row 0 contains sort of header
				if (sel && row != 0) {
					setBackground(Globals.BACKGROUND_COLOR_7);

				} else {
					setBackground(Globals.SIMPLE_ICON_NODE_RENDERER_BACKGROUND_COLOR);
				}
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
			return this;
		}

		return this;
	}
}
