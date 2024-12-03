/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
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
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.SeparatedDocument;
import de.uib.utils.thread.WaitingSleeper;
import de.uib.utils.thread.WaitingWorker;
import de.uib.utils.userprefs.UserPreferences;

public class LoginDialog extends JFrame implements WaitingSleeper, KeyListener, FocusListener {
	private static final int SECS_WAIT_FOR_CONNECTION = 100;

	// 5000 reproduceable error
	private static final long TIMEOUT_MS = SECS_WAIT_FOR_CONNECTION * 1000L;

	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;

	private ConfigedMain configedMain;
	private OpsiServiceNOMPersistenceController persistenceController;

	private GlassPane glassPane;

	private WaitingWorker waitingWorker;

	private JLabel jLabelTitle;
	private JLabel jLabelVersion;
	private JLabel jLabelLogo;
	private JLabel jLabelLoadingSSOState;

	private FlatTextField fieldUser = new FlatTextField();

	private FlatPasswordField passwordField = new FlatPasswordField();
	private FlatPasswordField fieldOTP = new FlatPasswordField();

	private FlatComboBox<String> fieldHost = new FlatComboBox<>();

	private JCheckBox checkUseOTP;

	private JButton jButtonCancel;
	private JButton jButtonCommit;
	private JButton jButtonSSO;
	private boolean ssoActiveByServer;
	private Map<String, Boolean> ssoServerStates;

	public LoginDialog(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;
		ssoServerStates = new HashMap<>();
		initGuiElements();
		setupLayout();

		finishAndMakeVisible();

		initGlassPane();
	}

	private void initGlassPane() {
		glassPane = new GlassPane();

		setGlassPane(glassPane);
	}

	public void setHost(String host) {
		if (host == null) {
			host = "";
		}
		fieldHost.setSelectedItem(host);
		fieldUser.requestFocus();
		initSSO();
	}

	public void setServers(List<String> hosts) {
		if (hosts == null) {
			hosts = List.of("localhost");
		}
		Logging.debug(this, "setServers " + hosts);
		fieldHost.setModel(new DefaultComboBoxModel<>(hosts.toArray(new String[0])));
		initSSO();
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

	private void setActivated(boolean active) {
		Logging.info(this, "activate");

		glassPane.activate(!active);

		if (active) {
			glassPane.setInfoText(null);
		} else {
			glassPane.setInfoText(Configed.getResourceValue("LoginDialog.WaitInfo.label"));
		}
	}

	private void initGuiElements() {
		setTitle(Configed.getResourceValue("LoginDialog.title"));
		setIconImage(Utils.getMainIcon());

		// Opsilogo
		String logoPath;
		if (FlatLaf.isLafDark()) {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_ohne_Text_quer_neg.png";
		} else {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_kurz_quer.png";
		}

		jLabelLogo = new JLabel(Utils.createImageIcon(logoPath, null, 150, 50));

		jLabelLoadingSSOState = new JLabel(Configed.getResourceValue("LoginDialog.loadingSSOState"),
				SwingConstants.CENTER);
		jLabelLoadingSSOState.setBorder(new EmptyBorder(3, 0, 0, 0));

		jLabelTitle = new JLabel(Globals.APPNAME);
		jLabelVersion = new JLabel(Configed.getResourceValue("LoginDialog.version") + "  " + Globals.VERSION + "  ("
				+ Globals.VERDATE + ") ");

		fieldHost.setPlaceholderText(Configed.getResourceValue("LoginDialog.placeholderHost"));
		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.getEditor().getEditorComponent().addKeyListener(this);
		// initSSO() is called when the user selects a host from dropdown or the hostTextfield looses focus
		fieldHost.getEditor().getEditorComponent().addFocusListener(this);
		fieldHost.addActionListener((ActionEvent e) -> {
			initSSO();
		});

		fieldUser.setPlaceholderText(Configed.getResourceValue("username"));
		fieldUser.addKeyListener(this);
		fieldUser.setMargin(new Insets(0, 3, 0, 3));

		passwordField.setPlaceholderText(Configed.getResourceValue("password"));
		passwordField.addKeyListener(this);
		passwordField.setMargin(new Insets(0, 3, 0, 3));

		fieldOTP.setDocument(new SeparatedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }, 6,
				Character.MIN_VALUE, 6, true));
		fieldOTP.setPlaceholderText(Configed.getResourceValue("LoginDialog.placeholderOTP"));
		fieldOTP.addKeyListener(this);
		fieldOTP.setVisible(false);
		fieldOTP.setPreferredSize(new Dimension(0, 0));

		checkUseOTP = new JCheckBox(Configed.getResourceValue("LoginDialog.checkUseOTP"));
		checkUseOTP.setToolTipText(Configed.getResourceValue("LoginDialog.checkUseOTP.toolTip"));
		checkUseOTP.addItemListener((ItemEvent event) -> {
			showOTPField(checkUseOTP.isSelected());
			UserPreferences.setBoolean(UserPreferences.OTP, checkUseOTP.isSelected());
		});
		checkUseOTP.setSelected(UserPreferences.getBoolean(UserPreferences.OTP));

