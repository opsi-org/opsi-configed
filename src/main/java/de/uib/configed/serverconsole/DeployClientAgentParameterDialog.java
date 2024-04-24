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
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
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
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.PanelStateSwitch;

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

	private JLabel jLabelClient = new JLabel();
	private JLabel jLabelUserData = new JLabel();
	private JLabel jLabelVerbosity = new JLabel();
	private JLabel jLabelIgnorePing = new JLabel();
	private JLabel jLabelFinalize = new JLabel();
	private JLabel jLabelOperatingSystem = new JLabel();

	private JButton jButtonCopySelectedClients;

	private JTextField jTextFieldClient;

	private FinalActionType finalAction;
	private PanelStateSwitch<FinalActionType> panelFinalAction;

	private JCheckBox jCheckBoxIgnorePing;
	private JComboBox<Integer> jCheckBoxVerbosity;
	private JComboBox<String> jComboBoxOperatingSystem;

	private SingleCommandDeployClientAgent commandDeployClientAgent = new SingleCommandDeployClientAgent();
	private ConfigedMain configedMain;

	private DeployClientAgentAuthPanel authPanel;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private boolean isGlobalReadOnly = persistenceController.getUserRolesConfigDataService().isGlobalReadOnly();

	public DeployClientAgentParameterDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("DeployClientAgentParameterDialog.title"), false);
		this.configedMain = configedMain;
		authPanel = new DeployClientAgentAuthPanel(commandDeployClientAgent);

		init();
		super.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());

		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setVisible(true);

		Logging.info(this.getClass(), "DeployClientAgentParameterDialog build");
	}

	private void init() {
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setPreferredSize(new Dimension(376, 220));

		jLabelIgnorePing.setText(Configed.getResourceValue("DeployClientAgentParameterDialog.ignorePing"));
		jCheckBoxIgnorePing = new JCheckBox("", !commandDeployClientAgent.isPingRequired());
		jCheckBoxIgnorePing.setEnabled(!isGlobalReadOnly);
		jCheckBoxIgnorePing.addItemListener((ItemEvent itemEvent) -> commandDeployClientAgent.togglePingIsRequired());

		jLabelVerbosity.setText(Configed.getResourceValue("verbosity"));
		jCheckBoxVerbosity = new JComboBox<>();
		jCheckBoxVerbosity.setToolTipText(Configed.getResourceValue("verbosity.tooltip"));
		jCheckBoxVerbosity.setEnabled(!isGlobalReadOnly);
		jCheckBoxVerbosity.setEditable(!isGlobalReadOnly);
		for (int i = 0; i < 5; i++) {
			jCheckBoxVerbosity.addItem(i);
		}

		jCheckBoxVerbosity.setSelectedItem(1);
		jCheckBoxVerbosity.addItemListener(itemEvent -> changeVerbosity());

		jLabelClient.setText(Configed.getResourceValue("DeployClientAgentParameterDialog.jLabelClient"));
		jTextFieldClient = new JTextField();
		jTextFieldClient
				.setToolTipText(Configed.getResourceValue("DeployClientAgentParameterDialog.tooltip.tf_client"));
		jTextFieldClient.setEnabled(!isGlobalReadOnly);
		jTextFieldClient.setEditable(!isGlobalReadOnly);
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

		jLabelUserData
				.setText(Configed.getResourceValue("DeployClientAgentParameterDialog.targetclient_authentication"));
		jLabelFinalize.setText(Configed.getResourceValue("DeployClientAgentParameterDialog.lbl_finalize"));

		panelFinalAction = new PanelStateSwitch<>(null, FinalActionType.START_OCD, FinalActionType.values(),
				new String[] { Configed.getResourceValue("DeployClientAgentParameterDialog.lbl_finalize.START_OCD"),
						Configed.getResourceValue("DeployClientAgentParameterDialog.lbl_finalize.REBOOT"),
						Configed.getResourceValue("DeployClientAgentParameterDialog.lbl_finalize.SHUTDOWN") },
				FinalActionType.class, ((Enum<FinalActionType> val) -> {
					Logging.info(this, "change to " + val);
					finalAction = (FinalActionType) val;
				}));

		jLabelOperatingSystem
				.setText(Configed.getResourceValue("DeployClientAgentParameterDialog.opsiClientAgent.label"));
		jComboBoxOperatingSystem = new JComboBox<>(
				new String[] { OS.WINDOWS.toString(), OS.LINUX.toString(), OS.MACOS.toString() });
		jComboBoxOperatingSystem
				.setToolTipText(Configed.getResourceValue("DeployClientAgentParameterDialog.opsiClientAgent.toolTip"));

		jButtonCopySelectedClients = new JButton(
				Configed.getResourceValue("DeployClientAgentParameterDialog.btn_copy_selected_clients"));

		jButtonCopySelectedClients.addActionListener(actionEvent -> doCopySelectedClients());

		JButton jButtonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		jButtonExecute.setEnabled(!isGlobalReadOnly);

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonExecute.addActionListener(actionEvent -> doAction2());
		}

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));
		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonExecute);

		doCopySelectedClients();

		changeClient();
		authPanel.changeUser();
		authPanel.changePassw();

		changeVerbosity();

		initLayout();
	}

	private void changeVerbosity() {
		commandDeployClientAgent.setVerbosity((int) jCheckBoxVerbosity.getSelectedItem());
	}

	private void changeClient() {
		commandDeployClientAgent.setClient(jTextFieldClient.getText().trim());
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
					Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.title"), true,
					new String[] { Configed.getResourceValue("buttonCancel"),
							Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.proceed") });
			StringBuilder message = new StringBuilder();
			message.append(Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.message1"));
			message.append("\n\n");
			message.append(nonExistingHostNames.toString().replace("[", "").replace("]", "").replace(",", "\n"));
			message.append("\n\n");
			message.append(Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.message2"));
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
		CommandExecutor executor = new CommandExecutor(configedMain, commandDeployClientAgent);
		executor.execute();
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
				Configed.getResourceValue("DeployClientAgentParameterDialog.noClientSpecified.title"), true,
				new String[] { Configed.getResourceValue("buttonClose") });
		fNoClientSpecifiedDialog
				.setMessage(Configed.getResourceValue("DeployClientAgentParameterDialog.noClientSpecified.message"));
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
						.addComponent(authPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
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
						.addComponent(authPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
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
