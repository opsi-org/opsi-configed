/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.gui.share.table.DefaultListModelProducer;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.ConfigOption.TYPE;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class PropertiesCellEditorAndRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
	private static final int BOOLEAN = 0;
	private static final int SINGLE_SELECTION_SINGLE_LINE = 1;
	private static final int SINGLE_SELECTION_MULTI_LINE = 2;
	private static final int MULTI_SELECTION = 3;

	// Components for the renderer
	private FlatTriStateCheckBox rendererCheckBox;
	private JLabel rendererLabel;

	// Components for the editor
	private JLabel unusedfield;

	private FlatTriStateCheckBox checkBox;
	private JComboBox<String> comboBox;
	private JTextArea multiLineTextArea;
	private JScrollPane multiLineScrollPane;

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

		checkBox.addActionListener(actionEvent -> stopCellEditing());

		comboBox = new JComboBox<>();

		comboBox.addActionListener(actionEvent -> stopCellEditing());

		multiLineTextArea = new JTextArea();
		multiLineTextArea.setLineWrap(true);
		multiLineTextArea.setWrapStyleWord(true);
		multiLineScrollPane = new JScrollPane(multiLineTextArea);
		multiLineScrollPane.setPreferredSize(ListSelectionDialog.DEFAULT_MULTI_LINE_EDITOR_SIZE);
		multiLineScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		unusedfield = new JLabel();

		listSelectionDialog = new ListSelectionDialog(null, null, true);
	}

	public void setModelProducer(ListModelProducer producer) {
		this.modelProducer = producer == null ? new DefaultListModelProducer() : producer;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		UserRolesConfigDataService userRolesConfigDataService = PersistenceControllerFactory.getPersistenceController()
				.getDataServices().userRoles;
		if (userRolesConfigDataService.isGlobalReadOnly() && !userRolesConfigDataService.canEditOwnServerRole()) {
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
			result = null;
		} else {
			if (modelProducer.getListCellOptions(key).getType() == TYPE.BOOL_CONFIG) {
				result = getBooleanEditor(value, key);
			} else if (modelProducer.getSelectionMode(row) == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
				result = getMultiValueEditor((String) table.getValueAt(row, 0), value, row);
			} else if (value.toString().contains("\n")) {
				result = getSingleValueMultiLineEditor((String) table.getValueAt(row, 0), value);
			} else {
				result = getSingleValueEditor(value, row);
			}

			ColorTableCellRenderer.colorize(result, isSelected, row % 2 == 0, column % 2 == 0);
		}

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

	public void editMultiValueSingleLine(JTable table, int row) {
		Object result = startMultiLineEditor((String) table.getValueAt(row, 0), table.getValueAt(row, 1));

		if (result != null && result.equals(JOptionPane.OK_OPTION)) {
			table.setValueAt(List.of(multiLineTextArea.getText()), table.getSelectedRow(), 1);
		}
	}

	private Object startMultiLineEditor(String title, Object value) {
		if (!((List<?>) value).isEmpty()) {
			multiLineTextArea.setText(((List<?>) value).get(0).toString());
		}

		JOptionPane optionPane = new JOptionPane(multiLineScrollPane, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		Utils.enableDialogResizing(optionPane);
		JDialog dialog = optionPane.createDialog(ConfigedMain.getMainFrame(), title);
		dialog.pack();
		dialog.setVisible(true);

		return optionPane.getValue();
	}

	private Component getSingleValueMultiLineEditor(String title, Object value) {
		selectionMode = SINGLE_SELECTION_MULTI_LINE;

		Object result = startMultiLineEditor(title, value);

		// Invoke this outside so that editing will be started before it's stopped or cancelled.
		// Editing is started when the Component (not null) is returned
		SwingUtilities.invokeLater(() -> {
			if (result != null && result.equals(JOptionPane.OK_OPTION)) {
				stopCellEditing();
			} else {
				cancelCellEditing();
			}
		});

		// We cannot return null here, otherwise the editing is cancelled...
		return unusedfield;
	}

	private Component getSingleValueEditor(Object value, int row) {
		selectionMode = SINGLE_SELECTION_SINGLE_LINE;

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

	private Component getMultiValueEditor(String title, Object value, int row) {
		selectionMode = MULTI_SELECTION;

		listSelectionDialog.setTitle(title);
		listSelectionDialog.setModel(modelProducer.getListModel(row));

		listSelectionDialog.setPreviousSelectionValues(POJOReMapper.remap(value));
		listSelectionDialog.setEditable(modelProducer.isEditable(row));
		listSelectionDialog.setAlwaysOnTop(true);
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
		return switch (selectionMode) {
		case BOOLEAN -> List.of(checkBox.getChecked());
		case SINGLE_SELECTION_SINGLE_LINE -> List.of(getComboBoxValue());
		case SINGLE_SELECTION_MULTI_LINE -> List.of(multiLineTextArea.getText());
		case MULTI_SELECTION -> listSelectionDialog.getSelectedValues();
		default -> throw new IllegalStateException("Unexpected value: " + selectionMode);
		};
	}

	// The correct value for the combo box depends on whether it is editable or not.
	// If it is editable, we return the item from the editor, otherwise we return the
	// selected item from the combo box.
	private Object getComboBoxValue() {
		if (comboBox.isEditable()) {
			return comboBox.getEditor().getItem();
		} else {
			return comboBox.getSelectedItem();
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
		Logging.debug("firstVal = [", firstVal, "], type = ",
				(firstVal != null ? firstVal.getClass().getName() : "null"));
		if (firstVal instanceof Boolean boolVal) {
			Logging.debug("evaluating key: '", key, "' with value: '", boolVal, "' (type: Bool)");
			boolState = boolVal;
		} else if (firstVal instanceof String strVal) {
			Logging.debug("evaluating key: '", key, "' with value: '", strVal, "' (type: String)");
			String normalized = strVal.trim().toLowerCase(Locale.ROOT);
			if ("true".equals(normalized)) {
				boolState = true;
			} else if ("false".equals(normalized)) {
				boolState = false;
			} else {
				Logging.debug("value for key '", key,
						"' is not a recognizable Boolean. Attempting to resolve via config options.");
				boolState = getDefaultBoolean(key, firstVal);
			}
		} else {
			Logging.debug("value for key '", key,
					"' is not a recognizable Boolean. Attempting to resolve via config options.");
			boolState = getDefaultBoolean(key, firstVal);
		}
		return boolState;
	}

	@SuppressWarnings("squid:S2047")
	private static boolean getDefaultBoolean(String key, Object firstVal) {
		Map<String, ConfigOption> configOptions = PersistenceControllerFactory.getPersistenceController()
				.getDataServices().config.getConfigOptionsPD();
		if (configOptions.containsKey(key)) {
			Logging.debug("using default config value for key: ", key);
			return (Boolean) configOptions.get(key).getDefaultValues().get(0);
		} else {
			Logging.debug("config key '", key, "' not found. Using fallback: false (original input = ", firstVal, ")");
			return false;
		}
	}

	public static String formatList(Object value) {
		StringBuilder result = new StringBuilder();
		for (Object element : ((List<?>) value)) {
			result.append(formatValue(element));
			result.append(" • ");
		}
		if (!result.isEmpty()) {
			// Remove the last " • "
			result.setLength(result.length() - 3);
		}

		return result.toString();
	}

	public static String formatValue(Object value) {
		StringBuilder result = new StringBuilder();

		String str = value.toString();
		if (str.contains("\n")) {
			result.append(str.substring(0, str.indexOf('\n')));
			result.append("...");
		} else {
			result.append(str);
		}
		return result.toString();
	}
}
