/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import java.awt.Component;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;

/**
 * This Class creates a SSH connection to a server.
 **/
public class SSHConnect {
	/** Port for server to connected as **/
	public static final String DEFAULT_PORT = "22";
	public static final String PORT_SSH = DEFAULT_PORT;

	private static int successfulConnectObservedCount;
	protected static Session session;

	/** Hostname for server to connected with **/
	protected String commandInfoName;
	/** If needed the sudo password **/
	private String pwSudo;
	protected ConfigedMain configedMain;

	private SSHConnectionInfo connectionInfo;

	private int retriedTimesJschex = 1;
	private int retriedTimesAuth = 1;

	/**
	 * Instanz for SSH connection {@link de.uib.configed.ConfigedMain}
	 * 
	 * @param configedMain configed main class
	 **/
	public SSHConnect(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		connectionInfo = SSHConnectionInfo.getInstance();
	}

	public static boolean isConnectionAllowed() {
		return SSHConnectionInfo.getInstance().isSSHActivate();
	}

	/**
	 * Test if already connected.
	 * 
	 * @return True - if connected
	 **/
	protected boolean isConnected() {
		boolean result = false;

		if (session != null && session.isConnected()) {
			result = true;
		}

		Logging.info(this, "isConnected session.isConnected " + result);
		if (!result && successfulConnectObservedCount > 0) {
			Logging.info("No SSH connection after successful connections: " + successfulConnectObservedCount + "\n"
					+ "check server authentication configuration");
		}

		return result;
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
		if (rememberPw && pwSudo != null) {
			return pwSudo;
		}

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
		if (dialog == null) {
			dialog = ConfigedMain.getMainFrame();
		}

		Logging.debug(this, "getSudoPass dialog " + dialog);
		final JPasswordField passwordField = new JPasswordField(10);
		passwordField.setEchoChar('*');
		final JOptionPane opPane = new JOptionPane(
				new Object[] { new JLabel(Configed.getResourceValue("SSHConnection.sudoPassw")), passwordField },
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

		if (command != null) {
			Logging.info(this, "connect command " + command.getMenuText());
		} else {
			Logging.info(this, "connect command null");
		}

		try {
			JSch jsch = new JSch();
			connectionInfo.checkUserData();
			Logging.info(this, "connect user@host " + connectionInfo.getUser() + "@" + connectionInfo.getHost());
			Logging.debug(this, "connect with password log version " + connectionInfo.getShortPassw());
			Logging.info(this, "connect to login host " + (ConfigedMain.getHost().equals(connectionInfo.getHost())));
			Logging.info(this, "connect user " + connectionInfo.getUser());

			setSession(connectionInfo, jsch);

			Map<String, String> config = Collections.singletonMap("StrictHostKeyChecking", "no");

			JSch.setConfig(new Hashtable<>(config));

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
		} catch (JSchException authfail) {
			retriedTimesAuth = retry(retriedTimesAuth, authfail);
			if (retriedTimesAuth >= 2) {
				Logging.warning(this, "connect Authentication failed. " + authfail);
				if (successfulConnectObservedCount > 0) {
					Logging.error("authentication failed after successful authentifications: "
							+ successfulConnectObservedCount + "\n" + "\n" + "check server authentication configuration"
							+ "\n" + "\n");
				}
			} else {
				connect(command);
			}
		}

		return false;
	}

	private static void setSession(SSHConnectionInfo connectionInfo, JSch jsch) throws JSchException {
		if (connectionInfo.usesKeyfile()) {
			if (!connectionInfo.getKeyfilePassphrase().isEmpty()) {
				jsch.addIdentity(connectionInfo.getKeyfilePath(), connectionInfo.getKeyfilePassphrase());
			}

			jsch.addIdentity(connectionInfo.getKeyfilePath());
			Logging.info(SSHConnect.class, "connect this.keyfilepath " + connectionInfo.getKeyfilePath());
			Logging.info(SSHConnect.class, "connect useKeyfile " + connectionInfo.usesKeyfile() + " addIdentity "
					+ connectionInfo.getKeyfilePath());
			session = jsch.getSession(connectionInfo.getUser(), connectionInfo.getHost(),
					Integer.valueOf(connectionInfo.getPort()));
		} else {
			session = jsch.getSession(connectionInfo.getUser(), connectionInfo.getHost(),
					Integer.valueOf(connectionInfo.getPort()));
			Logging.info(SSHConnect.class, "connect this.password "

					+ SSHCommandFactory.CONFIDENTIAL);

			session.setPassword(connectionInfo.getPassw());
			Logging.info(SSHConnect.class, "connect useKeyfile " + connectionInfo.usesKeyfile() + " use password …");
		}
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
			if (kill) {
				channel.sendSignal("9");
			}

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
