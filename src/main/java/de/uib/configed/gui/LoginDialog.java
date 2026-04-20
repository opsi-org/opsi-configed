/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.HostData;
import de.uib.configed.core.infrastructure.ServerFacade;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.WindowsPositionManager;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.swing.SeparatedDocument;
import de.uib.configed.share.FileUtils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.userprefs.UserPreferences;
import net.miginfocom.swing.MigLayout;

public class LoginDialog extends JFrame {
	private GlassPane glassPane;

	private JLabel jLabelTitle;
	private JLabel jLabelVersion;
	private JLabel jLabelLogo;

	private JLabel jLabelHost;
	private JLabel jLabelUser;
	private JLabel jLabelPassword;
	private JLabel jLabelOTP;

	private JTextField fieldUser = new JTextField();

	private JPasswordField passwordField = new JPasswordField();
	private JPasswordField fieldOTP = new JPasswordField();

	private JComboBox<String> fieldHost = new JComboBox<>();

	private JCheckBox checkUseOTP;

	private JButton jButtonCancel;
	private JButton jButtonCommit;
	private JButton jButtonSSO;
	private Boolean ssoActiveByServer;
	private FocusListener myFocusListener = new FocusListener() {
		@Override
		public void focusGained(FocusEvent e) {
			fieldHost.getEditor().selectAll();
		}

		@Override
		public void focusLost(FocusEvent e) {
			initSSO();
		}
	};

	public LoginDialog(HostData hostData) {
		super();

		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		initGuiElements();
		setupLayout();
		setServers();

		finishAndMakeVisible();

		initGlassPane();

		setHostData(hostData);
	}

	private void setHostData(HostData hostData) {
		if (hostData.getHost() != null && !hostData.getHost().isEmpty()) {
			fieldHost.setSelectedItem(hostData.getHost());
			fieldUser.requestFocus();
			initSSO();
		}

		if (hostData.getUser() != null) {
			fieldUser.setText(hostData.getUser());
			passwordField.requestFocus();
		}

		if (hostData.getPassword() != null) {
			passwordField.setText(hostData.getPassword());
		}

		if (hostData.getOtp() != null) {
			checkUseOTP.setSelected(!hostData.getOtp().isEmpty());
			fieldOTP.setText(hostData.getOtp());
		}

		Logging.info("become interactive");
		Logging.info("using sso ? ", hostData.useSSO());
		setVisible(true);

		if (hostData.getHost() == null) {
			Logging.info("host is not set (yet)");
		} else if (!hostData.useSSO() && (hostData.getUser() == null || hostData.getPassword() == null)) {
			Logging.info("user or password not given (yet)");
		} else {
			// This must be called last, so that loading frame for connection is called last
			// and on top of the login-frame
			Logging.info("loginDialog tryConnecting with sso ", hostData.useSSO());
			tryConnectingDependOnServer(hostData.useSSO());
		}
	}

	private void initGlassPane() {
		glassPane = new GlassPane();

		setGlassPane(glassPane);
	}

