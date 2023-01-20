package de.uib.opsicommand.sshcommand;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTextField;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.ssh.SSHConnectionOutputDialog;
import de.uib.configed.gui.ssh.SSHConnectionTerminalDialog;
import de.uib.utilities.logging.Logging;

public class SSHConnectTerminal extends SSHConnect {
	Session session = null;
	ChannelShell channel = null;
	private SSHConnectionTerminalDialog dialog;
	private KeyListener inputKeyListener = null;
	private ActionListener connectionKeyListener = null;
	private OutputStream out = null;
	public static final String SOME_COMMAND = "/bin/bash";
	String currentDirectory = "";
	boolean getCurrentDirectorySilent = false;

	public SSHConnectTerminal(ConfigedMain main, SSHConnectionTerminalDialog dialog) {
		super(main);
		this.dialog = dialog;
		if (dialog == null)
			dialog = new SSHConnectionTerminalDialog(Configed.getResourceValue("MainFrame.jMenuSSHTerminal") + " "
					+ SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost(),
					this);
		connect();
	}

	public SSHConnectTerminal(ConfigedMain main) {
		super(main);
		dialog = new SSHConnectionTerminalDialog(
				Configed.getResourceValue("MainFrame.jMenuSSHTerminal") + " "
						+ SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost(),
				this);
		connect();
	}

	public SSHConnectionOutputDialog getDialog() {
		if (dialog != null)
			return dialog;
		return null;
	}

	class MyOutputPrinter extends PrintStream {
		SSHConnectionTerminalDialog theDialog;

		MyOutputPrinter(SSHConnectionTerminalDialog dialog, OutputStream out) {
			super(out);
			theDialog = dialog;
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			try {
				String str = new String(buf, off, len, "UTF-8");
				theDialog.append(str);
			} catch (UnsupportedEncodingException ue) {
				Logging.warning("UnsupportedEncodingException", ue);
			}
		}
	}

	@Override
	public void connect() {
		if (!isConnectionAllowed()) {
			Logging.error(this, "connection forbidden.");

		} else {

			Logging.info(this, "connect ...");
			try {
				JSch jsch = new JSch();
				SSHConnectionInfo.getInstance().checkUserData();
				session = jsch.getSession(SSHConnectionInfo.getInstance().getUser(),
						SSHConnectionInfo.getInstance().getHost(),
						Integer.valueOf(SSHConnectionInfo.getInstance().getPort()));
				Logging.info(this, "connect user@host " + SSHConnectionInfo.getInstance().getUser() + "@"
						+ SSHConnectionInfo.getInstance().getHost());
				if (SSHConnectionInfo.getInstance().usesKeyfile()) {
					if (!SSHConnectionInfo.getInstance().getKeyfilePassphrase().equals(""))
						jsch.addIdentity(SSHConnectionInfo.getInstance().getKeyfilePath(),
								SSHConnectionInfo.getInstance().getKeyfilePassphrase());
					jsch.addIdentity(SSHConnectionInfo.getInstance().getKeyfilePath());
					Logging.info(this, "connect this.keyfilepath " + SSHConnectionInfo.getInstance().getKeyfilePath());
					Logging.info(this, "connect useKeyfile " + SSHConnectionInfo.getInstance().usesKeyfile()
							+ " addIdentity " + SSHConnectionInfo.getInstance().getKeyfilePath());
					session = jsch.getSession(SSHConnectionInfo.getInstance().getUser(),
							SSHConnectionInfo.getInstance().getHost(),
							Integer.valueOf(SSHConnectionInfo.getInstance().getPort()));
				} else {
					session = jsch.getSession(SSHConnectionInfo.getInstance().getUser(),
							SSHConnectionInfo.getInstance().getHost(),
							Integer.valueOf(SSHConnectionInfo.getInstance().getPort()));
					session.setPassword(SSHConnectionInfo.getInstance().getPassw());
					Logging.info(this,
							"connect useKeyfile " + SSHConnectionInfo.getInstance().usesKeyfile() + " use password …");
				}
				// Do not use StrictHostKeyChecking=no. See JSch SFTP security with

				// http://stackoverflow.com/questions/30178936/jsch-sftp-security-with-session-setconfigstricthostkeychecking-no
				session.setConfig("StrictHostKeyChecking", "no"); // otherwise exception if not in knwon_hosts or
																	// unknown fingerprint
				session.connect();
				channel = (ChannelShell) session.openChannel("shell");

				// naechste zeile activiert den Hinweis, falls die nicht die standard bash
				// verwendet wird, soll der befehl bash ausgefuehrt werden..

				Logging.info(this, "Connect");

				// a hack for MS-DOS prompt on Windows.

				channel = setStreams(channel);

				channel.setPtyType("dumb");

				channel.connect();
				Logging.info(this, "connect " + SSHConnectionInfo.getInstance().getUser() + "@"
						+ SSHConnectionInfo.getInstance().getHost());
				dialog.setTitle(
						SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost());
				dialog.setVisible(true);
				dialog.setAutocompleteList(getList(getCompletionList(true, true)));

				Logging.info(this, "SSHConnectTerminal connected");

				initListeners();
				initInputFieldFromDialog();
				initKillProcessButtonFromDialog();

				Globals.threadSleep(this, 1000);

				exec(SOME_COMMAND + "\n");
			} catch (Exception e) {
				Logging.error(this, "SSHConnectTerminal connect exception", e);
			}
		}
	}

