/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditStringList;
import de.uib.utilities.table.DefaultListModelProducer;
import de.uib.utilities.table.ListModelProducer;

public class SensitiveCellEditor extends AbstractCellEditor implements TableCellEditor {
	private JTextField field;
	private FEditStringList listeditor;

	private int editingRow = -1;
	protected String myKey;
	private ListModelProducer<String> modelProducer;

	public SensitiveCellEditor() {
		super();

		field = new JTextField();

		field.setEditable(false);
		listeditor = new FEditStringList(field, this);

		// true has undesired effects in the interaction of the CellEditor and the FEditList
		listeditor.setModal(false);
		listeditor.init();
	}

	public void reInit() {
		listeditor.init();
	}

	public void setModelProducer(ListModelProducer<String> producer) {
		this.modelProducer = producer == null ? new DefaultListModelProducer<>() : producer;
	}

	private void startListEditor() {
		field.setEditable(false);
		listeditor.init();

		SwingUtilities.invokeLater(() -> {
			listeditor.setLocationRelativeTo(ConfigedMain.getMainFrame());
			listeditor.setVisible(true);
			listeditor.repaint();
		});
	}

	public void hideListEditor() {
		SwingUtilities.invokeLater(() -> listeditor.setVisible(false));
	}

	@Override
	public boolean stopCellEditing() {
		super.cancelCellEditing();
		return super.stopCellEditing();
	}

	public boolean stopEditingAndSave() {
		return super.stopCellEditing();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		Logging.debug(this, "  celleditor working in " + row + ", " + column + " with value " + value + ", class "
				+ value.getClass().getName());

		List<String> val = modelProducer.toList(value);

		// is now always
		if (val instanceof List) {
			ListModel<String> model = modelProducer.getListModel(row, column);
			Logging.debug(this,
					" try list editing, modelproducer tells nullable " + modelProducer.isNullable(row, column));
			listeditor.setVisible(false);
			listeditor.setTitle(modelProducer.getCaption(row, column));

			if (model != null) {
				Logging.debug(this, "Selected values: " + val);

				listeditor.setListModel(model);
				listeditor.setSelectionMode(modelProducer.getSelectionMode(row, column));
				listeditor.setEditable(modelProducer.isEditable(row, column));
				listeditor.setNullable(modelProducer.isNullable(row, column));
				listeditor.setSelectedValues(val);
				listeditor.enter();

				startListEditor();

				editingRow = row;
			} else {
				model = new DefaultListModel<>();
				listeditor.setListModel(model);
				listeditor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				listeditor.setEditable(true);
				listeditor.setSelectedValues(new ArrayList<>());
				listeditor.enter();
				listeditor.setStartValue("");

				startListEditor();

				editingRow = -1;
			}
		}

		field.setText("" + value);

		return field;
	}

	@Override
	public Object getCellEditorValue() {
		Object result = null;

		if (listeditor.getValue() == null) {
			result = null;
		} else if (listeditor.getValue() instanceof List) {
			List<?> list = (List<?>) listeditor.getValue();

			if (list.isEmpty()) {
				result = list;
			} else if (List.class.isAssignableFrom(modelProducer.getClass(editingRow))) {
				result = list;
			} else if (Integer.class.isAssignableFrom(modelProducer.getClass(editingRow))) {
				result = list.get(0);
			} else if (Boolean.class.isAssignableFrom(modelProducer.getClass(editingRow))) {
				result = list.get(0);
			} else {
				StringBuilder buf = new StringBuilder("");

				for (Object element : list) {
					buf.append("" + element + ",");
				}

				buf.append("" + list.get(list.size() - 1));

				result = buf.toString();

				if ("null".equalsIgnoreCase((String) result)) {
					result = null;
				}
			}
		} else {
			result = listeditor.getValue();
		}

		return result;
	}
}
