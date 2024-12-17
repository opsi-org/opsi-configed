/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.SeparatedDocument;
import de.uib.utils.userprefs.UserPreferences;

public class LoginDialog extends JFrame implements KeyListener {
	private ConfigedMain configedMain;

	private GlassPane glassPane;

	private JLabel jLabelTitle;
	private JLabel jLabelVersion;
	private JLabel jLabelLogo;

	private FlatTextField fieldUser = new FlatTextField();

	private FlatPasswordField passwordField = new FlatPasswordField();
	private FlatPasswordField fieldOTP = new FlatPasswordField();

	private FlatComboBox<String> fieldHost = new FlatComboBox<>();

	private JCheckBox checkUseOTP;

	private JButton jButtonCancel;
	private JButton jButtonCommit;
	private JButton jButtonSSO;
	private Boolean ssoActiveByServer;
	private FocusListener myFocusListener = new FocusListener() {
		@Override
		public void focusGained(java.awt.event.FocusEvent e) {
			fieldHost.getEditor().selectAll();
		}

		@Override
		public void focusLost(java.awt.event.FocusEvent e) {
			initSSO();
		}
	};

	public LoginDialog(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;
		initGuiElements();
		setupLayout();
		setServers();

		finishAndMakeVisible();

		initGlassPane();
		initSSO();
	}

	private void initGlassPane() {
		glassPane = new GlassPane();

		setGlassPane(glassPane);
	}

	public void setHost(String host) {
		fieldHost.setSelectedItem(host);
		fieldUser.requestFocus();
		initSSO();
	}

	private void setServers() {
		List<String> savedServers = Utils.readLocallySavedServerNames();

		if (savedServers.isEmpty()) {
			savedServers.add("localhost");
		}

		fieldHost.setModel(new DefaultComboBoxModel<>(savedServers.toArray(new String[0])));
	}

	public void setUser(String user) {
		if (user == null) {
			user = "";
		}
		fieldUser.setText(user);
		passwordField.requestFocus();
	}

	public void setPassword(String password) {
		if (password == null) {
			password = "";
		}
		passwordField.setText(password);
	}

	public void setOTP(String otp) {
		if (otp == null) {
			otp = "";
		}
		checkUseOTP.setSelected(!otp.isEmpty());
		fieldOTP.setText(otp);
	}

	public void setActivated(boolean active) {
		Logging.info(this, "activate");

		glassPane.activate(!active);

		if (active) {
			glassPane.setInfoText(null);
		} else {
			glassPane.setInfoText(Configed.getResourceValue("LoginDialog.WaitInfo.label"));
		}
	}

	public void setInfoText(String text) {
		glassPane.setInfoText(text);
	}

	public void returnToLogin() {
		if (PersistenceControllerFactory.getConnectionState().getMessage().indexOf("authorized") > -1) {
			Logging.info(this, "(not) authorized");

			fieldUser.requestFocus();
			fieldUser.setCaretPosition(fieldUser.getText().length());
		} else {
			fieldHost.requestFocus();
		}

		setActivated(true);
	}

