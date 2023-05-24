/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandDeployClientAgent;
import de.uib.opsicommand.sshcommand.CommandDeployClientAgent.FinalActionType;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PanelStateSwitch;

public class SSHDeployClientAgentParameterDialog extends FGeneralDialog {

	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 500;

	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel winAuthPanel = new JPanel();

	private JLabel jLabelClient = new JLabel();
	private JLabel jLabelUser = new JLabel();
	private JLabel jLabelPassword = new JLabel();
	private JLabel jLabelUserData = new JLabel();
	private JLabel jLabelVerbosity = new JLabel();
	private JLabel jLabelFullCommand = new JLabel();
	private JLabel jLabelApplySudo = new JLabel();
	private JLabel jLabelIgnorePing = new JLabel();
	private JLabel jLabelFinalize = new JLabel();

	private JButton jButtonExecute;
	private JButton jButtonCopySelectedClients;
	private JButton jButtonHelp;

	private JTextField jTextFieldClient;
	private JTextField jTextFieldUser;
	private JPasswordField jTextFieldPassword;

	private CommandDeployClientAgent.FinalActionType finalAction;
	private PanelStateSwitch<FinalActionType> panelFinalAction;

	private JButton jButtonShowPassword;
	private JCheckBox jCheckBoxApplySudo;
	private JCheckBox jCheckBoxIgnorePing;
	private JComboBox<Integer> jCheckBoxVerbosity;

	private String defaultWinUser = "";

	private CommandDeployClientAgent commandDeployClientAgent = new CommandDeployClientAgent();
	private ConfigedMain main;

	private boolean aktive;

	public SSHDeployClientAgentParameterDialog() {
		this(null);
	}

