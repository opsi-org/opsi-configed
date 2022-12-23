package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandDeployClientAgent;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.PanelStateSwitch;

public class SSHDeployClientAgentParameterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel winAuthPanel = new JPanel();

	private JLabel lbl_client = new JLabel();
	private JLabel lbl_user = new JLabel();
	private JLabel lbl_passw = new JLabel();
	private JLabel lbl_userdata = new JLabel();
	private JLabel lbl_verbosity = new JLabel();
	private JLabel lbl_fullCommand = new JLabel();
	private JLabel lbl_applySudo = new JLabel();
	private JLabel lbl_ignorePing = new JLabel();
	private JLabel lbl_finalize = new JLabel();

	private JButton btn_execute;
	private JButton btn_copy_selected_clients;
	private JButton btn_close;
	private JButton btn_help;

	private JTextField tf_client;
	private JTextField tf_user;
	private JPasswordField tf_passw;
	

	// public enum FinalActionType { START_OCD, REBOOT, SHUTDOWN };
	protected CommandDeployClientAgent.FinalActionType finalAction;
	protected PanelStateSwitch panelFinalAction;

	private JButton btn_showPassw;
	private JCheckBox cb_applySudo;
	private JCheckBox cb_ignorePing;
	private JComboBox cb_verbosity;

	private String defaultWinUser = "";

	private final int frameWidth = 800;
	private final int frameHight = 500;
	private CommandDeployClientAgent commandDeployClientAgent = new CommandDeployClientAgent();
	private ConfigedMain main;

	public SSHDeployClientAgentParameterDialog() {
		this(null);
	}

	public SSHDeployClientAgentParameterDialog(ConfigedMain m) {
		super(null, configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.title"), false);
		main = m;
		getDefaultAuthData();

		init();
		pack();
		this.setSize(new Dimension(frameWidth, frameHight));
		this.centerOn(Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		logging.info(this, "SSHDeployClientAgentParameterDialog build");

		setComponentsEnabled(!Globals.isGlobalReadOnly());
	}

	private void getDefaultAuthData() {
		Map<String, Object> configs = main.getPersistenceController()
				.getConfig(main.getPersistenceController().getHostInfoCollections().getConfigServer());
		ArrayList<Object> result_config_list = (ArrayList<Object>) configs
				.get(main.getPersistenceController().KEY_SSH_DEFAULTWINUSER);
		if (result_config_list == null || result_config_list.isEmpty()) {
			// defaultWinUser =
			// configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.defaultWinUser");
			logging.info(this, "KEY_SSH_DEFAULTWINUSER not existing");
			
			// the config will be created in this run of configed
		} else {
			defaultWinUser = (String) result_config_list.get(0);
			logging.info(this, "KEY_SSH_DEFAULTWINUSER " + ((String) result_config_list.get(0)));
			
		}

		result_config_list = (ArrayList<Object>) configs.get(main.getPersistenceController().KEY_SSH_DEFAULTWINPW);
		if (result_config_list == null || result_config_list.isEmpty()) {
			// defaultWinUser =
			// configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.defaultWinUser");
			logging.info(this, "KEY_SSH_DEFAULTWINPW not existing");
			
			// the config will be created in this run of configed
		} else {
			if (tf_passw == null) {
				tf_passw = new JPasswordField("", 15);
				tf_passw.setEchoChar('*');
			}
			tf_passw.setText((String) result_config_list.get(0));
			logging.info(this, "key_ssh_shell_active " + SSHCommandFactory.getInstance().confidential);
			
		}
	}

	private void setComponentsEnabled(boolean value) {
		tf_client.setEnabled(value);
		tf_client.setEditable(value);

		tf_user.setEnabled(value);
		tf_user.setEditable(value);

		tf_passw.setEnabled(value);
		tf_passw.setEditable(value);

		

		btn_showPassw.setEnabled(value);

		cb_verbosity.setEnabled(value);
		cb_verbosity.setEditable(value);

		btn_help.setEnabled(value);
		btn_execute.setEnabled(value);

		cb_ignorePing.setEnabled(value);
		cb_applySudo.setEnabled(value);

	}

	protected void init() {
		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		winAuthPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		winAuthPanel.setBorder(new LineBorder(Globals.blueGrey, 2, true));
		inputPanel.setPreferredSize(new java.awt.Dimension(376, 220));

		{
			cb_applySudo = new JCheckBox("", commandDeployClientAgent.needSudo());
			lbl_applySudo
					.setText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.applySudo"));
			cb_applySudo.addItemListener(itemEvent -> {
				commandDeployClientAgent.setNeedingSudo(!commandDeployClientAgent.needSudo());
				updateCommand();
			});
		}

		{
			cb_ignorePing = new JCheckBox("", !commandDeployClientAgent.isPingRequired());
			lbl_ignorePing
					.setText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.ignorePing"));
			cb_ignorePing.addItemListener(itemEvent -> {
				commandDeployClientAgent.togglePingIsRequired();
				updateCommand();
			});
		}

		{
			lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
			cb_verbosity = new JComboBox<>();
			cb_verbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
			for (int i = 0; i < 5; i++)
				cb_verbosity.addItem(i);
			cb_verbosity.setSelectedItem(1);
			cb_verbosity.addItemListener(itemEvent -> changeVerbosity());
		}
		{
			lbl_client.setText(
					configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelClient"));
			tf_client = new JTextField();
			tf_client.setToolTipText(
					configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.tooltip.tf_client"));
			tf_client.getDocument().addDocumentListener(new DocumentListener() {
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

			lbl_user.setText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelUser"));
			tf_user = new JTextField(defaultWinUser);
			tf_user.setToolTipText(
					configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.tooltip.tf_user"));
			tf_user.getDocument().addDocumentListener(new DocumentListener() {
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

			lbl_passw.setText(
					configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelPassword"));
			tf_passw = new JPasswordField("nt123", 15);
			tf_passw.setEchoChar('*');

			// cb_passw_echo = new
			// JCheckBox(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.showPassword"));

			// btn_showPassw = new JButton("",
			// Globals.createImageIcon("images/eye_open.png", ""));//
			// Globals.createImageIcon("images/eye_open_close.png"));
			btn_showPassw = new JButton(Globals.createImageIcon("images/eye_blue_open.png", ""));
			// "***"); //configed.getResourceValue("SSHConnection.passwordButtonText"));
			// btn_showPassw.setPreferredSize(new Dimension(Globals.squareButtonWidth,
			
			btn_showPassw.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
			btn_showPassw.setToolTipText(
					configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.showPassword.tooltip"));
			btn_showPassw.addActionListener(actionEvent -> changeEchoChar());

			tf_passw.getDocument().addDocumentListener(new DocumentListener() {
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
		}
		{
			lbl_userdata.setText(configed
					.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.targetclient_authentication"));
			lbl_finalize.setText(
					configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize"));

			panelFinalAction = new PanelStateSwitch(null, // configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize"),
					(Enum) CommandDeployClientAgent.FinalActionType.START_OCD,
					CommandDeployClientAgent.FinalActionType.values(),
					new String[] {
							configed.getResourceValue(
									"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.START_OCD"),
							configed.getResourceValue(
									"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.REBOOT"),
							configed.getResourceValue(
									"SSHConnection.ParameterDialog.deploy-clientagent.lbl_finalize.SHUTDOWN") },
					CommandDeployClientAgent.FinalActionType.class, (val -> {
						logging.info(this, "change to " + val);
						finalAction = (CommandDeployClientAgent.FinalActionType) val;
						//
					}), 2, 2);

			panelFinalAction.setOpaque(false);
			// panelFinalAction.setBorder(new LineBorder( Globals.blueGrey,
			

		}

		/*
		 * {
		 * lbl_freeInput.setText(configed.getResourceValue(
		 * "SSHConnection.ParameterDialog.jLabelFreeInput"));
		 * tf_freeInput = new JTextField();
		 * tf_freeInput.setToolTipText(configed.getResourceValue(
		 * "SSHConnection.ParameterDialog.tooltip.freeInput"));
		 * // tf_freeInput.setText("");
		 * addListener(tf_freeInput);
		 * }
		 */
		{

			btn_copy_selected_clients = new JButton(configed
					.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.btn_copy_selected_clients"));
			// btn_copy_selected_clients.setToolTipText(
			// configed.getResourceValue("SSHConnection.buttonHelp") );
			// btn_copy_selected_clients.setText(configed.getResourceValue("SSHConnection.buttonHelp"));
			
			btn_copy_selected_clients.addActionListener(actionEvent -> doCopySelectedClients());

			btn_help = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
			btn_help.setToolTipText(configed.getResourceValue("SSHConnection.buttonHelp"));
			btn_help.setText(configed.getResourceValue("SSHConnection.buttonHelp"));
			
			btn_help.addActionListener(actionEvent -> doActionHelp());

			btn_execute = new JButton();
			buttonPanel.add(btn_execute);
			btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
			btn_execute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
			if (!(Globals.isGlobalReadOnly()))
				btn_execute.addActionListener(actionEvent -> doAction1());

			btn_close = new JButton();
			buttonPanel.add(btn_close);
			btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
			btn_close.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
			btn_close.addActionListener(actionEvent -> cancel());
		}
		{
			lbl_fullCommand.setText("opsi-deploy-client-agent ");
			updateCommand();
		}

		doCopySelectedClients();

		changeClient();
		changeUser();
		changePassw();
		
		changeVerbosity();

		initLayout();

	}

	private void updateCommand() {
		lbl_fullCommand.setText(commandDeployClientAgent.getCommand());
	}

	private void changeVerbosity() {
		commandDeployClientAgent.setVerbosity((int) cb_verbosity.getSelectedItem());
	}

	private void changeClient() {
		commandDeployClientAgent.setClient(tf_client.getText().trim());
		updateCommand();
	}

	private void changeUser() {
		if (!(tf_user.getText().equals(defaultWinUser)))
			commandDeployClientAgent.setUser(tf_user.getText().trim());
		else
			commandDeployClientAgent.setUser("");
		updateCommand();
	}

	private void changePassw() {
		commandDeployClientAgent.setPassw(new String(tf_passw.getPassword()).trim());
		updateCommand();
	}

	boolean aktive = false;

	public void changeEchoChar() {
		if (aktive) {
			aktive = false;
			((JPasswordField) tf_passw).setEchoChar('*');
		} else {
			aktive = true;
			((JPasswordField) tf_passw).setEchoChar((char) 0);
		}
	}

	public void setComponentsEditable(boolean value) {
		tf_user.setEnabled(value);
		tf_passw.setEnabled(value);
	}

	// /* This method gets called when button 2 is pressed */
	public void cancel() {
		super.doAction2();
	}
	// public void doAction2()
	// {
	// 

	
	
	// }

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		logging.info(this, "doAction1 deploy-clientagent ");
		if (tf_client.getText().equals("")) {
			logging.warning(this, "Client name(s) missing.");
			return;
		}

		commandDeployClientAgent.finish(finalAction);
		try {
			new SSHConnectExec(commandDeployClientAgent);
		} catch (Exception e) {
			logging.warning(this, "doAction1, exception occurred", e);
		}
	}

	public void doCopySelectedClients() {
		String[] clients_list = main.getSelectedClients();
		if (clients_list.length > 0) {
			StringBuffer clients = new StringBuffer();
			for (String c : clients_list) {
				clients.append(c);
				clients.append(" ");
			}
			tf_client.setText(clients.toString());
		}
	}

	public void doActionHelp() {
		SSHConnectionExecDialog dia = commandDeployClientAgent.startHelpDialog();
		dia.setVisible(true);
	}

	private void initLayout() {
		int PREF = GroupLayout.PREFERRED_SIZE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		GroupLayout.Alignment center = GroupLayout.Alignment.CENTER;
		GroupLayout winAuthPanelLayout = new GroupLayout((JComponent) winAuthPanel);
		winAuthPanel.setLayout(winAuthPanelLayout);

		winAuthPanelLayout
				.setHorizontalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(winAuthPanelLayout
								.createParallelGroup(
										leading)
								.addGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(lbl_user, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Globals.BUTTON_WIDTH)
										.addGap(Globals.GAP_SIZE)
										.addComponent(tf_user, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE).addGap(Globals.ICON_WIDTH).addGap(Globals.GAP_SIZE))
								.addGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(lbl_passw, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Globals.BUTTON_WIDTH)
										.addGap(Globals.GAP_SIZE)
										.addComponent(tf_passw, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(btn_showPassw, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
												Globals.ICON_WIDTH)
										.addGap(Globals.GAP_SIZE)))
						.addGap(Globals.GAP_SIZE));
		winAuthPanelLayout.setVerticalGroup(winAuthPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(winAuthPanelLayout.createParallelGroup(center)
						.addComponent(lbl_user, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(tf_user, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(winAuthPanelLayout.createParallelGroup(center)
						.addComponent(lbl_passw, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(tf_passw, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(btn_showPassw, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));

		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(lbl_client, PREF, PREF, PREF)
								.addGap(Globals.GAP_SIZE).addComponent(tf_client, PREF, PREF, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(lbl_userdata, PREF, PREF, PREF))
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(btn_copy_selected_clients, PREF,
								PREF, PREF))
						.addComponent(winAuthPanel, PREF, PREF, Short.MAX_VALUE)

						.addGroup(
								inputPanelLayout.createSequentialGroup()
										.addGroup(inputPanelLayout.createParallelGroup()
												.addComponent(lbl_finalize, PREF, PREF, PREF)
												.addComponent(lbl_applySudo, PREF, PREF, PREF)
												.addComponent(lbl_verbosity, PREF, PREF, PREF)
												.addComponent(lbl_ignorePing, PREF, PREF, PREF))
										.addGap(2 * Globals.GAP_SIZE)
										.addGroup(inputPanelLayout.createParallelGroup().addComponent(panelFinalAction)
												.addComponent(cb_applySudo, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
														Globals.ICON_WIDTH)
												.addComponent(cb_ignorePing, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
														Globals.ICON_WIDTH)
												.addComponent(cb_verbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
														Globals.ICON_WIDTH))

						// .addGap(Globals.gapSize)
						)
				// .addGap(Globals.gapSize)
				// .addGroup(inputPanelLayout.createSequentialGroup()
				// .addGap(Globals.gapSize)
				// )
				));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						// .addGap(Globals.minGapSize)
						.addComponent(lbl_client, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(tf_client, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				// .addGap(GroupLayout.PREFERRED_SIZE)
				).addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
						btn_copy_selected_clients, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(inputPanelLayout.createParallelGroup(leading).addComponent(lbl_userdata,
						Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE).addComponent(winAuthPanel, PREF, PREF, PREF).addGap(Globals.GAP_SIZE * 2)

				.addGroup(
						inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(lbl_finalize, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(panelFinalAction))

				.addGap(Globals.GAP_SIZE / 2)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_ignorePing, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_ignorePing, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_applySudo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_applySudo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_verbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_verbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));
	}
}