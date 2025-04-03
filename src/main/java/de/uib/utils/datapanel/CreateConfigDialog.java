/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.datapanel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.PropertiesCellEditorAndRenderer;

public class CreateConfigDialog {
	private EditMapPanelX editMapPanelX;

	private JTextField textFieldConfigEntry;
	private JTextField textFieldDescription;

	private JCheckBox booleanDefault;

	private ListSelectionDialog defaultValuesSelectionDialog;
	private ListSelectionDialog possibleValuesSelectionDialog;

	private JCheckBox isBoolean;
	private JCheckBox isEditable;
	private JCheckBox isMultiValue;

	private JPanel panel;

	private JPanel generalPanel;
	private JPanel booleanDetailsPanel;
	private JPanel unicodeDetailsPanel;

	private JDialog dialog;

	public CreateConfigDialog(EditMapPanelX editMapPanelX) {
		this.editMapPanelX = editMapPanelX;

		initGeneralPanel();
		initBooleanDetailsPanel();
		initUnicodeDetailsPanel();

		// Create the tabbed pane after the details panels have been initialized
		initPanel();

		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
				new Object[] { Configed.getResourceValue("save"), Configed.getResourceValue("buttonCancel") });

		dialog = pane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntry"));

		dialog.setVisible(true);

		Logging.info(this, "result of create config dialog ", pane.getValue());

