package de.uib.configed.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JTree;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

public class IconNodeRendererClientTree extends IconNodeRenderer {

	protected ConfigedMain main;

	public static int labelWidth = 200;
	public static int labelHeight = 20;

	VisualClientNodeNameModifier modifier = new VisualClientNodeNameModifierFactory().getModifier();

	public IconNodeRendererClientTree(ConfigedMain main) {
		this.main = main;
		setOpaque(true);
		setForeground(Globals.lightBlack);
		setTextSelectionColor(Globals.lightBlack);
		setBackground(Color.white);
		setBorder(new javax.swing.border.EmptyBorder(new Insets(0, 0, 0, 0)));
		setPreferredSize(new java.awt.Dimension(labelWidth, labelHeight));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		setBackground(Color.white);
		if (value instanceof IconNode) {
			String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

			setText(stringValue);
			setToolTipText(((IconNode) value).getToolTipText());

			// Attention: must be a IconNode
			IconNode node = (IconNode) value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);

			node.setEnabled(enabled);

			if (!node.getAllowsChildren()) // client
			{

				if (

				main.getActiveTreeNodes().containsKey(stringValue)) {
					setFont(Globals.defaultFontStandardBold);

					setIcon(node.getLeafIcon());

				} else {

					setFont(Globals.defaultFont);
					setIcon(node.getNonSelectedLeafIcon());

				}
			} else // group
			{
				String visualText = modifier.modify(stringValue);

				setText(visualText);

				setIcon(node.getClosedIcon()); // default,will be changed, if clients are childs

				if (main.getActiveParents().contains(stringValue)) {
					setIcon(node.getEmphasizedIcon());
				}

				if (

				main.getActiveTreeNodes().containsKey(stringValue)) {
					setFont(Globals.defaultFontStandardBold);

				} else {
					setFont(Globals.defaultFont);

				}
			}

			if (tree.getSelectionPath() != null && node.equals(tree.getSelectionPath().getLastPathComponent())
					&& tree.hasFocus())

			{

				Map attributes = getFont().getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
				setFont(getFont().deriveFont(attributes));

			}

			setComponentOrientation(tree.getComponentOrientation());
			return this;
		}

		return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

	}
}
