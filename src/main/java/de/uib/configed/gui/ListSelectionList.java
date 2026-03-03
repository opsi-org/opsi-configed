/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.UIManager;

import de.uib.configed.gui.share.table.gui.PropertiesCellEditorAndRenderer;
import de.uib.configed.share.Icons;

public class ListSelectionList extends JList<String> {
	private Set<Integer> nonDeselectableIndices = new HashSet<>();

	// Original unfiltered model to preserve original data regardless of filtered views
	private DefaultListModel<String> originalModel = new DefaultListModel<>();

	public ListSelectionList() {
		super.setFixedCellHeight(20);
		super.setCellRenderer(this::renderLabel);
		super.setModel(originalModel);
	}

	@SuppressWarnings("java:S4968")
	public void setNonDeselectableValues(Collection<String> nonDeselectableValues) {
		if (nonDeselectableValues == null) {
			// No need to do anything if there are no non-deselectable values
			return;
		}

		nonDeselectableIndices.clear();
		for (int i = 0; i < getModel().getSize(); i++) {
			if (nonDeselectableValues.contains(getModel().getElementAt(i))) {
				nonDeselectableIndices.add(i);
			}
		}

		setSelectionModel(new ExcludeFromDelelectionListSelectionModel(this, nonDeselectableIndices));
	}

	public static class ExcludeFromDelelectionListSelectionModel extends DefaultListSelectionModel {
		private Collection<Integer> nonDeselectableIndices;
		private JList<String> jList;

		public ExcludeFromDelelectionListSelectionModel(JList<String> jList,
				Collection<Integer> nonDeselectableValues) {
			this.jList = jList;
			this.nonDeselectableIndices = nonDeselectableValues;
		}

		private void removeSelectionIntervalFromJList(int index0, int index1) {
			for (int i = index0; i <= index1; i++) {
				if (!nonDeselectableIndices.contains(i)) {
					super.removeSelectionInterval(i, i);
				}
			}
		}

		@Override
		public void removeSelectionInterval(int index0, int index1) {
			removeSelectionIntervalFromJList(index0, index1);
		}

		@Override
		public void clearSelection() {
			removeSelectionIntervalFromJList(0, jList.getModel().getSize() - 1);
		}
	}

	@SuppressWarnings("java:S4968")
	private JLabel renderLabel(JList<? extends String> l, String value, int index, boolean isSelected,
			boolean cellHasFocus) {
		JLabel label = new JLabel(PropertiesCellEditorAndRenderer.formatValue(value));
		label.setOpaque(true);

		if (nonDeselectableIndices.contains(index)) {
			label.setForeground(UIManager.getColor("List.selectionInactiveForeground"));
			label.setBackground(UIManager.getColor("List.selectionInactiveBackground"));
			label.setIcon(Icons.getIntellijIcon("locked"));
		} else {
			label.setForeground(isSelected ? UIManager.getColor("List.selectionForeground")
					: UIManager.getColor("List.foreground"));
			label.setBackground(isSelected ? UIManager.getColor("List.selectionBackground")
					: UIManager.getColor("List.background"));
		}

		return label;
	}

	public void addItem(String element) {
		if (element == null || element.isBlank()) {
			return;
		}

		if (!originalModel.contains(element)) {
			originalModel.addElement(element);
		}
	}

	public void updateSelection() {
		int idx = getModel().getSize() - 1;

		addSelectionInterval(idx, idx);
		ensureIndexIsVisible(getMaxSelectionIndex());
	}

	@Override
	public void setModel(ListModel<String> model) {
		// Treat DefaultListModel as authoritative (unfiltered) data source.
		if (model instanceof DefaultListModel) {
			// Sync incoming data into the original model and use it for the view.
			originalModel = (DefaultListModel<String>) model;
			super.setModel(originalModel);
		} else {
			// Non-default models (e.g., arrays from setListData during filtering) are treated as
			// transient filtered snapshots. Show them without altering the original model.
			super.setModel(model);
		}
	}

	public void removeItem(String element) {
		if (element == null) {
			return;
		}
		originalModel.removeElement(element);
	}

	public DefaultListModel<String> getOriginalModel() {
		return originalModel;
	}

	public void setPreviousSelectionValues(Collection<String> previouslySelectedValues) {
		int[] indices = getPreviouslySelectedIndicesFromValues(previouslySelectedValues);
		setSelectedIndices(indices);

		ensureIndexIsVisible(getSelectedIndex());
	}

	private int[] getPreviouslySelectedIndicesFromValues(Collection<String> previouslySelectedValues) {
		int[] indices = new int[previouslySelectedValues.size()];
		int n = 0;
		for (int i = 0; i < getModel().getSize(); i++) {
			if (previouslySelectedValues.contains(getModel().getElementAt(i))) {
				indices[n] = i;
				n++;
			}
		}
		return indices;
	}
}
