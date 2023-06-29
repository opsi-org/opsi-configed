/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import org.json.JSONObject;

import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditStringList;
import de.uib.utilities.table.DefaultListModelProducer;
import de.uib.utilities.table.ListModelProducer;

public class SensitiveCellEditor extends AbstractCellEditor implements TableCellEditor, MouseListener {

	private JTextField field;

	private FEditStringList listeditor;
	private int editingRow = -1;
	private int editingColumn = -1;
	protected String myKey;

	private ListModelProducer<String> modelProducer;

	private boolean usingListEditor;

	public SensitiveCellEditor() {
		super();

		field = new JTextField();

		field.setEditable(false);
		field.addMouseListener(this);
		listeditor = new FEditStringList(field, this);

		// true has undesired effects in the interaction of the CellEditor and the FEditList
		listeditor.setModal(false);
		listeditor.init();
	}

	public void reInit() {
		listeditor.init();
	}

	public void setModelProducer(ListModelProducer<String> producer) {

		this.modelProducer = producer;
		if (producer == null) {
			// build default producer

			modelProducer = new DefaultListModelProducer<>();
		}
	}

	private void startListEditor() {
		field.setEditable(false);
		listeditor.init();

		SwingUtilities.invokeLater(() -> {
			// center on mainFrame
			listeditor.setLocationRelativeTo(ConfigedMain.getMainFrame());
			listeditor.setVisible(true);
			listeditor.repaint();
		});

		usingListEditor = true;
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

				listeditor.setListModel(model);

				Logging.info(this, "startValue set: " + value);

				listeditor.setSelectionMode(modelProducer.getSelectionMode(row, column));
				listeditor.setEditable(modelProducer.isEditable(row, column));
				listeditor.setNullable(modelProducer.isNullable(row, column));
				listeditor.setSelectedValues(modelProducer.getSelectedValues(row, column));

				listeditor.enter();
				startListEditor();

				editingRow = row;
				editingColumn = column;
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
				editingColumn = -1;
			}
		}

		field.setText("" + value);

		return field;
	}

	@Override
	public Object getCellEditorValue() {

		if (listeditor.getValue() == null) {
			return null;
		}

		if (listeditor.getValue() instanceof List) {
			List<?> list = (List<?>) listeditor.getValue();

			if (List.class.isAssignableFrom(modelProducer.getClass(editingRow))) {

				return list;
			}

			int n = list.size();

			if (Integer.class.isAssignableFrom(modelProducer.getClass(editingRow))) {

				if (n == 0) {
					return null;
				}

				return list.get(0);
			}

			if (Boolean.class.isAssignableFrom(modelProducer.getClass(editingRow))) {

				if (n == 0) {
					return null;
				}

				return list.get(0);
			}

			if (n == 0) {
				return "";
			}

			StringBuilder buf = new StringBuilder("");

			for (int i = 0; i < n - 1; i++) {
				buf.append("" + list.get(i) + ",");
			}
			buf.append("" + list.get(n - 1));

			String result = buf.toString();

			if ("null".equalsIgnoreCase(result)) {
				return JSONObject.NULL;
			}

			return result;
		}

		return listeditor.getValue();
	}

	// MouseListener for textfield
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == field && usingListEditor && e.getClickCount() > 1) {
			listeditor.setVisible(true);
			listeditor.repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}
}
