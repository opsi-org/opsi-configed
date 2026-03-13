/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Predicate;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;

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
			updateSelection(e);

			if (condition == null || condition.test(e)) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private static void updateSelection(MouseEvent e) {
		switch (e.getSource()) {
		case JTree tree -> updateSelectionInTree(tree, e);
		case JTable table -> updateSelectionInTable(table, e);
		case JList<?> list -> updateSelectionInList(list, e);
		default -> {
			// for other components, we can not change the selection, since we also use this
			// listener for other components, e.g. JPanel.
		}
		}
	}

	private static void updateSelectionInTree(JTree tree, MouseEvent e) {
		int row = tree.getRowForLocation(e.getX(), e.getY());

		// These methods accept null and in that case, the selection will be cleared, so we don't need to check for null here
		if (!tree.isRowSelected(row)) {
			tree.setSelectionRow(row);
		}
	}

	private static void updateSelectionInTable(JTable table, MouseEvent e) {
		int row = table.rowAtPoint(e.getPoint());
		if (row == -1) {
			table.clearSelection();
		} else if (!table.isRowSelected(row)) {
			table.setRowSelectionInterval(row, row);
		} else {
			// Keep selection if the clicked row is already selected
		}
	}

	private static void updateSelectionInList(JList<?> list, MouseEvent e) {
		int index = list.locationToIndex(e.getPoint());

		// We need to check the selection, because locationToIndex returns the closest index,
		// even if the click is outside of any item
		if (index != -1 && !list.getCellBounds(index, index).contains(e.getPoint())) {
			// Clicked outside of any item, clear selection
			list.clearSelection();
		} else if (!list.isSelectedIndex(index)) {
			list.setSelectedIndex(index);
		} else {
			// Keep selection if the clicked index is already selected
		}
	}
}
