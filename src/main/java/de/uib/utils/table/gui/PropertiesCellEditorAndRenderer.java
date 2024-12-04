/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FSelectionList;
import de.uib.configed.type.ConfigOption.TYPE;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.DefaultListModelProducer;

public class PropertiesCellEditorAndRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
	private static final int BOOLEAN = 0;
	private static final int SINGLE_SELECTION = 1;
	private static final int MULTI_SELECTION = 2;

	// Components for the renderer
	private FlatTriStateCheckBox rendererCheckBox;
	private JLabel rendererLabel;

	// Components for the editor
	private JLabel unusedfield;

	private FlatTriStateCheckBox checkBox;
	private JComboBox<String> comboBox;

	private FSelectionList listSelectionDialog;
	private FlatTextField addValuesTextField;

	private int selectionMode;

	private DefaultListModelProducer modelProducer;

	public PropertiesCellEditorAndRenderer() {
		super();

		// The components for the renderer
		rendererCheckBox = new FlatTriStateCheckBox();
		rendererCheckBox.setAllowIndeterminate(false);

		rendererLabel = new JLabel();
		rendererLabel.setOpaque(true);

		// The components for the editor
		checkBox = new FlatTriStateCheckBox();
		checkBox.setAllowIndeterminate(false);

		checkBox.addItemListener(itemEvent -> stopCellEditing());

		comboBox = new JComboBox<>();

		JTextField editorComponent = (JTextField) comboBox.getEditor().getEditorComponent();
		editorComponent.addActionListener((ActionEvent e) -> {
			String newItem = editorComponent.getText();
			comboBox.addItem(newItem);
			comboBox.setSelectedItem(newItem);

			stopCellEditing();
		});

		comboBox.addItemListener(itemEvent -> stopCellEditing());

		unusedfield = new JLabel();

		addValuesTextField = new FlatTextField();

		listSelectionDialog = new FSelectionList(ConfigedMain.getMainFrame(), null, true,
				new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") }, 400,
				500, addValuesTextField);
		listSelectionDialog.setMultiSelection();

		JButton addValueButton = new JButton(Icons.getIntellijIcon("add"));
		addValueButton.addActionListener(actionEvent -> listSelectionDialog.addItem(addValuesTextField.getText()));

		addValuesTextField.setTrailingComponent(addValueButton);
		addValuesTextField.setShowClearButton(true);
		addValuesTextField.addActionListener(actionEvent -> listSelectionDialog.addItem(addValuesTextField.getText()));
	}

	public void setModelProducer(DefaultListModelProducer producer) {
		this.modelProducer = producer == null ? new DefaultListModelProducer() : producer;
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

		Logging.debug(this, "  celleditor working in ", row, ", with value ", value, ", class ",
				value.getClass().getName());

		Component result;

		if (modelProducer.getListCellOptions(key).getType() == TYPE.BOOL_CONFIG) {
			result = getBooleanEditor(value);
		} else if (modelProducer.getSelectionMode(row) == ListSelectionModel.SINGLE_SELECTION) {
			result = getSingleValueEditor(key, value, row);
		} else {
			result = getMultiValueEditor(table, value, row);
		}

		ColorTableCellRenderer.colorize(result, isSelected, row % 2 == 0, column % 2 == 0);
		return result;
	}

	private Component getBooleanEditor(Object value) {
		selectionMode = BOOLEAN;

		// We want a checkbox
		if (((List<?>) value).isEmpty()) {
			checkBox.setIndeterminate(true);
		} else {
			checkBox.setChecked((Boolean) ((List<?>) value).get(0));
		}

		return checkBox;
	}

	private Component getSingleValueEditor(String key, Object value, int row) {
		selectionMode = SINGLE_SELECTION;

		comboBox.setModel(new DefaultComboBoxModel<>(
				modelProducer.getListCellOptions(key).getPossibleValues().toArray(new String[0])));
		if (((List<?>) value).isEmpty()) {
			comboBox.setSelectedItem(null);
		} else {
			String listElement = (String) ((List<?>) value).get(0);

			if (((DefaultComboBoxModel<String>) comboBox.getModel()).getIndexOf(listElement) == -1) {
				comboBox.addItem(listElement);
			}

			comboBox.setSelectedItem(listElement);
		}

		comboBox.setEditable(modelProducer.isEditable(row));

		return comboBox;
	}

	private Component getMultiValueEditor(JTable table, Object value, int row) {
		selectionMode = MULTI_SELECTION;

		listSelectionDialog.setTitle((String) table.getValueAt(row, 0));
		listSelectionDialog.setModel(modelProducer.getListModel(row));

		listSelectionDialog.setPreviousSelectionValues(POJOReMapper.remap(modelProducer.toList(value)));
		listSelectionDialog.setSize(400, 500);
		listSelectionDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		addValuesTextField.setVisible(modelProducer.isEditable(row));
		addValuesTextField.setText(null);
		listSelectionDialog.setVisible(true);

		// We should put this code into invokeLater, because otherwise we will call stop 
		// or cancel editing before it actually began. Editing would not have an effect
		SwingUtilities.invokeLater(() -> {
			if (listSelectionDialog.getResult() == 2) {
				stopCellEditing();
			} else {
				cancelCellEditing();
			}
		});

		// We cannot return null here, otherwise the editing is cancelled...
		return unusedfield;
	}

	@Override
	public Object getCellEditorValue() {
		if (selectionMode == BOOLEAN) {
			return Collections.singletonList(checkBox.getChecked());
		} else if (selectionMode == SINGLE_SELECTION) {
			return Collections.singletonList(comboBox.getSelectedItem());
		} else {
			return listSelectionDialog.getSelectedValues();
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		String key = (String) table.getValueAt(row, 0);

		Component result;
		if (modelProducer.getListCellOptions(key).getType() == TYPE.BOOL_CONFIG) {
			// We want a checkbox
			if (((List<?>) value).isEmpty()) {
				rendererCheckBox.setIndeterminate(true);
			} else {
				rendererCheckBox.setChecked((Boolean) ((List<?>) value).get(0));
			}

			result = rendererCheckBox;
		} else {
			rendererLabel.setText(formatList(value));

			result = rendererLabel;
		}

		ColorTableCellRenderer.colorize(result, isSelected, row % 2 == 0, column % 2 == 0);

		return result;
	}

	public static String formatList(Object value) {
		String s = value.toString();
		return s.substring(1, s.length() - 1);
	}
}
