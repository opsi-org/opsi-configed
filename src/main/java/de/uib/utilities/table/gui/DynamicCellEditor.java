package de.uib.utilities.table.gui;

import java.awt.Component;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;

import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.swing.CellRendererByIndex;

public class DynamicCellEditor extends DefaultCellEditor {

	JComboBox cc;
	ComboBoxModeller cbm;
	ComboBoxModel nullModel;

	public DynamicCellEditor(JComboBox cc, ComboBoxModeller cbm, Set<String> knownKeys) {
		super(cc);
		this.cc = cc;
		this.cbm = cbm;
		nullModel = new DefaultComboBoxModel<>(new String[] { "" });

		cc.setRenderer(new CellRendererByIndex(knownKeys, null, 30));

	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		int modelRow = table.convertRowIndexToModel(row);
		int modelColumn = table.convertColumnIndexToModel(column);
		if (cbm == null || cbm.getComboBoxModel(modelRow, modelColumn) == null
				|| cbm.getComboBoxModel(modelRow, modelColumn).getSize() <= 1) {
			cc.setModel(nullModel);

			if (cbm != null && cbm.getComboBoxModel(modelRow, modelColumn) != null
					&& cbm.getComboBoxModel(modelRow, modelColumn).getSize() == 1)
				cc.setToolTipText((String) cbm.getComboBoxModel(modelRow, modelColumn).getElementAt(0));
		} else
			cc.setModel(cbm.getComboBoxModel(modelRow, modelColumn));

		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
		if (c instanceof JComponent)
			((JComponent) c).setToolTipText("" + value);

		return c;
	}

}
