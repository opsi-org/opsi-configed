package de.uib.configed.gui;

import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;

import de.uib.utilities.logging.logging;

public class FDialogRemoteControl extends de.uib.utilities.swing.FEditList {
	Map<String, String> meanings;
	Map<String, Boolean> editable;
	String selText;

	public FDialogRemoteControl() {

		loggingPanel.setVisible(true);
	}

	public void setMeanings(Map<String, String> meanings) {
		this.meanings = meanings;
	}

	public void setEditable(Map<String, Boolean> editable) {
		this.editable = editable;
	}

	public String getValue(String key) {
		return meanings.get(key);
	}

	// hack to modify settings from superclass
	@Override
	protected void initExtraField() {
		checkSelected();
	}

	@Override
	protected void initComponents() {
		super.initComponents();

		buttonCommit.createIconButton(
				de.uib.configed.configed.getResourceValue("FDialogRemoteControl.SaveButtonTooltip"),
				"images/executing_command_red_22.png", "images/executing_command_red_22_over.png",
				"images/executing_command_22_disabled.png", true);

		buttonCancel.createIconButton(
				de.uib.configed.configed.getResourceValue("FDialogRemoteControl.CancelButtonTooltip"),
				"images/cancel.png", "images/cancel_over.png", "images/cancel_disabled.png", true);

		extraField.getDocument().addDocumentListener(this);

	}

	private void noText() {
		extraField.setEditable(false);
		extraField.setEnabled(false);
		extraField.setText("");
		selText = null;
	}

	private void checkSelected() {
		if (visibleList.getSelectedValue() != null && selValue != null && !selValue.equals("")) {
			setDataChanged(true);
		}

		else {
			setDataChanged(false);
			noText();
		}
	}

	public void resetValue() {
		visibleList.setSelectedValue(selValue, true);
		checkSelected();
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

		if (e.getClickCount() > 1)
			commit();

	}

	// ======================
	// interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);

		// Ignore extra messages.
		if (e.getValueIsAdjusting())
			return;

		checkSelected();

		if (visibleList.getSelectedValue() != null)
			selValue = visibleList.getSelectedValue();

		selText = "" + selValue;

		logging.debug(this, "valueChanged, selText " + selText);
		logging.debug(this, "valueChanged, meanings.get(selText) " + meanings.get(selText));

		if (meanings != null && selText != null && meanings.get(selText) != null) {
			extraField.setText(meanings.get(selText));
			extraField.setEditable(editable.get(selText));
			extraField.setEnabled(editable.get(selText));
		}
	}
	// ======================

	private void saveEditedText() {
		if (extraField.isEditable() && selText != null && !selText.equals("") && meanings.get(selText) != null) {
			meanings.put(selText, extraField.getText());
		}
	}

	// DocumentListener
	// ======================
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
	// ======================

	// ======================
	// interface ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == extraField)
			commit();
	}

}