	private void setServers() {
		List<String> savedServers = FileUtils.readLocallySavedServerNames();
		fieldHost.setModel(new DefaultComboBoxModel<>(
				savedServers.isEmpty() ? new String[] { "localhost" } : savedServers.toArray(new String[0])));
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

		jLabelHost = SwingUtils.createBoldLabel("LoginDialog.placeholderHost");

		jLabelUser = SwingUtils.createBoldLabel("username");
		jLabelPassword = SwingUtils.createBoldLabel("password");

		jLabelOTP = SwingUtils.createBoldLabel("LoginDialog.placeholderOTP");
		jLabelOTP.setVisible(false);

		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		((JTextField) fieldHost.getEditor().getEditorComponent())
				.addActionListener(actionEvent -> loginFromHostField());
		fieldHost.getEditor().getEditorComponent().addFocusListener(myFocusListener);
		fieldUser.addActionListener(e -> loginFromUserField());
		passwordField.addActionListener(e -> tryConnecting(false));

		fieldOTP.setDocument(new SeparatedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }, 6,
				Character.MIN_VALUE, 6, false));
		fieldOTP.addActionListener(e -> tryConnecting(false));
		fieldOTP.setVisible(false);

		checkUseOTP = new JCheckBox(Configed.getResourceValue("LoginDialog.checkUseOTP"));
		checkUseOTP.setToolTipText(Configed.getResourceValue("LoginDialog.checkUseOTP.toolTip"));
		checkUseOTP.addItemListener(itemEvent -> showOTPField(checkUseOTP.isSelected()));
		checkUseOTP.setSelected(UserPreferences.getBoolean(UserPreferences.OTP));

		jButtonCancel = new JButton(Configed.getResourceValue("LoginDialog.jButtonCancel"));
		jButtonCancel.addActionListener(actionEvent -> ConfigedMain.finishApp(false, 0));

		jButtonCommit = new JButton(Configed.getResourceValue("LoginDialog.jButtonCommit"));
		jButtonCommit.addActionListener(actionEvent -> tryConnecting());
		jButtonSSO = new JButton(Configed.getResourceValue("LoginDialog.jButtonSSO"));
		jButtonSSO.addActionListener(actionEvent -> tryConnecting(true));
	}

	private static boolean wasSuccessfullyAuthenticated() {
		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();
		if (persistenceController != null) {
			Logging.warning("Connection state ", persistenceController.getConnectionState());
			return true;
		}
		return false;
	}

	private void initSSO() {
		Logging.info("LoginDialog.initSSO");
		String host = (String) fieldHost.getSelectedItem();
		if (host == null || host.isEmpty()) {
			Logging.debug(this, "No host provided");
			return;
		} else if (wasSuccessfullyAuthenticated()) {
			Logging.info("was connected");
		} else {
			// Not needed.
		}
		ssoActiveByServer = true;
		Logging.info("get auth info for ", host);
		ServerFacade serverFacade = new ServerFacade(host, false);
		Map<String, List<String>> headers = serverFacade.getHeaders();

		if (headers != null && headers.containsKey("X-opsi-auth-methods")) {
			String authMethods = headers.get("X-opsi-auth-methods").toString();
			ssoActiveByServer = authMethods.contains("saml");
			Logging.debug("Authentication methods for host ", host, ": ", authMethods);
		} else {
			ssoActiveByServer = false;
		}

		jButtonSSO.setVisible(ssoActiveByServer);
		pack();
		Logging.notice("SSO active by server ", ssoActiveByServer);
	}

	private void showOTPField(boolean show) {
		fieldOTP.setVisible(show);
		jLabelOTP.setVisible(show);
		pack();

		UserPreferences.setBoolean(UserPreferences.OTP, show);
	}

	private void setupLayout() {
		JComponent cp = (JComponent) getContentPane();
		cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		cp.setLayout(new MigLayout("insets 0, wrap 1", "[grow]", "[]0"));

		int minGap = Globals.GAP_SIZE;
		int prefGap = 100;

		cp.add(jLabelLogo, "gapleft " + minGap + ":" + prefGap + ":push, gapright " + minGap + ":" + prefGap
				+ ":push, alignx center");
		cp.add(jLabelTitle, "gapleft " + minGap + ":" + prefGap + ":push, gapright " + minGap + ":" + prefGap
				+ ":push, alignx center");
		cp.add(jLabelVersion, "gapbottom " + Globals.GAP_SIZE + ", gapleft " + minGap + ":" + prefGap
				+ ":push, gapright " + minGap + ":" + prefGap + ":push, alignx center");

		cp.add(jLabelHost);
		cp.add(fieldHost, "growx, pushx, gapbottom " + Globals.GAP_SIZE);

		cp.add(jLabelUser);
		cp.add(fieldUser, "growx, pushx, gapbottom " + Globals.GAP_SIZE);

		cp.add(jLabelPassword);
		cp.add(passwordField, "growx, pushx, gapbottom " + Globals.GAP_SIZE);

		cp.add(checkUseOTP, "gapbottom " + Globals.GAP_SIZE);

		cp.add(jLabelOTP, "hidemode 3");
		cp.add(fieldOTP, "growx, pushx, hidemode 2, gapbottom " + Globals.GAP_SIZE * 2);

		JPanel buttonPanel = new JPanel(new MigLayout("insets 0, fillx"));
		buttonPanel.add(jButtonCancel, "align left");
		buttonPanel.add(jButtonSSO, "align center, pushx");
		buttonPanel.add(jButtonCommit, "align right");

		cp.add(buttonPanel, "growx, pushx");
	}

	private void finishAndMakeVisible() {
		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		Logging.notice(" OS ", strOS, "  Version ", osVersion);

		fieldHost.requestFocus();
		// Sets the window on the main screen
		pack();
		if (WindowsPositionManager
				.isOnAnyScreen(WindowsPositionManager.getWindowBounds(WindowsPositionManager.MAIN_WINDOW))) {
			WindowsPositionManager.centerDialogOnWindowScreen(this);
		} else {
			setLocationRelativeTo(null);
		}
		setResizable(false);
		setVisible(true);
	}

	public void tryConnecting() {
		tryConnecting(false);
	}

	private void tryConnectingDependOnServer(boolean requestSSO) {
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
		Configed.initSavedStates(fieldHost.getSelectedItem().toString().toLowerCase(Locale.ROOT));

		Logging.info(this, "we are in EventDispatchThread ", SwingUtilities.isEventDispatchThread());
		Logging.info(this, "  Thread.currentThread() ", Thread.currentThread());
		Logging.info(this, "starting thread");

		new LoginThread(this, new HostData((String) fieldHost.getSelectedItem(), user,
				String.valueOf(passwordField.getPassword()), String.valueOf(fieldOTP.getPassword()), useSSO, false))
						.start();
	}

	private void loginFromHostField() {
		initSSO();
		if (Boolean.TRUE.equals(ssoActiveByServer)) {
			tryConnecting(true);
		} else {
			fieldUser.requestFocus();
		}
	}

	private void loginFromUserField() {
		if (Boolean.TRUE.equals(ssoActiveByServer)) {
			tryConnecting(true);
		} else if (passwordField.getPassword().length == 0) {
			passwordField.requestFocus();
		} else {
			tryConnecting(false);
		}
	}
}
