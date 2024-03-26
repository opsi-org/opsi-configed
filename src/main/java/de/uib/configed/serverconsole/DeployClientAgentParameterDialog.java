/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandDeployClientAgent;
import de.uib.configed.serverconsole.command.SingleCommandDeployClientAgent.FinalActionType;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PanelStateSwitch;
import utils.Utils;

public class DeployClientAgentParameterDialog extends FGeneralDialog {
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 500;

	private enum OS {
		WINDOWS("Windows"), LINUX("Linux"), MACOS("MacOS");

		private String displayName;

		OS(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel winAuthPanel = new JPanel();

	private JLabel jLabelClient = new JLabel();
	private JLabel jLabelUser = new JLabel();
	private JLabel jLabelPassword = new JLabel();
	private JLabel jLabelUserData = new JLabel();
	private JLabel jLabelVerbosity = new JLabel();
	private JLabel jLabelFullCommand = new JLabel();
	private JLabel jLabelIgnorePing = new JLabel();
	private JLabel jLabelFinalize = new JLabel();
	private JLabel jLabelOperatingSystem = new JLabel();

	private JButton jButtonExecute;
	private JButton jButtonCopySelectedClients;

	private JTextField jTextFieldClient;
	private JTextField jTextFieldUser;
	private JPasswordField jTextFieldPassword;

	private FinalActionType finalAction;
	private PanelStateSwitch<FinalActionType> panelFinalAction;

	private JButton jButtonShowPassword;
	private JCheckBox jCheckBoxIgnorePing;
	private JComboBox<Integer> jCheckBoxVerbosity;
	private JComboBox<String> jComboBoxOperatingSystem;

	private String defaultWinUser = "";

	private SingleCommandDeployClientAgent commandDeployClientAgent = new SingleCommandDeployClientAgent();
	private ConfigedMain configedMain;

	private boolean aktive;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public DeployClientAgentParameterDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.title"), false);
		this.configedMain = configedMain;
		getDefaultAuthData();

		init();
		super.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());

		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setVisible(true);

		Logging.info(this.getClass(), "SSHDeployClientAgentParameterDialog build");

		setComponentsEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());
	}

	private void getDefaultAuthData() {
		Map<String, Object> configs = persistenceController.getConfigDataService()
				.getHostConfig(persistenceController.getHostInfoCollections().getConfigServer());

		List<Object> resultConfigList = (List<Object>) configs
				.get(OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINUSER);
		if (resultConfigList == null || resultConfigList.isEmpty()) {
			Logging.info(this, "KEY_SSH_DEFAULTWINUSER not existing");

			// the config will be created in this run of configed
		} else {
			defaultWinUser = (String) resultConfigList.get(0);
			Logging.info(this, "KEY_SSH_DEFAULTWINUSER " + ((String) resultConfigList.get(0)));
		}

		resultConfigList = (List<Object>) configs.get(OpsiServiceNOMPersistenceController.KEY_SSH_DEFAULTWINPW);
		if (resultConfigList == null || resultConfigList.isEmpty()) {
			Logging.info(this, "KEY_SSH_DEFAULTWINPW not existing");

			// the config will be created in this run of configed
		} else {
			if (jTextFieldPassword == null) {
				jTextFieldPassword = new JPasswordField("", 15);
				jTextFieldPassword.setEchoChar('*');
			}
			jTextFieldPassword.setText((String) resultConfigList.get(0));
			Logging.info(this, "key_ssh_shell_active " + SSHCommandFactory.CONFIDENTIAL);
		}
	}

	private void setComponentsEnabled(boolean value) {
		jTextFieldClient.setEnabled(value);
		jTextFieldClient.setEditable(value);

		jTextFieldUser.setEnabled(value);
		jTextFieldUser.setEditable(value);

		jTextFieldPassword.setEnabled(value);
		jTextFieldPassword.setEditable(value);

		jButtonShowPassword.setEnabled(value);

		jCheckBoxVerbosity.setEnabled(value);
		jCheckBoxVerbosity.setEditable(value);

		jButtonExecute.setEnabled(value);

		jCheckBoxIgnorePing.setEnabled(value);
	}

	private void init() {
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		winAuthPanel.setBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 2, true));
		inputPanel.setPreferredSize(new Dimension(376, 220));

		jCheckBoxIgnorePing = new JCheckBox("", !commandDeployClientAgent.isPingRequired());
		jLabelIgnorePing
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.ignorePing"));
		jCheckBoxIgnorePing.addItemListener((ItemEvent itemEvent) -> {
			commandDeployClientAgent.togglePingIsRequired();
			updateCommand();
		});

		jLabelVerbosity.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		jCheckBoxVerbosity = new JComboBox<>();
		jCheckBoxVerbosity.setToolTipText(Configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
		for (int i = 0; i < 5; i++) {
			jCheckBoxVerbosity.addItem(i);
		}

		jCheckBoxVerbosity.setSelectedItem(1);
		jCheckBoxVerbosity.addItemListener(itemEvent -> changeVerbosity());

		jLabelClient
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelClient"));
		jTextFieldClient = new JTextField();
		jTextFieldClient.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.tooltip.tf_client"));
		jTextFieldClient.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				changeClient();
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				changeClient();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				changeClient();
			}
		});

		jLabelUser.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelUser"));
		jTextFieldUser = new JTextField(defaultWinUser);
		jTextFieldUser.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.tooltip.tf_user"));
		jTextFieldUser.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				changeUser();
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				changeUser();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				changeUser();
			}
		});

		jLabelPassword
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelPassword"));
		jTextFieldPassword = new JPasswordField("nt123", 15);
		jTextFieldPassword.setEchoChar('*');

		jButtonShowPassword = new JButton(Utils.createImageIcon("images/eye_blue_open.png", ""));

		jButtonShowPassword.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));
		jButtonShowPassword.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.showPassword.tooltip"));
		jButtonShowPassword.addActionListener(actionEvent -> changeEchoChar());

		jTextFieldPassword.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				changePassw();
			}

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				changePassw();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				changePassw();
			}
		});

		jLabelUserData.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.targetclient_authentication"));
		jLabelFinalize
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize"));

		panelFinalAction = new PanelStateSwitch<>(null, FinalActionType.START_OCD, FinalActionType.values(),
				new String[] {
						Configed.getResourceValue(
								"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.START_OCD"),
						Configed.getResourceValue(
								"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.REBOOT"),
						Configed.getResourceValue(
								"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.SHUTDOWN") },
				FinalActionType.class, ((Enum<FinalActionType> val) -> {
					Logging.info(this, "change to " + val);
					finalAction = (FinalActionType) val;
				}));

		jLabelOperatingSystem
				.setText(Configed.getResourceValue("SSHDeployClientAgentParameterDialog.opsiClientAgent.label"));
		jComboBoxOperatingSystem = new JComboBox<>(
				new String[] { OS.WINDOWS.toString(), OS.LINUX.toString(), OS.MACOS.toString() });
		jComboBoxOperatingSystem.setToolTipText(
				Configed.getResourceValue("SSHDeployClientAgentParameterDialog.opsiClientAgent.toolTip"));

		jButtonCopySelectedClients = new JButton(Configed
				.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.btn_copy_selected_clients"));

		jButtonCopySelectedClients.addActionListener(actionEvent -> doCopySelectedClients());

		jButtonExecute = new JButton(Configed.getResourceValue("SSHConnection.buttonExec"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonExecute.addActionListener(actionEvent -> doAction2());
		}

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));
		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonExecute);

		jLabelFullCommand.setText("opsi-deploy-client-agent ");
		updateCommand();

		doCopySelectedClients();

		changeClient();
		changeUser();
		changePassw();

		changeVerbosity();

		initLayout();
	}

	private void updateCommand() {
		jLabelFullCommand.setText(commandDeployClientAgent.getCommand());
	}

	private void changeVerbosity() {
		commandDeployClientAgent.setVerbosity((int) jCheckBoxVerbosity.getSelectedItem());
	}

	private void changeClient() {
		commandDeployClientAgent.setClient(jTextFieldClient.getText().trim());
		updateCommand();
	}

	private void changeUser() {
		if (!(jTextFieldUser.getText().equals(defaultWinUser))) {
			commandDeployClientAgent.setUser(jTextFieldUser.getText().trim());
		} else {
			commandDeployClientAgent.setUser("");
		}

		updateCommand();
	}

	private void changePassw() {
		commandDeployClientAgent.setPassw(new String(jTextFieldPassword.getPassword()).trim());
		updateCommand();
	}

	private void changeEchoChar() {
		if (aktive) {
			aktive = false;
			jTextFieldPassword.setEchoChar('*');
		} else {
			aktive = true;
			jTextFieldPassword.setEchoChar((char) 0);
		}
	}

	private void cancel() {
		super.doAction1();
	}

	@Override
	public void doAction2() {
		Logging.info(this, "doAction2 deploy-clientagent ");
		if (jTextFieldClient.getText().isEmpty()) {
			Logging.warning(this, "Client name(s) missing.");
			displayNoClientSpecified();
			return;
		}
		Set<String> clients = Set.of(jTextFieldClient.getText().trim().split(" "));
		Set<String> nonExistingHostNames = getNonExistingHostNames(clients);
		if (!nonExistingHostNames.isEmpty()) {
			FTextArea fQuestion = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("SSHDeployClientAgentParameterDialog.clientDoesNotExist.title"), true,
					new String[] { Configed.getResourceValue("buttonCancel"), Configed
							.getResourceValue("SSHDeployClientAgentParameterDialog.clientDoesNotExist.proceed") });
			StringBuilder message = new StringBuilder();
			message.append(
					Configed.getResourceValue("SSHDeployClientAgentParameterDialog.clientDoesNotExist.message1"));
			message.append("\n\n");
			message.append(nonExistingHostNames.toString().replace("[", "").replace("]", "").replace(",", "\n"));
			message.append("\n\n");
			message.append(
					Configed.getResourceValue("SSHDeployClientAgentParameterDialog.clientDoesNotExist.message2"));
			fQuestion.setMessage(message.toString());
			fQuestion.setLocationRelativeTo(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 2) {
				clients.removeAll(nonExistingHostNames);
			} else {
				return;
			}
		}
		String selectedOS = jComboBoxOperatingSystem.getItemAt(jComboBoxOperatingSystem.getSelectedIndex());
		String opsiClientAgentDir = "";
		if (OS.LINUX.toString().equals(selectedOS)) {
			opsiClientAgentDir = "opsi-linux-client-agent";
		} else if (OS.MACOS.toString().equals(selectedOS)) {
			opsiClientAgentDir = "opsi-mac-client-agent";
		} else {
			opsiClientAgentDir = "opsi-client-agent";
		}
		commandDeployClientAgent.setOpsiClientAgentDir(opsiClientAgentDir);
		commandDeployClientAgent.finish(finalAction);
		CommandExecutor executor = new CommandExecutor(configedMain);
		executor.executeSingleCommand(commandDeployClientAgent);
	}

	private Set<String> getNonExistingHostNames(Set<String> hostNames) {
		Set<String> nonExistingHostNames = new HashSet<>();
		if (hostNames == null || hostNames.isEmpty()) {
			return nonExistingHostNames;
		}
		nonExistingHostNames.addAll(hostNames);
		nonExistingHostNames.removeAll(persistenceController.getHostInfoCollections().getOpsiHostNames());
		return nonExistingHostNames;
	}

	private static void displayNoClientSpecified() {
		FTextArea fNoClientSpecifiedDialog = new FTextArea(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("SSHDeployClientAgentParameterDialog.noClientSpecified.title"), true,
				new String[] { Configed.getResourceValue("buttonClose") });
		fNoClientSpecifiedDialog
				.setMessage(Configed.getResourceValue("SSHDeployClientAgentParameterDialog.noClientSpecified.message"));
		fNoClientSpecifiedDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		fNoClientSpecifiedDialog.setAlwaysOnTop(true);
		fNoClientSpecifiedDialog.setVisible(true);
	}

	private void doCopySelectedClients() {
		List<String> clientsList = configedMain.getSelectedClients();
		if (!clientsList.isEmpty()) {
			StringBuilder clients = new StringBuilder();
			for (String c : clientsList) {
				clients.append(c);
				clients.append(" ");
			}
			jTextFieldClient.setText(clients.toString());
		}
	}

	private void initLayout() {
		GroupLayout winAuthPanelLayout = new GroupLayout(winAuthPanel);
		winAuthPanel.setLayout(winAuthPanelLayout);

		winAuthPanelLayout
				.setHorizontalGroup(
						winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
								.addGroup(
										winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addGroup(winAuthPanelLayout.createSequentialGroup()
														.addGap(Globals.GAP_SIZE)
														.addComponent(jLabelUser, Globals.BUTTON_WIDTH,
																Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
														.addGap(Globals.GAP_SIZE)
														.addComponent(jTextFieldUser, GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
														.addGap(Globals.GAP_SIZE).addGap(Globals.ICON_WIDTH)
														.addGap(Globals.GAP_SIZE))
												.addGroup(winAuthPanelLayout.createSequentialGroup()
														.addGap(Globals.GAP_SIZE)
														.addComponent(jLabelPassword, Globals.BUTTON_WIDTH,
																Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
														.addGap(Globals.GAP_SIZE)
														.addComponent(jTextFieldPassword, Globals.BUTTON_WIDTH,
																Globals.BUTTON_WIDTH, Short.MAX_VALUE)
														.addGap(Globals.GAP_SIZE)
														.addComponent(jButtonShowPassword, Globals.ICON_WIDTH,
																Globals.ICON_WIDTH, Globals.ICON_WIDTH)
														.addGap(Globals.GAP_SIZE)))
								.addGap(Globals.GAP_SIZE));

		winAuthPanelLayout
				.setVerticalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jTextFieldUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(winAuthPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jTextFieldPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jButtonShowPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE));

		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup().addGroup(inputPanelLayout.createSequentialGroup()
						.addComponent(jLabelClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(jTextFieldClient, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE)
						.addComponent(jLabelUserData, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCopySelectedClients, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(winAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addGroup(inputPanelLayout.createSequentialGroup()
								.addGroup(inputPanelLayout.createParallelGroup()
										.addComponent(jLabelFinalize, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelVerbosity, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelOperatingSystem, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelIgnorePing, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(2 * Globals.GAP_SIZE)
								.addGroup(inputPanelLayout.createParallelGroup().addComponent(panelFinalAction)
										.addComponent(jCheckBoxIgnorePing, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
												Globals.ICON_WIDTH)
										.addComponent(jCheckBoxVerbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
												Globals.ICON_WIDTH)
										.addComponent(jComboBoxOperatingSystem, Globals.BUTTON_WIDTH,
												Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH))))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout
				.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelClient, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jTextFieldClient, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
								jButtonCopySelectedClients, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE * 2)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
								jLabelUserData, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addComponent(winAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE * 2)

						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelFinalize, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(panelFinalAction))

						.addGap(Globals.MIN_GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelIgnorePing, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxIgnorePing, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelOperatingSystem, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxOperatingSystem, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE));
	}
}