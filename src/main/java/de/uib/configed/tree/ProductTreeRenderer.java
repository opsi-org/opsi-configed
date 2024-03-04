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
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import utils.Utils;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {
	private Map<String, Map<String, String>> groups;

	private ImageIcon productIcon = Utils.getThemeIconPNG("bootstrap/product", "client");
	private ImageIcon productSelectedIcon = Utils.getThemeIconPNG("bootstrap/product_selected", "client");

	private ImageIcon groupIcon = Utils.getThemeIconPNG("bootstrap/group", "group unselected");
	private ImageIcon groupContainsSelectedIcon = Utils.getThemeIconPNG("bootstrap/group_selected", "group selected");

	private ImageIcon groupOpenIcon = Utils.getThemeIconPNG("bootstrap/group_open", "");
	private ImageIcon groupOpenContainsSelectedIcon = Utils.getThemeIconPNG("bootstrap/group_selected_open", "");

	private ProductTree productTree;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ProductTreeRenderer(Map<String, Map<String, String>> groups, ProductTree productTree) {
		this.groups = groups;
		this.productTree = productTree;

		super.setPreferredSize(Globals.LABEL_SIZE_OF_JTREE);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		String text = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

		if (((DefaultMutableTreeNode) value).getAllowsChildren()) {
			setGroupIcon(expanded, productTree.getActiveParents().contains(text));
		} else {
			if (sel) {
				setIcon(productSelectedIcon);
			} else {
				setIcon(productIcon);
			}
		}

		if (value instanceof GroupNode && groups.containsKey(text)) {
			setToolTipText(groups.get(text).get("description"));
		} else {
			setToolTipText(persistenceController.getProductDataService().getProductInfo(text));
		}

		if (hasFocus) {
			setFont(getFont()
					.deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
		} else {
			setFont(getFont().deriveFont(Collections.singletonMap(TextAttribute.UNDERLINE, -1)));
		}

		return this;
	}

	private void setGroupIcon(boolean expanded, boolean containsSelected) {
		if (expanded) {
			if (containsSelected) {
				setIcon(groupOpenContainsSelectedIcon);
			} else {
				setIcon(groupOpenIcon);
			}
		} else {
			if (containsSelected) {
				setIcon(groupContainsSelectedIcon);
			} else {
				setIcon(groupIcon);
			}
		}
	}
}