	private void initGuiElements() {
		setTitle(Configed.getResourceValue("LoginDialog.title"));
		setIconImage(Icons.getMainIcon());

		jLabelLogo = new JLabel(Icons.getOpsiLogoWide());
		jLabelTitle = new JLabel(Globals.APPNAME);
		jLabelVersion = new JLabel(Configed.getResourceValue("LoginDialog.version") + "  " + Globals.VERSION + "  ("
				+ Globals.VERDATE + ") ");

		fieldHost.setPlaceholderText(Configed.getResourceValue("LoginDialog.placeholderHost"));
		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.getEditor().getEditorComponent().addKeyListener(this);
		fieldHost.getEditor().getEditorComponent().addFocusListener(myFocusListener);

		fieldUser.setPlaceholderText(Configed.getResourceValue("username"));
		fieldUser.addKeyListener(this);

		passwordField.setPlaceholderText(Configed.getResourceValue("password"));
		passwordField.addKeyListener(this);

		fieldOTP.setDocument(new SeparatedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }, 6,
				Character.MIN_VALUE, 6, true));
		fieldOTP.setPlaceholderText(Configed.getResourceValue("LoginDialog.placeholderOTP"));
		fieldOTP.addKeyListener(this);
		fieldOTP.setVisible(false);
		fieldOTP.setPreferredSize(new Dimension(0, 0));

		checkUseOTP = new JCheckBox(Configed.getResourceValue("LoginDialog.checkUseOTP"));
		checkUseOTP.setToolTipText(Configed.getResourceValue("LoginDialog.checkUseOTP.toolTip"));
		checkUseOTP.addItemListener(itemEvent -> showOTPField(checkUseOTP.isSelected()));
		checkUseOTP.setSelected(UserPreferences.getBoolean(UserPreferences.OTP));

		jButtonCancel = new JButton(Configed.getResourceValue("LoginDialog.jButtonCancel"));
		jButtonCancel.addActionListener(actionEvent -> endProgram());

		jButtonCommit = new JButton(Configed.getResourceValue("LoginDialog.jButtonCommit"));
		jButtonCommit.addActionListener(actionEvent -> tryConnecting());
		jButtonSSO = new JButton(Configed.getResourceValue("LoginDialog.jButtonSSO"));
		jButtonSSO.addActionListener(actionEvent -> tryConnecting(true));
	}

	private void initSSO() {
		String host = (ConfigedMain.getHost() != null) ? ConfigedMain.getHost() : (String) fieldHost.getSelectedItem();
		if (host == null || host.isEmpty()) {
			Logging.debug(this, "No host provided");
			return;
		}

		ssoActiveByServer = true;
		Logging.info("get auth info for ", host);
		ServerFacade serverFacade = new ServerFacade(host, false);
		Map<String, List<String>> headers = serverFacade.getHeaders();

		if (headers != null && headers.containsKey("X-opsi-auth-methods")) {
			String authMethods = headers.get("X-opsi-auth-methods").toString();
			ssoActiveByServer = authMethods.contains("saml");
			Logging.debug("Authentication methods for host ", host, ": ", authMethods);
		}
		jButtonSSO.setVisible(ssoActiveByServer);
		Logging.notice("SSO active by server ", ssoActiveByServer);
		setupLayout();
	}

	private void showOTPField(boolean show) {
		if (show) {
			fieldOTP.setVisible(true);
			fieldOTP.setPreferredSize(new Dimension(Globals.LINE_HEIGHT, Globals.LINE_HEIGHT));
			setSize(getPreferredSize());
		} else {
			fieldOTP.setVisible(false);
			fieldOTP.setPreferredSize(new Dimension(0, 0));
			setSize(getPreferredSize());
		}

		UserPreferences.setBoolean(UserPreferences.OTP, show);
	}

	private void setupLayout() {
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		((JComponent) getContentPane()).setBorder(padding);

		GroupLayout groupLayout = new GroupLayout(getContentPane());

		groupLayout.setHonorsVisibility(false);
		getContentPane().setLayout(groupLayout);
		ParallelGroup parGroup = groupLayout.createParallelGroup().addComponent(jButtonCancel,
				GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
		SequentialGroup seqGroup = groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jButtonCancel, 120, 120, 120).addGap(0, 0, Short.MAX_VALUE);

		if (Boolean.TRUE.equals(ssoActiveByServer)) {
			parGroup.addComponent(jButtonSSO, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE);
			seqGroup.addComponent(jButtonSSO, 120, 120, 120).addGap(0, 0, Globals.GAP_SIZE);
		}

		seqGroup.addComponent(jButtonCommit, 120, 120, 120).addGap(Globals.GAP_SIZE);
		parGroup.addComponent(jButtonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE);

		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addComponent(jLabelLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.LINE_HEIGHT)

				.addGap(2).addComponent(fieldHost, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.LINE_HEIGHT)
				.addComponent(fieldUser, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(passwordField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(fieldOTP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(checkUseOTP, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				.addGap(Globals.LINE_HEIGHT / 2, Globals.LINE_HEIGHT / 2, Globals.LINE_HEIGHT / 2)

				.addGroup(parGroup));
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE)
						.addComponent(jLabelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE))

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE)
						.addComponent(jLabelLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE)
						.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE))

				.addComponent(fieldHost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(fieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(fieldOTP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(checkUseOTP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(seqGroup));
	}

	private void finishAndMakeVisible() {
		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		Logging.notice(" OS ", strOS, "  Version ", osVersion);

		fieldHost.requestFocus();
		// Sets the window on the main screen
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	public void tryConnecting() {
		tryConnecting(false);
	}

	public void tryConnectingDependOnServer(boolean requestSSO) {
		if (ssoActiveByServer == null) {
			ssoActiveByServer = false;
			initSSO();
		}

		if ((!ssoActiveByServer) && requestSSO) {
			Logging.error("SSO not available. Consider to remove parameter or activate sso for this server.");
			return;
		}
		tryConnecting(requestSSO);
	}

	public void tryConnecting(boolean useSSO) {
		Logging.info(this, "started  tryConnecting with SSO ", useSSO);
		setActivated(false);
		String user = fieldUser.getText().toLowerCase(Locale.ROOT);
		if (useSSO) {
			ConfigedMain.setUseSSO(useSSO);
		} else {
			ConfigedMain.setUser(user);
			ConfigedMain.setPassword(String.valueOf(passwordField.getPassword()));
		}
		ConfigedMain.setHost((String) fieldHost.getSelectedItem());
		Configed.initSavedStates();

		Logging.info(this, "we are in EventDispatchThread ", SwingUtilities.isEventDispatchThread());
		Logging.info(this, "  Thread.currentThread() ", Thread.currentThread());
		Logging.info(this, "starting thread");

		new LoginThread(this, configedMain, fieldHost.getSelectedItem(), user, passwordField.getPassword(),
				fieldOTP.getPassword(), useSSO).start();
	}

	private static void endProgram() {
		ConfigedMain.finishApp(false, 0);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			endProgram();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (e.getSource() == passwordField) {
				tryConnecting(false);
			} else if (Boolean.TRUE.equals(ssoActiveByServer)) {
				tryConnecting(true);
			} else if (e.getSource() == fieldHost.getEditor().getEditorComponent() || fieldUser.getText() == null
					|| fieldUser.getText().isEmpty()) {
				fieldUser.requestFocus();
			} else if (passwordField.getPassword() == null || passwordField.getPassword().length == 0) {
				passwordField.requestFocus();
			} else {
				tryConnecting(false);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			endProgram();
		} else {
			// Do nothing with other keys
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Not needed here
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Not needed here
	}
}
