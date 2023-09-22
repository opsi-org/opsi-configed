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

import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.tree.SimpleTreePath;

public class HostConfigTreeModel extends DefaultTreeModel {
	public final SimpleIconNode rootNode;

	private Set<SimpleTreePath> allPathes;

	private Map<String, String> tooltips;

	public HostConfigTreeModel(Map<String, String> keysWithTooltips) {
		super(new SimpleIconNode(""));

		Logging.debug(this.getClass(), "HostConfigTreeModel created for " + keysWithTooltips);
		super.setAsksAllowsChildren(true);

		rootNode = (SimpleIconNode) super.getRoot();

		this.tooltips = keysWithTooltips;

		generateFrom(keysWithTooltips.keySet());
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

	private void generateFrom(Set<String> dottedKeys) {
		allPathes = new TreeSet<>();

		if (dottedKeys != null) {
			for (String key : dottedKeys) {

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

		Map<SimpleTreePath, SimpleIconNode> path2Node = new TreeMap<>();

		for (SimpleTreePath path : allPathes) {
			SimpleIconNode parent = rootNode;

			for (int i = 1; i <= path.size(); i++) {
				if (i > 1) {
					parent = path2Node.get(path.subList(0, i - 1));
				}

				SimpleTreePath partialPath = path.subList(0, i);
				SimpleIconNode node = path2Node.get(partialPath);

				if (node == null) {
					node = createNode(path, partialPath, i);

					path2Node.put(path.subList(0, i), node);
					parent.add(node);
				}
			}
		}

		Logging.debug(this, "generateFrom allPathes ready");
	}

	private SimpleIconNode createNode(SimpleTreePath path, SimpleTreePath partialPath, int i) {
		// node must be created
		SimpleIconNode node = new SimpleIconNode(path.get(i - 1));

		if (tooltips != null) {
			String key = partialPath.dottedString(0, partialPath.size());

			if (tooltips.get(key) == null) {
				node.setToolTipText(key);
			} else {
				node.setToolTipText(Configed.getResourceValue(tooltips.get(key)));
			}
		}

		return node;
	}
}
