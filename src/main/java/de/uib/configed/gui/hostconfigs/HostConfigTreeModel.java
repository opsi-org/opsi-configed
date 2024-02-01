/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.tree.SimpleTreePath;

public class HostConfigTreeModel extends DefaultTreeModel {
	public final DefaultMutableTreeNode rootNode;

	private Set<SimpleTreePath> allPathes;

	public HostConfigTreeModel(Set<String> keys) {
		super(new DefaultMutableTreeNode(""));

		Logging.debug(this.getClass(), "HostConfigTreeModel created for " + keys);
		super.setAsksAllowsChildren(true);

		rootNode = (DefaultMutableTreeNode) super.getRoot();

		generateFrom(keys);
	}

	public NavigableSet<String> getGeneratedKeys() {
		TreeSet<String> result = new TreeSet<>();

		for (SimpleTreePath path : allPathes) {
			result.add(path.dottedString(0, path.size()));
		}

		return result;
	}

	public void setRootLabel(String s) {
		((DefaultMutableTreeNode) getRoot()).setUserObject(s);
	}

	private void generateFrom(Set<String> keys) {
		allPathes = new TreeSet<>();

		if (keys != null) {
			for (String key : keys) {
				String remainder = key;

				int j = -1;
				int k = remainder.indexOf('.');
				SimpleTreePath path = new SimpleTreePath();

				while (k > j) {
					String componentKey = key.substring(j + 1, k);
					path.add(componentKey);
					allPathes.add(new SimpleTreePath(path));

					remainder = key.substring(k + 1);

					j = k;
					k = j + 1 + remainder.indexOf('.');
				}

				path.add(remainder);

				allPathes.add(path);
			}
		}

		generateAllPaths();
	}

	private void generateAllPaths() {
		Logging.debug(this, "generateFrom allPathes " + allPathes);

		Map<SimpleTreePath, DefaultMutableTreeNode> path2Node = new TreeMap<>();

		for (SimpleTreePath path : allPathes) {
			DefaultMutableTreeNode parent = rootNode;

			for (int i = 1; i <= path.size(); i++) {
				if (i > 1) {
					parent = path2Node.get(path.subList(0, i - 1));
				}

				SimpleTreePath partialPath = path.subList(0, i);
				DefaultMutableTreeNode node = path2Node.get(partialPath);

				if (node == null) {
					node = new DefaultMutableTreeNode(path.get(i - 1));

					path2Node.put(path.subList(0, i), node);
					parent.add(node);
				}
			}
		}

		Logging.debug(this, "generateFrom allPathes ready");
	}
}
