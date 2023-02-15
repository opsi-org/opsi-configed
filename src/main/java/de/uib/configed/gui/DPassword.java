/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2021 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

package de.uib.configed.gui;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.border.Border;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.PanelLinedComponents;
import de.uib.utilities.swing.ProgressBarPainter;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.thread.WaitingSleeper;
import de.uib.utilities.thread.WaitingWorker;

/**
 * DPassword description: A JDialog for logging in copyright: Copyright (c)
 * 2000-2016 organization: uib.de
 * 
 * @author D. Oertel; R. Roeder
 */
public class DPassword extends JDialog implements WaitingSleeper {
	private static final String TESTSERVER = "";
	private static final String TESTUSER = "";
	private static final String TESTPASSWORD = "";
	private static final int SECS_WAIT_FOR_CONNECTION = 100;

	// 5000 reproduceable error
	private static final long TIMEOUT_MS = SECS_WAIT_FOR_CONNECTION * 1000l;

	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;

	private ConfigedMain configedMain;
	private AbstractPersistenceController persis;

	private WaitCursor waitCursor;

	private WaitingWorker waitingWorker;

	private Containership containership;

	private JLabel jLabelVersion = new JLabel();
	private JLabel jLabelJavaVersion = new JLabel();
	private JLabel jLabelLabelJavaVersion = new JLabel(Configed.getResourceValue("DPassword.jdkVersionBased"));

	private JLabel jLabelUser = new JLabel();
	private JTextField fieldUser = new JTextField();

	private JPasswordField passwordField = new JPasswordField();
	private JLabel jLabelPassword = new JLabel();

	private JLabel jLabelHost = new JLabel();
	private JComboBox<String> fieldHost = new JComboBox<>();

