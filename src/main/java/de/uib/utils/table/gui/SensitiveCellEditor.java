/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.FEditStringList;
import de.uib.utils.table.DefaultListModelProducer;
import de.uib.utils.table.ListModelProducer;

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
		listeditor.setPreferredScrollPaneSize(new Dimension(300, 240));
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
		String key = (String) table.getValueAt(row, 0);
		if (Utils.isKeyForSecretValue(key)) {
			if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
					.isGlobalReadOnly()) {
				Logging.warning(this, Configed.getResourceValue("SensitiveCellEditor.editHiddenText.forbidden"));
				return null;
			}

			int returnedOption = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("SensitiveCellEditor.editHiddenText.text"),
					Configed.getResourceValue("SensitiveCellEditor.editHiddenText.title"), JOptionPane.YES_NO_OPTION);

			Logging.info(this, " getTableCellEditorComponent, celleditor working, returned option ", returnedOption);
			if (returnedOption != JOptionPane.YES_OPTION) {
				return null;
			}
		}

		Logging.debug(this, "  celleditor working in ", row, ", ", column, " with value ", value, ", class ",
				value.getClass().getName());

		List<String> val = modelProducer.toList(value);

		ListModel<String> model = modelProducer.getListModel(row, column);
		Logging.debug(this, " try list editing, modelproducer tells nullable ", modelProducer.isNullable(row, column));
		listeditor.setVisible(false);
		listeditor.setTitle(modelProducer.getCaption(row, column));

		if (model != null) {
			Logging.debug(this, "Selected values: ", val);

			listeditor.setListModel(model);
			listeditor.setSelectionMode(modelProducer.getSelectionMode(row, column));
			listeditor.setEditable(modelProducer.isEditable(row, column));
			listeditor.setNullable(modelProducer.isNullable(row, column));
			listeditor.setSelectedValues(val);

			startListEditor();

			editingRow = row;
		} else {
			model = new DefaultListModel<>();
			listeditor.setListModel(model);
			listeditor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listeditor.setEditable(true);
			listeditor.setSelectedValues(new ArrayList<>());
			listeditor.setStartValue("");

			startListEditor();

			editingRow = -1;
		}

		field.setText("" + value);

		return field;
	}

	@Override
	public Object getCellEditorValue() {
		Object result = null;

		List<?> list = listeditor.getSelectedList();

		if (list.isEmpty()) {
			result = list;
		} else if (List.class.isAssignableFrom(modelProducer.getClass(editingRow))) {
			result = list;
		} else if (Integer.class.isAssignableFrom(modelProducer.getClass(editingRow))) {
			result = list.get(0);
		} else if (Boolean.class.isAssignableFrom(modelProducer.getClass(editingRow))) {
			result = list.get(0);
		} else {
			StringBuilder buf = new StringBuilder();

			for (int i = 0; i < list.size() - 1; i++) {
				buf.append("" + list.get(i) + ",");
			}

			buf.append("" + list.get(list.size() - 1));

			result = buf.toString();

			if ("null".equalsIgnoreCase((String) result)) {
				result = null;
			}
		}

		return result;
	}
}
