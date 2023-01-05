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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.GregorianCalendar;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.JSONthroughHTTP;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
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
public class DPassword extends JDialog implements WaitingSleeper// implements Runnable
{
	private static final String TESTSERVER = "";
	private static final String TESTUSER = "";
	private static final String TESTPASSWORD = "";
	private static final int SECS_WAIT_FOR_CONNECTION = 100;
	private static final long TIMEOUT_MS = SECS_WAIT_FOR_CONNECTION * 1000l; // 5000 reproduceable error

	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 6000;

	private ConfigedMain main; // controller
	private PersistenceController persis;

	private WaitCursor waitCursor;

	private WaitingWorker waitingWorker;

	private Containership containership;

	private JLabel jLabelVersion = new JLabel();
	private JLabel jLabelJavaVersion = new JLabel();
	private JLabel jLabelLabelJavaVersion = new JLabel(configed.getResourceValue("DPassword.jdkVersionBased"));

	private JLabel jLabelUser = new JLabel();
	private JTextField fieldUser = new JTextField();

	private JPasswordField passwordField = new JPasswordField();
	private JLabel jLabelPassword = new JLabel();

	private JLabel jLabelHost = new JLabel();
	private JComboBox<String> fieldHost = new JComboBox<>();

	private JPanel jPanelButtons = new JPanel();
	private FlowLayout flowLayoutButtons = new FlowLayout();

	private JProgressBar jProgressBar = new JProgressBar();
	private JLabel waitLabel = new JLabel();
	private long timeOutMillis = TIMEOUT_MS;

