/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class ClientTreeRenderer extends DefaultTreeCellRenderer {
	private ConfigedMain configedMain;

	private Map<String, HostInfo> host2HostInfo;

	private ImageIcon iconClient = Utils.createImageIcon("images/client_small.png", "client");
	private ImageIcon nonSelectedIconClient = Utils.createImageIcon("images/client_small_unselected.png", "client");

	private ImageIcon groupUnselectedIcon = Utils.createImageIcon("images/group_small_unselected.png",
			"group unselected");
	private ImageIcon groupSelected = Utils.createImageIcon("images/group_small.png", "client");
	private ImageIcon group1SelectedIcon = Utils.createImageIcon("images/group_small_1selected.png", "group 1selected");

	public ClientTreeRenderer(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		super.setPreferredSize(Globals.LABEL_SIZE_OF_JTREE);
	}

	public void setHost2HostInfo(Map<String, HostInfo> host2HostInfo) {
		this.host2HostInfo = host2HostInfo;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (!(value instanceof DefaultMutableTreeNode)) {
			Logging.warning(this, "We expected a DefaultMutableTreeNode, but received " + value.getClass().toString());
		}

		String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

		setText(stringValue);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		setTooltip(node);

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

		if (hasFocus) {
			setFont(getFont()
					.deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
		} else {
			setFont(getFont().deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, -1)));
		}

		return this;
	}

	private void setTooltip(DefaultMutableTreeNode node) {
		Object userObject = node.getUserObject();

		if (node instanceof GroupNode) {
			// TODO We still need to find a better solution for this...
			setToolTipText(((GroupNode) node).getToolTipText());
		} else if (host2HostInfo != null && host2HostInfo.get(userObject) != null
				&& !"".equals(host2HostInfo.get(userObject).getDescription())) {
			setToolTipText(host2HostInfo.get(userObject).getDescription());
		} else {
			setToolTipText(node.getUserObject().toString());
		}
	}
}
