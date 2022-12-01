package de.uib.opsicommand.sshcommand;

import java.awt.Component;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2022 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

/**
 * This Class creates a SSH connection to a server.
 **/
public class SSHConnect {
	/** Hostname for server to connected with **/
	protected String commandInfoName = null;
	protected String host;
	/** Username for server to connected as **/
	protected String user;
	/** Port for server to connected as **/
	public final static String default_port = "22";
	public static String portSSH = default_port;
	/** Password for server and username **/
	protected String password;
	/** If needed the sudo password **/
	protected String pw_sudo;
	/** If needed the root password **/
	protected String pw_root;
	private JSch jsch = null;
	protected static Session session = null;
	protected ConfigedMain main;

	SSHCommandFactory factory = SSHCommandFactory.getInstance();
	SSHConnectionInfo connectionInfo = null;

	/**
	 * Instanz for SSH connection {@link de.uib.configed.ConfigedMain}
	 * 
	 * @param main configed main class
	 **/
	public SSHConnect(ConfigedMain main) {
		this.main = main;
		connectionInfo = SSHConnectionInfo.getInstance();
		// if (main.SSHKEY != null)
		// useKeyfile = true;
	}

	public static boolean isConnectionAllowed() {
		return SSHConnectionInfo.getInstance().getSSHActivateStatus();
	}

	/**
	 * Check if result is not an error.
	 * 
	 * @param result Result
	 * @return True - if result is not an error
	 **/
	protected boolean isNotError(String result) {
		if (result.compareTo("error") == 0)
			return false;
		return true;
	}

	/**
	 * Shows a message to the user.
	 * 
	 * @param msg Message
	 **/
	protected void showMessage(String msg) {
		JOptionPane.showMessageDialog(null, msg);
		logging.info(this, "show message: " + msg);
	}

	/**
	 * Test if already connected.
	 * 
	 * @return True - if connected
	 **/
	protected boolean isConnected() {
		boolean result = false;

		if (session != null) {
			if (session.isConnected()) {
				result = true;
			}
		}

		logging.info(this, "isConnected session.isConnected " + result);
		if (!result && factory.successfulConnectObservedCount > 0)
			logging.info("No SSH connection after successful connections: " + factory.successfulConnectObservedCount
					+ "\n" + "check server authentication configuration");
		return result;
	}

	/**
	 * Calls {@link getSudoPass(Component)} with null.
	 **/
	protected String getSudoPass() {
		return getSudoPass(null);
	}

	/**
	 * If newConfirmDialog is false and sudo password already given return sudo
	 * password. Overwise calls {@link getSudoPass(Component)}.
	 * 
	 * @param dialog
	 * @param newConfirmDialog true for entering new sudo password
	 **/
	protected String getSudoPass(Component dialog, boolean rememberPw) {
		logging.debug(this, "getSudoPass dialog " + dialog + " newConfirmDialog " + rememberPw);
		if ((rememberPw) && (pw_sudo != null))
			return pw_sudo;
		return getSudoPass(dialog);
	}