	private JButton jButtonCommit = new JButton();
	private JButton jButtonCancel = new JButton();

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
		if (host == null)
			host = "";
		fieldHost.setSelectedItem(host);
		fieldUser.requestFocus();

	}

	public void setServers(List<String> hosts) {
		fieldHost.setModel(new DefaultComboBoxModel<>(hosts.toArray(new String[0])));
		((JTextField) fieldHost.getEditor().getEditorComponent())
				.setCaretPosition(((String) fieldHost.getSelectedItem()).length());
	}

	public void setUser(String user) {
		if (user == null)
			user = "";
		fieldUser.setText(user);
		passwordField.requestFocus();
	}

	public void setPassword(String password) {
		if (password == null)
			password = "";
		passwordField.setText(password);
	}

	public DPassword(ConfigedMain main) {
		super();
		this.main = main;

		guiInit();
		pack();
		setVisible(true);
	}

	private void setActivated(boolean active) {
		logging.info(this, "------------ activate");

		jProgressBar.setValue(0);

		waitLabel.setText(active ? "" : configed.getResourceValue("DPassword.WaitInfo.label"));
		setEnabled(active);
	}

	private void guiInit() {
		MessageFormat messageFormatTitle = new MessageFormat(configed.getResourceValue("DPassword.title"));
		setTitle(messageFormatTitle.format(new Object[] { Globals.APPNAME }));

		setIconImage(Globals.mainIcon);

		JPanel panel = new JPanel();

		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		panel.setBorder(padding);

		jLabelHost.setText(configed.getResourceValue("DPassword.jLabelHost"));

		containership = new Containership(panel);

		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.addKeyListener(newKeyListener);

		jLabelUser.setText(configed.getResourceValue("DPassword.jLabelUser"));

		fieldUser.setText(TESTUSER);
		fieldUser.addKeyListener(newKeyListener);

		fieldUser.setMargin(new Insets(0, 3, 0, 3));

		jLabelPassword.setText(configed.getResourceValue("DPassword.jLabelPassword"));

		passwordField.setText(TESTPASSWORD);
		passwordField.addKeyListener(newKeyListener);
		passwordField.setMargin(new Insets(0, 3, 0, 3));

		JCheckBox checkCompression = new JCheckBox(configed.getResourceValue("DPassword.checkCompression"),
				JSONthroughHTTP.compressTransmission);
		checkCompression.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {

				JSONthroughHTTP.compressTransmission = (e.getStateChange() == ItemEvent.SELECTED);

				logging.debug(this, "itemStateChanged " + de.uib.opsicommand.JSONthroughHTTP.gzipTransmission);
			}
		});

		JCheckBox checkTrySSH = new JCheckBox(configed.getResourceValue("DPassword.checkTrySSH"),
				configed.sshconnect_onstart);
		logging.info(this, "checkTrySSH  " + configed.sshconnect_onstart);
		checkTrySSH.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {

				configed.sshconnect_onstart = (e.getStateChange() == ItemEvent.SELECTED);

				logging.info(this, "checkTrySSH itemStateChanged " + checkTrySSH);
			}
		});

		final JTextField fieldRefreshMinutes = new JTextField("" + configed.refreshMinutes);
		fieldRefreshMinutes.setToolTipText(configed.getResourceValue("DPassword.pullReachableInfoTooltip"));
		fieldRefreshMinutes.setPreferredSize(new Dimension(Globals.shortlabelDimension));
		fieldRefreshMinutes.setHorizontalAlignment(SwingConstants.RIGHT);
		fieldRefreshMinutes.getDocument().addDocumentListener(new DocumentListener() {

			private void setRefreshMinutes() {
				String s = fieldRefreshMinutes.getText();

				try {
					configed.refreshMinutes = Integer.valueOf(s);

				} catch (NumberFormatException ex) {
					fieldRefreshMinutes.setText("");
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {

				setRefreshMinutes();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {

				setRefreshMinutes();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {

				setRefreshMinutes();
			}
		});

		JPanel jPanelParameters = new PanelLinedComponents(new JComponent[] {
				// checkTrySSH, checkGzip
				checkTrySSH, checkCompression });

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarPainter(Globals.opsiLogoBlue));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new ProgressBarPainter(Globals.opsiLogoLightBlue));
		jProgressBar.putClientProperty("Nimbus.Overrides", defaults);

		jPanelButtons.setLayout(flowLayoutButtons);

		jButtonCommit.setText(configed.getResourceValue("DPassword.jButtonCommit"));
		jButtonCommit.setMaximumSize(new Dimension(100, 20));
		jButtonCommit.setPreferredSize(new Dimension(100, 20));
		jButtonCommit.setSelected(true);
		jButtonCommit.addActionListener(this::jButtonCommitActionPerformed);

		jButtonCancel.setText(configed.getResourceValue("DPassword.jButtonCancel"));
		jButtonCancel.setMaximumSize(new Dimension(100, 20));
		jButtonCancel.setPreferredSize(new Dimension(100, 20));
		jButtonCancel.addActionListener(this::jButtonCancelActionPerformed);

		jPanelButtons.add(jButtonCommit);
		jPanelButtons.add(jButtonCancel);

		GroupLayout groupLayout = new GroupLayout(panel);
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

				.addComponent(waitLabel, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)

				.addComponent(jProgressBar, Globals.PROGRESS_BAR_HEIGHT, Globals.PROGRESS_BAR_HEIGHT,
						Globals.PROGRESS_BAR_HEIGHT)

				.addGap(Globals.LINE_HEIGHT / 2)

				.addComponent(jPanelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

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
						.addGroup(groupLayout.createSequentialGroup().addComponent(fieldHost,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jLabelUser,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup().addComponent(fieldUser,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(
								jLabelPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup().addComponent(passwordField,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE)
								.addComponent(jPanelParameters, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGap(Globals.VGAP_SIZE))

						.addComponent(jProgressBar)

						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE)
								.addComponent(waitLabel).addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE))

						.addGroup(groupLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(
								jPanelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)));

		this.getContentPane().add(panel);

		Containership csPanel = new Containership(getContentPane());

		csPanel.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_7 },
				JPanel.class);

		MessageFormat messageFormatVersion = new MessageFormat(configed.getResourceValue("DPassword.jLabelVersion"));
		jLabelVersion.setText(messageFormatVersion
				.format(new Object[] { Globals.VERSION, "(" + Globals.VERDATE + ")", Globals.VERHASHTAG }));

		jLabelJavaVersion.setText(configed.JAVA_VENDOR + " " + configed.JAVA_VERSION);

		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		logging.notice(" OS " + strOS + "  Version " + osVersion);
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
	}

	@Override
	public void actAfterWaiting() {
		waitCursor.stop();

		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED) {
			// we can finish
			logging.info(this, "connected with persis " + persis);
			main.setPersistenceController(persis);

			MessageFormat messageFormatMainTitle = new MessageFormat(
					configed.getResourceValue("ConfigedMain.appTitle"));
			main.setAppTitle(messageFormatMainTitle
					.format(new Object[] { Globals.APPNAME, fieldHost.getSelectedItem(), fieldUser.getText() }));
			main.loadDataAndGo();
		} else { // return to Passwordfield
			if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.INTERRUPTED) {
				// return to password dialog
				logging.info(this, "interrupted");
			} else {
				logging.info(this, "not connected, timeout or not authorized");

				MessageFormat messageFormatDialogContent = new MessageFormat(
						configed.getResourceValue("DPassword.noConnectionMessageDialog.content"));

				if (waitingWorker != null && waitingWorker.isTimeoutReached())
					messageFormatDialogContent = new MessageFormat("Timeout in connecting");

				JOptionPane.showMessageDialog(this,
						messageFormatDialogContent.format(
								new Object[] { PersistenceControllerFactory.getConnectionState().getMessage() }),
						configed.getResourceValue("DPassword.noConnectionMessageDialog.title"),
						JOptionPane.INFORMATION_MESSAGE);
			}

			passwordField.setText("");
			if (PersistenceControllerFactory.getConnectionState().getMessage().indexOf("authorized") > -1) {
				logging.info(this, "(not) authorized");

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
		return waitLabel;
	}

	@Override
	public long getStartActionMillis() {
		return new GregorianCalendar().getTimeInMillis();
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
		return waitLabel.getText();
	}

	@Override
	public void setCursor(Cursor c) {
		super.setCursor(c);
		try {
			containership.doForAllContainedCompis("setCursor", new Object[] { c });
		} catch (Exception ex) {
			logging.warning(this, "containership error", ex);
		}

	}

	private void okAction() {
		logging.info(this, "ok_action");

		// we make first a waitCursor and a waitInfo window

		if (waitCursor != null)
			waitCursor.stop(); // we want only one running instance

		// correctly

		tryConnecting();

	}

	public void tryConnecting() {
		logging.info(this, "started  tryConnecting");
		setActivated(false);

		waitCursor = new WaitCursor(this, "ok_action");

		ConfigedMain.HOST = (String) fieldHost.getSelectedItem();
		ConfigedMain.USER = fieldUser.getText();
		ConfigedMain.PASSWORD = String.valueOf(passwordField.getPassword());
		logging.info(this, "invoking PersistenceControllerFactory host, user, " + fieldHost.getSelectedItem() + ", "
				+ fieldUser.getText()

		);

		if (waitingWorker != null && !waitingWorker.isReady()) {

			logging.info(this, "old waiting task not ready");
			return;
		}

		logging.info(this, "we are in EventDispatchThread " + SwingUtilities.isEventDispatchThread());
		logging.info(this, "  Thread.currentThread() " + Thread.currentThread());
		boolean localApp = ("" + Thread.currentThread()).indexOf("main]") > -1;
		logging.info(this, "is local app  " + localApp);
		if (localApp) {

			logging.info(this, "start WaitingWorker");
			waitingWorker = new WaitingWorker(this);
			waitingWorker.execute();

			new Thread() {
				@Override
				public void run() {
					logging.info(this, "get persis");
					persis = PersistenceControllerFactory.getNewPersistenceController(
							(String) fieldHost.getSelectedItem(), fieldUser.getText(),
							String.valueOf(passwordField.getPassword()));

					logging.info(this, "got persis, == null " + (persis == null));

					logging.info(this, "waitingTask can be set to ready");
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

			if (waited >= TIMEOUT_MS)
				logging.error(" no connection");
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
		main.finishApp(false, 0);
	}

	private void jButtonCancelActionPerformed(ActionEvent e) {
		if (waitCursor != null)
			waitCursor.stop();
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
