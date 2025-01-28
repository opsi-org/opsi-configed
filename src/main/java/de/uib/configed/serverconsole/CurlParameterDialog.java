/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Font;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandCurl;
import de.uib.configed.serverconsole.command.SingleCommandHelp;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class CurlParameterDialog {
	private JPanel inputPanel = new JPanel();

	private JLabel jLabelURL = new JLabel();
	private JLabel jLabelDir = new JLabel();
	private JLabel jLabelLoglevel = new JLabel();
	private JLabel jLabelFreeInput = new JLabel();

	private JButton jButtonSearchDir;

	private JTextField jTextFieldURL;
	private JComboBox<String> jComboBoxDir;
	private JComboBox<Integer> jComboBoxLoglevel;
	private JTextField jTextFieldFreeInput;

	private SingleCommandCurl commandCurl = new SingleCommandCurl();
	private CompletionComboButton completion;
	private CurlAuthenticationPanel curlAuthPanel;

	private ConfigedMain configedMain;

	private JDialog dialog;

	public CurlParameterDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		this.completion = new CompletionComboButton() {
			@Override
			public void setItems(Set<String> items, final String curdir) {
				super.setItems(items, curdir);
				dialog.pack();
			}
		};

		init();
		initLayout();

		jComboBoxDir.setEnabled(true);

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());

		JButton buttonParameterInfo = new JButton(Configed.getResourceValue("CurlParameterDialog.buttonParameterInfo"));
		buttonParameterInfo.addActionListener(actionEvent -> showParameterInfo());

		JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null, new Object[] { buttonExecute, buttonParameterInfo, Configed.getResourceValue("buttonCancel") });
		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("CurlParameterDialog.title"));
		dialog.setModal(false);
		dialog.setVisible(true);
	}

	private void init() {
		jLabelURL.setText(Configed.getResourceValue("CurlParameterDialog.jLabelUrl"));
		jLabelURL.setFont(jLabelURL.getFont().deriveFont(Font.BOLD));

		jTextFieldURL = new JTextField();

		jLabelDir.setText(Configed.getResourceValue("CurlParameterDialog.jLabelDirectory"));
		jLabelDir.setFont(jLabelDir.getFont().deriveFont(Font.BOLD));

		jComboBoxDir = completion.getCombobox();
		jButtonSearchDir = completion.getButton();

		jLabelLoglevel.setText(Configed.getResourceValue("loglevel"));
		jLabelLoglevel.setFont(jLabelLoglevel.getFont().deriveFont(Font.BOLD));

		jComboBoxLoglevel = new JComboBox<>();
		for (int i = 3; i <= 9; i++) {
			jComboBoxLoglevel.addItem(i);
		}

		jComboBoxLoglevel.setSelectedItem(4);
		jComboBoxLoglevel
				.addItemListener(itemEvent -> commandCurl.setLoglevel((int) jComboBoxLoglevel.getSelectedItem()));

		jLabelFreeInput.setText(Configed.getResourceValue("CurlParameterDialog.jLabelFreeInput"));
		jLabelFreeInput.setFont(jLabelFreeInput.getFont().deriveFont(Font.BOLD));

		jTextFieldFreeInput = new JTextField();
		jTextFieldFreeInput.setToolTipText(Configed.getResourceValue("CurlParameterDialog.jLabelFreeInput.tooltip"));
		jTextFieldFreeInput.getDocument().addDocumentListener(new DocumentListenerAdapter(this::changeFreeInput));

		curlAuthPanel = new CurlAuthenticationPanel();
		curlAuthPanel.getCheckBox().setSelected(false);

		// Add the listener and invoke it later so that it is executed after the panel 
		// is set visible
		curlAuthPanel.getCheckBox().addItemListener(event -> SwingUtilities.invokeLater(() -> dialog.pack()));
		curlAuthPanel.isOpen(true);
		curlAuthPanel.close();

		changeFreeInput();
	}

	private void changeFreeInput() {
		if (!jTextFieldFreeInput.getText().isBlank()) {
			commandCurl.setFreeInput(jTextFieldFreeInput.getText().trim());
		} else {
			commandCurl.setFreeInput("");
		}
	}

	private void execute() {
		Logging.warning(this, "execute");
		if (jTextFieldURL.getText().isBlank()) {
			Logging.warning(this, "Please enter url.");
			return;
		}

		commandCurl.setDir((String) jComboBoxDir.getSelectedItem());
		if (curlAuthPanel.getCheckBox().isSelected()) {
			commandCurl
					.setAuthentication("--insecure -u " + curlAuthPanel.getUser() + ":" + curlAuthPanel.getPassword());
		} else {
			commandCurl.setAuthentication("");
		}

		if (commandCurl.checkCommand()) {
			new Thread() {
				@Override
				public void run() {
					Logging.info(this, "doAction3 wget ");
					CommandExecutor executor = new CommandExecutor(configedMain, commandCurl);
					executor.execute();
				}
			}.start();
		}
	}

	private void showParameterInfo() {
		CommandExecutor executor = new CommandExecutor(configedMain, new SingleCommandHelp(commandCurl));
		executor.execute();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup()
				.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(inputPanelLayout.createSequentialGroup()
						.addComponent(jComboBoxDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(jLabelLoglevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxLoglevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(curlAuthPanel.getCheckBox(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(curlAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(jLabelFreeInput, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldFreeInput, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
				.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelLoglevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxLoglevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(curlAuthPanel.getCheckBox(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(curlAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelFreeInput, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldFreeInput, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	private static class DocumentListenerAdapter implements DocumentListener {
		private Runnable method;

		public DocumentListenerAdapter(Runnable method) {
			this.method = method;
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			method.run();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			method.run();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			method.run();
		}
	}
}
