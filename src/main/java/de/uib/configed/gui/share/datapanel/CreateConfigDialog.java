/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.table.gui.PropertiesCellEditorAndRenderer;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class CreateConfigDialog {
	private EditMapPanelX editMapPanelX;

	private JTextField textFieldConfigEntry;
	private JTextField textFieldDescription;

	private JRadioButton isBooleanTrue;

	private ListSelectionDialog valuesSelectionDialog;

	JRadioButton booleanButton;
	JRadioButton unicodeButton;

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
		JLabel defaultLabel = SwingUtils.createBoldLabel("CreateConfigDialog.defaultValue");

		isBooleanTrue = new JRadioButton("true", true);
		JRadioButton isBooleanFalse = new JRadioButton("false", false);

		ButtonGroup booleanGroup = new ButtonGroup();
		booleanGroup.add(isBooleanTrue);
		booleanGroup.add(isBooleanFalse);

		booleanDetailsPanel = new JPanel();
		booleanDetailsPanel.setLayout(new MigLayout("insets 0, wrap 1", "", "[]0"));

		booleanDetailsPanel.add(defaultLabel);
		booleanDetailsPanel.add(isBooleanTrue);
		booleanDetailsPanel.add(isBooleanFalse);

		booleanDetailsPanel.setVisible(false);
	}

	private void initUnicodeDetailsPanel() {
		JLabel propertiesLabel = SwingUtils.createBoldLabel("CreateConfigDialog.properties");
		isEditable = new JCheckBox(Configed.getResourceValue("CreateConfigDialog.editable"), true);
		isMultiValue = new JCheckBox(Configed.getResourceValue("CreateConfigDialog.multiSelection"));

		valuesSelectionDialog = createSelectionDialog(Configed.getResourceValue("CreateConfigDialog.values"));
		updateSelectionModeForDefaultValuesSelectionDialog();

		JTextField defaultValuesTextField = createDisabledTextField();
		JTextField possibleValuesTextField = createDisabledTextField();
		MouseAdapter selectionMouseHandler = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				activateSelection(valuesSelectionDialog, defaultValuesTextField, possibleValuesTextField);
			}
		};
		defaultValuesTextField.addMouseListener(selectionMouseHandler);
		possibleValuesTextField.addMouseListener(selectionMouseHandler);

		isMultiValue.addActionListener(actionEvent -> updateSelectionModeForDefaultValuesSelectionDialog());

		JLabel defaultValuesLabel = SwingUtils.createBoldLabel("CreateConfigDialog.defaultValues");
		JLabel possibleValuesLabel = SwingUtils.createBoldLabel("CreateConfigDialog.possibleValues");

		unicodeDetailsPanel = new JPanel();
		unicodeDetailsPanel.setLayout(new MigLayout("insets 0, fill, wrap 1", "", "[]0"));

		unicodeDetailsPanel.add(propertiesLabel);
		unicodeDetailsPanel.add(isEditable);
		unicodeDetailsPanel.add(isMultiValue, "gapbottom " + Globals.GAP_SIZE);

		unicodeDetailsPanel.add(defaultValuesLabel);
		unicodeDetailsPanel.add(defaultValuesTextField, "grow, gapbottom " + Globals.GAP_SIZE);

		unicodeDetailsPanel.add(possibleValuesLabel);
		unicodeDetailsPanel.add(possibleValuesTextField, "grow");
	}

	private void updateSelectionModeForDefaultValuesSelectionDialog() {
		if (isMultiValue.isSelected()) {
			valuesSelectionDialog.setMultiSelection();
		} else {
			valuesSelectionDialog.setSingleSelection();
		}
	}

	private static JTextField createDisabledTextField() {
		JTextField jTextField = new JTextField();
		jTextField.setEnabled(false);
		return jTextField;
	}

	private void activateSelection(ListSelectionDialog listSelectionDialog, JTextField defaultValuesTextField,
			JTextField possibleValuesTextField) {
		// We need to call this before setVisible
		List<String> savedSelectedValues = listSelectionDialog.getSelectedValues();

		listSelectionDialog.show(dialog);

		if (listSelectionDialog.wasAccepted()) {
			defaultValuesTextField
					.setText(PropertiesCellEditorAndRenderer.formatList(listSelectionDialog.getSelectedValues()));
			possibleValuesTextField
					.setText(PropertiesCellEditorAndRenderer.formatList(listSelectionDialog.getValues()));
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
		JLabel labelConfigEntry = SwingUtils.createBoldLabel("EditMapPanelX.configName");

		int selectionIndex = editMapPanelX.table.getSelectionModel().getMinSelectionIndex();
		String value = selectionIndex >= 0 ? (String) editMapPanelX.table.getValueAt(selectionIndex, 0) : null;
		textFieldConfigEntry = new JTextField(value);
		// Need to call this method inside invokeLater, otherwise it won't work
		SwingUtilities.invokeLater(() -> textFieldConfigEntry.requestFocus());

		JLabel labelDescription = SwingUtils.createBoldLabel("description");

		textFieldDescription = new JTextField();

		JLabel labelDataType = SwingUtils.createBoldLabel("CreateConfigDialog.dataType");

		booleanButton = new JRadioButton("boolean", false);
		unicodeButton = new JRadioButton("unicode", true);

		// We add it to both of them, so that the details tab is updated
		// when the user clicks on either of them.
		booleanButton.addActionListener(event -> updateDetailsTab());
		unicodeButton.addActionListener(event -> updateDetailsTab());

		// Create a group so that only one can be selected
		ButtonGroup booleanGroup = new ButtonGroup();
		booleanGroup.add(booleanButton);
		booleanGroup.add(unicodeButton);

		generalPanel = new JPanel();
		generalPanel.setLayout(new MigLayout("insets 0, fill, wrap 1", "", "[]0"));

		generalPanel.add(labelConfigEntry);
		generalPanel.add(textFieldConfigEntry, "grow, gapbottom " + Globals.GAP_SIZE);

		generalPanel.add(labelDescription);
		generalPanel.add(textFieldDescription, "grow, gapbottom " + Globals.GAP_SIZE);

		generalPanel.add(labelDataType);
		generalPanel.add(booleanButton);
		generalPanel.add(unicodeButton);
	}

	private void initPanel() {
		panel = new JPanel();

		panel.setLayout(new MigLayout("insets 0, fillx, wrap 1", "", "[]0"));

		panel.add(generalPanel, "growx, gapbottom " + Globals.GAP_SIZE);
		panel.add(unicodeDetailsPanel, "hidemode 3, growx");
		panel.add(booleanDetailsPanel, "hidemode 3, growx");
	}

	private void updateDetailsTab() {
		booleanDetailsPanel.setVisible(booleanButton.isSelected());
		unicodeDetailsPanel.setVisible(!booleanButton.isSelected());

		dialog.pack();
	}

	private void createConfig() {
		List<?> defaultValues;
		List<?> possibleValues;
		if (booleanButton.isSelected()) {
			defaultValues = new ArrayList<>(List.of(isBooleanTrue.isSelected()));
			possibleValues = new ArrayList<>(defaultValues);
		} else {
			defaultValues = new ArrayList<>(valuesSelectionDialog.getSelectedValues());
			possibleValues = new ArrayList<>(valuesSelectionDialog.getValues());
		}

		if (!editMapPanelX.addEntry(textFieldConfigEntry.getText().strip(), textFieldDescription.getText(),
				booleanButton.isSelected(), isMultiValue.isSelected(), isEditable.isSelected(), defaultValues,
				possibleValues)) {
			JOptionPane.showMessageDialog(dialog,
					Configed.getResourceValue("CreateConfigDialog.couldNotCreate.message") + ": "
							+ textFieldConfigEntry.getText().strip(),
					Configed.getResourceValue("error"), JOptionPane.ERROR_MESSAGE);
			dialog.setVisible(true);
		}
	}
}