	public SSHDeployClientAgentParameterDialog(ConfigedMain m) {
		super(null, Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.title"), false);
		main = m;
		getDefaultAuthData();

		init();
		super.pack();
		super.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		if (!Main.THEMES) {
			super.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setVisible(true);

		Logging.info(this, "SSHDeployClientAgentParameterDialog build");

		setComponentsEnabled(!Globals.isGlobalReadOnly());
	}

	private void getDefaultAuthData() {
		Map<String, Object> configs = main.getPersistenceController()
				.getConfig(main.getPersistenceController().getHostInfoCollections().getConfigServer());
		main.getPersistenceController();
		List<Object> resultConfigList = (List<Object>) configs
				.get(OpsiserviceNOMPersistenceController.KEY_SSH_DEFAULTWINUSER);
		if (resultConfigList == null || resultConfigList.isEmpty()) {

			Logging.info(this, "KEY_SSH_DEFAULTWINUSER not existing");

			// the config will be created in this run of configed
		} else {
			defaultWinUser = (String) resultConfigList.get(0);
			Logging.info(this, "KEY_SSH_DEFAULTWINUSER " + ((String) resultConfigList.get(0)));

		}

		main.getPersistenceController();
		resultConfigList = (List<Object>) configs.get(OpsiserviceNOMPersistenceController.KEY_SSH_DEFAULTWINPW);
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

		jButtonHelp.setEnabled(value);
		jButtonExecute.setEnabled(value);

		jCheckBoxIgnorePing.setEnabled(value);
		jCheckBoxApplySudo.setEnabled(value);

	}

	private void init() {
		if (!Main.THEMES) {
			inputPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			winAuthPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		winAuthPanel.setBorder(new LineBorder(Globals.blueGrey, 2, true));
		inputPanel.setPreferredSize(new Dimension(376, 220));

		jCheckBoxApplySudo = new JCheckBox("", commandDeployClientAgent.needSudo());
		jLabelApplySudo
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.applySudo"));
		jCheckBoxApplySudo.addItemListener((ItemEvent itemEvent) -> {
			commandDeployClientAgent.setNeedingSudo(!commandDeployClientAgent.needSudo());
			updateCommand();
		});

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

		jButtonShowPassword = new JButton(Globals.createImageIcon("images/eye_blue_open.png", ""));

		jButtonShowPassword.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
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

		panelFinalAction = new PanelStateSwitch<>(null, CommandDeployClientAgent.FinalActionType.START_OCD,
				CommandDeployClientAgent.FinalActionType.values(),
				new String[] {
						Configed.getResourceValue(
								"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.START_OCD"),
						Configed.getResourceValue(
								"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.REBOOT"),
						Configed.getResourceValue(
								"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.SHUTDOWN") },
				CommandDeployClientAgent.FinalActionType.class, ((Enum<FinalActionType> val) -> {
					Logging.info(this, "change to " + val);
					finalAction = (CommandDeployClientAgent.FinalActionType) val;
				}), 2, 2);

		panelFinalAction.setOpaque(false);

		jButtonCopySelectedClients = new JButton(Configed
				.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.btn_copy_selected_clients"));

		jButtonCopySelectedClients.addActionListener(actionEvent -> doCopySelectedClients());

		jButtonHelp = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
		jButtonHelp.setToolTipText(Configed.getResourceValue("SSHConnection.buttonHelp"));
		jButtonHelp.setText(Configed.getResourceValue("SSHConnection.buttonHelp"));

		jButtonHelp.addActionListener(actionEvent -> doActionHelp());

		jButtonExecute = new JButton();
		jButtonExecute.setText(Configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonExecute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly())) {
			jButtonExecute.addActionListener(actionEvent -> doAction2());
		}

		JButton jButtonClose = new JButton();
		jButtonClose.setText(Configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
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

	public void changeEchoChar() {
		if (aktive) {
			aktive = false;
			jTextFieldPassword.setEchoChar('*');
		} else {
			aktive = true;
			jTextFieldPassword.setEchoChar((char) 0);
		}
	}

	public void setComponentsEditable(boolean value) {
		jTextFieldUser.setEnabled(value);
		jTextFieldPassword.setEnabled(value);
	}

	// /* This method gets called when button 1 is pressed */
	public void cancel() {
		super.doAction1();
	}

	// 

	/* This method is called when button 2 is pressed */
	@Override
	public void doAction2() {
		Logging.info(this, "doAction2 deploy-clientagent ");
		if (jTextFieldClient.getText().isEmpty()) {
			Logging.warning(this, "Client name(s) missing.");
			return;
		}

		commandDeployClientAgent.finish(finalAction);
		try {
			new SSHConnectExec(commandDeployClientAgent);
		} catch (Exception e) {
			Logging.warning(this, "doAction2, exception occurred", e);
		}
	}

	public void doCopySelectedClients() {
		String[] clientsList = main.getSelectedClients();
		if (clientsList.length > 0) {
			StringBuilder clients = new StringBuilder();
			for (String c : clientsList) {
				clients.append(c);
				clients.append(" ");
			}
			jTextFieldClient.setText(clients.toString());
		}
	}

	public void doActionHelp() {
		SSHConnectionExecDialog dia = commandDeployClientAgent.startHelpDialog();
		dia.setVisible(true);
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
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jLabelUserData,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jButtonCopySelectedClients,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(winAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addGroup(inputPanelLayout.createSequentialGroup()
								.addGroup(inputPanelLayout.createParallelGroup()
										.addComponent(jLabelFinalize, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelApplySudo, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelVerbosity, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelIgnorePing, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(2 * Globals.GAP_SIZE)
								.addGroup(inputPanelLayout.createParallelGroup().addComponent(panelFinalAction)
										.addComponent(jCheckBoxApplySudo, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
												Globals.ICON_WIDTH)
										.addComponent(jCheckBoxIgnorePing, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
												Globals.ICON_WIDTH)
										.addComponent(jCheckBoxVerbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
												Globals.ICON_WIDTH))

						)));

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

						.addGap(Globals.GAP_SIZE / 2)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelIgnorePing, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxIgnorePing, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelApplySudo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxApplySudo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE));
	}
}
