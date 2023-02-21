package de.uib.configed.tree;

import java.awt.Component;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JTree;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

public class IconNodeRendererClientTree extends IconNodeRenderer {

	protected ConfigedMain main;

	VisualClientNodeNameModifier modifier = new VisualClientNodeNameModifierFactory().getModifier();

	public IconNodeRendererClientTree(ConfigedMain main) {
		this.main = main;
		super.setOpaque(true);
		super.setForeground(Globals.lightBlack);
		super.setTextSelectionColor(Globals.lightBlack);
		super.setBackground(Globals.ICON_NODE_RENDERER_BACKGROUND_COLOR);
		super.setBorder(new javax.swing.border.EmptyBorder(new Insets(0, 0, 0, 0)));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		setBackground(Globals.PRIMARY_BACKGROUND_COLOR);
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

				// default,will be changed, if clients are childs
				setIcon(node.getClosedIcon());

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