	private JProgressBar jProgressBar = new JProgressBar();
	private JLabel waitingLabel = new JLabel();

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
			}
		}

	};

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

	public DPassword(ConfigedMain main) {
		super();
		this.configedMain = main;

		guiInit();
	}

	private void setActivated(boolean active) {
		Logging.info(this, "------------ activate");

		jProgressBar.setVisible(!active);
		jProgressBar.setValue(0);

		waitingLabel.setText(active ? "" : Configed.getResourceValue("DPassword.WaitInfo.label"));
		setEnabled(active);
	}

	private void guiInit() {
		MessageFormat messageFormatTitle = new MessageFormat(Configed.getResourceValue("DPassword.title"));
		setTitle(messageFormatTitle.format(new Object[] { Globals.APPNAME }));

		setIconImage(Globals.mainIcon);

		JPanel panel = new JPanel();

		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		panel.setBorder(padding);

		jLabelHost.setText(Configed.getResourceValue("DPassword.jLabelHost"));

		containership = new Containership(panel);

		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.addKeyListener(newKeyListener);

		jLabelUser.setText(Configed.getResourceValue("DPassword.jLabelUser"));

		fieldUser.setText(TESTUSER);
		fieldUser.addKeyListener(newKeyListener);

		fieldUser.setMargin(new Insets(0, 3, 0, 3));

		jLabelPassword.setText(Configed.getResourceValue("DPassword.jLabelPassword"));

		passwordField.setText(TESTPASSWORD);
		passwordField.addKeyListener(newKeyListener);
		passwordField.setMargin(new Insets(0, 3, 0, 3));

		JCheckBox checkTrySSH = new JCheckBox(Configed.getResourceValue("DPassword.checkTrySSH"),
				Configed.sshConnectOnStart);
		Logging.info(this, "checkTrySSH  " + Configed.sshConnectOnStart);
		checkTrySSH.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {

				Configed.sshConnectOnStart = (e.getStateChange() == ItemEvent.SELECTED);

				Logging.info(this, "checkTrySSH itemStateChanged " + checkTrySSH);
			}
		});

		JPanel jPanelParameters = new PanelLinedComponents(new JComponent[] {
				// checkTrySSH, 
				checkTrySSH });

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarPainter(Globals.opsiLogoBlue));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new ProgressBarPainter(Globals.opsiLogoLightBlue));
		jProgressBar.putClientProperty("Nimbus.Overrides", defaults);
		jProgressBar.setVisible(false);

		jButtonCancel.setText(Configed.getResourceValue("DPassword.jButtonCancel"));
		jButtonCancel.addActionListener(this::jButtonCancelActionPerformed);

		jButtonCommit.setText(Configed.getResourceValue("DPassword.jButtonCommit"));
		jButtonCommit.setSelected(true);
		jButtonCommit.addActionListener(this::jButtonCommitActionPerformed);

		GroupLayout groupLayout = new GroupLayout(panel);

		// With this, the jProgressBar will take up the vertical
		// space even when it's invisible
		groupLayout.setHonorsVisibility(false);
		panel.setLayout(groupLayout);

		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGroup(groupLayout.createParallelGroup()
						.addComponent(jLabelLabelJavaVersion, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(jLabelJavaVersion, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))

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

				.addGap(2).addComponent(waitingLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				.addComponent(jProgressBar, Globals.PROGRESS_BAR_HEIGHT, Globals.PROGRESS_BAR_HEIGHT,
						Globals.PROGRESS_BAR_HEIGHT)

				.addGap(Globals.LINE_HEIGHT / 2)

				.addGroup(groupLayout.createParallelGroup()
						.addComponent(jButtonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		groupLayout
				.setHorizontalGroup(groupLayout.createParallelGroup()
						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE)
								.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE)

								.addComponent(jLabelLabelJavaVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE / 2)
								.addComponent(jLabelJavaVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jLabelHost,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

						.addComponent(fieldHost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jLabelUser,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

						.addComponent(fieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(
								jLabelPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

						.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE)
								.addComponent(jPanelParameters, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGap(Globals.VGAP_SIZE))

						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE)
								.addComponent(waitingLabel).addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE))

						.addComponent(jProgressBar)

						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
								.addComponent(jButtonCancel, 120, 120, 120).addGap(0, 0, Short.MAX_VALUE)
								.addComponent(jButtonCommit, 120, 120, 120).addGap(Globals.HGAP_SIZE)));

		this.getContentPane().add(panel);

		Containership csPanel = new Containership(getContentPane());

		csPanel.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_7 },
				JPanel.class);

		MessageFormat messageFormatVersion = new MessageFormat(Configed.getResourceValue("DPassword.jLabelVersion"));
		jLabelVersion.setText(messageFormatVersion
				.format(new Object[] { Globals.VERSION, "(" + Globals.VERDATE + ")", Globals.VERHASHTAG }));

		jLabelJavaVersion.setText(Configed.JAVA_VENDOR + " " + Configed.JAVA_VERSION);

		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		Logging.notice(" OS " + strOS + "  Version " + osVersion);
		String host = TESTSERVER; // ""

		pack();

		if (host.equals("")) {
			setHost("localhost");
			fieldHost.requestFocus();
			((JTextField) fieldHost.getEditor().getEditorComponent())
					.setCaretPosition(((String) (fieldHost.getSelectedItem())).length());
		}

		// Sets the window on the main screen
		setLocationRelativeTo(null);

		pack();
		setVisible(true);
	}

	@Override
	public void actAfterWaiting() {
		waitCursor.stop();

		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED) {
			// we can finish
			Logging.info(this, "connected with persis " + persis);
			configedMain.setPersistenceController(persis);

			MessageFormat messageFormatMainTitle = new MessageFormat(
					Configed.getResourceValue("ConfigedMain.appTitle"));
			configedMain.setAppTitle(messageFormatMainTitle
					.format(new Object[] { Globals.APPNAME, fieldHost.getSelectedItem(), fieldUser.getText() }));
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

			passwordField.setText("");
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
		return jProgressBar;
	}

	@Override
	public JLabel getLabel() {
		return waitingLabel;
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
		return waitingLabel.getText();
	}

	@Override
	public void setCursor(Cursor c) {
		super.setCursor(c);
		try {
			containership.doForAllContainedCompis("setCursor", new Object[] { c });
		} catch (Exception ex) {
			Logging.warning(this, "containership error", ex);
		}

	}

	private void okAction() {
		Logging.info(this, "ok_action");

		// we make first a waitCursor and a waitInfo window

		if (waitCursor != null) {
			// we want only one running instance
			waitCursor.stop();
		}

		// correctly

		tryConnecting();
	}

	public void tryConnecting() {
		Logging.info(this, "started  tryConnecting");
		setActivated(false);

		waitCursor = new WaitCursor(this, "ok_action");

		ConfigedMain.host = (String) fieldHost.getSelectedItem();
		ConfigedMain.user = fieldUser.getText();
		ConfigedMain.password = String.valueOf(passwordField.getPassword());
		Logging.info(this, "invoking PersistenceControllerFactory host, user, " + fieldHost.getSelectedItem() + ", "
				+ fieldUser.getText()

		);

		if (waitingWorker != null && !waitingWorker.isReady()) {

			Logging.info(this, "old waiting task not ready");
			return;
		}

		Logging.info(this, "we are in EventDispatchThread " + SwingUtilities.isEventDispatchThread());
		Logging.info(this, "  Thread.currentThread() " + Thread.currentThread());
		boolean localApp = ("" + Thread.currentThread()).indexOf("main]") > -1;
		Logging.info(this, "is local app  " + localApp);
		if (localApp) {
			Logging.info(this, "start WaitingWorker");
			waitingWorker = new WaitingWorker(this);
			waitingWorker.execute();

			new Thread() {
				@Override
				public void run() {
					Logging.info(this, "get persis");
					persis = PersistenceControllerFactory.getNewPersistenceController(
							(String) fieldHost.getSelectedItem(), fieldUser.getText(),
							String.valueOf(passwordField.getPassword()));

					Logging.info(this, "got persis, == null " + (persis == null));

					Logging.info(this, "waitingTask can be set to ready");
					waitingWorker.setReady();

				}
			}.start();
		} else {
			persis = PersistenceControllerFactory.getNewPersistenceController((String) fieldHost.getSelectedItem(),
					fieldUser.getText(), String.valueOf(passwordField.getPassword()));

			long interval = 2000;
			long waited = 0;

			while ((PersistenceControllerFactory.getConnectionState() == ConnectionState.ConnectionUndefined)
					&& waited < TIMEOUT_MS) {
				Globals.threadSleep(this, interval);
				waited = waited + interval;
			}

			if (waited >= TIMEOUT_MS) {
				Logging.error(" no connection");
			}
		}

		de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setUser(fieldUser.getText());

		de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance()
				.setPassw(String.valueOf(passwordField.getPassword()));
		de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setHost((String) fieldHost.getSelectedItem());
	}

	private void jButtonCommitActionPerformed(ActionEvent e) {
		okAction();
	}

	private void endProgram() {
		configedMain.finishApp(false, 0);
	}

	private void jButtonCancelActionPerformed(ActionEvent e) {
		if (waitCursor != null) {
			waitCursor.stop();
		}
		endProgram();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			endProgram();
		}
	}
}
