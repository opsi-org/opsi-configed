/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.SingleCommandDeployClientAgent;
import de.uib.configed.serverconsole.command.SingleCommandDeployClientAgent.FinalActionType;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.PanelStateSwitch;

public class DeployClientAgentParameterDialog {
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

	private JLabel jLabelClient = new JLabel();
	private JLabel jLabelUserData = new JLabel();
	private JLabel jLabelLoglevel = new JLabel();
	private JLabel jLabelIgnorePing = new JLabel();
	private JLabel jLabelFinalize = new JLabel();
	private JLabel jLabelOperatingSystem = new JLabel();

	private JButton jButtonCopySelectedClients;

	private JTextField jTextFieldClient;

	private FinalActionType finalAction;
	private PanelStateSwitch<FinalActionType> panelFinalAction;

	private JCheckBox jCheckBoxIgnorePing;
	private JComboBox<Integer> jCheckBoxLoglevel;
	private JComboBox<String> jComboBoxOperatingSystem;

	private SingleCommandDeployClientAgent commandDeployClientAgent = new SingleCommandDeployClientAgent();
	private ConfigedMain configedMain;

	private DeployClientAgentAuthPanel authPanel;

	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public DeployClientAgentParameterDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		authPanel = new DeployClientAgentAuthPanel(commandDeployClientAgent);

		init();
		initLayout();

		Logging.info(this, "DeployClientAgentParameterDialog build");

		JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null,
				new Object[] { Configed.getResourceValue("buttonExecute"), Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("DeployClientAgentParameterDialog.title"));

		dialog.setVisible(true);

		if (optionPane.getValue() != null && optionPane.getValue().equals(Configed.getResourceValue("buttonExecute"))) {
			execute();
		}
	}

	private void init() {
		jLabelIgnorePing.setText(Configed.getResourceValue("DeployClientAgentParameterDialog.ignorePing"));
		jCheckBoxIgnorePing = new JCheckBox("", !commandDeployClientAgent.isPingRequired());
		jCheckBoxIgnorePing.addItemListener(itemEvent -> commandDeployClientAgent.togglePingIsRequired());

		jLabelLoglevel.setText(Configed.getResourceValue("loglevel"));
		jCheckBoxLoglevel = new JComboBox<>();
		for (int i = 3; i <= 9; i++) {
			jCheckBoxLoglevel.addItem(i);
		}

		jCheckBoxLoglevel.setSelectedItem(4);
		jCheckBoxLoglevel.addItemListener(itemEvent -> updateLoglevel());

		jLabelClient.setText(Configed.getResourceValue("DeployClientAgentParameterDialog.jLabelClient"));
		jTextFieldClient = new JTextField();
		jTextFieldClient
				.setToolTipText(Configed.getResourceValue("DeployClientAgentParameterDialog.tooltip.tf_client"));
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
					Logging.info(this, "change to ", val);
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

		changeClient();
		authPanel.changeUser();
		authPanel.changePassw();

		updateLoglevel();
	}

	private void updateLoglevel() {
		commandDeployClientAgent.setLoglevel((int) jCheckBoxLoglevel.getSelectedItem());
	}

	private void changeClient() {
		commandDeployClientAgent.setClient(jTextFieldClient.getText().trim());
	}

	private void execute() {
		Logging.info(this, "doAction2 deploy-clientagent ");
		if (jTextFieldClient.getText().isEmpty()) {
			Logging.warning(this, "Client name(s) missing.");
			JOptionPane.showMessageDialog(dialog,
					Configed.getResourceValue("DeployClientAgentParameterDialog.noClientSpecified.message"),
					Configed.getResourceValue("DeployClientAgentParameterDialog.noClientSpecified.title"),
					JOptionPane.ERROR_MESSAGE);

			return;
		}
		Set<String> clients = Set.of(jTextFieldClient.getText().trim().split(" "));
		Set<String> nonExistingHostNames = getNonExistingHostNames(clients);
		if (!nonExistingHostNames.isEmpty()) {
			StringBuilder message = new StringBuilder();
			message.append(Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.message1"));
			message.append("\n\n");
			message.append(nonExistingHostNames.toString().replace("[", "").replace("]", "").replace(",", "\n"));
			message.append("\n\n");
			message.append(Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.message2"));

			int answer = JOptionPane.showOptionDialog(dialog, message.toString(),
					Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.title"), 0, 0, null,
					new String[] { Configed.getResourceValue("buttonCancel"),
							Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.proceed") },
					null);

			if (answer == 1) {
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
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup()
						.addComponent(jLabelClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(jTextFieldClient, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelUserData, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jButtonCopySelectedClients, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(authPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(inputPanelLayout.createSequentialGroup().addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jLabelFinalize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelLoglevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelOperatingSystem, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelIgnorePing, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup().addComponent(panelFinalAction)
								.addComponent(jCheckBoxIgnorePing, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)
								.addComponent(jCheckBoxLoglevel, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)
								.addComponent(jComboBoxOperatingSystem, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH))));

		inputPanelLayout
				.setVerticalGroup(inputPanelLayout.createSequentialGroup()
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
								.addComponent(jLabelLoglevel, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxLoglevel, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelOperatingSystem, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxOperatingSystem, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)));
	}
}
