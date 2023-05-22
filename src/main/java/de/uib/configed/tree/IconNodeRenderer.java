package de.uib.configed.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.Main;
import de.uib.configed.Globals;

public class IconNodeRenderer extends DefaultTreeCellRenderer {
	private static final int LABEL_WIDTH = 200;
	private static final int LABEL_HEIGHT = 20;

	public IconNodeRenderer() {
		super();

		if (!Main.THEMES) {
			super.setOpaque(true);
			super.setForeground(Globals.lightBlack);
			super.setTextSelectionColor(Globals.lightBlack);
			super.setBackground(Globals.ICON_NODE_RENDERER_BACKGROUND_COLOR);
		}
		super.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
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

			if (!Main.THEMES) {
				if (sel) {
					setBackground(Globals.BACKGROUND_COLOR_7);

				} else {
					setBackground(Globals.PRIMARY_BACKGROUND_COLOR);
				}
			}

			if (leaf) {
				setIcon(node.getLeafIcon());
			} else if (expanded) {
				setIcon(node.getOpenIcon());
			} else {
				setIcon(node.getClosedIcon());
			}

			setComponentOrientation(tree.getComponentOrientation());
		}

		return this;
	}
}
