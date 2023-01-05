package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnect;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.CheckedDocument;

public class SSHConfigDialog extends /* javax.swing.JDialog */ FGeneralDialog {
	private JPanel connectionPanel = new JPanel();
	private JPanel settingsPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private static JCheckBox cb_useDefault;
	private static JCheckBox cb_useKeyfile;
	private JCheckBox cb_useOutputColor;
	private JCheckBox cb_execInBackground;
	private JButton btn_save;
	private JButton btn_openChooser;
	private JButton btn_close;
	private JButton btn_kill;

	private JLabel lbl_keyfile = new JLabel();
	private JLabel lbl_passphrase = new JLabel();
	private JLabel lbl_host = new JLabel();
	private JLabel lbl_user = new JLabel();
	private JLabel lbl_passw = new JLabel();
	private JLabel lbl_port = new JLabel();
	private JLabel lbl_connectionState = new JLabel();

	private static JComboBox<String> cb_host;
	private static JTextField tf_keyfile;
	private static JPasswordField tf_passphrase;
	private static JTextField tf_user;
	private static JTextField tf_port;
	private static JPasswordField tf_passw;
	private static boolean cb_useDefault_state;
	private ConfigedMain configedMain;
	private static SSHConfigDialog instance;
	private static SSHConnectionInfo connectionInfo = null;

	private SSHConfigDialog(ConfigedMain cmain) {
		super(null, configed.getResourceValue("MainFrame.jMenuSSHConfig"), false);
		configedMain = cmain;
		connectionInfo = SSHConnectionInfo.getInstance();

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		init();

		this.setSize(500, 535);
		setLocationRelativeTo(Globals.mainFrame);
		this.setVisible(true);
		cb_useDefault_state = cb_useDefault.isSelected();
		if (Globals.isGlobalReadOnly()) {
			setComponentsEnabled_RO(false);
		}
	}

	public static SSHConfigDialog getInstance(ConfigedMain cmain) {
		if (instance == null)
			instance = new SSHConfigDialog(cmain);
		else
			instance.setLocationRelativeTo(Globals.mainFrame);

		instance.setVisible(true);
		checkComponents();
		return instance;
	}

	private void checkComponentStates() {
		boolean state = compareStates();
		logging.debug(this, "checkComponentStates  identical " + state);

		setSSHState();
	}

	private boolean compareStates() {
		logging.info(this, "compareStates ");

		if (connectionInfo.getHost() == null) {
			logging.info(this, "probably host not in hostsAllowed");
			return false;
		}

		if (!cb_useDefault.isSelected()) {
			if (!connectionInfo.getHost().equals(cb_host.getSelectedItem())) {
				logging.debug(this, "compareStates 1");
				return false;
			}
			if (!connectionInfo.getUser().equals(tf_user.getText())) {
				logging.debug(this, "compareStates 2");
				return false;
			}
			if (!connectionInfo.getPassw().equals(new String(tf_passw.getPassword()))) {
				logging.debug(this, "compareStates 3");
				logging.debug(this, "connection.getPW " + connectionInfo.getPassw());
				logging.debug(this, "tf.getPW " + new String(tf_passw.getPassword()));
				return false;
			}
			if ((!connectionInfo.getPort().equals(tf_port.getText())) && (!connectionInfo.usesKeyfile())) {
				logging.debug(this, "compareStates 4");
				return false;
			}
		} else {

			if (!connectionInfo.getHost().equals(ConfigedMain.HOST)) {
				logging.info(this,
						"compareStates 5 >" + connectionInfo.getHost() + "<     <>    >" + ConfigedMain.HOST + "<");
				return false;
			}
			if (!connectionInfo.getPort().equals(tf_port.getText())) {
				logging.debug(this, "compareStates 6");
				return false;
			}
			if (!connectionInfo.getUser().equals(ConfigedMain.USER)) {
				logging.debug(this, "compareStates 7");
				return false;
			}
			if ((!connectionInfo.getPassw().equals(ConfigedMain.PASSWORD)) && (!connectionInfo.usesKeyfile())) {
				logging.debug(this, "compareStates 8");
				return false;
			}
		}

		logging.debug(this, "compareStates until now == ");

		if (connectionInfo.usesKeyfile() != (cb_useKeyfile.isSelected())) {
			logging.info(this, "compareStates 9");
			return false;
		} else if (cb_useKeyfile.isSelected()) {
			try {

				if (!connectionInfo.getKeyfilePath().equals(tf_keyfile.getText())) {
					logging.debug(this, "compareStates 10");
					return false;
				}

				String pp = Arrays.toString(tf_passphrase.getPassword());

				if (!connectionInfo.getKeyfilePassphrase().equals(pp)) {
					logging.debug(this, "compareStates 11");
					return false;
				}
			} catch (Exception e) {
				logging.warning(this, "Error", e);
			}
		}

		if (cb_useOutputColor != null) {
			SSHCommandFactory.getInstance();
			logging.debug(this, "compareStates  (factory.ssh_colored_output != cb_useOutputColor.isSelected()) "
					+ SSHCommandFactory.ssh_colored_output + " != " + cb_useOutputColor.isSelected());
			SSHCommandFactory.getInstance();
			if (SSHCommandFactory.ssh_colored_output != cb_useOutputColor.isSelected()) {
				logging.debug(this, "compareStates 12");
				return false;
			}
		}
		if (cb_execInBackground != null) {
			logging.debug(this,
					"compareStates  (factory.ssh_always_exec_in_background != cb_execInBackground.isSelected()) "
							+ SSHCommandFactory.ssh_always_exec_in_background + " != "
							+ cb_execInBackground.isSelected());
			SSHCommandFactory.getInstance();
			if (SSHCommandFactory.ssh_always_exec_in_background != cb_execInBackground.isSelected()) {
				logging.debug(this, "compareStates 13");
				return false;
			}
		}

		return true;
	}

