package de.uib.utilities.table.gui;

import java.awt.Component;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;

import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.swing.CellRendererByIndex;

public class AdaptingCellEditorValuesByIndex extends DefaultCellEditor {

	JComboBox<String> cc;
	ComboBoxModeller cbm;
	ComboBoxModel<String> nullModel;

	public AdaptingCellEditorValuesByIndex(JComboBox<String> cc, ComboBoxModeller cbm,
			Map<String, String> universalMapOfStrings, String imagesBase) {
		super(cc);
		this.cc = cc;
		this.cbm = cbm;
		nullModel = new DefaultComboBoxModel<>(new String[] { "" });

		cc.setRenderer(new CellRendererByIndex(universalMapOfStrings, imagesBase));

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

		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
		if (c instanceof JComponent) {
			((JComponent) c).setToolTipText("" + value);
		}

		return c;
	}
}
