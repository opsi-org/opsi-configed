package de.uib.utilities.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Globals;

public class SimpleIconNodeRenderer extends DefaultTreeCellRenderer {
	public static int labelWidth = 300;
	public static int labelHeight = 22;
	protected Font emphasized;
	protected Font standard;
	public static Dimension preferred = new Dimension(labelWidth, labelHeight);

	public SimpleIconNodeRenderer() {
		super();

		setOpaque(true);

		standard = Globals.defaultFontBig;

		emphasized = Globals.defaultFontStandardBold;

		setFont(standard);
		setForeground(Globals.lightBlack);
		setTextSelectionColor(Globals.lightBlack);
		setBackground(Color.white);
		setBorder(new javax.swing.border.EmptyBorder(new Insets(0, 0, 0, 0)));
		setPreferredSize(preferred);
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

			if (sel && row != 0) // assuming that row 0 contains sort of header
			{
				setBackground(Globals.backLightBlue);

			} else {
				setBackground(Color.white);

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

			if (!sel)
				setIcon(node.getNonSelectedIcon());

			setComponentOrientation(tree.getComponentOrientation());
			return this;
		}

		return this;

	}
}
