/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class GroupTreeRenderer extends DefaultTreeCellRenderer {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private AbstractGroupTree abstractGroupTree;

	private ImageIcon objectIcon;
	private ImageIcon objectSelectedIcon;

	private ImageIcon groupIcon = Utils.getThemeIconPNG("bootstrap/group", "group unselected");
	private ImageIcon groupContainsSelectedIcon = Utils.getThemeIconPNG("bootstrap/group_selected", "group selected");

	private ImageIcon groupOpenIcon = Utils.getThemeIconPNG("bootstrap/group_open", "");
	private ImageIcon groupOpenContainsSelectedIcon = Utils.getThemeIconPNG("bootstrap/group_selected_open", "");

	public GroupTreeRenderer(AbstractGroupTree abstractGroupTree) {
		this.abstractGroupTree = abstractGroupTree;

		super.setPreferredSize(Globals.LABEL_SIZE_OF_JTREE);

		if (abstractGroupTree instanceof ClientTree) {
			objectIcon = Utils.getThemeIcon("desktop", 16);
			objectSelectedIcon = Utils.getThemeFilledIcon("desktop", 16);
		} else {
			objectIcon = Utils.getThemeIcon("package", 16);
			objectSelectedIcon = Utils.getThemeFilledIcon("package", 16);
		}
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

		if (node instanceof GroupNode) {
			setToolTipText(abstractGroupTree.getGroupDescription(text));
		} else if (abstractGroupTree instanceof ClientTree) {
			setToolTipText(
					persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps().get(text).getDescription());
		} else {
			// We don't want to show the description for a Product since we only know the productId
			// so the description is not clear
			setToolTipText(null);
		}

		if (!node.getAllowsChildren()) {
			// client
			if (abstractGroupTree.isSelectedInTable(text)) {
				setIcon(objectSelectedIcon);
			} else {
				setIcon(objectIcon);
			}
		} else {
			// group
			setGroupIcon(expanded, abstractGroupTree.getActiveParents().contains(text));
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
