/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * DynamicCombo.java
 *
 * Created on 14.04.2009, 10:36:25
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.table.gui.AdaptingCellEditor;

public class DynamicCombo extends JPanel {

	private JTable table;
	private JComboBox<String> combo;
	private ComboBoxModeller modelsource;

	private TableColumn col;

	public DynamicCombo() {
		initComponents();
	}

	private void initComponents() {

		table = new JTable();

		DefaultTableModel tablemodel = new DefaultTableModel(new String[] { "column 0" }, 0);

		tablemodel.addRow(new String[] { "" });

		table.setModel(tablemodel);

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

		setLayout(new BorderLayout());

		add(table);
	}

	public void setModelSource(ComboBoxModeller modelSource) {
		this.modelsource = modelSource;

		col.setCellEditor(new AdaptingCellEditor(combo, modelsource));
	}

	public Object getSelectedItem() {
		return table.getValueAt(0, 0);
	}
}
