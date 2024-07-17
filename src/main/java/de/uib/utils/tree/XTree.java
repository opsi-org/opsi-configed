/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.tree;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.uib.utils.logging.Logging;

public class XTree extends JTree {
	public XTree() {
		super();
		init();
	}

	private void init() {
		MouseMotionListener ml = new MouseAdapter() {
			Cursor infoCursor = new Cursor(Cursor.HAND_CURSOR);

			@Override
			public void mouseMoved(MouseEvent e) {
				TreePath currentPath = getPathForLocation(e.getX(), e.getY());
				if (currentPath != null && getModel() != null
						&& getModel().isLeaf(currentPath.getLastPathComponent())) {
					setCursor(infoCursor);
					setSelectionPath(currentPath);
				} else {
					setCursor(null);
				}
			}
		};
		addMouseMotionListener(ml);

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	public List<Integer> getToggledRows(TreePath parent) {
		List<Integer> result = new ArrayList<>();
		Enumeration<TreePath> enumer = super.getDescendantToggledPaths(parent);
		while (enumer.hasMoreElements()) {
			result.add(getRowForPath(enumer.nextElement()));
		}
		return result;
	}

	public void expandRows(Collection<Integer> rows) {
		Logging.debug(this, "expandRows ", rows.size());
		for (Integer row : rows) {
			expandRow(row);
		}
	}

	public void expandAll() {
		for (int row = 0; row < getRowCount(); row++) {
			expandRow(row);
		}
	}
}