	public final void exec(String text) {
		if (!isConnectionAllowed()) {
			Logging.warning(this, "connection forbidden.");

		} else {

			try {
				Logging.info(this, "exec out " + out);
				Logging.info(this, "exec text " + text);
				if ((out != null) && (text.trim().length() >= 0)) {
					SSHCommand command = new EmptyCommand(text);
					String ntext = SSHCommandFactory.getInstance(main).getParameterHandler()
							.parseParameterToString(command, this);
					out.write(ntext.getBytes());
					Logging.debug(this, " exec getPrivateStatus " + dialog.getPrivateStatus());
					Logging.info(this, " exec text " + text);
					Logging.info(this, " exec ntext " + ntext);
					if (!(dialog.getPrivateStatus())) {
						dialog.setPrivate(false);
					} else {
						Logging.debug(this, " exec addToHistory " + text);
						dialog.addToHistory(text.trim());
					}
					dialog.setLastHistoryIndex();
					out.flush();
				}

			} catch (IOException ioe) {
				Logging.error(this, "SSHConnectTerminal exec ioexception", ioe);
			} catch (Exception e) {
				Logging.error(this, "SSHConnectTerminal exec exception", e);
			}
			Logging.info(this, " exec finished  " + text);

		}
	}

	private ChannelShell setStreams(ChannelShell ch) throws IOException {
		return setStreams(ch, false);
	}

	private ChannelShell setStreams(ChannelShell ch, boolean silent) throws IOException {
		ch.setInputStream(new FilterInputStream(System.in) {
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return in.read(b, off, (len > 1024 ? 1024 : len));
			}
		});

		PrintStream myOut = System.out;

		ch.setOutputStream(new MyOutputPrinter(dialog, myOut));
		out = ch.getOutputStream();
		return ch;
	}

	private void initKillProcessButtonFromDialog() {
		Logging.info(this, "initKillProcessButtonFromDialog ");
		initListeners();
		this.dialog.jButtonKillProcess.removeActionListener(connectionKeyListener);
		this.dialog.jButtonKillProcess.addActionListener(connectionKeyListener);
	}

	public void initInputFieldFromDialog() {
		Logging.info(this, "initInputFieldFromDialog ");
		Logging.info(this, "initInputFieldFromDialog inputField " + dialog.getInputField());
		initListeners();
		dialog.getInputField().removeKeyListener(inputKeyListener);
		dialog.getInputField().addKeyListener(inputKeyListener);
	}

	public final void clear() {
		dialog.getOutputField().setText("");
		dialog.getInputField().setText("");
		try {
			if (out != null)
				out.write("\n".getBytes());
			else
				Logging.warning(this, "Pipe closed");
		} catch (Exception e2) {
			Logging.error("Error", e2);
		}
	}

	private void initListeners() {
		connectionKeyListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.info(this, "interrupt with btn ");
				exec(new String(new byte[] { 3 }) + "\n");
			}
		};
		inputKeyListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
					Logging.info(this, "interrupt with keys ");
					exec(new String(new byte[] { 3 }) + "\n");
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				JTextField textField = (JTextField) e.getSource();

				if (key == KeyEvent.VK_ENTER) {
					Logging.info(this, "initInputFieldFromDialog keyReleased ENTER ");
					Logging.info(this, "initInputFieldFromDialog inputfield " + textField);
					Logging.info(this, "initInputFieldFromDialog dialog " + dialog);
					if (textField.getText().trim().equalsIgnoreCase("clear")) {
						clear();
						((Component) textField).requestFocusInWindow();
					}

					else {

						exec(textField.getText() + "\n");

						((Component) textField).requestFocusInWindow();
						dialog.getInputField().setText("");
					}
				}

				else if ((key == KeyEvent.VK_UP) || (key == KeyEvent.VK_KP_UP)) {
					dialog.getInputField().setText(dialog.getPrevCommandUp());
					((Component) textField).requestFocusInWindow();
				} else if ((key == KeyEvent.VK_DOWN) || (key == KeyEvent.VK_KP_DOWN)) {
					dialog.getInputField().setText(dialog.getPrevCommandDown());
					((Component) textField).requestFocusInWindow();
				}
			}
		};
	}

	public List<String> commandsCompgen;

	private String getCompletionList(boolean newCommands, boolean dirchanged) {
		SSHConnectExec ssh = new SSHConnectExec();
		String result = "";
		if (newCommands) {

			result = ssh.exec(new EmptyCommand(
					// http://stackoverflow.com/questions/948008/linux-command-to-list-all-available-commands-and-aliases
					SSHCommandFactory.STRING_COMMAND_GET_LINUX_COMMANDS), false, null);
			if (result == null)
				Logging.warning(this, "no commands could be found for autocompletion");
			else {
				commandsCompgen = getList(result);
				Logging.debug(this, "getCompletionList commands compgen -c " + result);
			}
		}

		return result;
	}

	private List<String> getList(String str) {
		if (str.equals(""))
			return null;

		String[] arr = str.split("\n");

		return Arrays.asList(arr);
	}

	@Override
	public void disconnect() {
		Logging.info(this, "disconnect");
		if (session != null && session.isConnected()) {
			Logging.info(this, "disconnect session");
			session.disconnect();
			this.session.disconnect();
			session = null;
		}

		if (channel != null && channel.isConnected()) {
			Logging.info(this, "disconnect channel");
			channel.disconnect();
			this.channel.disconnect();
			channel = null;
		}
	}
}
