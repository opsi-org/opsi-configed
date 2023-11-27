/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class SSHWgetParameterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel jLabelURL = new JLabel();
	private JLabel jLabelDir = new JLabel();
	private JLabel jLabelVerbosity = new JLabel();
	private JLabel jLabelFreeInput = new JLabel();
	private JLabel jLabelFullCommand = new JLabel();

	private JButton jButtonHelp;
	private JButton jButtonExecute;
	private JButton jButtonSearchDir;

	private JTextField jTextFieldURL;
	private JTextField jTextFieldDir;
	private JComboBox<String> jComboBoxDir;
	private JComboBox<Integer> jComboBoxVerbosity;
	private JTextField jTextFieldFreeInput;

	private CommandWget commandWget = new CommandWget();
	private SSHCompletionComboButton completion = new SSHCompletionComboButton();
	private SSHWgetAuthenticationPanel wgetAuthPanel;

	public SSHWgetParameterDialog() {
		super(null, Configed.getResourceValue("SSHConnection.ParameterDialog.wget.title"), false);

		wgetAuthPanel = new SSHWgetAuthenticationPanel();
		init();
		initLayout();
		super.setSize(Globals.DIALOG_FRAME_DEFAULT_HEIGHT, Globals.DIALOG_FRAME_DEFAULT_WIDTH);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setVisible(true);
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			setComponentsEnabledRO(false);
		}

		jComboBoxDir.setEnabled(true);
	}

	private void setComponentsEnabledRO(boolean value) {
		jTextFieldURL.setEnabled(value);
		jTextFieldURL.setEditable(value);
		jTextFieldDir.setEnabled(value);
		jTextFieldDir.setEditable(value);
		jComboBoxDir.setEnabled(value);
		jComboBoxDir.setEditable(value);
		jComboBoxVerbosity.setEnabled(value);

		jTextFieldFreeInput.setEnabled(value);
		jTextFieldFreeInput.setEditable(value);

		jButtonExecute.setEnabled(value);
		jButtonHelp.setEnabled(value);
	}

	private void init() {
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setPreferredSize(new Dimension(376, 220));

		jLabelURL.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelUrl"));
		jTextFieldURL = new JTextField();
		jTextFieldURL.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url"));
		jTextFieldURL.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				changeUrl();
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				changeUrl();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				changeUrl();
			}
		});
		jTextFieldURL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (jTextFieldURL.getText()
						.equals(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url"))) {
					jTextFieldURL.setSelectionStart(0);
					jTextFieldURL.setSelectionEnd(jTextFieldURL.getText().length());
				}
			}
		});

		jLabelDir.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelDirectory"));
		jTextFieldDir = new JTextField();

		jComboBoxDir = completion.getCombobox();
		jButtonSearchDir = completion.getButton();

		jLabelVerbosity.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		jComboBoxVerbosity = new JComboBox<>();
		jComboBoxVerbosity.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
		for (int i = 0; i < 5; i++) {
			jComboBoxVerbosity.addItem(i);
		}

		jComboBoxVerbosity.setSelectedItem(1);
		jComboBoxVerbosity.addItemListener((ItemEvent itemEvent) -> {
			commandWget.setVerbosity((int) jComboBoxVerbosity.getSelectedItem());
			updateCommand();
		});

		jLabelFreeInput.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
		jTextFieldFreeInput = new JTextField();
		jTextFieldFreeInput
				.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.freeInput"));
		jTextFieldFreeInput.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				changeFreeInput();
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				changeFreeInput();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				changeFreeInput();
			}
		});

		((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).setSelected(false);
		wgetAuthPanel.isOpen = true;
		wgetAuthPanel.close();
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 67, Globals.BUTTON_HEIGHT);

		jButtonHelp = new JButton(Configed.getResourceValue("SSHConnection.buttonParameterInfo"));
		jButtonHelp.setToolTipText(Configed.getResourceValue("SSHConnection.buttonParameterInfo.tooltip"));
		jButtonHelp.addActionListener(actionEvent -> doActionHelp());

		jButtonExecute = new JButton(Configed.getResourceValue("SSHConnection.buttonExec"));

		jButtonExecute.addActionListener((ActionEvent actionEvent) -> {
			if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
					.isGlobalReadOnly()) {
				doAction3();
			}
		});

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));

		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonHelp);
		buttonPanel.add(jButtonExecute);

		jLabelFullCommand.setText("wget ");

		changeUrl();
		changeFreeInput();
	}

	private void updateCommand() {
		jLabelFullCommand.setText(commandWget.getCommand());
	}

	private void changeFreeInput() {
		if (!jTextFieldFreeInput.getText().trim().isEmpty()) {
			commandWget.setFreeInput(jTextFieldFreeInput.getText().trim());
		} else {
			commandWget.setFreeInput("");
		}

		updateCommand();
	}

	private void changeUrl() {
		if (!(jTextFieldURL.getText().isEmpty())) {
			commandWget.setUrl(jTextFieldURL.getText().trim());
		} else {
			commandWget.setUrl("");
		}

		updateCommand();
	}

	@Override
	public void doAction3() {
		if (jTextFieldURL.getText()
				.equals(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url"))
				|| jTextFieldURL.getText().isEmpty()) {
			Logging.warning(this, "Please enter url.");
			return;
		}

		commandWget.setDir((String) jComboBoxDir.getSelectedItem());
		if (((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
			commandWget.setAuthentication(" --no-check-certificate --user=" + wgetAuthPanel.getUser() + " --password="
					+ wgetAuthPanel.getPw() + " ");
		} else {
			commandWget.setAuthentication(" ");
		}

		updateCommand();

		if (commandWget.checkCommand()) {
			new Thread() {
				@Override
				public void run() {
					Logging.info(this, "doAction3 wget ");
					new SSHConnectExec(commandWget, jButtonExecute);
				}
			}.start();
		}
	}

	private void doActionHelp() {
		SSHConnectionExecDialog dia = commandWget.startHelpDialog();
		dia.setVisible(true);
	}

	private void cancel() {
		super.doAction1();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addGroup(inputPanelLayout.createSequentialGroup()
								.addGroup(inputPanelLayout.createParallelGroup()
										.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelVerbosity, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(
												wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelFreeInput, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(inputPanelLayout.createParallelGroup()
										.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jTextFieldURL,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE))
										.addGroup(inputPanelLayout.createSequentialGroup()
												.addComponent(jComboBoxDir, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
														Short.MAX_VALUE)
												.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
										.addComponent(jComboBoxVerbosity, GroupLayout.Alignment.LEADING,
												Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
										.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
												GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jTextFieldFreeInput, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)))
						.addComponent(wgetAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jTextFieldURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jLabelURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonSearchDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jLabelDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxVerbosity, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jLabelVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH), Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
								GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addComponent(wgetAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jTextFieldFreeInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jLabelFreeInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE).addContainerGap(70, 70));
	}
}
