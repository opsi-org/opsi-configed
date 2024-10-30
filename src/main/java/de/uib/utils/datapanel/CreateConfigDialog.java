/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.datapanel;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utils.Icons;

public class CreateConfigDialog extends JDialog {
	private EditMapPanelX editMapPanelX;

	public CreateConfigDialog(EditMapPanelX editMapPanelX) {
		super(ConfigedMain.getMainFrame(), true);
		super.setTitle(Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntry"));

		this.editMapPanelX = editMapPanelX;

		init();
	}

	private void init() {
		JLabel labelConfigEntry = new JLabel(Configed.getResourceValue("EditMapPanelX.configName"));
		labelConfigEntry.setFont(getFont().deriveFont(Font.BOLD));

		JTextField textFieldConfigEntry = new JTextField();

		JLabel labelDescription = new JLabel(Configed.getResourceValue("EditMapPanelX.description"));
		labelDescription.setFont(getFont().deriveFont(Font.BOLD));

		JTextField textFieldDescription = new JTextField();

		JCheckBox isBoolean = new JCheckBox("boolean");
		JCheckBox isEditable = new JCheckBox("editable", true);
		JCheckBox isMultiValue = new JCheckBox("multivalue");

		isBoolean.addActionListener((ActionEvent event) -> {
			isEditable.setEnabled(!isBoolean.isSelected());
			isEditable.setSelected(!isBoolean.isSelected());

			isMultiValue.setEnabled(!isBoolean.isSelected());
			isMultiValue.setSelected(false);
		});

		JButton cancel = new JButton(Icons.getIntellijIcon("close"));
		cancel.addActionListener(actionEvent -> dispose());

		JButton accept = new JButton(Icons.getIntellijIcon("checkmark"));
		accept.setEnabled(false);
		accept.addActionListener((ActionEvent e) -> {
			editMapPanelX.addEntry(textFieldConfigEntry.getText().strip(), textFieldDescription.getText(),
					isBoolean.isSelected(), isMultiValue.isSelected(), isEditable.isSelected());
			dispose();
		});

		// We only want the accept button to be active, when the input for config name is not blank
		textFieldConfigEntry.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				// Not needed here
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				accept.setEnabled(!textFieldConfigEntry.getText().isBlank());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				accept.setEnabled(!textFieldConfigEntry.getText().isBlank());
			}
		});

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(labelConfigEntry)
				.addComponent(textFieldConfigEntry).addGap(Globals.MIN_GAP_SIZE * 2).addComponent(labelDescription)
				.addComponent(textFieldDescription).addGap(Globals.MIN_GAP_SIZE).addComponent(isBoolean)
				.addComponent(isEditable).addComponent(isMultiValue).addGap(Globals.MIN_GAP_SIZE * 4)
				.addGroup(layout.createParallelGroup().addComponent(cancel).addComponent(accept)));

		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(labelConfigEntry).addComponent(textFieldConfigEntry)
						.addComponent(labelDescription).addComponent(textFieldDescription).addComponent(isBoolean)
						.addComponent(isEditable).addComponent(isMultiValue).addGroup(layout.createSequentialGroup()
								.addComponent(cancel).addGap(0, 0, Short.MAX_VALUE).addComponent(accept)));

		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(Globals.MIN_GAP_SIZE * 2,
				Globals.MIN_GAP_SIZE * 2, Globals.MIN_GAP_SIZE * 2, Globals.MIN_GAP_SIZE * 2));

		pack();
		setLocationRelativeTo(ConfigedMain.getMainFrame());
		setResizable(false);
		setVisible(true);
	}
}