	private void setSSHState() {
		String str = SSHCommandFactory.getInstance().getConnectionState();
		logging.info(this, "setSSHState " + str);
		String labeltext = "<html><font color='blue'>" + str + "</font></html>";

		btn_save.setEnabled(!str.equals(SSHCommandFactory.CONNECTED));
		btn_kill.setEnabled(str.equals(SSHCommandFactory.CONNECTED));

		if (str.equals(SSHCommandFactory.CONNECTED)) {
			labeltext = "<html><font color='green'>" + str + "</font></html>";
		} else {
			if (str.equals(SSHCommandFactory.NOT_CONNECTED))
				labeltext = "<html><font color='red'>" + str + "</font></html>";
		}
		lbl_connectionState.setText(labeltext);
		logging.debug(this, "setSSHState setText " + labeltext);
	}

	protected void init() {
		logging.info(this, "init ");
		connectionPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		settingsPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		getContentPane().add(connectionPanel, BorderLayout.NORTH);
		getContentPane().add(settingsPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		GroupLayout connectionPanelLayout = new GroupLayout(connectionPanel);
		connectionPanel.setLayout(connectionPanelLayout);
		GroupLayout settingsPanelLayout = new GroupLayout(settingsPanel);
		settingsPanel.setLayout(settingsPanelLayout);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

		connectionPanel.setBorder(
				BorderFactory.createTitledBorder(configed.getResourceValue("SSHConnection.Config.serverPanelTitle")));
		connectionPanel.setPreferredSize(new java.awt.Dimension(400, 350));

		settingsPanel.setBorder(
				BorderFactory.createTitledBorder(configed.getResourceValue("SSHConnection.Config.settingsPanelTitle")));

		lbl_host = new JLabel();
		lbl_host.setText(configed.getResourceValue("SSHConnection.Config.jLabelHost"));

		cb_host = new JComboBox<>();
		String host = connectionInfo.getHost();
		if (host == null)
			host = ConfigedMain.HOST;
		cb_host.addItem(host);

		PersistenceController persist = PersistenceControllerFactory.getPersistenceController();
		Set<String> depots = persist.getDepotPropertiesForPermittedDepots().keySet();
		depots.remove(host); // remove login host name if identical with depot fqdn
		for (String depot : depots) {
			cb_host.addItem(depot);
		}

		logging.debug(this, "init host " + host);
		cb_host.setSelectedItem(host);

		cb_host.addItemListener(itemEvent -> checkComponentStates());

		lbl_port = new JLabel();
		lbl_port.setText(configed.getResourceValue("SSHConnection.Config.jLabelPort"));
		tf_port = new JTextField(new CheckedDocument(/* allowedChars */
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', }, 5), String.valueOf("22"), 1);
		tf_port.getDocument().addDocumentListener(new DocumentListener() {
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

		lbl_user = new JLabel();
		lbl_user.setText(configed.getResourceValue("SSHConnection.Config.jLabelUser"));
		tf_user = new JTextField();
		tf_user.setText(connectionInfo.getUser());
		tf_user.getDocument().addDocumentListener(new DocumentListener() {
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

		lbl_passw = new JLabel();
		lbl_passw.setText(configed.getResourceValue("SSHConnection.Config.jLabelPassword"));
		tf_passw = new JPasswordField();
		tf_passw.setText(connectionInfo.getPassw());

		tf_passw.getDocument().addDocumentListener(new DocumentListener() {
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

		btn_save = new IconButton(configed.getResourceValue("SSHConnection.Config.SaveConfiguration"),
				"images/apply.png", "images/apply.png", "images/apply_disabled.png", false);
		btn_save.setPreferredSize(Globals.smallButtonDimension);

		btn_close = new IconButton(configed.getResourceValue("SSHConnection.Config.CancelConfiguration"),
				"images/cancel.png", "images/cancel_over.png", " ", true);
		btn_close.setPreferredSize(Globals.smallButtonDimension);

		btn_kill = new IconButton(configed.getResourceValue("SSHConnection.Config.StopUsing"), "images/edit-delete.png",
				"images/edit-delete.png", "images/edit-delete_disabled.png", false);
		btn_kill.setPreferredSize(Globals.smallButtonDimension);

		btn_kill.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btn_kill.setEnabled(false);

				logging.info(this,
						"actionPerformed on btn_kill " + SSHCommandFactory.getInstance().getConnectionState());

				SSHCommandFactory.getInstance().unsetConnection();

				// there seems to be nothing got disconnect
				setSSHState();
			}
		});

		buttonPanel.add(btn_save);
		buttonPanel.add(btn_kill);

		buttonPanel.add(new JLabel("            "));

		logging.info(this, "actionlistener for button1 " + Globals.isGlobalReadOnly());
		if (!(Globals.isGlobalReadOnly())) {
			btn_save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					logging.debug(this, "actionPerformed on button1");
					doAction1();
				}
			});
		}

		btn_openChooser = new IconButton(configed.getResourceValue("SSHConnection.Config.SelectKeyFile"),
				"images/folder_16.png", " ", "images/folder_16.png", true);
		btn_openChooser.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH / 4, Globals.BUTTON_HEIGHT));
		if (!(Globals.isGlobalReadOnly()))
			btn_openChooser.addActionListener(actionEvent -> doActionOeffnen());

		buttonPanel.add(btn_close);
		btn_close.addActionListener(actionEvent -> doAction2());

		lbl_keyfile = new JLabel();
		lbl_keyfile.setText(configed.getResourceValue("SSHConnection.Config.jLabelKeyfile"));
		tf_keyfile = new JTextField();
		tf_keyfile.setText(connectionInfo.getKeyfilePath());
		tf_keyfile.getDocument().addDocumentListener(new DocumentListener() {
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

		lbl_passphrase = new JLabel();
		lbl_passphrase.setText(configed.getResourceValue("SSHConnection.Config.jLabelPassphrase"));
		tf_passphrase = new JPasswordField();
		tf_passphrase.setEnabled(false);
		tf_passphrase.setText(connectionInfo.getKeyfilePassphrase());
		tf_passphrase.getDocument().addDocumentListener(new DocumentListener() {
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

		cb_useKeyfile = new JCheckBox();
		cb_useKeyfile.setText(configed.getResourceValue("SSHConnection.Config.useKeyfile"));
		cb_useKeyfile.setSelected(false);
		tf_passw.setEnabled(false);
		tf_keyfile.setEnabled(false);
		cb_useKeyfile.addItemListener(itemEvent -> {
			Boolean value = false;
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				value = true;
			}
			if (!cb_useDefault.isSelected())
				tf_passw.setEnabled(!value);
			btn_openChooser.setEnabled(value);
			tf_keyfile.setEnabled(value);
			tf_passphrase.setEnabled(value);
			checkComponentStates();
		});

		cb_useDefault = new JCheckBox();
		cb_useDefault.setText(configed.getResourceValue("SSHConnection.Config.useDefaultAuthentication"));
		cb_useDefault.setSelected(true);

		setComponentsEditable(false);
		cb_useDefault.addItemListener(itemEvent -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				setComponentsEditable(false);
				cb_host.setSelectedItem(connectionInfo.getHost());
				tf_user.setText(connectionInfo.getUser());
				tf_passw.setText(connectionInfo.getPassw());
				tf_port.setText(connectionInfo.getPort());
			} else
				setComponentsEditable(true);
			checkComponentStates();
		});

		if (!connectionInfo.getKeyfilePath().equals(""))
			cb_useKeyfile.setSelected(true);

		cb_useOutputColor = new JCheckBox();
		cb_useOutputColor.setText(configed.getResourceValue("SSHConnection.Config.coloredOutput"));
		cb_useOutputColor.setToolTipText(configed.getResourceValue("SSHConnection.Config.coloredOutput.tooltip"));
		cb_useOutputColor.setSelected(true);
		SSHCommandFactory.ssh_colored_output = true;
		cb_useOutputColor.addItemListener(itemEvent -> checkComponentStates());

		cb_execInBackground = new JCheckBox();
		cb_execInBackground.setText(configed.getResourceValue("SSHConnection.Config.AlwaysExecBackground"));
		cb_execInBackground
				.setToolTipText(configed.getResourceValue("SSHConnection.Config.AlwaysExecBackground.tooltip"));
		cb_execInBackground.setSelected(SSHCommandFactory.ssh_always_exec_in_background);
		cb_execInBackground.addItemListener(itemEvent -> checkComponentStates());

		logging.debug(this, "sshConfigDialog building layout ");
		connectionPanelLayout.setHorizontalGroup(connectionPanelLayout.createSequentialGroup()
				.addGroup(connectionPanelLayout.createParallelGroup()
						.addComponent(cb_useDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGroup(connectionPanelLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE * 2)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addComponent(lbl_host, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lbl_port, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lbl_user, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lbl_passw, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.VGAP_SIZE)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addComponent(cb_host, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE)
										.addComponent(tf_port, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE)
										.addComponent(tf_user, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE)
										.addComponent(tf_passw, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE))
								.addGap(Globals.VGAP_SIZE))
						.addGap(Globals.VGAP_SIZE)
						.addComponent(cb_useKeyfile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGroup(connectionPanelLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE * 2)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addComponent(lbl_keyfile, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lbl_passphrase, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.VGAP_SIZE)
								.addGroup(connectionPanelLayout.createParallelGroup()
										.addGroup(connectionPanelLayout.createSequentialGroup()
												.addComponent(tf_keyfile, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
												.addComponent(btn_openChooser, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
										.addComponent(tf_passphrase, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

								// )
								.addGap(Globals.VGAP_SIZE))
						.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(lbl_connectionState,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)))
				.addContainerGap());

		connectionPanelLayout.setVerticalGroup(connectionPanelLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE)
				.addComponent(cb_useDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(cb_host, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_host, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(tf_port, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_port, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(tf_user, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_user, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(tf_passw, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_passw, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE)
				.addComponent(cb_useKeyfile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(btn_openChooser, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_keyfile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_keyfile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(tf_passphrase, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_passphrase, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(
						lbl_connectionState, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE).addContainerGap(70, 70));

		settingsPanelLayout.setHorizontalGroup(settingsPanelLayout.createSequentialGroup()
				.addGroup(settingsPanelLayout.createParallelGroup()
						.addComponent(cb_useOutputColor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(cb_execInBackground, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)));
		settingsPanelLayout.setVerticalGroup(settingsPanelLayout.createSequentialGroup()
				.addComponent(cb_useOutputColor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(10).addComponent(cb_execInBackground, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	private void setComponentsEnabled_RO(boolean value) {
		cb_useDefault.setEnabled(value);
		cb_useOutputColor.setEnabled(value);
		cb_execInBackground.setEnabled(value);
		setComponentsEditable(value);
	}

	public void setComponentsEditable(boolean value) {
		cb_host.setEnabled(value);
		tf_port.setEnabled(value);
		tf_user.setEnabled(value);
		if (cb_useKeyfile.isSelected())
			tf_passw.setEnabled(false);
		else {
			tf_passw.setEnabled(value);
		}
		if (cb_useDefault.isSelected())
			tf_passw.setEnabled(false);
	}

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		logging.info(this, "doAction1  ");
		setSSHState();

		if (cb_useDefault.isSelected()) {
			logging.info(this, "doAction1  cb_useDefault.isSelected true");
			if (!cb_useDefault_state)
			// state has changed
			{
				connectionInfo.setUserData(ConfigedMain.HOST, ConfigedMain.USER, ConfigedMain.PASSWORD,
						SSHConnect.portSSH);
			}

			cb_host.setSelectedItem(connectionInfo.getHost());
			tf_user.setText(connectionInfo.getUser());
			tf_port.setText(connectionInfo.getPort());
			tf_passw.setText(connectionInfo.getPassw());
		} else {
			logging.info(this, "doAction1  cb_useDefault.isSelected false");
			String host = (String) cb_host.getSelectedItem();
			logging.info(this, "doAction1 host " + host);

			connectionInfo.setUserData(host, tf_user.getText(), new String(tf_passw.getPassword()), tf_port.getText());
		}
		connectionInfo.useKeyfile(false);
		if (cb_useKeyfile.isSelected()) {
			logging.info(this, "doAction1  cb_useKeyfile.isSelected true");
			logging.info(this, "set keyfile true keyfile " + tf_keyfile.getText());
			connectionInfo.useKeyfile(true, tf_keyfile.getText(), new String(tf_passphrase.getPassword()));
			tf_passw.setText("");
			connectionInfo.setPassw("");
		} else {
			tf_passphrase.setText("");
			tf_keyfile.setText("");
		}

		SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);

		factory.testConnection(connectionInfo.getUser(), connectionInfo.getHost());

		SSHCommandFactory.ssh_colored_output = cb_useOutputColor.isSelected();
		SSHCommandFactory.ssh_always_exec_in_background = cb_execInBackground.isSelected();
		cb_useDefault_state = cb_useDefault.isSelected();
		checkComponentStates();
		logging.info(this, "request focus");
	}

	/* This method gets called when button 2 is pressed */
	@Override
	public void doAction2() {
		logging.info(this, "doAction2 cb_host.getSelectedItem() " + cb_host.getSelectedItem());

		super.doAction2();
	}

	public void doActionOeffnen() {
		final JFileChooser chooser = new JFileChooser("Choose directory");
		chooser.setPreferredSize(Globals.filechooserSize);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		String userDirLocation = System.getProperty(logging.envVariableForUserDirectory);
		File userDir = new File(userDirLocation);
		// default to user directory
		chooser.setCurrentDirectory(userDir);

		chooser.setFileHidingEnabled(false);

		chooser.setVisible(true);
		final int result = chooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File inputVerzFile = chooser.getSelectedFile();
			String inputVerzStr = inputVerzFile.getPath();
			tf_keyfile.setText(inputVerzStr);
		}
		logging.info(this, "doActionOeffnen canceled");
		chooser.setVisible(false);
	}

	private static void checkComponents() {
		if (cb_useDefault.isSelected()) {
			connectionInfo.setUserData(

					ConfigedMain.HOST, // persist.getHostInfoCollections().getConfigServer(),
					ConfigedMain.USER, ConfigedMain.PASSWORD, SSHConnect.portSSH);

		}
		cb_host.setSelectedItem(connectionInfo.getHost());
		tf_user.setText(connectionInfo.getUser());
		tf_passw.setText(connectionInfo.getPassw());
		tf_port.setText(connectionInfo.getPort());
	}
}
