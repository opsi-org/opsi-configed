/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Predicate;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class PopupMouseListener extends MouseAdapter {
	private JPopupMenu popupMenu;
	private Predicate<MouseEvent> condition;

	public PopupMouseListener(JPopupMenu popup) {
		this(popup, null);
	}

	public PopupMouseListener(JPopupMenu popup, Predicate<MouseEvent> condition) {
		popupMenu = popup;
		this.condition = condition;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	protected void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			switch (e.getSource()) {
			case JTree tree -> updateSelectionInTree(tree, e);
			case JTable table -> updateSelectionInTable(table, e);
			default -> {
				// for other components, we can not (and should not) change the selection
			}
			}

			if (condition == null || condition.test(e)) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private static void updateSelectionInTree(JTree tree, MouseEvent e) {
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		if (path != null && !tree.isPathSelected(path)) {
			tree.setSelectionPath(path);
		}
	}

	private static void updateSelectionInTable(JTable table, MouseEvent e) {
		int row = table.rowAtPoint(e.getPoint());
		if (row != -1 && !table.isRowSelected(row)) {
			table.setRowSelectionInterval(row, row);
		}
	}
}
