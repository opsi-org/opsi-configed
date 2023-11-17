/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditStringList;

public class FDialogRemoteControl extends FEditStringList {
	private Map<String, String> meanings;
	private Map<String, Boolean> editableFields;
	private String selText;

	private ConfigedMain configedMain;

	public FDialogRemoteControl(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		loggingPanel.setVisible(true);
	}

	public void setMeanings(Map<String, String> meanings) {
		this.meanings = meanings;
	}

	public void setEditableFields(Map<String, Boolean> editable) {
		this.editableFields = editable;
	}

	protected String getValue(String key) {
		return meanings.get(key);
	}

	@Override
	protected void initComponents() {
		super.initComponents();

		buttonCommit.createIconButton(Configed.getResourceValue("FDialogRemoteControl.SaveButtonTooltip"),
				"images/executing_command_red_22.png", "images/executing_command_red_22_over.png",
				"images/executing_command_22_disabled.png", true);

		buttonCancel.createIconButton(Configed.getResourceValue("buttonCancel"), "images/cancel.png",
				"images/cancel_over.png", "images/cancel_disabled.png", true);

		extraField.getDocument().addDocumentListener(this);
	}

	private void noText() {
		extraField.setEditable(false);
		extraField.setEnabled(false);
		extraField.setText("");
		selText = null;
	}

	private void checkSelected() {
		if (visibleList.getSelectedValue() != null && selValue != null && !"".equals(selValue)) {
			setDataChanged(true);
		} else {
			setDataChanged(false);
			noText();
		}
	}

	public void resetValue() {
		visibleList.setSelectedValue(selValue, true);
		checkSelected();
	}

	protected void appendLog(final String s) {
		SwingUtilities.invokeLater(() -> {
			if (s == null) {
				loggingArea.setText("");
			} else {
				loggingArea.append(s);
				loggingArea.setCaretPosition(loggingArea.getText().length());
			}
		});
	}

	@Override
	public void commit() {
		super.commit();
		setVisible(true);

		Logging.debug(this, "getSelectedValue " + getSelectedList());

		appendLog(null);

		if (!getSelectedList().isEmpty()) {
			final String command = "" + getSelectedList().get(0);
			executeCommand(command, configedMain.getSelectedClients());
		}
	}

	private void executeCommand(String command, String[] targetClients) {
		RemoteCommandExecutor remoteCommandExecutor = new RemoteCommandExecutor(this, command, targetClients);
		remoteCommandExecutor.execute();
	}

	@Override
	protected void createComponents() {
		super.createComponents();
		extraField.setVisible(true);
		extraField.addActionListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		checkSelected();

		if (e.getClickCount() > 1) {
			commit();
		}
	}

	// interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);

		// Ignore extra messages.
		if (e.getValueIsAdjusting()) {
			return;
		}

		checkSelected();

		if (visibleList.getSelectedValue() != null) {
			selValue = visibleList.getSelectedValue();
		}

		selText = "" + selValue;

		Logging.debug(this, "valueChanged, selText " + selText);
		Logging.debug(this, "valueChanged, meanings.get(selText) " + meanings.get(selText));

		if (meanings != null && selText != null && meanings.get(selText) != null) {
			extraField.setText(meanings.get(selText));
			extraField.setEditable(editableFields.get(selText));
			extraField.setEnabled(editableFields.get(selText));
		}
	}

	private void saveEditedText() {
		if (extraField.isEditable() && selText != null && !selText.isEmpty() && meanings.get(selText) != null) {
			meanings.put(selText, extraField.getText());
		}
	}

	// DocumentListener

	@Override
	public void changedUpdate(DocumentEvent e) {
		saveEditedText();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		saveEditedText();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		saveEditedText();
	}

	// interface ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == extraField) {
			commit();
		}
	}
}
