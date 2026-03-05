/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

import de.uib.configed.gui.share.swing.ColoredListCellRenderer;

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
}
