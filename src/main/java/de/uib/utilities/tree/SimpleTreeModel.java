package de.uib.utilities.tree;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class SimpleTreeModel extends DefaultTreeModel {
	java.util.LinkedHashMap<String, Object> virtualLines;

	public final SimpleIconNode rootNode;

	Set<SimpleTreePath> allPathes;
	Map<SimpleTreePath, SimpleIconNode> path2Node;

	Map<String, String> tooltips;

	public SimpleTreeModel(java.util.Set<String> dottedKeys, Map<String, String> tooltips) {
		super(new SimpleIconNode(""));

		Logging.debug(this, "SimpleTreeModel created for " + dottedKeys);
		super.setAsksAllowsChildren(true);

		rootNode = (SimpleIconNode) super.getRoot();

		this.tooltips = tooltips;
		generateFrom(dottedKeys);
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

	private void generateFrom(java.util.Set<String> dottedKeys) {
		allPathes = new TreeSet<>();
		path2Node = new TreeMap<>();

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

		Logging.debug(this, "generateFrom allPathes " + allPathes);

		for (SimpleTreePath path : allPathes) {
			SimpleIconNode parent = rootNode;

			for (int i = 1; i <= path.size(); i++) {
				if (i > 1) {
					parent = path2Node.get(path.subList(0, i - 1));
				}

				SimpleTreePath partialPath = path.subList(0, i);
				SimpleIconNode node = path2Node.get(partialPath);

				if (node == null) {
					// node must be created
					node = new SimpleIconNode(path.get(i - 1));
					node.setIcon(Globals.createImageIcon("images/opentable_small.png", "open table"));
					node.setNonSelectedIcon(Globals.createImageIcon("images/closedtable_small.png", "closed table"));

					if (tooltips != null) {
						String key = partialPath.dottedString(0, partialPath.size());
						String description = tooltips.get(key);
						if (description == null || description.trim().equals("")) {
							node.setToolTipText(key);
						} else {
							node.setToolTipText(description);
						}
					}

					path2Node.put(path.subList(0, i), node);
					parent.add(node);
				}
			}
		}

		Logging.debug(this, "generateFrom allPathes ready");
	}
}
