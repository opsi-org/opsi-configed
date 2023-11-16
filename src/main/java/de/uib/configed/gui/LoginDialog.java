/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.sshcommand.SSHConnectionInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PanelLinedComponents;
import de.uib.utilities.thread.WaitingSleeper;
import de.uib.utilities.thread.WaitingWorker;
import utils.Utils;

public class LoginDialog extends JFrame implements WaitingSleeper {
	private static final int SECS_WAIT_FOR_CONNECTION = 100;

	// 5000 reproduceable error
	private static final long TIMEOUT_MS = SECS_WAIT_FOR_CONNECTION * 1000L;

	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;

	private ConfigedMain configedMain;
	private OpsiServiceNOMPersistenceController persistenceController;

	private GlassPane glassPane;

	private WaitingWorker waitingWorker;

	private JLabel jLabelTitle = new JLabel();
	private JLabel jLabelVersion = new JLabel();
	private JLabel jLabelLogo = new JLabel();

	private JLabel jLabelUser = new JLabel();
	private JTextField fieldUser = new JTextField();

	private JPasswordField passwordField = new JPasswordField();
	private JLabel jLabelPassword = new JLabel();

	private JLabel jLabelHost = new JLabel();
	private JComboBox<String> fieldHost = new JComboBox<>();

	private JPanel jPanelParameters;
	private JCheckBox checkTrySSH;

	private long timeOutMillis = TIMEOUT_MS;

	private JButton jButtonCancel = new JButton();
	private JButton jButtonCommit = new JButton();

