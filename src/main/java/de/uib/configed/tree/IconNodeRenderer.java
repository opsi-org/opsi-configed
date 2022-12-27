package de.uib.configed.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Globals;

public class IconNodeRenderer extends DefaultTreeCellRenderer {
	public static int labelWidth = 200;
	public static int labelHeight = 20;

	public IconNodeRenderer() {
		super();
		setOpaque(true);
		setForeground(Globals.lightBlack);
		setTextSelectionColor(Globals.lightBlack);
		setBackground(Globals.ICON_NODE_RENDERER_BACKGROUND_COLOR);
		setBorder(new javax.swing.border.EmptyBorder(new Insets(0, 0, 0, 0)));
		setPreferredSize(new java.awt.Dimension(labelWidth, labelHeight));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		if (value instanceof IconNode) {

			String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

			setText(stringValue);

			// adaption to size of bold font??

			// Attention: must be a IconNode
			IconNode node = (IconNode) value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);
			node.setEnabled(enabled);

			if (sel) {
				setBackground(Globals.backLightBlue);

			} else {
				setBackground(Color.white);

			}

			if (leaf) {
				setIcon(node.getLeafIcon());
			} else if (expanded) {
				setIcon(node.getOpenIcon());
			} else {
				setIcon(node.getClosedIcon());
			}

			setComponentOrientation(tree.getComponentOrientation());
			return this;
		}

		return this;

	}
}
