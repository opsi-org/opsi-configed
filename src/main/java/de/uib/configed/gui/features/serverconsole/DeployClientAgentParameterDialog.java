/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandDeployClientAgent;
import de.uib.configed.gui.features.serverconsole.command.SingleCommandDeployClientAgent.FinalActionType;
import de.uib.configed.gui.share.swing.PanelStateSwitch;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class DeployClientAgentParameterDialog {
	private enum OS {
		WINDOWS, LINUX, MACOS
	}

	private JPanel inputPanel = new JPanel();

	private JLabel jLabelClient;
	private JLabel jLabelUserData;
	private JLabel jLabelLoglevel;
	private JLabel jLabelFinalize;
	private JLabel jLabelOperatingSystem;

	private JButton jButtonCopySelectedClients;

	private JTextField jTextFieldClient;

	private FinalActionType finalAction;
	private PanelStateSwitch<FinalActionType> panelFinalAction;

	private JCheckBox jCheckBoxIgnorePing;
	private JComboBox<Integer> jComboBoxLoglevel;
	private JComboBox<OS> jComboBoxOperatingSystem;

	private SingleCommandDeployClientAgent commandDeployClientAgent = new SingleCommandDeployClientAgent();
	private ConfigedMain configedMain;

	private DeployClientAgentAuthPanel authPanel;

	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public DeployClientAgentParameterDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
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

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(actionEvent -> execute());

		JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, new Object[] { buttonExecute, Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("DeployClientAgentParameterDialog.title"));
		dialog.setModal(false);
		dialog.setVisible(true);
	}

	private void init() {
		jCheckBoxIgnorePing = new JCheckBox(Configed.getResourceValue("DeployClientAgentParameterDialog.ignorePing"),
				!commandDeployClientAgent.isPingRequired());
		jCheckBoxIgnorePing.addItemListener(itemEvent -> commandDeployClientAgent.togglePingIsRequired());

		jLabelLoglevel = Utils.createBoldLabel("loglevel");
		jComboBoxLoglevel = new JComboBox<>();
		for (int i = 3; i <= 9; i++) {
			jComboBoxLoglevel.addItem(i);
		}

		jComboBoxLoglevel.setSelectedItem(4);
		jComboBoxLoglevel.addItemListener(itemEvent -> updateLoglevel());

		jLabelClient = Utils.createBoldLabel("DeployClientAgentParameterDialog.jLabelClient");
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

		jLabelUserData = Utils.createBoldLabel("DeployClientAgentParameterDialog.targetclient_authentication");

		jLabelFinalize = Utils.createBoldLabel("DeployClientAgentParameterDialog.lbl_finalize");

		panelFinalAction = new PanelStateSwitch<>(null, FinalActionType.START_OCD, FinalActionType.values(),
				new String[] { Configed.getResourceValue("DeployClientAgentParameterDialog.lbl_finalize.START_OCD"),
						Configed.getResourceValue("DeployClientAgentParameterDialog.lbl_finalize.REBOOT"),
						Configed.getResourceValue("DeployClientAgentParameterDialog.lbl_finalize.SHUTDOWN") },
				FinalActionType.class, ((Enum<FinalActionType> val) -> {
					Logging.info(this, "change to ", val);
					finalAction = (FinalActionType) val;
				}));

		jLabelOperatingSystem = Utils.createBoldLabel("DeployClientAgentParameterDialog.opsiClientAgent.label");

		jComboBoxOperatingSystem = new JComboBox<>(OS.values());
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
		commandDeployClientAgent.setLoglevel((int) jComboBoxLoglevel.getSelectedItem());
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
					Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					new String[] {
							Configed.getResourceValue("DeployClientAgentParameterDialog.clientDoesNotExist.proceed"),
							Configed.getResourceValue("buttonCancel") },
					null);

			if (answer == 0) {
				clients.removeAll(nonExistingHostNames);
			} else {
				return;
			}
		}
		String opsiClientAgentDir = switch (jComboBoxOperatingSystem
				.getItemAt(jComboBoxOperatingSystem.getSelectedIndex())) {
		case LINUX -> "opsi-linux-client-agent";
		case MACOS -> "opsi-mac-client-agent";
		case WINDOWS -> "opsi-client-agent";
		};

		commandDeployClientAgent.setOpsiClientAgentDir(opsiClientAgentDir);
		commandDeployClientAgent.finish(finalAction);
		CommandExecutor executor = new CommandExecutor(configedMain, commandDeployClientAgent);
		executor.executeAsync();
	}

	private Set<String> getNonExistingHostNames(Set<String> hostNames) {
		Set<String> nonExistingHostNames = new HashSet<>();
		if (hostNames.isEmpty()) {
			return nonExistingHostNames;
		}
		nonExistingHostNames.addAll(hostNames);
		nonExistingHostNames.removeAll(persistenceController.getDataServices().hostInfoCollections.getOpsiHostNames());
		return nonExistingHostNames;
	}

	private void doCopySelectedClients() {
		List<String> clientsList = configedMain.getSelectedClients();
		if (!clientsList.isEmpty()) {
			jTextFieldClient.setText(String.join(" ", clientsList));
		}
	}

	private void initLayout() {
		inputPanel.setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));
		inputPanel.add(jLabelClient);
		inputPanel.add(jTextFieldClient, "growx, gapbottom " + Globals.MIN_GAP_SIZE);
		inputPanel.add(jButtonCopySelectedClients, "gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(jLabelUserData, "gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(authPanel, "growx");
		inputPanel.add(jLabelFinalize);
		inputPanel.add(panelFinalAction, "gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(jCheckBoxIgnorePing, "gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(jLabelLoglevel);
		inputPanel.add(jComboBoxLoglevel, "gapbottom " + Globals.GAP_SIZE);
		inputPanel.add(jLabelOperatingSystem);
		inputPanel.add(jComboBoxOperatingSystem);
	}
}
