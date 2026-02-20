/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;

public class GroupTreeRenderer extends DefaultTreeCellRenderer {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private AbstractGroupTree abstractGroupTree;

	private ImageIcon objectIcon;
	private ImageIcon objectSelectedIcon;

	private ImageIcon groupIcon = Icons.getThemeIntellijIcon("groups", 16);
	private ImageIcon groupContainsSelectedIcon = Icons.getThemeFilledIcon("groups", 16);

	private ImageIcon groupOpenIcon = Icons.getThemeIntellijIcon("groups_open", 16);
	private ImageIcon groupOpenContainsSelectedIcon = Icons.getThemeFilledIcon("groups_open", 16);

	public GroupTreeRenderer(AbstractGroupTree abstractGroupTree) {
		this.abstractGroupTree = abstractGroupTree;

		super.setPreferredSize(Globals.LABEL_SIZE_OF_JTREE);

		if (abstractGroupTree instanceof ClientTree) {
			objectIcon = Icons.getThemeIntellijIcon("desktop", 16);
			objectSelectedIcon = Icons.getThemeFilledIcon("desktop", 16);
		} else {
			objectIcon = Icons.getThemeIntellijIcon("package", 16);
			objectSelectedIcon = Icons.getThemeFilledIcon("package", 16);
		}
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (!(value instanceof DefaultMutableTreeNode)) {
			Logging.warning(this, "We expected a DefaultMutableTreeNode, but received ", value.getClass());
		}

		String text = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		if (node instanceof GroupNode) {
			setToolTipText(abstractGroupTree.getGroupDescription(text));
		} else if (abstractGroupTree instanceof ClientTree) {
			setToolTipText(persistenceController.getDataServices().hostInfoCollections.getMapOfAllPCInfoMaps().get(text)
					.getString(HostInfo.CLIENT_DESCRIPTION_KEY));
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
			setFont(getFont().deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
		} else {
			setFont(getFont().deriveFont(Map.of(TextAttribute.UNDERLINE, -1)));
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