	private KeyListener newKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				okAction();
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				endProgram();
			} else {
				// Do nothing with other keys
			}
		}
	};

	public LoginDialog(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;

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
	}

	public void setServers(List<String> hosts) {
		fieldHost.setModel(new DefaultComboBoxModel<>(hosts.toArray(new String[0])));
		((JTextField) fieldHost.getEditor().getEditorComponent())
				.setCaretPosition(((String) fieldHost.getSelectedItem()).length());
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

	private void setActivated(boolean active) {
		Logging.info(this, "activate");

		glassPane.activate(!active);

		if (active) {
			glassPane.setInfoText(null);
		} else {
			glassPane.setInfoText(Configed.getResourceValue("DPassword.WaitInfo.label"));
		}

		fieldHost.setEnabled(active);
		fieldUser.setEnabled(active);
		passwordField.setEnabled(active);
		checkTrySSH.setEnabled(active);
		jButtonCommit.setEnabled(active);
	}

	private void initGuiElements() {
		setTitle(Globals.APPNAME + " " + Configed.getResourceValue("DPassword.title"));

		setIconImage(Utils.getMainIcon());

		// Opsilogo
		String logoPath;
		if (FlatLaf.isLafDark()) {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_ohne_Text_quer_neg.png";
		} else {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_kurz_quer.png";
		}

		jLabelLogo = new JLabel(Utils.createImageIcon(logoPath, null, 150, 50));

		jLabelTitle.setText(Globals.APPNAME);
		jLabelVersion.setText(Configed.getResourceValue("DPassword.version") + "  " + Globals.VERSION + "  ("
				+ Globals.VERDATE + ") " + Globals.VERHASHTAG);

		jLabelHost.setText(Configed.getResourceValue("DPassword.jLabelHost"));

		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.addKeyListener(newKeyListener);

		jLabelUser.setText(Configed.getResourceValue("DPassword.jLabelUser"));

		fieldUser.addKeyListener(newKeyListener);
		fieldUser.setMargin(new Insets(0, 3, 0, 3));

		jLabelPassword.setText(Configed.getResourceValue("DPassword.jLabelPassword"));

		passwordField.addKeyListener(newKeyListener);
		passwordField.setMargin(new Insets(0, 3, 0, 3));

		checkTrySSH = new JCheckBox(Configed.getResourceValue("DPassword.checkTrySSH"),
				Configed.isSSHConnectionOnStart());
		Logging.info(this, "checkTrySSH  " + Configed.isSSHConnectionOnStart());
		checkTrySSH.addItemListener(Configed.sshConnectOnStartListener);

		jPanelParameters = new PanelLinedComponents(new JComponent[] { checkTrySSH });

		jButtonCancel.setText(Configed.getResourceValue("DPassword.jButtonCancel"));
		jButtonCancel.addActionListener((ActionEvent e) -> endProgram());

		jButtonCommit.setText(Configed.getResourceValue("DPassword.jButtonCommit"));
		jButtonCommit.addActionListener((ActionEvent e) -> okAction());
	}

	private void setupLayout() {
		JPanel panel = new JPanel();

		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		panel.setBorder(padding);

		GroupLayout groupLayout = new GroupLayout(panel);

		// With this, the jProgressBar will take up the vertical
		// space even when it's invisible
		groupLayout.setHonorsVisibility(false);
		panel.setLayout(groupLayout);

		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addComponent(jLabelLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.LINE_HEIGHT)

				.addComponent(jLabelHost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(2).addComponent(fieldHost, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.LINE_HEIGHT)
				.addComponent(jLabelUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(2).addComponent(fieldUser, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(2)
				.addComponent(jLabelPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(2).addComponent(passwordField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				.addComponent(jPanelParameters, (int) (1.2 * Globals.LINE_HEIGHT), (int) (1.2 * Globals.LINE_HEIGHT),
						(int) (1.2 * Globals.LINE_HEIGHT))

				.addGap(Globals.LINE_HEIGHT / 2, Globals.LINE_HEIGHT / 2, Globals.LINE_HEIGHT / 2)

				.addGroup(groupLayout.createParallelGroup()
						.addComponent(jButtonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

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

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jLabelHost,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				.addComponent(fieldHost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jLabelUser,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				.addComponent(fieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jLabelPassword,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jPanelParameters, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE))

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jButtonCancel, 120, 120, 120).addGap(0, 0, Short.MAX_VALUE)
						.addComponent(jButtonCommit, 120, 120, 120).addGap(Globals.GAP_SIZE)));

		this.getContentPane().add(panel);
	}

	private void finishAndMakeVisible() {
		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		Logging.notice(" OS " + strOS + "  Version " + osVersion);

		pack();

		setHost("localhost");
		fieldHost.requestFocus();
		((JTextField) fieldHost.getEditor().getEditorComponent())
				.setCaretPosition(((String) (fieldHost.getSelectedItem())).length());

		// Sets the window on the main screen
		setLocationRelativeTo(null);

		pack();
		setVisible(true);
	}

	@Override
	public void actAfterWaiting() {
		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED) {
			glassPane.setInfoText(Configed.getResourceValue("LoadingObserver.start"));

			// we can finish
			Logging.info(this, "connected with persis " + persistenceController);
			configedMain.setPersistenceController(persistenceController);
			configedMain.setAppTitle(
					"(" + fieldUser.getText() + ") " + fieldHost.getSelectedItem() + " - " + Globals.APPNAME);
			configedMain.loadDataAndGo();
		} else {
			// return to Passwordfield

			if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.INTERRUPTED) {
				// return to password dialog
				Logging.info(this, "interrupted");
			} else {
				Logging.info(this, "not connected, timeout or not authorized");

				MessageFormat messageFormatDialogContent = new MessageFormat(
						Configed.getResourceValue("DPassword.noConnectionMessageDialog.content"));

				if (waitingWorker != null && waitingWorker.isTimeoutReached()) {
					messageFormatDialogContent = new MessageFormat("Timeout in connecting");
				}

				JOptionPane.showMessageDialog(this,
						messageFormatDialogContent.format(
								new Object[] { PersistenceControllerFactory.getConnectionState().getMessage() }),
						Configed.getResourceValue("DPassword.noConnectionMessageDialog.title"),
						JOptionPane.INFORMATION_MESSAGE);
			}

			if (PersistenceControllerFactory.getConnectionState().getMessage().indexOf("authorized") > -1) {
				Logging.info(this, "(not) authorized");

				fieldUser.requestFocus();
				fieldUser.setCaretPosition(fieldUser.getText().length());
			} else {
				fieldHost.requestFocus();
			}

			setActivated(true);
		}
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
		return timeOutMillis;
	}

	@Override
	public long getOneProgressBarLengthWaitingMillis() {
		return ESTIMATED_TOTAL_WAIT_MILLIS;
	}

	@Override
	public String setLabellingStrategy(long millisLevel) {
		return "";
	}

	private void okAction() {
		Logging.info(this, "ok_action");

		// correctly
		tryConnecting();
	}

	public void tryConnecting() {
		Logging.info(this, "started  tryConnecting");
		setActivated(false);

		ConfigedMain.setHost((String) fieldHost.getSelectedItem());
		ConfigedMain.setUser(fieldUser.getText());
		ConfigedMain.setPassword(String.valueOf(passwordField.getPassword()));
		Logging.info(this, "invoking PersistenceControllerFactory host, user, " + fieldHost.getSelectedItem() + ", "
				+ fieldUser.getText());

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
				configedMain.initSavedStates();
				persistenceController = PersistenceControllerFactory.getNewPersistenceController(
						(String) fieldHost.getSelectedItem(), fieldUser.getText(),
						String.valueOf(passwordField.getPassword()));

				Logging.info(this, "got persis, == null " + (persistenceController == null));

				Logging.info(this, "waitingTask can be set to ready");
				waitingWorker.setReady();
			}
		}.start();

		SSHConnectionInfo.getInstance().setUser(fieldUser.getText());

		SSHConnectionInfo.getInstance().setPassw(String.valueOf(passwordField.getPassword()));
		SSHConnectionInfo.getInstance().setHost((String) fieldHost.getSelectedItem());
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
}
