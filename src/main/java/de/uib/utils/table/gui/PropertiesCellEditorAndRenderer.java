/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.type.ConfigOption.TYPE;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
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

	private ListSelectionDialog listSelectionDialog;

	private int selectionMode;

	private ListModelProducer modelProducer;

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
		checkBox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				stopCellEditing();
			}
		});

		comboBox = new JComboBox<>();

		JTextField editorComponent = (JTextField) comboBox.getEditor().getEditorComponent();
		editorComponent.addActionListener((ActionEvent e) -> actOnEditorComponentAction(editorComponent));
		editorComponent.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				actOnEditorComponentAction(editorComponent);
			}
		});

		comboBox.addItemListener(itemEvent -> stopCellEditing());
		comboBox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				stopCellEditing();
			}
		});

		unusedfield = new JLabel();

		listSelectionDialog = new ListSelectionDialog(null, null, true);
		listSelectionDialog.setMultiSelection();
	}

	private void actOnEditorComponentAction(JTextField editorComponent) {
		String newItem = editorComponent.getText();
		if (!containsItem(newItem)) {
			comboBox.addItem(newItem);
		}
		comboBox.setSelectedItem(newItem);

		stopCellEditing();
	}

	private boolean containsItem(String item) {
		ComboBoxModel<String> model = comboBox.getModel();
		int size = model.getSize();

		for (int i = 0; i < size; i++) {
			if (item.equals(model.getElementAt(i))) {
				return true;
			}
		}

		return false;
	}

	public void setModelProducer(ListModelProducer producer) {
		this.modelProducer = producer == null ? new DefaultListModelProducer() : producer;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			Logging.warning(this, Configed.getResourceValue("SensitiveCellEditor.editHiddenText.forbidden"));
			return null;
		}

		String key = (String) table.getValueAt(row, 0);

		if (Utils.isKeyForSecretValue(key)) {
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

		if (modelProducer.getListCellOptions(key) == null) {
			return null;
		} else if (modelProducer.getListCellOptions(key).getType() == TYPE.BOOL_CONFIG) {
			result = getBooleanEditor(value, key);
		} else if (modelProducer.getSelectionMode(row) == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
			result = getMultiValueEditor(table, value, row);
		} else {
			result = getSingleValueEditor(value, row);
		}

		ColorTableCellRenderer.colorize(result, isSelected, row % 2 == 0, column % 2 == 0);
		return result;
	}

	private Component getBooleanEditor(Object value, String key) {
		selectionMode = BOOLEAN;

		// We want a checkbox
		if (((List<?>) value).isEmpty()) {
			checkBox.setIndeterminate(true);
		} else {
			checkBox.setChecked(resolveBooleanState(value, key));
		}

		return checkBox;
	}

	private Component getSingleValueEditor(Object value, int row) {
		selectionMode = SINGLE_SELECTION;

		comboBox.setModel(modelProducer.getComboBoxModel(row));

		if (((List<?>) value).isEmpty()) {
			comboBox.setSelectedItem(null);
		} else {
			String listElement = ((List<?>) value).get(0).toString();

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

		listSelectionDialog.setPreviousSelectionValues(POJOReMapper.remap(value));
		listSelectionDialog.setEditable(modelProducer.isEditable(row));
		listSelectionDialog.show(ConfigedMain.getMainFrame());

		// We should put this code into invokeLater, because otherwise we will call stop 
		// or cancel editing before it actually began. Editing would not have an effect
		SwingUtilities.invokeLater(() -> {
			if (listSelectionDialog.wasAccepted()) {
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
			return Collections.singletonList(comboBox.getEditor().getItem());
		} else {
			return listSelectionDialog.getSelectedValues();
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		String key = (String) table.getValueAt(row, 0);

		Component result;
		if (modelProducer.getListCellOptions(key) == null
				|| modelProducer.getListCellOptions(key).getType() != TYPE.BOOL_CONFIG) {
			rendererLabel.setText(formatList(value));

			result = rendererLabel;
		} else {
			// We want a checkbox
			if (((List<?>) value).isEmpty()) {
				rendererCheckBox.setIndeterminate(true);
			} else {
				rendererCheckBox.setChecked(resolveBooleanState(value, key));
			}

			result = rendererCheckBox;
		}

		ColorTableCellRenderer.colorize(result, isSelected, row % 2 == 0, column % 2 == 0);

		return result;
	}

	private static boolean resolveBooleanState(Object value, String key) {
		Object firstVal = ((List<?>) value).get(0);
		boolean boolState;
		if (firstVal instanceof Boolean val) {
			boolState = val;
		} else {
			boolState = (Boolean) PersistenceControllerFactory.getPersistenceController().getConfigDataService()
					.getConfigOptionsPD().get(key).getDefaultValues().get(0);
		}
		return boolState;
	}

	public static String formatList(Object value) {
		String s = value.toString();
		return s.substring(Math.min(s.length(), 1), Math.max(0, s.length() - 1));
	}
}
