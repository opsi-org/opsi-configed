package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.swing.ColoredListCellRenderer;

public class AdaptingCellEditor extends DefaultCellEditor {

	private JComboBox<String> cc;
	ComboBoxModeller cbm;
	ComboBoxModel<String> nullModel;

	public AdaptingCellEditor(JComboBox<String> cc, ComboBoxModeller cbm) {
		super(cc);
		this.cc = cc;
		this.cbm = cbm;
		nullModel = new DefaultComboBoxModel<>(new String[] { "" });

		cc.setRenderer(new ColoredListCellRenderer());

	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		int modelRow = table.convertRowIndexToModel(row);
		int modelColumn = table.convertColumnIndexToModel(column);
		if (cbm == null || cbm.getComboBoxModel(modelRow, modelColumn).getSize() <= 1) {
			cc.setModel(nullModel);

			if (cbm != null && cbm.getComboBoxModel(modelRow, modelColumn).getSize() == 1) {
				cc.setToolTipText(cbm.getComboBoxModel(modelRow, modelColumn).getElementAt(0));
			}
		} else {
			cc.setModel(cbm.getComboBoxModel(modelRow, modelColumn));
		}

		return super.getTableCellEditorComponent(table, value, isSelected, row, column);

	}

}
