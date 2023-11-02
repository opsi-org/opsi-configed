/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.script.Interpreter;
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

	private String getValue(String key) {
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

	private void appendLog(final String s) {
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
			final String firstSelectedClient = "" + getSelectedList().get(0);

			for (int j = 0; j < configedMain.getSelectedClients().length; j++) {
				final String targetClient = configedMain.getSelectedClients()[j];

				new Thread() {
					@Override
					public void run() {
						executeCommand(firstSelectedClient, targetClient);
					}
				}.start();
			}
		}
	}

	private void executeCommand(String firstSelectedClient, String targetClient) {
		String cmd = getValue(firstSelectedClient);

		Interpreter trans = new Interpreter(new String[] { "%host%", "%hostname%", "%ipaddress%", "%inventorynumber%",
				"%hardwareaddress%", "%opsihostkey%", "%depotid%", "%configserverid%" });

		trans.setCommand(cmd);

		Map<String, String> values = new HashMap<>();
		values.put("%host%", targetClient);
		String hostName = targetClient;
		Logging.info(this, " targetClient " + targetClient);
		if (targetClient.contains(".")) {
			String[] parts = targetClient.split("\\.");
			Logging.info(this, " targetClient " + Arrays.toString(parts));
			hostName = parts[0];
		}

		values.put("%hostname%", hostName);

		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();

		HostInfo pcInfo = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().get(targetClient);
		values.put("%ipaddress%", pcInfo.getIpAddress());
		values.put("%hardwareaddress%", pcInfo.getMacAddress());
		values.put("%inventorynumber%", pcInfo.getInventoryNumber());
		values.put("%opsihostkey%", pcInfo.getHostKey());
		values.put("%depotid%", pcInfo.getInDepot());
		values.put("%configserverid%", persistenceController.getHostInfoCollections().getConfigServer());

		trans.setValues(values);

		cmd = trans.interpret();

		List<String> parts = Interpreter.splitToList(cmd);

		try {
			Logging.debug(this, "startRemoteControlForSelectedClients, cmd: " + cmd + " splitted to " + parts);

			ProcessBuilder pb = new ProcessBuilder(parts);
			pb.redirectErrorStream(true);

			Process proc = pb.start();

			BufferedReader br = new BufferedReader(
					new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));

			String line = null;
			while ((line = br.readLine()) != null) {
				appendLog(firstSelectedClient + " on " + targetClient + " >" + line + "\n");
			}
		} catch (IOException ex) {
			Logging.error("Runtime error for command >>" + cmd + "<<, : " + ex, ex);
		}
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