		jButtonCancel = new JButton(Configed.getResourceValue("LoginDialog.jButtonCancel"));
		jButtonCancel.addActionListener((ActionEvent e) -> endProgram());

		jButtonCommit = new JButton(Configed.getResourceValue("LoginDialog.jButtonCommit"));
		jButtonCommit.addActionListener((ActionEvent e) -> tryConnecting());

		jButtonSSO = new JButton(Configed.getResourceValue("LoginDialog.jButtonSSO"));
		jButtonSSO.addActionListener(actionEvent -> tryConnecting(true));
	}

	private void initSSO() {
		Logging.debug(this, "initSSO");
		String host = (String) fieldHost.getSelectedItem() != null ? (String) fieldHost.getSelectedItem()
				: ConfigedMain.getHost();
		Boolean state = ssoServerStates.get(host);
		if (state != null) {
			Logging.debug(this, "use cached value for host " + host + " " + state);
			// use cached value if set to true (otherwise try to get it from server and decide later)
			initSSOitems(state);
			return;
		}

		Logging.info(this, "check authentication method for host " + host);
		if (host == null || host.isEmpty()) {
			Logging.info(this, "No host provided");
			return;
		}
		jLabelLoadingSSOState.setVisible(true);
		ssoActiveByServer = false;
		ServerFacade serverFacade = new ServerFacade(host, false);
		Map<String, List<String>> headers = serverFacade.getHeaders();

		if (headers != null && headers.containsKey("X-opsi-auth-methods")) {
			String authMethods = headers.get("X-opsi-auth-methods").toString();
			ssoActiveByServer = authMethods.contains("saml");
			// cache status of server
			ssoServerStates.put(host, ssoActiveByServer);
			Logging.debug("Authentication methods for host " + host + ": ",
					authMethods + " SSO active: " + ssoActiveByServer);
		}
		initSSOitems(ssoActiveByServer);
	}

	private void initSSOitems(boolean ssoActive) {
		ssoActiveByServer = ssoActive;
		jLabelLoadingSSOState.setVisible(false);
		jButtonSSO.setVisible(ssoActiveByServer);
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
	}

	private void setupLayout() {
		// JPanel panel = new JPanel();

		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		((JComponent) getContentPane()).setBorder(padding);

		GroupLayout groupLayout = new GroupLayout(((JComponent) getContentPane()));

		groupLayout.setHonorsVisibility(false);
		((JComponent) getContentPane()).setLayout(groupLayout);

		ParallelGroup parGroup = groupLayout.createParallelGroup().addComponent(jButtonCancel,
				GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
		SequentialGroup seqGroup = groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jButtonCancel, 120, 120, 120).addGap(0, 0, Short.MAX_VALUE);

		if (!ssoActiveByServer) {
			parGroup.addComponent(jLabelLoadingSSOState, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE);
			seqGroup.addComponent(jLabelLoadingSSOState, 120, 120, 120).addGap(0, 0, Globals.GAP_SIZE);
			seqGroup.addComponent(jButtonCommit, 120, 120, 120).addGap(Globals.GAP_SIZE);
		} else {
			parGroup.addComponent(jButtonSSO, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE);
			seqGroup.addComponent(jButtonSSO, 120, 120, 120).addGap(0, 0, Globals.GAP_SIZE);
			seqGroup.addComponent(jButtonCommit, 120, 120, 120).addGap(Globals.GAP_SIZE);
		}
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

		// this.getContentPane().add(panel);
	}

	private void finishAndMakeVisible() {
		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		Logging.notice(" OS " + strOS + "  Version " + osVersion);

		fieldHost.requestFocus();

		// Sets the window on the main screen
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void actAfterWaiting() {
		Logging.debug("Act after waiting");
		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED
				&& ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3")) {
			glassPane.setInfoText(Configed.getResourceValue("LoadingObserver.start"));

			// we can finish
			Logging.info(this, "connected with persis " + persistenceController);
			configedMain.setPersistenceController(persistenceController);
			configedMain.loadDataAndGo();
		} else {
			// Clear cache
			CacheManager.getInstance().clearAllCachedData();
			// return to Passwordfield
			if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.INTERRUPTED) {
				// return to password dialog
				Logging.info(this, "interrupted");
			} else {
				Logging.info(this, "not connected, timeout or not authorized");

				String message;

				if (waitingWorker != null && waitingWorker.isTimeoutReached()) {
					message = Configed.getResourceValue("LoginDialog.timeoutReached");
				} else if (!ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3")) {
					message = Configed.getResourceValue("LoginDialog.oldServerVersion");
				} else {
					message = new MessageFormat(
							Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.content")).format(
									new Object[] { PersistenceControllerFactory.getConnectionState().getMessage() });
				}

				JOptionPane.showMessageDialog(this, message,
						Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.title"),
						JOptionPane.ERROR_MESSAGE);
			}

			if (PersistenceControllerFactory.getConnectionState().getMessage().indexOf("authorized") > -1) {
				Logging.info(this, "(not) authorized");

				fieldUser.requestFocus();
				fieldUser.setCaretPosition(fieldUser.getText().length());
			} else {
				fieldHost.requestFocus();
			}
		}

		setActivated(true);
	}

	@Override
	public JProgressBar getProgressBar() {
		return new JProgressBar();
	}

	@Override
	public JLabel getLabel() {
		return new JLabel();
	}

	@Override
	public long getStartActionMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public long getWaitingMillis() {
		return TIMEOUT_MS;
	}

	@Override
	public long getOneProgressBarLengthWaitingMillis() {
		return ESTIMATED_TOTAL_WAIT_MILLIS;
	}

	@Override
	public String setLabellingStrategy(long millisLevel) {
		return "";
	}

	public void tryConnecting() {
		tryConnecting(false);
	}

	public void tryConnectingDependOnServer(boolean requestSSO) {
		if (!ssoActiveByServer) {
			ssoActiveByServer = false;
			initSSO();
		}

		if (!ssoActiveByServer && requestSSO) {
			Logging.error("SSO not available. Consider to remove parameter or activate sso for this server.");
			return;
		}
		tryConnecting(requestSSO);
	}

	public void tryConnecting(boolean useSSO) {
		Logging.debug(this, "started  tryConnecting with sso " + useSSO);
		setActivated(false);
		ConfigedMain.setHost((String) fieldHost.getSelectedItem());
		String user = fieldUser.getText().toLowerCase(Locale.ROOT);
		if (useSSO) {
			ConfigedMain.setUseSSO(useSSO);
		} else {
			ConfigedMain.setUser(user);
			ConfigedMain.setPassword(String.valueOf(passwordField.getPassword()));
		}

		Logging.info(this,
				"invoking PersistenceControllerFactory host, user, " + fieldHost.getSelectedItem() + ", " + user);

		Configed.setHost((String) fieldHost.getSelectedItem());
		Configed.initSavedStates();
		if (waitingWorker != null && !waitingWorker.isReady()) {
			Logging.info(this, "old waiting task not ready");
			return;
		}

		Logging.info(this, "we are in EventDispatchThread " + SwingUtilities.isEventDispatchThread());
		Logging.info(this, "  Thread.currentThread() " + Thread.currentThread());
		Logging.info(this, "start WaitingWorker");
		waitingWorker = new WaitingWorker(this);
		waitingWorker.execute();

		new Thread() {
			@Override
			public void run() {
				Logging.info(this, "get persis");
				boolean invalidHost = fieldHost.getSelectedItem() == null
						|| fieldHost.getSelectedItem().toString().isEmpty();
				boolean invalidUser = user == null || user.isEmpty();
				boolean invalidPw = String.valueOf(passwordField.getPassword()) == null
						|| String.valueOf(passwordField.getPassword()).isEmpty();
				if (!useSSO && (invalidHost || invalidUser || invalidPw)) {
					Logging.error(this, "No host, user or password provided");
					Logging.debug(this,
							"Validate credentials: invalids " + invalidHost + " " + invalidUser + " " + invalidPw);
					setActivated(true);
					return;
				}

				persistenceController = PersistenceControllerFactory.getNewPersistenceController(
						(String) fieldHost.getSelectedItem(), user, String.valueOf(passwordField.getPassword()),
						String.valueOf(fieldOTP.getPassword()), useSSO);

				Logging.info(this, "got persis, == null " + (persistenceController == null));

				Logging.info(this, "waitingTask can be set to ready");
				waitingWorker.setReady();
			}
		}.start();
	}

	private void endProgram() {
		configedMain.finishApp(false, 0);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			endProgram();
		}
	}

	@Override
	public void focusGained(java.awt.event.FocusEvent e) {
		fieldHost.getEditor().selectAll();
	}

	@Override
	public void focusLost(java.awt.event.FocusEvent e) {
		initSSO();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (ssoActiveByServer) {
				tryConnecting(true);
			} else {
				if (e.getSource() == fieldHost.getEditor().getEditorComponent()) {
					fieldUser.requestFocus();
				} else if (fieldUser.getText() == null || fieldUser.getText().isEmpty()) {
					fieldUser.requestFocus();
				} else if (passwordField.getPassword() == null || passwordField.getPassword().length == 0) {
					passwordField.requestFocus();
				} else {
					tryConnecting();
				}
			}
		} else if (e.getKeyCode() == KeyEvent.VK_TAB) {
			if (e.getSource() == fieldHost.getEditor().getEditorComponent()) {
				fieldUser.requestFocus();
			} else if (e.getSource() == fieldUser) {
				passwordField.requestFocus();
			} else if (e.getSource() == passwordField) {
				fieldHost.requestFocus();
			} else {
				fieldHost.requestFocus();
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
