/*
 * DynamicCombo.java
 *
 * Created on 14.04.2009, 10:36:25
 */

package de.uib.utilities.swing;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import de.uib.configed.Globals;
import de.uib.utilities.table.gui.AdaptingCellEditor;

/**
 * @author roeder
 */
public class DynamicCombo extends javax.swing.JPanel {

	protected JTable table;
	protected DefaultTableModel tablemodel;
	protected JComboBox combo;
	protected de.uib.utilities.ComboBoxModeller modelsource;

	protected TableColumn col;

	public DynamicCombo(de.uib.utilities.ComboBoxModeller modelsource) {
		this.modelsource = modelsource;
		initComponents();
	}

	public DynamicCombo() {
		this(null);
	}

	private void initComponents() {

		table = new JTable();

		tablemodel = new DefaultTableModel(new String[] { "column 0" }, 0);

		tablemodel.addRow(new String[] { "" });

		table.setModel(tablemodel);
		table.setSelectionBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		table.setSelectionForeground(Globals.lightBlack);
		table.setShowGrid(false);

		col = table.getColumnModel().getColumn(0);
		col.setHeaderRenderer(null);

		combo = new JComboBox<>();

		combo.setBorder(null);

		combo.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				/* Not needed */ }

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

				combo.setSelectedItem(combo.getSelectedItem());
				// ensures that we leave the combo box completely when we set the focus
				// somewhere else
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				/* Not needed */ }
		});

		col.setCellEditor(new AdaptingCellEditor(combo, modelsource));

		setLayout(new java.awt.BorderLayout());

		add(table);

	}

	@Override
	public void setFont(java.awt.Font font) {
		if (combo != null)
			combo.setFont(font);
	}

	public void setModelSource(de.uib.utilities.ComboBoxModeller modelSource) {
		this.modelsource = modelSource;

		col.setCellEditor(

				new de.uib.utilities.table.gui.AdaptingCellEditor(combo, modelsource));

	}

	public Object getSelectedItem() {
		return table.getValueAt(0, 0);
	}

}
