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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.PanelLinedComponents;
import de.uib.utilities.swing.ProgressBarPainter;
import de.uib.utilities.thread.WaitCursor;

/**
 * DPassword description: A JDialog for logging in copyright: Copyright (c)
 * 2000-2016 organization: uib.de
 * 
 * @author D. Oertel; R. Roeder
 */
public class DPassword extends JDialog // implements Runnable
{
	private static final String TESTSERVER = "";
	private static final String TESTUSER = "";
	private static final String TESTPASSWORD = "";
	private static final int SECS_WAIT_FOR_CONNECTION = 100;
	private static final long TIMEOUT_MS = SECS_WAIT_FOR_CONNECTION * 1000l; // 5000 reproducable error
	private boolean localApp;

	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;

	ConfigedMain main; // controller
	PersistenceController persis;
	Cursor saveCursor;

	private class WaitInfo extends JFrame implements de.uib.utilities.thread.WaitingSleeper {
		JLabel waitLabel;
		JProgressBar waitingProgressBar;
		long timeOutMillis;

		private WaitInfo(long timeOutMillis) {
			logging.info(this, "created with timeout " + timeOutMillis);
			this.timeOutMillis = timeOutMillis;

			setIconImage(Globals.mainIcon);

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (persis != null && persis.getConnectionState().getState() == ConnectionState.STARTED_CONNECTING)
						// we stop the connect thread as well
						persis.setConnectionState(new ConnectionState(ConnectionState.INTERRUPTED));

					setCursor(saveCursor);
				}
			});

			// setSize (350,100);
			setTitle(Globals.APPNAME + " login");
			waitLabel = new JLabel();
			// waitLabel.setPreferredSize (new Dimension (200, 25));
			waitLabel.setText(configed.getResourceValue("DPassword.WaitInfo.label"));

			waitingProgressBar = new JProgressBar();
			// waitingProgressBar.setPreferredSize (new Dimension (100, 10));
			// waitingProgressBar.setToolTipText(
			// configed.getResourceValue("FStartWakeOnLan.timeElapsed.toolTip") );
			waitingProgressBar.setValue(0);
			waitingProgressBar.setEnabled(true);
			// waitingProgressBar.setMaximum(max);

			UIDefaults defaults = new UIDefaults();
			defaults.put("ProgressBar[Enabled].foregroundPainter",
					new ProgressBarPainter(de.uib.configed.Globals.opsiLogoBlue));
			defaults.put("ProgressBar[Enabled].backgroundPainter",
					new ProgressBarPainter(de.uib.configed.Globals.opsiLogoLightBlue));
			waitingProgressBar.putClientProperty("Nimbus.Overrides", defaults);

			JPanel cPanel = new JPanel();
			GroupLayout cLayout = new GroupLayout(cPanel);
			cPanel.setLayout(cLayout);
			cLayout.setVerticalGroup(cLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE)
					.addGroup(cLayout.createSequentialGroup()
							.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
							.addComponent(waitingProgressBar, Globals.PROGRESS_BAR_HEIGHT, Globals.PROGRESS_BAR_HEIGHT,
									Globals.PROGRESS_BAR_HEIGHT)
							.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
							.addComponent(waitLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE, Globals.VGAP_SIZE))
					.addGap(Globals.VGAP_SIZE / 2));
			cLayout.setHorizontalGroup(cLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE / 2)
					.addGroup(cLayout.createParallelGroup()
							.addGroup(cLayout.createSequentialGroup()
									.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
									.addComponent(waitingProgressBar, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))
							.addGroup(cLayout.createSequentialGroup()
									.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
									.addComponent(waitLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)))
					.addGap(Globals.HGAP_SIZE / 2));

			getContentPane().add(cPanel);

			/*
			 * getContentPane().setLayout(new FlowLayout());
			 * getContentPane().add(waitingProgressBar);
			 * getContentPane().add(waitLabel);
			 */

			pack();

			setLocationRelativeTo(DPassword.this);
		}

		// interface WaitingSleeper

		public void actAfterWaiting() {
			// setCursor(saveCursor);
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

					if (waitingTask != null && waitingTask.isTimeoutReached())
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

			// Deactivate WaitInfo, because we finished waiting
			setVisible(false);
		}

		public JProgressBar getProgressBar() {
			return waitingProgressBar;
		}

		public JLabel getLabel() {
			return waitLabel;
		}

		public long getStartActionMillis() {
			return new GregorianCalendar().getTimeInMillis();
		}

		public long getWaitingMillis() {
			return timeOutMillis;
		}

		public long getOneProgressBarLengthWaitingMillis() {
			return ESTIMATED_TOTAL_WAIT_MILLIS;
		}

		public String setLabellingStrategy(long millisLevel) {
			return waitLabel.getText();
		}
	}

	private WaitCursor waitCursor;
	boolean connected = false;

	de.uib.utilities.thread.WaitingWorker waitingTask;

	JPanel panel;

	Containership containership;

	int defaultBlinkRate;

	JLabel jLabelVersion = new JLabel();
	JLabel jLabelJavaVersion = new JLabel();
	JLabel jLabelLabelJavaVersion = new JLabel(configed.getResourceValue("DPassword.jdkVersionBased"));

	JLabel jLabelUser = new JLabel();
	JTextField fieldUser = new JTextField();

	JPasswordField passwordField = new JPasswordField();
	JLabel jLabelPassword = new JLabel();

	JLabel jLabelHost = new JLabel();
	JComboBox fieldHost = new JComboBox();

	JPanel jPanelParameters1;
	JPanel jPanelParameters2;

	JPanel jPanelButtons = new JPanel();
	FlowLayout flowLayoutButtons = new FlowLayout();

	JButton jButtonCommit = new JButton();
	JButton jButtonCancel = new JButton();

	TitledBorder titledBorder1;

	JPanel jPanelRadioButtons = new JPanel();
	FlowLayout flowLayoutRadioButtons = new FlowLayout();
	// JRadioButton jRadioButton_ssh = new JRadioButton();
	JRadioButton jRadioButton_ssh2 = new JRadioButton();
	// JRadioButton jRadioButton_ftp = new JRadioButton();
	JRadioButton jRadioButton_localfs = new JRadioButton();

	// myAuthenticator myAuth = new myAuthenticator();
	MyKeyListener myKeyListener = new MyKeyListener(this);
	ButtonGroup buttonGroup1 = new ButtonGroup();

	public void setHost(String host) {
		if (host == null)
			host = "";
		fieldHost.setSelectedItem(host);
		fieldUser.requestFocus();

	}

	public void setServers(Vector<String> hosts) {
		fieldHost.setModel(new DefaultComboBoxModel(hosts));
		((JTextField) fieldHost.getEditor().getEditorComponent())
				.setCaretPosition(((String) (fieldHost.getSelectedItem())).length());
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

	static void addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int width, int height,
			double weightx, double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}

	public void setActivated(boolean active) {
		logging.info(this, "------------ activate");

		setEnabled(active);
	}

	void guiInit() {
		MessageFormat messageFormatTitle = new MessageFormat(configed.getResourceValue("DPassword.title"));
		setTitle(messageFormatTitle.format(new Object[] { Globals.APPNAME }));

		setIconImage(Globals.mainIcon);

		titledBorder1 = new TitledBorder("");

		panel = new JPanel();
		//panel.setEnabled(false);

		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		panel.setBorder(padding);

		// center
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// this.setLocation((screenSize.width - 450) / 2, 200);

		/*final Rectangle dim = de.uib.utilities.Globals.buildLocationOnDefaultDisplay(getSize().width, getSize().height,
				de.uib.utilities.Globals.smallFramesDistanceFromLeft,
				de.uib.utilities.Globals.smallFramesDistanceFromTop);
		this.setLocation(dim.x, dim.y);*/

		jLabelHost.setText(configed.getResourceValue("DPassword.jLabelHost"));

		containership = new Containership(panel);

		defaultBlinkRate = fieldUser.getCaret().getBlinkRate();

		// fieldHost.setMargin(new Insets(0, 3, 0, 3));
		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.addKeyListener(myKeyListener);

		jLabelUser.setText(configed.getResourceValue("DPassword.jLabelUser"));

		fieldUser.setText(TESTUSER);
		fieldUser.addKeyListener(myKeyListener);
		// fieldUser.setText(System.getProperty("user.name"));
		// logging.debug(passwordField.getPassword());
		fieldUser.setMargin(new Insets(0, 3, 0, 3));

		jLabelPassword.setText(configed.getResourceValue("DPassword.jLabelPassword"));

		passwordField.setText(TESTPASSWORD);
		passwordField.addKeyListener(myKeyListener);
		passwordField.setMargin(new Insets(0, 3, 0, 3));
		/*
		 * passwordField.addComponentListener(new java.awt.event.ComponentAdapter()
		 * {
		 * public void componentShown(ComponentEvent e)
		 * {
		 * passwordField_componentShown(e);
		 * }
		 * });
		 */

		// JCheckBox checkGzip = new
		// JCheckBox(configed.getResourceValue("DPassword.checkGzip") ,
		// de.uib.opsicommand.JSONthroughHTTP.gzipTransmission);
		JCheckBox checkCompression = new JCheckBox(configed.getResourceValue("DPassword.checkCompression"),
				de.uib.opsicommand.JSONthroughHTTP.compressTransmission);
		checkCompression.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// logging.debug(this, "itemStateChanged " + e);
				// de.uib.opsicommand.JSONthroughHTTP.gzipTransmission =
				de.uib.opsicommand.JSONthroughHTTP.compressTransmission = (e.getStateChange() == ItemEvent.SELECTED);

				logging.debug(this, "itemStateChanged " + de.uib.opsicommand.JSONthroughHTTP.gzipTransmission);;
			}
		});

		JCheckBox checkTrySSH = new JCheckBox(configed.getResourceValue("DPassword.checkTrySSH"),
				de.uib.configed.configed.sshconnect_onstart);
		logging.info(this, "checkTrySSH  " + de.uib.configed.configed.sshconnect_onstart);
		checkTrySSH.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// logging.debug(this, "itemStateChanged " + e);
				de.uib.configed.configed.sshconnect_onstart = (e.getStateChange() == ItemEvent.SELECTED);

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

			public void changedUpdate(DocumentEvent e) {
				// logging.debug(this, "++ changedUpdate on " );
				setRefreshMinutes();
			}

			public void insertUpdate(DocumentEvent e) {
				// logging.debug(this, "++ insertUpdate on " );
				setRefreshMinutes();
			}

			public void removeUpdate(DocumentEvent e) {
				// logging.debug(this, "++ removeUpdate on " );
				setRefreshMinutes();
			}
		});

		jPanelParameters1 = new PanelLinedComponents(new JComponent[] {
				// checkTrySSH, checkGzip
				checkTrySSH, checkCompression });
		jPanelParameters2 = new PanelLinedComponents(new JComponent[] {
				new JLabel(configed.getResourceValue("DPassword.pullReachableInfo")), fieldRefreshMinutes,
				new JLabel(configed.getResourceValue("DPassword.pullReachableInfoMinutes")) });

		jPanelButtons.setLayout(flowLayoutButtons);

		jButtonCommit.setText(configed.getResourceValue("DPassword.jButtonCommit"));
		jButtonCommit.setMaximumSize(new Dimension(100, 20));
		jButtonCommit.setPreferredSize(new Dimension(100, 20));
		jButtonCommit.setSelected(true);
		jButtonCommit.addActionListener(this::jButtonCommit_actionPerformed);

		jButtonCancel.setText(configed.getResourceValue("DPassword.jButtonCancel"));
		jButtonCancel.setMaximumSize(new Dimension(100, 20));
		jButtonCancel.setPreferredSize(new Dimension(100, 20));
		jButtonCancel.addActionListener(this::jButtonCancel_actionPerformed);

		jPanelButtons.add(jButtonCommit);
		jPanelButtons.add(jButtonCancel);

		// jPanelRadioButtons.setLayout(flowLayoutRadioButtons);
		// jPanelRadioButtons.add(jRadioButton_ssh2);
		// jPanelRadioButtons.add(jRadioButton_localfs);

		// x y w h wx wy

		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);

		gpl.setVerticalGroup(gpl.createSequentialGroup()
				.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				// .addGap(Globals.lineHeight/2)
				.addGroup(gpl.createParallelGroup()
						.addComponent(jLabelLabelJavaVersion, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(jLabelJavaVersion, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				// .addComponent(jLabelJavaVersion, GroupLayout.PREFERRED_SIZE,
				// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
				.addGap(Globals.LINE_HEIGHT)
				.addComponent(jPanelParameters1, (int) (1.2 * Globals.LINE_HEIGHT), (int) (1.2 * Globals.LINE_HEIGHT),
						(int) (1.2 * Globals.LINE_HEIGHT))
				// .addComponent(jPanelParameters2, (int) (1.2 * Globals.lineHeight), (int) (1.2
				// * Globals.lineHeight), (int) (1.2 * Globals.lineHeight))
				.addGap(Globals.LINE_HEIGHT).addComponent(jPanelButtons, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		gpl.setHorizontalGroup(
				gpl.createParallelGroup()
						.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE)
								.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE))
						.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE)
								// .addGap(Globals.hGapSize)
								.addComponent(jLabelLabelJavaVersion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE / 2)
								.addComponent(jLabelJavaVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, 40, Short.MAX_VALUE))
						.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jLabelHost,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gpl.createSequentialGroup().addComponent(fieldHost, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jLabelUser,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gpl.createSequentialGroup().addComponent(fieldUser, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jLabelPassword,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gpl.createSequentialGroup().addComponent(passwordField, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE)
								.addComponent(jPanelParameters1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGap(Globals.VGAP_SIZE))
						/*
						 * .addGroup(gpl.createSequentialGroup()
						 * .addGap(Globals.vGapSize)
						 * .addComponent(jPanelParameters2, GroupLayout.PREFERRED_SIZE,
						 * GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						 * )
						 */
						.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jPanelButtons,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		this.getContentPane().add(panel);

		Containership csPanel = new Containership(getContentPane());

		csPanel.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.backLightBlue }, JPanel.class);

		/*
		 * csPanel.doForAllContainedCompis
		 * ("setBackground", new Object[]{ java.awt.Color.yellow
		 * });//Globals.backLightBlue});
		 */
		// myAuth.setDefault(myAuth);
		// Authenticator.setDefault(myAuth);

		// fieldUser.setText(System.getProperty("user.name"));
		// jLabelVersion.setText("opsi configuration editor, version " + Globals.VERSION
		// + " date " + Globals.VERDATE);
		// jLabelVersion.setText( sprintf(
		// configed.getResourceValue("DPassword.jLabelVersion"), Globals.VERSION,
		// Globals.VERDATE) );

		MessageFormat messageFormatVersion = new MessageFormat(configed.getResourceValue("DPassword.jLabelVersion"));
		jLabelVersion.setText(messageFormatVersion
				.format(new Object[] { Globals.VERSION, "(" + Globals.VERDATE + ")", Globals.VERHASHTAG }));

		jLabelJavaVersion.setText(configed.javaVendor + " " + configed.javaVersion);

		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		logging.notice(" OS " + strOS + "  Version " + osVersion);
		String host = TESTSERVER; // ""
		/*
		 * if (strOS.startsWith("Windows") && (osVersion.compareToIgnoreCase("4.0") >=
		 * 0))
		 * {
		 * Process process = Runtime.getRuntime().exec("cmd.exe /q /c echo %HOST%");
		 * BufferedReader br = new BufferedReader( new
		 * InputStreamReader(process.getInputStream()));
		 * host = br.readLine();
		 * br.close();
		 * }
		 */

		// process = Runtime.getRuntime().exec("cmd.exe /c echo %UNAME%");
		// br = new BufferedReader( new InputStreamReader(process.getInputStream()));
		// String uname = br.readLine();

		pack();

		// logging.debug(" ---- host:" + host + "--");
		if (host.equals("")) {
			setHost("localhost");
			fieldHost.requestFocus();
			((JTextField) fieldHost.getEditor().getEditorComponent())
					.setCaretPosition(((String) (fieldHost.getSelectedItem())).length());
		}

		saveCursor = getCursor();

		// Sets the window on the main screen
		setLocationRelativeTo(null);
	}

	@Override
	public void setCursor(Cursor c) {
		super.setCursor(c);
		try {
			containership.doForAllContainedCompis("setCursor", new Object[] { c });
		} catch (Exception ex) {
			logging.warning(this, "containership error", ex);
		}

		/*
		 * if (c.equals(saveCursor))
		 * {
		 * fieldHost.getCaret().setBlinkRate(defaultBlinkRate);
		 * fieldUser.getCaret().setBlinkRate(defaultBlinkRate);
		 * passwordField.getCaret().setBlinkRate(defaultBlinkRate);
		 * }
		 * else
		 * {
		 * fieldHost.getCaret().setBlinkRate(0);
		 * fieldUser.getCaret().setBlinkRate(0);
		 * passwordField.getCaret().setBlinkRate(0);
		 * }
		 */

	}

	public void ok_action() {
		logging.info(this, "ok_action");

		// we make first a waitCursor and a waitInfo window

		if (waitCursor != null)
			waitCursor.stop(); // we want only one running instance

		// waitCursor = new WaitCursor(this, "ok_action");
		// ??we dont need this wait cursor instance; and it seems not to finish
		// correctly

		tryConnecting();
		// waitInfo.toFront();
	}
	/*
	 * public static PersistenceController producePersistenceController(String
	 * server)
	 * {
	 * PersistenceController persis =
	 * PersistenceControllerFactory.getNewPersistenceController(server, "", "");
	 * persis.setConnectionState(new ConnectionState
	 * (ConnectionState.STARTED_CONNECTING));
	 * persis.makeConnection();
	 * if ( persis.getConnectionState().getState() == ConnectionState.CONNECTED )
	 * return persis;
	 * 
	 * return null;
	 * }
	 */

	public void tryConnecting() {
		logging.info(this, "started  tryConnecting");
		setActivated(false);
		//jButtonCommit.setEnabled(false);

		// saveCursor = getCursor();
		// setCursor(new Cursor(Cursor.WAIT_CURSOR));

		waitCursor = new WaitCursor(this, "ok_action");

		ConfigedMain.HOST = (String) fieldHost.getSelectedItem();
		ConfigedMain.USER = fieldUser.getText();
		ConfigedMain.PASSWORD = String.valueOf(passwordField.getPassword());
		logging.info(this, "invoking PersistenceControllerFactory host, user, " +
		// .password " +
				fieldHost.getSelectedItem() + ", " + fieldUser.getText()
		// + ", " + String.valueOf( passwordField.getPassword())
		);

		if (waitingTask != null && !waitingTask.isReady()) {

			logging.info(this, "old waiting task not ready");
			return;
		}

		WaitInfo waitInfo = new WaitInfo(TIMEOUT_MS);
		setActivated(false);
		waitInfo.setAlwaysOnTop(true);
		waitInfo.setVisible(true);

		logging.info(this, "we are in EventDispatchThread " + SwingUtilities.isEventDispatchThread());
		logging.info(this, "  Thread.currentThread() " + Thread.currentThread());
		localApp = ("" + Thread.currentThread()).indexOf("main]") > -1;
		logging.info(this, "is local app  " + localApp);
		if (localApp) {

			//waitInfo.setAlwaysOnTop(true);
			logging.info(this, "start WaitingWorker");
			waitingTask = new de.uib.utilities.thread.WaitingWorker(waitInfo);
			// waitingTask.addPropertyChangeListener(this);
			waitingTask.execute();

			new Thread() {
				@Override
				public void run() {
					logging.info(this, "get persis");
					persis = PersistenceControllerFactory.getNewPersistenceController(
							(String) fieldHost.getSelectedItem(), fieldUser.getText(),
							String.valueOf(passwordField.getPassword()));
					// logging.checkErrorList(null);
					logging.info(this, "got persis, == null " + (persis == null));
					/*
					 * long TIMEOUT = 100000; //ms
					 * long interval = 2000;
					 * long waited = 0;
					 * 
					 * while (
					 * PersistenceControllerFactory.getConnectionState() ==
					 * ConnectionState.ConnectionUndefined
					 * &&
					 * waited < TIMEOUT
					 * )
					 * {
					 * try
					 * {
					 * Thread.sleep(interval);
					 * waited = waited + interval;
					 * logging.info(this, "waited for persis: " + waited);
					 * }
					 * catch (Exception waitException)
					 * {}
					 * }
					 * 
					 * if (waited >= TIMEOUT)
					 * logging.error(" no connection");
					 * 
					 */

					// stopWaitInfo();

					logging.info(this, "waitingTask can be set to ready");
					waitingTask.setReady();

				}
			}.start();
		} else {

			persis = PersistenceControllerFactory.getNewPersistenceController((String) fieldHost.getSelectedItem(),
					fieldUser.getText(), String.valueOf(passwordField.getPassword()));

			long interval = 2000;
			long waited = 0;

			while ((PersistenceControllerFactory.getConnectionState() == ConnectionState.ConnectionUndefined)
					&& waited < TIMEOUT_MS) {
				try {
					Thread.sleep(interval);
					waited = waited + interval;
				} catch (InterruptedException waitException) {
					Thread.currentThread().interrupt();
				}
			}

			if (waited >= TIMEOUT_MS)
				logging.error(" no connection");

			waitInfo.actAfterWaiting();
		}

		// de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setHost(HOST);
		// de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setHost((String)
		// fieldHost.getSelectedItem());
		// de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setUser(USER);
		de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setUser(fieldUser.getText());
		// de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setPassw(PASSWORD);
		de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance()
				.setPassw(String.valueOf(passwordField.getPassword()));
		de.uib.opsicommand.sshcommand.SSHConnectionInfo.getInstance().setHost((String) fieldHost.getSelectedItem());
	}

	void jButtonCommit_actionPerformed(ActionEvent e) {
		ok_action();
	}

	void end_program() {
		main.finishApp(false, 0);
	}

	void jButtonCancel_actionPerformed(ActionEvent e) {
		if (waitCursor != null)
			waitCursor.stop();
		end_program();
	}

	void passwordField_componentShown(ComponentEvent e) {
		passwordField.requestFocus();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			end_program();
		}
	}

	class MyKeyListener extends KeyAdapter {
		DPassword myHome;

		MyKeyListener(DPassword home) {
			myHome = home;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == 10) // Return
			{
				myHome.ok_action();
			} else if (e.getKeyCode() == 27) // Escape
			{
				myHome.end_program();
			}
		}
	}

}
