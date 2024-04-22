/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.uib.utils.logging.Logging;

public class HostConfigTreeModel extends DefaultTreeModel {
	public final DefaultMutableTreeNode rootNode;

	private NavigableSet<String> allPathes;

	public HostConfigTreeModel(Set<String> keys) {
		super(new DefaultMutableTreeNode(""));

		Logging.debug(this.getClass(), "HostConfigTreeModel created for " + keys);
		super.setAsksAllowsChildren(true);

		rootNode = (DefaultMutableTreeNode) super.getRoot();

		generateAllPaths(keys);
	}

	public NavigableSet<String> getGeneratedKeys() {
		return allPathes;
	}

	public void setRootLabel(String s) {
		((DefaultMutableTreeNode) getRoot()).setUserObject(s);
	}

	private void generateAllPaths(Set<String> keys) {
		Logging.debug(this, "generateFrom keys " + keys);

		Map<String, DefaultMutableTreeNode> path2Node = new TreeMap<>();

		for (String path : keys) {
			DefaultMutableTreeNode parent = rootNode;

			List<String> pathAsList = Arrays.asList(path.split("\\."));

			for (int i = 1; i <= pathAsList.size(); i++) {
				String partialPath = String.join(".", pathAsList.subList(0, i).toArray(new String[0]));
				if (i > 1) {
					parent = path2Node.get(String.join(".", pathAsList.subList(0, i - 1).toArray(new String[0])));
				}

				final int lastElement = i - 1;
				final DefaultMutableTreeNode parentNode = parent;

				path2Node.computeIfAbsent(partialPath, (String arg) -> {
					DefaultMutableTreeNode node = new DefaultMutableTreeNode(pathAsList.get(lastElement));
					parentNode.add(node);
					return node;
				});
			}
		}

		allPathes = new TreeSet<>(path2Node.keySet());
		Logging.debug(this, "generateFrom keys ready");
	}
}
