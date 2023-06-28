/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.uib.utilities.tree.SimpleTreePath;

public class Leafname2AllItsPaths {
	Map<String, ArrayList<SimpleTreePath>> invertedSimpleClientPaths = new HashMap<>();

	public Leafname2AllItsPaths() {
		invertedSimpleClientPaths = new HashMap<>();
	}

	public Set<String> keySet() {
		return invertedSimpleClientPaths.keySet();
	}

	public List<SimpleTreePath> get(String k) {
		return invertedSimpleClientPaths.get(k);
	}

	public void clear() {
		invertedSimpleClientPaths.clear();
	}

	public void rebuildFromTree(DefaultMutableTreeNode node) {
		clear();

		Enumeration<TreeNode> e = node.breadthFirstEnumeration();

		while (e.hasMoreElements()) {
			DefaultMutableTreeNode element = (DefaultMutableTreeNode) e.nextElement();

			if (!element.getAllowsChildren()) {
				String nodeinfo = (String) element.getUserObject();
				add(nodeinfo, new SimpleTreePath(element.getPath()));
			}
		}
	}

	public List<SimpleTreePath> getSimpleTreePaths(String leafname) {
		return invertedSimpleClientPaths.get(leafname);
	}

	private void add(String leafname, SimpleTreePath simpleTreePath) {
		invertedSimpleClientPaths.computeIfAbsent(leafname, arg -> new ArrayList<>()).add(simpleTreePath);
	}

	public void add(String leafname, TreePath clientPath) {

		add(leafname, new SimpleTreePath(clientPath.getPath()));
	}

	public void remove(String leafname, SimpleTreePath clientPath) {
		if (invertedSimpleClientPaths.get(leafname) != null) {
			invertedSimpleClientPaths.get(leafname).remove(clientPath);
		}
	}
}
