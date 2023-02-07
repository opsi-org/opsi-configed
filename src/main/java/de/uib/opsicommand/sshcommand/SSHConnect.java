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

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;

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
	public static final String DEFAULT_PORT = "22";
	public static final String PORT_SSH = DEFAULT_PORT;
	/** Password for server and username **/
	protected String password;
	/** If needed the sudo password **/
	protected String pwSudo;
	/** If needed the root password **/
	protected String pwRoot;
	protected static Session session = null;
	protected ConfigedMain main;

	SSHConnectionInfo connectionInfo = null;
	private static int successfulConnectObservedCount = 0;

	/**
	 * Instanz for SSH connection {@link de.uib.configed.ConfigedMain}
	 * 
	 * @param main configed main class
	 **/
	public SSHConnect(ConfigedMain main) {
		this.main = main;
		connectionInfo = SSHConnectionInfo.getInstance();

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
		return result.compareTo("error") != 0;
	}

	/**
	 * Shows a message to the user.
	 * 
	 * @param msg Message
	 **/
	protected void showMessage(String msg) {
		JOptionPane.showMessageDialog(null, msg);
		Logging.info(this, "show message: " + msg);
	}

	/**
	 * Test if already connected.
	 * 
	 * @return True - if connected
	 **/
	protected boolean isConnected() {
		boolean result = false;

		if (session != null && session.isConnected())
			result = true;

		Logging.info(this, "isConnected session.isConnected " + result);
		if (!result && successfulConnectObservedCount > 0)
			Logging.info("No SSH connection after successful connections: " + successfulConnectObservedCount + "\n"
					+ "check server authentication configuration");
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
		Logging.debug(this, "getSudoPass dialog " + dialog + " newConfirmDialog " + rememberPw);
		if ((rememberPw) && (pwSudo != null))
			return pwSudo;
		return getSudoPass(dialog);
	}

	/**
	 * Opens a confirm dialog for entering the sudo password.
	 * 
	 * @param dialog
	 **/
	protected String getSudoPass(Component dialog) {
		if (!isConnectionAllowed()) {
			Logging.error(this, "connection forbidden.");
			return "";
		}
		if (dialog == null)
			dialog = ConfigedMain.getMainFrame();
		Logging.debug(this, "getSudoPass dialog " + dialog);
		final JPasswordField passwordField = new JPasswordField(10);
		passwordField.setEchoChar('*');
		final JOptionPane opPane = new JOptionPane(
				new Object[] { new JLabel(Configed.getResourceValue("SSHConnection.sudoPassw1")),
						new JLabel(Configed.getResourceValue("SSHConnection.sudoPassw2")), passwordField },
				JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				super.selectInitialValue();
				((Component) passwordField).requestFocusInWindow();
			}
		};
		final JDialog jdialog = opPane.createDialog(dialog,
				Configed.getResourceValue("SSHConnection.Config.jLabelPassword"));
		jdialog.setVisible(true);
		Logging.debug(this, "getSudoPass joptiontype value " + opPane.getValue());
		Logging.debug(this, "getSudoPass joptiontype ok option " + JOptionPane.OK_OPTION);
		if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION) {
			pwSudo = String.valueOf(passwordField.getPassword());
			return pwSudo;
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
		Logging.info(this, "connect " + "null");
		connect(null);
	}

	public boolean connectTest() {
		return connect(new EmptyCommand(EmptyCommand.TESTCOMMAND, EmptyCommand.TESTCOMMAND, "", false));
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
			Logging.warning(this, "connection forbidden.");
			return false;
		}
		if (command != null)
			Logging.info(this, "connect command " + command.getMenuText());
		else
			Logging.info(this, "connect command null");
		try {
			JSch jsch = new JSch();
			connectionInfo.checkUserData();
			Logging.info(this, "connect user@host " + connectionInfo.getUser() + "@" + connectionInfo.getHost());
			Logging.debug(this, "connect with password log version " + connectionInfo.getShortPassw());
			Logging.info(this, "connect to login host " + (ConfigedMain.host.equals(connectionInfo.getHost())));
			Logging.info(this, "connect user " + connectionInfo.getUser());

			if (connectionInfo.usesKeyfile()) {
				if (!connectionInfo.getKeyfilePassphrase().equals(""))
					jsch.addIdentity(connectionInfo.getKeyfilePath(), connectionInfo.getKeyfilePassphrase());
				jsch.addIdentity(connectionInfo.getKeyfilePath());
				Logging.info(this, "connect this.keyfilepath " + connectionInfo.getKeyfilePath());
				Logging.info(this, "connect useKeyfile " + connectionInfo.usesKeyfile() + " addIdentity "
						+ connectionInfo.getKeyfilePath());
				session = jsch.getSession(connectionInfo.getUser(), connectionInfo.getHost(),
						Integer.valueOf(connectionInfo.getPort()));
			} else {
				session = jsch.getSession(connectionInfo.getUser(), connectionInfo.getHost(),
						Integer.valueOf(connectionInfo.getPort()));
				Logging.info(this, "connect this.password "

						+ SSHCommandFactory.CONFIDENTIAL);

				session.setPassword(connectionInfo.getPassw());
				Logging.info(this, "connect useKeyfile " + connectionInfo.usesKeyfile() + " use password …");
			}

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");

			JSch.setConfig(config);

			// will prevent session ending
			// cf
			// https://stackoverflow.com/questions/37280442/jsch-0-1-53-session-connect-throws-end-of-io-stream-read

			int timeo = 10000;

			Logging.info(this, "we try to connect with timeout " + timeo);

			session.connect(timeo);
			Logging.info(this, "we did connect " + connectionInfo);
			Logging.info(this, "connect " + connectionInfo);

			successfulConnectObservedCount++;

			return true;
		} catch (com.jcraft.jsch.JSchException authfail) {
			retriedTimesAuth = retry(retriedTimesAuth, authfail);
			if (retriedTimesAuth >= 2) {
				Logging.warning(this, "connect Authentication failed. " + authfail);
				if (successfulConnectObservedCount > 0)

					Logging.error("authentication failed after successful authentifications: "
							+ successfulConnectObservedCount + "\n" + "\n" + "check server authentication configuration"
							+ "\n" + "\n");

				return false;

			} else
				connect(command);
		} catch (Exception e) {
			retriedTimesJschex = retry(retriedTimesJschex, e);
			if (retriedTimesJschex >= 3) {
				Logging.warning(this, "connect error: " + e);
				return false;
			} else
				connect(command);
		}
		return false;
	}

	private int retry(int retriedTimes, Exception e) {
		if (retriedTimes >= 3) {
			retriedTimes = 1;

			Logging.warning(this, "Error", e);
		} else {
			Logging.warning(this, "[" + retriedTimes + "] seems to be a session exception " + e);
			retriedTimes = retriedTimes + 1;
		}
		return retriedTimes;
	}

	private int retriedTimesJschex = 1;
	private int retriedTimesAuth = 1;

	/**
	 * Get current session
	 * 
	 * @return the current jsch.session
	 */
	protected Session getSession() {
		Logging.info(this, "getSession " + session);
		return session;
	}

	public void interruptChannel(Channel channel) {
		interruptChannel(channel, true);
	}

	// http://stackoverflow.com/questions/22476506/kill-process-before-disconnecting
	public void interruptChannel(Channel channel, boolean kill) {
		try {
			Logging.info(this, "interruptChannel _channel " + channel);
			channel.sendSignal("2");
			if (kill)
				channel.sendSignal("9");
			Logging.info(this, "interrupted");
		} catch (Exception e) {
			Logging.error("Failed interrupting channel", e);
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

		Logging.debug(this, "disconnect");
	}
}
