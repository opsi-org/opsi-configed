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

import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class ClientTreeRenderer extends DefaultTreeCellRenderer {
	private ClientTree clientTree;

	private Map<String, HostInfo> host2HostInfo;
	private Map<String, Map<String, String>> groups;

	private ImageIcon clientIcon = Utils.getThemeIconPNG("bootstrap/laptop", "client");
	private ImageIcon clientSelectedIcon = Utils.getThemeIconPNG("bootstrap/laptop_selected", "client");

	private ImageIcon groupIcon = Utils.getThemeIconPNG("bootstrap/group", "group unselected");
	private ImageIcon groupContainsSelectedIcon = Utils.getThemeIconPNG("bootstrap/group_selected", "group selected");

	public ClientTreeRenderer(ClientTree clientTree) {
		this.clientTree = clientTree;

		super.setPreferredSize(Globals.LABEL_SIZE_OF_JTREE);
	}

	public void setHost2HostInfo(Map<String, HostInfo> host2HostInfo) {
		this.host2HostInfo = host2HostInfo;
	}

	public void setGroupNodeTooltips(Map<String, Map<String, String>> groups) {
		this.groups = groups;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (!(value instanceof DefaultMutableTreeNode)) {
			Logging.warning(this, "We expected a DefaultMutableTreeNode, but received " + value.getClass().toString());
		}

		String text = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		setTooltip(node);

		if (!node.getAllowsChildren()) {
			// client
			if (sel) {
				setIcon(clientSelectedIcon);
			} else {
				setIcon(clientIcon);
			}
		} else {
			// group
			if (clientTree.getActiveParents().contains(text)) {
				setIcon(groupContainsSelectedIcon);
			} else {
				setIcon(groupIcon);
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
			if (groups != null) {
				setToolTipText(groups.get(node.toString()).get("description"));
			}
		} else if (host2HostInfo != null && host2HostInfo.get(userObject) != null
				&& !"".equals(host2HostInfo.get(userObject).getDescription())) {
			setToolTipText(host2HostInfo.get(userObject).getDescription());
		} else {
			setToolTipText(null);
		}
	}
}
