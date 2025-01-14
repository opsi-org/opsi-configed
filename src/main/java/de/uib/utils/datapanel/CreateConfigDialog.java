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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FSelectionList;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.PropertiesCellEditorAndRenderer;

public class CreateConfigDialog {
	private EditMapPanelX editMapPanelX;

	private JTextField textFieldConfigEntry;
	private JTextField textFieldDescription;

	private JCheckBox booleanDefault;

	private FSelectionList defaultValuesSelectionDialog;
	private FSelectionList possibleValuesSelectionDialog;

	private JCheckBox isBoolean;
	private JCheckBox isEditable;
	private JCheckBox isMultiValue;

	private JTabbedPane jTabbedPane;

	private JPanel generalPanel;
	private JPanel booleanDetailsPanel;
	private JPanel unicodeDetailsPanel;

	private JDialog dialog;

	public CreateConfigDialog(EditMapPanelX editMapPanelX) {
		this.editMapPanelX = editMapPanelX;

		initGeneralPanel();
		initBooleanDetailsPanel();
		initUnicodeDetailsPanel();

		initPanel();

		JOptionPane pane = new JOptionPane(jTabbedPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
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
				.addComponent(isMultiValue).addGap(Globals.MIN_GAP_SIZE).addComponent(defaultValuesLabel)
				.addComponent(defaultValuesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(possibleValuesLabel).addComponent(possibleValuesTextField,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE));
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(isEditable).addComponent(isMultiValue)
				.addComponent(defaultValuesLabel).addComponent(defaultValuesTextField).addComponent(possibleValuesLabel)
				.addComponent(possibleValuesTextField));
	}

	private JTextField createTextFieldAssociated(FSelectionList fSelectionList) {
		JTextField jTextField = new JTextField();
		jTextField.setEnabled(false);
		jTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				activateSelection(fSelectionList, jTextField);
			}
		});

		return jTextField;
	}

	private void activateSelection(FSelectionList fSelectionList, JTextField jTextField) {
		fSelectionList.setSize(300, 400);
		fSelectionList.setLocationRelativeTo(dialog);

		// We need to call this before setVisible
		List<String> savedSelectedValues = fSelectionList.getSelectedValues();

		fSelectionList.setVisible(true);

		if (fSelectionList.getResult() == 2) {
			jTextField.setText(PropertiesCellEditorAndRenderer.formatList(fSelectionList.getSelectedValues()));
		} else {
			DefaultListModel<String> model = new DefaultListModel<>();
			model.addAll(savedSelectedValues);
			fSelectionList.setModel(model);
			fSelectionList.setPreviousSelectionValues(savedSelectedValues);
		}
	}

	private static FSelectionList createSelectionDialog(String title) {
		FlatTextField addValuesTextField = new FlatTextField();

		FSelectionList fSelectionList = new FSelectionList(ConfigedMain.getMainFrame(), title, addValuesTextField);
		fSelectionList.setModel(new DefaultListModel<>());

		JButton addValueButton = new JButton(Icons.getIntellijIcon("add"));
		addValueButton.addActionListener(actionEvent -> fSelectionList.addItem(addValuesTextField.getText()));

		addValuesTextField.setTrailingComponent(addValueButton);
		addValuesTextField.setShowClearButton(true);
		addValuesTextField.addActionListener(actionEvent -> fSelectionList.addItem(addValuesTextField.getText()));

		return fSelectionList;
	}

	private void initGeneralPanel() {
		JLabel labelConfigEntry = new JLabel(Configed.getResourceValue("EditMapPanelX.configName"));
		labelConfigEntry.setFont(labelConfigEntry.getFont().deriveFont(Font.BOLD));

		textFieldConfigEntry = new JTextField();
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

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelConfigEntry)
				.addComponent(textFieldConfigEntry, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE * 2).addComponent(labelDescription).addComponent(textFieldDescription,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(isBoolean));

		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(labelConfigEntry).addComponent(textFieldConfigEntry)
						.addComponent(labelDescription).addComponent(textFieldDescription).addComponent(isBoolean));
	}

	private void initPanel() {
		jTabbedPane = new JTabbedPane();
		jTabbedPane.putClientProperty("JTabbedPane.tabAreaAlignment", "fill");
		jTabbedPane.putClientProperty("JTabbedPane.tabWidthMode", "equal");
		jTabbedPane.addTab(Configed.getResourceValue("CreateConfigDialog.generalOptions"), generalPanel);
		jTabbedPane.addTab(Configed.getResourceValue("CreateConfigDialog.details"), unicodeDetailsPanel);
	}

	private void updateDetailsTab() {
		jTabbedPane.setComponentAt(1, isBoolean.isSelected() ? booleanDetailsPanel : unicodeDetailsPanel);
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