	/**
	 * Opens a confirm dialog for entering the sudo password.
	 * 
	 * @param dialog
	 **/
	protected String getSudoPass(Component dialog) {
		if (!isConnectionAllowed()) {
			logging.error(this, "connection forbidden.");
			return "";
		}
		if (dialog == null)
			dialog = de.uib.configed.Globals.mainFrame;
		logging.debug(this, "getSudoPass dialog " + dialog);
		final JPasswordField passwordField = new JPasswordField(10);
		passwordField.setEchoChar('*');
		final JOptionPane opPane = new JOptionPane(
				new Object[] { new JLabel(configed.getResourceValue("SSHConnection.sudoPassw1")),
						new JLabel(configed.getResourceValue("SSHConnection.sudoPassw2")), passwordField },
				JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				super.selectInitialValue();
				((Component) passwordField).requestFocusInWindow();
			}
		};
		final JDialog jdialog = opPane.createDialog(dialog,
				configed.getResourceValue("SSHConnection.Config.jLabelPassword"));
		jdialog.setVisible(true);
		logging.debug(this, "getSudoPass joptiontype value " + opPane.getValue());
		logging.debug(this, "getSudoPass joptiontype ok option " + JOptionPane.OK_OPTION);
		if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION) {
			pw_sudo = String.valueOf(passwordField.getPassword());
			return pw_sudo;
		}

		return null;

	}

	/**
	 * Sets authentificationsdata.
	 * 
	 * @param h  Hostname
	 * @param u  Username
	 * @param ps Password
	 **/
	public void setUserData(String h, String u, String ps, String p) {
		connectionInfo.setUserData(h, u, ps, p);
	}

	/**
	 * Calls {@link connect(SSHCommand)} with null command.
	 **/
	public void connect() {
		logging.info(this, "connect " + "null");
		connect(null);
	}

	public boolean connectTest() {
		return connect(new Empty_Command(Empty_Command.TESTCOMMAND, Empty_Command.TESTCOMMAND, "", false));
	}

	/**
	 * Connect to server und check if command (if given) needs root rights call
	 * {@link getRootPassword(Component)}.
	 * 
	 * @param command Command
	 * @return True - if successful
	 **/
	public boolean connect(SSHCommand command) {
		if (!isConnectionAllowed()) {
			logging.warning(this, "connection forbidden.");
			return false;
		}
		if (command != null)
			logging.info(this, "connect command " + command.getMenuText());
		else
			logging.info(this, "connect command null");
		try {
			jsch = new JSch();
			connectionInfo.checkUserData();
			logging.info(this, "connect user@host " + connectionInfo.getUser() + "@" + connectionInfo.getHost());
			logging.debug(this, "connect with password log version " + connectionInfo.getShortPassw());
			logging.info(this, "connect to login host " + (ConfigedMain.HOST.equals(connectionInfo.getHost())));
			logging.info(this, "connect user " + connectionInfo.getUser());

			if (connectionInfo.usesKeyfile()) {
				if (connectionInfo.getKeyfilePassphrase() != "")
					jsch.addIdentity(connectionInfo.getKeyfilePath(), connectionInfo.getKeyfilePassphrase());
				jsch.addIdentity(connectionInfo.getKeyfilePath());
				logging.info(this, "connect this.keyfilepath " + connectionInfo.getKeyfilePath());
				logging.info(this, "connect useKeyfile " + connectionInfo.usesKeyfile() + " addIdentity "
						+ connectionInfo.getKeyfilePath());
				session = jsch.getSession(connectionInfo.getUser(), connectionInfo.getHost(),
						Integer.valueOf(connectionInfo.getPort()));
			} else {
				session = jsch.getSession(connectionInfo.getUser(), connectionInfo.getHost(),
						Integer.valueOf(connectionInfo.getPort()));
				logging.info(this, "connect this.password "
						// + connectionInfo.getPassw() + " "
						+ SSHCommandFactory.getInstance().confidential);

				session.setPassword(connectionInfo.getPassw());
				logging.info(this, "connect useKeyfile " + connectionInfo.usesKeyfile() + " use password …");
			}
			// session.setTimeout(10000);
			// session.setConfig("StrictHostKeyChecking", "no");

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			// config.put("PreferredAuthentications", "password");
			jsch.setConfig(config);

			// session.setConfig("kex", "diffie-hellman-group1-sha1"); //hope, that this
			// will prevent session ending
			// cf
			// https://stackoverflow.com/questions/37280442/jsch-0-1-53-session-connect-throws-end-of-io-stream-read
			// int timeo = 4000;
			int timeo = 10000;

			logging.info(this, "we try to connect with timeout " + timeo);

			session.connect(timeo);
			logging.info(this, "we did connect " + connectionInfo);
			logging.info(this, "connect " + connectionInfo);

			factory.successfulConnectObservedCount++;

			return true;
		} catch (com.jcraft.jsch.JSchException authfail) {
			retriedTimes_auth = retry(retriedTimes_auth, authfail);
			if (retriedTimes_auth >= 2) {
				logging.warning(this, "connect Authentication failed. " + authfail);
				if (factory.successfulConnectObservedCount > 0)

					logging.error("authentication failed after successful authentifications: "
							+ factory.successfulConnectObservedCount + "\n" + "\n"
							+ "check server authentication configuration" + "\n" + "\n");

				return false;

			} else
				connect(command);
		} catch (Exception e) {
			retriedTimes_jschex = retry(retriedTimes_jschex, e);
			if (retriedTimes_jschex >= 3) {
				logging.warning(this, "connect error: " + e);
				return false;
			} else
				connect(command);
		}
		return false;
	}

	private int retry(int retriedTimes, Exception e) {
		if (retriedTimes >= 3) {
			retriedTimes = 1;
			// logging.error(this, "connect Exception " + e);
			logging.warning(this, "Error", e);
		} else {
			logging.warning(this, "[" + retriedTimes + "] seems to be a session exception " + e);
			retriedTimes = retriedTimes + 1;
		}
		return retriedTimes;
	}

	private int retriedTimes_jschex = 1;
	private int retriedTimes_auth = 1;

	/**
	 * Get current session
	 * 
	 * @return the current jsch.session
	 */
	protected Session getSession() {
		logging.info(this, "getSession " + session);
		return session;
	}

	public void interruptChannel(Channel _channel) {
		interruptChannel(_channel, true);
	}

	// http://stackoverflow.com/questions/22476506/kill-process-before-disconnecting
	public void interruptChannel(Channel _channel, boolean kill) {
		try {
			logging.info(this, "interruptChannel _channel " + _channel);
			_channel.sendSignal("2");
			if (kill)
				_channel.sendSignal("9");
			logging.info(this, "interrupted");
		} catch (Exception e) {
			logging.error("Failed interrupting channel", e);
		}
	}

	/**
	 * Disconnect from server.
	 **/
	public void disconnect() {
		if (isConnected()) {
			SSHCommandFactory.getInstance().unsetConnection();
			session.disconnect();
		}

		logging.debug(this, "disconnect");
	}
}