		if (pane.getValue() != null && pane.getValue().equals(Configed.getResourceValue("save"))) {
			createConfig();
		}
	}

	private void initBooleanDetailsPanel() {
		JLabel defaultLabel = new JLabel(Configed.getResourceValue("CreateConfigDialog.defaultValue"));
		booleanDefault = new JCheckBox();
		booleanDefault.setHorizontalTextPosition(SwingConstants.LEADING);

		booleanDetailsPanel = new JPanel();
		GroupLayout layout = new GroupLayout(booleanDetailsPanel);
		booleanDetailsPanel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup().addComponent(defaultLabel).addComponent(booleanDefault))
				.addGap(0, 0, Short.MAX_VALUE));
		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(defaultLabel)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE).addComponent(booleanDefault));

		booleanDetailsPanel.setVisible(false);
	}

	private void initUnicodeDetailsPanel() {
		isEditable = new JCheckBox(Configed.getResourceValue("CreateConfigDialog.editable"), true);
		isMultiValue = new JCheckBox(Configed.getResourceValue("CreateConfigDialog.multiSelection"));

		// These dialogs are there to 
		defaultValuesSelectionDialog = createSelectionDialog(
				Configed.getResourceValue("CreateConfigDialog.defaultValues"));
		possibleValuesSelectionDialog = createSelectionDialog(
				Configed.getResourceValue("CreateConfigDialog.possibleValues"));
		possibleValuesSelectionDialog.setMultiSelection();

		// These textfields will show currently selected values in the dialog.
		JTextField defaultValuesTextField = createTextFieldAssociated(defaultValuesSelectionDialog);
		JTextField possibleValuesTextField = createTextFieldAssociated(possibleValuesSelectionDialog);

		isMultiValue.addActionListener((ActionEvent actionEvent) -> {
			if (isMultiValue.isSelected()) {
				defaultValuesSelectionDialog.setMultiSelection();
			} else {
				defaultValuesSelectionDialog.setSingleSelection();
				// Update field according to single selection
				defaultValuesTextField.setText(
						PropertiesCellEditorAndRenderer.formatList(defaultValuesSelectionDialog.getSelectedValues()));
			}
		});

		JLabel defaultValuesLabel = new JLabel(Configed.getResourceValue("CreateConfigDialog.defaultValues"));
		defaultValuesLabel.setFont(defaultValuesLabel.getFont().deriveFont(Font.BOLD));

		JLabel possibleValuesLabel = new JLabel(Configed.getResourceValue("CreateConfigDialog.possibleValues"));
		possibleValuesLabel.setFont(possibleValuesLabel.getFont().deriveFont(Font.BOLD));

		unicodeDetailsPanel = new JPanel();
		GroupLayout layout = new GroupLayout(unicodeDetailsPanel);
		unicodeDetailsPanel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(isEditable)
				.addComponent(isMultiValue).addGap(Globals.GAP_SIZE).addComponent(defaultValuesLabel)
				.addComponent(defaultValuesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(possibleValuesLabel).addComponent(possibleValuesTextField,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE));
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(isEditable).addComponent(isMultiValue)
				.addComponent(defaultValuesLabel).addComponent(defaultValuesTextField).addComponent(possibleValuesLabel)
				.addComponent(possibleValuesTextField));
	}

	private JTextField createTextFieldAssociated(ListSelectionDialog selectionListDialog) {
		JTextField jTextField = new JTextField();
		jTextField.setEnabled(false);
		jTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				activateSelection(selectionListDialog, jTextField);
			}
		});

		return jTextField;
	}

	private void activateSelection(ListSelectionDialog listSelectionDialog, JTextField jTextField) {
		// We need to call this before setVisible
		List<String> savedSelectedValues = listSelectionDialog.getSelectedValues();

		listSelectionDialog.show(dialog);

		if (listSelectionDialog.wasAccepted()) {
			jTextField.setText(PropertiesCellEditorAndRenderer.formatList(listSelectionDialog.getSelectedValues()));
		} else {
			DefaultListModel<String> model = new DefaultListModel<>();
			model.addAll(savedSelectedValues);
			listSelectionDialog.setModel(model);
			listSelectionDialog.setPreviousSelectionValues(savedSelectedValues);
		}
	}

	private ListSelectionDialog createSelectionDialog(String title) {
		ListSelectionDialog listSelectionDialog = new ListSelectionDialog(dialog, title, true);
		listSelectionDialog.setModel(new DefaultListModel<>());
		return listSelectionDialog;
	}

	private void initGeneralPanel() {
		JLabel labelConfigEntry = new JLabel(Configed.getResourceValue("EditMapPanelX.configName"));
		labelConfigEntry.setFont(labelConfigEntry.getFont().deriveFont(Font.BOLD));

		int selectionIndex = editMapPanelX.table.getSelectionModel().getMinSelectionIndex();
		String value = selectionIndex >= 0 ? (String) editMapPanelX.table.getValueAt(selectionIndex, 0) : null;
		textFieldConfigEntry = new JTextField(value);
		// Need to call this method inside invokeLater, otherwise it won't work
		SwingUtilities.invokeLater(() -> textFieldConfigEntry.requestFocus());

		JLabel labelDescription = new JLabel(Configed.getResourceValue("description"));
		labelDescription.setFont(labelDescription.getFont().deriveFont(Font.BOLD));

		textFieldDescription = new JTextField();

		isBoolean = new JCheckBox("boolean");
		isBoolean.addActionListener(event -> updateDetailsTab());

		generalPanel = new JPanel();
		GroupLayout layout = new GroupLayout(generalPanel);
		generalPanel.setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(labelConfigEntry)
						.addComponent(textFieldConfigEntry, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(labelDescription).addComponent(textFieldDescription,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(isBoolean));

		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(labelConfigEntry).addComponent(textFieldConfigEntry)
						.addComponent(labelDescription).addComponent(textFieldDescription).addComponent(isBoolean));
	}

	private void initPanel() {
		panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(generalPanel)
				.addComponent(unicodeDetailsPanel).addComponent(booleanDetailsPanel));
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(generalPanel)
				.addComponent(unicodeDetailsPanel).addComponent(booleanDetailsPanel));
	}

	private void updateDetailsTab() {
		booleanDetailsPanel.setVisible(isBoolean.isSelected());
		unicodeDetailsPanel.setVisible(!isBoolean.isSelected());

		dialog.pack();
	}

	private void createConfig() {
		List<?> defaultValues;
		List<?> possibleValues;
		if (isBoolean.isSelected()) {
			defaultValues = new ArrayList<>(Arrays.asList(booleanDefault.isSelected()));
			possibleValues = new ArrayList<>(defaultValues);
		} else {
			defaultValues = new ArrayList<>(defaultValuesSelectionDialog.getSelectedValues());
			possibleValues = new ArrayList<>(possibleValuesSelectionDialog.getSelectedValues());
		}

		if (!editMapPanelX.addEntry(textFieldConfigEntry.getText().strip(), textFieldDescription.getText(),
				isBoolean.isSelected(), isMultiValue.isSelected(), isEditable.isSelected(), defaultValues,
				possibleValues)) {

			JOptionPane.showMessageDialog(dialog,
					Configed.getResourceValue("CreateConfigDialog.couldNotCreate.message") + ": "
							+ textFieldConfigEntry.getText().strip(),
					Configed.getResourceValue("error"), JOptionPane.ERROR_MESSAGE);
			dialog.setVisible(true);
		}
	}
}
