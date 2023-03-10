package de.uib.utilities.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Globals;

public class SimpleIconNodeRenderer extends DefaultTreeCellRenderer {
	private static final int LABEL_WIDTH = 300;
	private static final int LABEL_HEIGHT = 22;

	protected Font emphasized;
	protected Font standard;

	public SimpleIconNodeRenderer() {
		super();

		super.setOpaque(true);

		standard = Globals.defaultFontBig;

		emphasized = Globals.defaultFontStandardBold;

		super.setFont(standard);
		super.setForeground(Globals.lightBlack);
		super.setTextSelectionColor(Globals.lightBlack);
		super.setBackground(Globals.SIMPLE_ICON_NODE_RENDERER_BACKGROUND_COLOR);
		super.setBorder(new javax.swing.border.EmptyBorder(new Insets(0, 0, 0, 0)));
		super.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

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
				setFont(emphasized);
			} else {
				setFont(standard);
			}

			// assuming that row 0 contains sort of header
			if (sel && row != 0) {
				setBackground(Globals.BACKGROUND_COLOR_7);

			} else {
				setBackground(Globals.SIMPLE_ICON_NODE_RENDERER_BACKGROUND_COLOR);

			}

			if (leaf) {
				setIcon(node.getLeafIcon());
			} else

			{
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
