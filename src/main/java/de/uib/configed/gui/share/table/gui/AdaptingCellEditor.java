/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;

public class AdaptingCellEditor extends DefaultCellEditor {
	private final JComboBox<String> cc;
	private final ComboBoxModeller cbm;
	private final ComboBoxModel<String> nullModel = new DefaultComboBoxModel<>(new String[] { "" });

	private final boolean tooltipFromValue;

	public AdaptingCellEditor(JComboBox<String> comboBox, ComboBoxModeller modeller) {
		this(comboBox, modeller, false);
	}

	public AdaptingCellEditor(JComboBox<String> comboBox, ComboBoxModeller modeller, boolean tooltipFromValue) {
		super(comboBox);
		this.cc = comboBox;
		this.cbm = modeller;
		this.tooltipFromValue = tooltipFromValue;

		comboBox.setRenderer(new ColoredListCellRenderer());
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		int modelRow = table.convertRowIndexToModel(row);
		int modelColumn = table.convertColumnIndexToModel(column);
		if (cbm == null || cbm.getComboBoxModel(modelRow, modelColumn) == null
				|| cbm.getComboBoxModel(modelRow, modelColumn).getSize() <= 1) {
			cc.setModel(nullModel);

			if (cbm != null && cbm.getComboBoxModel(modelRow, modelColumn) != null
					&& cbm.getComboBoxModel(modelRow, modelColumn).getSize() == 1) {
				cc.setToolTipText(cbm.getComboBoxModel(modelRow, modelColumn).getElementAt(0));
			}
		} else {
			cc.setModel(cbm.getComboBoxModel(modelRow, modelColumn));
		}

		Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);

		if (tooltipFromValue) {
			((JComboBox<?>) component).setToolTipText("" + value);
		}

		return component;
	}

	private static class ColoredListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			ColorTableCellRenderer.colorize(c, isSelected, index);

			return c;
		}
	}

}
