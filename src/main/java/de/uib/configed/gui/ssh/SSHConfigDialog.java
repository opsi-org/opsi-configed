/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnect;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedDocument;

public final class SSHConfigDialog extends FGeneralDialog {
	private JCheckBox jCheckBoxDefault;
	private JCheckBox jCheckBoxUseKeyFile;

	private JComboBox<String> jComboBoxHost;
	private JButton iconButtonOpenChooser;
	private JTextField jTextFieldKeyFile;
	private JPasswordField jTextFieldPassphrase;
	private JTextField jTextFieldUser;
	private JTextField jTextFieldPort;
	private JPasswordField jTextFieldPassword;
	private boolean jComboBoxUseDefaultState;
	private SSHConnectionInfo connectionInfo;

	private ConfigedMain configedMain;

	private JPanel connectionPanel = new JPanel();
	private JPanel settingsPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JCheckBox jCheckBoxUseOutputColor;
	private JCheckBox jCheckBoxExecInBackground;
	private JButton jButtonSave;
	private JButton jButtonKill;

	private JLabel jLabelConnectionState = new JLabel();

	public SSHConfigDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("MainFrame.jMenuSSHConfig"), false);
		this.configedMain = configedMain;
		connectionInfo = SSHConnectionInfo.getInstance();

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		init();

		this.setSize(500, 535);
		setLocationRelativeTo(ConfigedMain.getMainFrame());
		this.setVisible(true);
		jComboBoxUseDefaultState = jCheckBoxDefault.isSelected();
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			setComponentsEnabledRO(false);
		}
		setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	private void checkComponentStates() {
		boolean state = compareStates();
		Logging.debug(this, "checkComponentStates  identical " + state);

		setSSHState();
	}

	private boolean compareStates() {
		Logging.info(this, "compareStates ");

		if (connectionInfo.getHost() == null) {
			Logging.info(this, "probably host not in hostsAllowed");
			return false;
		}

		if (!jCheckBoxDefault.isSelected()) {
			if (!connectionInfo.getHost().equals(jComboBoxHost.getSelectedItem())) {
				Logging.debug(this, "compareStates 1");
				return false;
			}
			if (!connectionInfo.getUser().equals(jTextFieldUser.getText())) {
				Logging.debug(this, "compareStates 2");
				return false;
			}
			if (!connectionInfo.getPassw().equals(new String(jTextFieldPassword.getPassword()))) {
				Logging.debug(this, "compareStates 3");
				Logging.debug(this, "connection.getPW " + connectionInfo.getPassw());
				Logging.debug(this, "tf.getPW " + new String(jTextFieldPassword.getPassword()));
				return false;
			}
			if (!connectionInfo.getPort().equals(jTextFieldPort.getText()) && !connectionInfo.usesKeyfile()) {
				Logging.debug(this, "compareStates 4");
				return false;
			}
		} else {
			if (!connectionInfo.getHost().equals(ConfigedMain.getHost())) {
				Logging.info(this, "compareStates 5 >" + connectionInfo.getHost() + "<     <>    >"
						+ ConfigedMain.getHost() + "<");
				return false;
			}
			if (!connectionInfo.getPort().equals(jTextFieldPort.getText())) {
				Logging.debug(this, "compareStates 6");
				return false;
			}
			if (!connectionInfo.getUser().equals(ConfigedMain.getUser())) {
				Logging.debug(this, "compareStates 7");
				return false;
			}
			if (!connectionInfo.getPassw().equals(ConfigedMain.getPassword()) && !connectionInfo.usesKeyfile()) {
				Logging.debug(this, "compareStates 8");
				return false;
			}
		}

		Logging.debug(this, "compareStates until now == ");

		if (connectionInfo.usesKeyfile() != jCheckBoxUseKeyFile.isSelected()) {
			Logging.info(this, "compareStates 9");
			return false;
		} else if (jCheckBoxUseKeyFile.isSelected()) {
			if (!connectionInfo.getKeyfilePath().equals(jTextFieldKeyFile.getText())) {
				Logging.debug(this, "compareStates 10");
				return false;
			}

			String pp = Arrays.toString(jTextFieldPassphrase.getPassword());

			if (!connectionInfo.getKeyfilePassphrase().equals(pp)) {
				Logging.debug(this, "compareStates 11");
				return false;
			}
		} else {
			// continue with the rest of the method
		}

		if (jCheckBoxUseOutputColor != null) {
			Logging.debug(this, "compareStates  (factory.ssh_colored_output != cb_useOutputColor.isSelected()) "
					+ SSHCommandFactory.hasColoredOutput() + " != " + jCheckBoxUseOutputColor.isSelected());
			if (SSHCommandFactory.hasColoredOutput() != jCheckBoxUseOutputColor.isSelected()) {
				Logging.debug(this, "compareStates 12");
				return false;
			}
		}
		if (jCheckBoxExecInBackground != null) {
			Logging.debug(this,
					"compareStates  (factory.ssh_always_exec_in_background != cb_execInBackground.isSelected()) "
							+ SSHCommandFactory.alwaysExecInBackground() + " != "
							+ jCheckBoxExecInBackground.isSelected());
			if (SSHCommandFactory.alwaysExecInBackground() != jCheckBoxExecInBackground.isSelected()) {
				Logging.debug(this, "compareStates 13");
				return false;
			}
		}

		return true;
	}

	private void setSSHState() {
		String str = SSHCommandFactory.getInstance().getConnectionState();
		Logging.info(this, "setSSHState " + str);
		String labeltext = "<html><font color='blue'>" + str + "</font></html>";

		jButtonSave.setEnabled(!str.equals(SSHCommandFactory.CONNECTED));
		jButtonKill.setEnabled(str.equals(SSHCommandFactory.CONNECTED));

		if (str.equals(SSHCommandFactory.CONNECTED)) {
			labeltext = "<html><font color='green'>" + str + "</font></html>";
		} else {
			if (str.equals(SSHCommandFactory.NOT_CONNECTED)) {
				labeltext = "<html><font color='red'>" + str + "</font></html>";
			}
		}
		jLabelConnectionState.setText(labeltext);
		Logging.debug(this, "setSSHState setText " + labeltext);
	}

	private void init() {
		Logging.info(this, "init ");

		getContentPane().add(connectionPanel, BorderLayout.NORTH);
		getContentPane().add(settingsPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		GroupLayout connectionPanelLayout = new GroupLayout(connectionPanel);
		connectionPanel.setLayout(connectionPanelLayout);
		GroupLayout settingsPanelLayout = new GroupLayout(settingsPanel);
		settingsPanel.setLayout(settingsPanelLayout);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

		connectionPanel.setBorder(
				BorderFactory.createTitledBorder(Configed.getResourceValue("SSHConnection.Config.serverPanelTitle")));
		connectionPanel.setPreferredSize(new Dimension(400, 350));

		settingsPanel.setBorder(
				BorderFactory.createTitledBorder(Configed.getResourceValue("SSHConnection.Config.settingsPanelTitle")));

		JLabel jLabelHost = new JLabel();
		jLabelHost.setText(Configed.getResourceValue("SSHConnection.Config.jLabelHost"));

		jComboBoxHost = new JComboBox<>();
		String host = connectionInfo.getHost();
		if (host == null) {
			host = ConfigedMain.getHost();
		}

		jComboBoxHost.addItem(host);

		OpsiServiceNOMPersistenceController persist = PersistenceControllerFactory.getPersistenceController();
		Set<String> depots = persist.getDepotDataService().getDepotPropertiesForPermittedDepots().keySet();

		// remove login host name if identical with depot fqdn
		depots.remove(host);
		for (String depot : depots) {
			jComboBoxHost.addItem(depot);
		}

		Logging.debug(this, "init host " + host);
		jComboBoxHost.setSelectedItem(host);

		jComboBoxHost.addItemListener(itemEvent -> checkComponentStates());

		JLabel jLabelPort = new JLabel();
		jLabelPort.setText(Configed.getResourceValue("SSHConnection.Config.jLabelPort"));
		jTextFieldPort = new JTextField(new CheckedDocument(/* allowedChars */
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', }, 5), String.valueOf("22"), 1);
		jTextFieldPort.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		JLabel jLabelUser = new JLabel();
		jLabelUser.setText(Configed.getResourceValue("SSHConnection.Config.jLabelUser"));
		jTextFieldUser = new JTextField();
		jTextFieldUser.setText(connectionInfo.getUser());
		jTextFieldUser.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		JLabel jLabelPassword = new JLabel();
		jLabelPassword.setText(Configed.getResourceValue("SSHConnection.Config.jLabelPassword"));

		jTextFieldPassword = new JPasswordField();
		jTextFieldPassword.setText(connectionInfo.getPassw());

		jTextFieldPassword.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		jButtonSave = new JButton(Configed.getResourceValue("SSHConnection.buttonConnect"));
		jButtonSave.setToolTipText(Configed.getResourceValue("SSHConnection.Config.SaveConfiguration"));

		jButtonKill = new JButton(Configed.getResourceValue("SSHConnection.buttonDisconnect"));
		jButtonKill.setToolTipText(Configed.getResourceValue("SSHConnection.Config.StopUsing"));
		jButtonKill.addActionListener(actionEvent -> doAction2());

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));
		jButtonClose.addActionListener(actionEvent -> doAction1());

		buttonPanel.add(new JLabel("            "));

		Logging.info(this, "actionlistener for button1 " + PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonSave.addActionListener(actionEvent -> doAction3());
		}

		iconButtonOpenChooser = new IconButton(Configed.getResourceValue("SSHConnection.Config.SelectKeyFile"),
				"images/folder_16.png", " ", "images/folder_16.png", true);
		iconButtonOpenChooser.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH / 4, Globals.BUTTON_HEIGHT));
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			iconButtonOpenChooser.addActionListener(actionEvent -> doActionOeffnen());
		}

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);

		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createSequentialGroup()
				.addComponent(jButtonClose, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jButtonKill, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jButtonSave, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		buttonPanelLayout.setVerticalGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(jButtonClose, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jButtonKill, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jButtonSave, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		JLabel jLabelKeyFile = new JLabel();
		jLabelKeyFile.setText(Configed.getResourceValue("SSHConnection.Config.jLabelKeyfile"));
		jTextFieldKeyFile = new JTextField();
		jTextFieldKeyFile.setText(connectionInfo.getKeyfilePath());
		jTextFieldKeyFile.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		JLabel jLabelPassphrase = new JLabel();
		jLabelPassphrase.setText(Configed.getResourceValue("SSHConnection.Config.jLabelPassphrase"));
		jTextFieldPassphrase = new JPasswordField();
		jTextFieldPassphrase.setEnabled(false);
		jTextFieldPassphrase.setText(connectionInfo.getKeyfilePassphrase());
		jTextFieldPassphrase.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkComponentStates();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		jCheckBoxUseKeyFile = new JCheckBox(Configed.getResourceValue("SSHConnection.Config.useKeyfile"));
		jCheckBoxUseKeyFile.setSelected(false);
		jTextFieldPassword.setEnabled(false);
		jTextFieldKeyFile.setEnabled(false);
		jCheckBoxUseKeyFile.addItemListener(this::applyUseKeyFile);

		jCheckBoxDefault = new JCheckBox(Configed.getResourceValue("SSHConnection.Config.useDefaultAuthentication"));
		jCheckBoxDefault.setSelected(true);

		setComponentsEditable(false);
		jCheckBoxDefault.addItemListener(this::applyDefault);

		if (!connectionInfo.getKeyfilePath().isEmpty()) {
			jCheckBoxUseKeyFile.setSelected(true);
		}

		jCheckBoxUseOutputColor = new JCheckBox(Configed.getResourceValue("SSHConnection.Config.coloredOutput"));
		jCheckBoxUseOutputColor.setToolTipText(Configed.getResourceValue("SSHConnection.Config.coloredOutput.tooltip"));
		jCheckBoxUseOutputColor.setSelected(true);
		SSHCommandFactory.setColoredOutput(true);
		jCheckBoxUseOutputColor.addItemListener(itemEvent -> checkComponentStates());

		jCheckBoxExecInBackground = new JCheckBox(
				Configed.getResourceValue("SSHConnection.Config.AlwaysExecBackground"));
		jCheckBoxExecInBackground
				.setToolTipText(Configed.getResourceValue("SSHConnection.Config.AlwaysExecBackground.tooltip"));
		jCheckBoxExecInBackground.setSelected(SSHCommandFactory.alwaysExecInBackground());
		jCheckBoxExecInBackground.addItemListener(itemEvent -> checkComponentStates());

		Logging.debug(this, "sshConfigDialog building layout ");
		connectionPanelLayout.setHorizontalGroup(connectionPanelLayout.createSequentialGroup()
				.addGroup(connectionPanelLayout.createParallelGroup()
						.addComponent(jCheckBoxDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGroup(connectionPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addComponent(jLabelHost, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelPort, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelUser, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelPassword, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.GAP_SIZE)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addComponent(jComboBoxHost, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addComponent(jTextFieldPort, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addComponent(jTextFieldUser, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addComponent(jTextFieldPassword, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
								.addGap(Globals.GAP_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addComponent(jCheckBoxUseKeyFile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGroup(connectionPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addComponent(jLabelKeyFile, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelPassphrase, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.GAP_SIZE)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addGroup(connectionPanelLayout.createSequentialGroup()
												.addComponent(jTextFieldKeyFile, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
												.addComponent(iconButtonOpenChooser, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
										.addComponent(jTextFieldPassphrase, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

								.addGap(Globals.GAP_SIZE))
						.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(jLabelConnectionState,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)))
				.addContainerGap());

		connectionPanelLayout.setVerticalGroup(connectionPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jCheckBoxDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jComboBoxHost, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelHost, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jTextFieldPort, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelPort, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jTextFieldUser, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelUser, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jTextFieldPassword, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelPassword, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(jCheckBoxUseKeyFile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(iconButtonOpenChooser, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldKeyFile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelKeyFile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jTextFieldPassphrase, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelPassphrase, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(
						jLabelConnectionState, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addContainerGap(70, 70));

		settingsPanelLayout.setHorizontalGroup(settingsPanelLayout.createSequentialGroup()
				.addGroup(settingsPanelLayout.createParallelGroup()
						.addComponent(jCheckBoxUseOutputColor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(jCheckBoxExecInBackground, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)));
		settingsPanelLayout.setVerticalGroup(settingsPanelLayout.createSequentialGroup()
				.addComponent(jCheckBoxUseOutputColor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(10).addComponent(jCheckBoxExecInBackground, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}

	private void setComponentsEnabledRO(boolean value) {
		jCheckBoxDefault.setEnabled(value);
		jCheckBoxUseOutputColor.setEnabled(value);
		jCheckBoxExecInBackground.setEnabled(value);
		setComponentsEditable(value);
	}

	private void setComponentsEditable(boolean value) {
		jComboBoxHost.setEnabled(value);
		jTextFieldPort.setEnabled(value);
		jTextFieldUser.setEnabled(value);
		if (jCheckBoxUseKeyFile.isSelected()) {
			jTextFieldPassword.setEnabled(false);
		} else {
			jTextFieldPassword.setEnabled(value);
		}
		if (jCheckBoxDefault.isSelected()) {
			jTextFieldPassword.setEnabled(false);
		}
	}

	/* This method gets called when button 1 is pressed */
	@Override
	public void doAction1() {
		Logging.info(this, "doAction1 cb_host.getSelectedItem() " + jComboBoxHost.getSelectedItem());

		super.doAction1();
	}

	/* This method is called when button 2 is pressed */
	@Override
	public void doAction2() {
		jButtonKill.setEnabled(false);

		Logging.info(this, "actionPerformed on btn_kill " + SSHCommandFactory.getInstance().getConnectionState());

		SSHCommandFactory.getInstance().unsetConnection();

		// there seems to be nothing got disconnect
		setSSHState();
	}

	/* This method is called when button 3 is pressed */
	@Override
	public void doAction3() {
		Logging.info(this, "doAction3  ");
		setSSHState();

		if (jCheckBoxDefault.isSelected()) {
			Logging.info(this, "doAction3  cb_useDefault.isSelected true");
			if (!jComboBoxUseDefaultState) {
				// state has changed

				connectionInfo.setUserData(ConfigedMain.getHost(), ConfigedMain.getUser(), ConfigedMain.getPassword(),
						SSHConnect.PORT_SSH);
			}

			jComboBoxHost.setSelectedItem(connectionInfo.getHost());
			jTextFieldUser.setText(connectionInfo.getUser());
			jTextFieldPort.setText(connectionInfo.getPort());
			jTextFieldPassword.setText(connectionInfo.getPassw());
		} else {
			Logging.info(this, "doAction3  cb_useDefault.isSelected false");
			String host = (String) jComboBoxHost.getSelectedItem();
			Logging.info(this, "doAction3 host " + host);

			connectionInfo.setUserData(host, jTextFieldUser.getText(), new String(jTextFieldPassword.getPassword()),
					jTextFieldPort.getText());
		}
		connectionInfo.useKeyfile(false);
		if (jCheckBoxUseKeyFile.isSelected()) {
			Logging.info(this, "doAction3  cb_useKeyfile.isSelected true");
			Logging.info(this, "set keyfile true keyfile " + jTextFieldKeyFile.getText());
			connectionInfo.useKeyfile(true, jTextFieldKeyFile.getText(),
					new String(jTextFieldPassphrase.getPassword()));
			jTextFieldPassword.setText("");
			connectionInfo.setPassw("");
		} else {
			jTextFieldPassphrase.setText("");
			jTextFieldKeyFile.setText("");
		}

		SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);

		String connectionState = factory.testConnection(connectionInfo.getUser(), connectionInfo.getHost());
		ConfigedMain.getMainFrame().updateSSHConnectedInfoMenu(connectionState);

		SSHCommandFactory.setColoredOutput(jCheckBoxUseOutputColor.isSelected());
		SSHCommandFactory.setAlwaysExecInBackground(jCheckBoxExecInBackground.isSelected());
		jComboBoxUseDefaultState = jCheckBoxDefault.isSelected();
		checkComponentStates();
		Logging.info(this, "request focus");
	}

	private void applyUseKeyFile(ItemEvent itemEvent) {
		Boolean value = false;
		if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
			value = true;
		}
		if (!jCheckBoxDefault.isSelected()) {
			jTextFieldPassword.setEnabled(!value);
		}
		iconButtonOpenChooser.setEnabled(value);
		jTextFieldKeyFile.setEnabled(value);
		jTextFieldPassphrase.setEnabled(value);
		checkComponentStates();
	}

	private void doActionOeffnen() {
		final JFileChooser chooser = new JFileChooser("Choose directory");
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		String userDirLocation = System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY);
		File userDir = new File(userDirLocation);
		// default to user directory
		chooser.setCurrentDirectory(userDir);

		chooser.setFileHidingEnabled(false);

		chooser.setVisible(true);
		final int result = chooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File inputVerzFile = chooser.getSelectedFile();
			String inputVerzStr = inputVerzFile.getPath();
			jTextFieldKeyFile.setText(inputVerzStr);
		}
		Logging.info(this, "doActionOeffnen canceled");
		chooser.setVisible(false);
	}

	private void applyDefault(ItemEvent itemEvent) {
		if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
			setComponentsEditable(false);
			jComboBoxHost.setSelectedItem(connectionInfo.getHost());
			jTextFieldUser.setText(connectionInfo.getUser());
			jTextFieldPassword.setText(connectionInfo.getPassw());
			jTextFieldPort.setText(connectionInfo.getPort());
		} else {
			setComponentsEditable(true);
		}

		checkComponentStates();
	}

	public void checkComponents() {
		if (jCheckBoxDefault.isSelected()) {
			connectionInfo.setUserData(ConfigedMain.getHost(), ConfigedMain.getUser(), ConfigedMain.getPassword(),
					SSHConnect.PORT_SSH);
		}
		jComboBoxHost.setSelectedItem(connectionInfo.getHost());
		jTextFieldUser.setText(connectionInfo.getUser());
		jTextFieldPassword.setText(connectionInfo.getPassw());
		jTextFieldPort.setText(connectionInfo.getPort());
	}
}
