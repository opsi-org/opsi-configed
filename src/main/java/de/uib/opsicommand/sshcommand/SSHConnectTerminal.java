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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.configed.gui.ssh.SSHConnectionOutputDialog;
import de.uib.configed.gui.ssh.SSHConnectionTerminalDialog;
import de.uib.utilities.logging.logging;

public class SSHConnectTerminal extends SSHConnect {
	private JSch jsch = null;
	Session session = null;
	ChannelShell channel = null;
	private SSHConnectionTerminalDialog dialog;
	private KeyListener inputKeyListener = null;
	private ActionListener connectionKeyListener = null;
	private OutputStream out = null;
	public static final String SOME_COMMAND = "/bin/bash"; // "opsi-admin --version";
	String currentDirectory = "";
	boolean getCurrentDirectorySilent = false;

	public SSHConnectTerminal(ConfigedMain main, SSHConnectionTerminalDialog dialog) {
		super(main);
		this.dialog = dialog;
		if (dialog == null)
			dialog = new SSHConnectionTerminalDialog(configed.getResourceValue("MainFrame.jMenuSSHTerminal") + " "
					+ SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost(), this,
					false /* visible = false */
			);
		connect();
	}

	public SSHConnectTerminal(ConfigedMain main) {
		super(main);
		dialog = new SSHConnectionTerminalDialog(
				configed.getResourceValue("MainFrame.jMenuSSHTerminal") + " "
						+ SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost(),
				this, false /* visible = false */
		);
		connect();
	}

	public SSHConnectionOutputDialog getDialog() {
		if (dialog != null)
			return (SSHConnectionOutputDialog) dialog;
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
				theDialog.append(str, theDialog.getInputField());
			} catch (UnsupportedEncodingException ue) {
				logging.warning("UnsupportedEncodingException", ue);
			}
		}
	}

	@Override
	public void connect() {
		if (!isConnectionAllowed()) {
			logging.error(this, "connection forbidden.");
			
		} else {

			logging.info(this, "connect ...");
			try {
				jsch = new JSch();
				SSHConnectionInfo.getInstance().checkUserData();
				session = jsch.getSession(SSHConnectionInfo.getInstance().getUser(),
						SSHConnectionInfo.getInstance().getHost(),
						Integer.valueOf(SSHConnectionInfo.getInstance().getPort()));
				logging.info(this, "connect user@host " + SSHConnectionInfo.getInstance().getUser() + "@"
						+ SSHConnectionInfo.getInstance().getHost());
				if (SSHConnectionInfo.getInstance().usesKeyfile()) {
					if (SSHConnectionInfo.getInstance().getKeyfilePassphrase() != "")
						jsch.addIdentity(SSHConnectionInfo.getInstance().getKeyfilePath(),
								SSHConnectionInfo.getInstance().getKeyfilePassphrase());
					jsch.addIdentity(SSHConnectionInfo.getInstance().getKeyfilePath());
					logging.info(this, "connect this.keyfilepath " + SSHConnectionInfo.getInstance().getKeyfilePath());
					logging.info(this, "connect useKeyfile " + SSHConnectionInfo.getInstance().usesKeyfile()
							+ " addIdentity " + SSHConnectionInfo.getInstance().getKeyfilePath());
					session = jsch.getSession(SSHConnectionInfo.getInstance().getUser(),
							SSHConnectionInfo.getInstance().getHost(),
							Integer.valueOf(SSHConnectionInfo.getInstance().getPort()));
				} else {
					session = jsch.getSession(SSHConnectionInfo.getInstance().getUser(),
							SSHConnectionInfo.getInstance().getHost(),
							Integer.valueOf(SSHConnectionInfo.getInstance().getPort()));
					session.setPassword(SSHConnectionInfo.getInstance().getPassw());
					logging.info(this,
							"connect useKeyfile " + SSHConnectionInfo.getInstance().usesKeyfile() + " use password …");
				}
				// Do not use StrictHostKeyChecking=no. See JSch SFTP security with
				// session.setConfig(“StrictHostKeyChecking”, “no”);.
				// http://stackoverflow.com/questions/30178936/jsch-sftp-security-with-session-setconfigstricthostkeychecking-no
				session.setConfig("StrictHostKeyChecking", "no"); // otherwise exception if not in knwon_hosts or
																	// unknown fingerprint
				session.connect();
				channel = (ChannelShell) session.openChannel("shell");

				// dialog.append("\n");
				// naechste zeile activiert den Hinweis, falls die nicht die standard bash
				// verwendet wird, soll der befehl bash ausgefuehrt werden..
				// dialog.append(configed.getResourceValue("SSHConnection.Terminal.note") +
				// "\n\n", dialog.getInputField());
				logging.info(this, "Connect");

				
				// a hack for MS-DOS prompt on Windows.
				// channel.setInputStream(new FilterInputStream(System.in){
				// public int read(byte[] b, int off, int len)throws IOException{
				// return in.read(b, off, (len>124?124:len));
				// }
				// });

				
				
				// channel = setChannels(new FilterInputStream(System.in){
				// public int read(byte[] b, int off, int len)throws IOException{
				// return in.read(b, off, (len>124?124:len));
				// }
				// }),
				// new MyOutputPrinter(dialog, System.out)
				
				channel = setStreams(channel);

				channel.setPtyType("dumb");
				
				

				channel.connect();
				logging.info(this, "connect " + SSHConnectionInfo.getInstance().getUser() + "@"
						+ SSHConnectionInfo.getInstance().getHost());
				dialog.setTitle(
						SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost());
				dialog.setVisible(true);
				dialog.setAutocompleteList(getList(getCompletionList(true, true)));

				logging.info(this, "SSHConnectTerminal connected");

				initListeners();
				initInputFieldFromDialog();
				initKillProcessButtonFromDialog();

				Thread.sleep(1000);
				exec(SOME_COMMAND + "\n");
			} catch (Exception e) {
				logging.error(this, "SSHConnectTerminal connect exception", e);
			}
		}
	}

	public final void exec(String text) {
		if (!isConnectionAllowed()) {
			logging.warning(this, "connection forbidden.");
			
		} else {

			try {
				logging.info(this, "exec out " + out);
				logging.info(this, "exec text " + text);
				if ((out != null) && (text.trim().length() >= 0)) {
					SSHCommand command = new Empty_Command(text);
					String ntext = SSHCommandFactory.getInstance(main).getParameterHandler()
							.parseParameterToString(command, this);
					out.write(ntext.getBytes());
					logging.debug(this, " exec getPrivateStatus " + dialog.getPrivateStatus());
					logging.info(this, " exec text " + text);
					logging.info(this, " exec ntext " + ntext);
					if (!(dialog.getPrivateStatus())) {
						dialog.setPrivate(false);
					} else {
						logging.debug(this, " exec addToHistory " + text);
						dialog.addToHistory(text.trim());
					}
					dialog.setLastHistoryIndex();
					out.flush();
				}
				
			} catch (IOException ioe) {
				logging.error(this, "SSHConnectTerminal exec ioexception", ioe);
			} catch (Exception e) {
				logging.error(this, "SSHConnectTerminal exec exception", e);
			}
			logging.info(this, " exec finished  " + text);

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

		/*
		 * does not help; nevertheless we need a terminal from which the configed is
		 * started
		 * PrintStream myOut = null;
		 * 
		 * try
		 * {
		 * logging.debug("starting");
		 * logging.info(this, "have we got System.out ? " + System.out.checkError());
		 * File f = File.createTempFile("configedout_", ".tmp");
		 * myOut = new PrintStream(f);
		 * logging.info(this, "temp file created for SSH terminal:" + f);
		 * }
		 * catch(Exception ex)
		 * {
		 * logging.info(this, "no temp file created, taking System.out");
		 * myOut = System.out;
		 * }
		 */

		ch.setOutputStream(new MyOutputPrinter(dialog, myOut));
		out = ch.getOutputStream();
		return ch;
	}

	private void initKillProcessButtonFromDialog() {
		logging.info(this, "initKillProcessButtonFromDialog ");
		initListeners();
		this.dialog.btn_killProcess.removeActionListener(connectionKeyListener);
		this.dialog.btn_killProcess.addActionListener(connectionKeyListener);
	}

	public void initInputFieldFromDialog() {
		logging.info(this, "initInputFieldFromDialog ");
		logging.info(this, "initInputFieldFromDialog inputField " + dialog.getInputField());
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
				logging.warning(this, "Pipe closed");
		} catch (Exception e2) {
			logging.error("Error", e2);
		}
	}

	private void initListeners() {
		connectionKeyListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logging.info(this, "interrupt with btn ");
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
					logging.info(this, "interrupt with keys ");
					exec(new String(new byte[] { 3 }) + "\n");
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				JTextField textField = (JTextField) e.getSource();
				// if (textField.getText().trim().toLowerCase().equals("exit")) { //
				
				
				if (key == KeyEvent.VK_ENTER) {
					logging.info(this, "initInputFieldFromDialog keyReleased ENTER ");
					logging.info(this, "initInputFieldFromDialog inputfield " + textField);
					logging.info(this, "initInputFieldFromDialog dialog " + dialog);
					if (textField.getText().trim().equalsIgnoreCase("clear")) {
						clear();
						((Component) textField).requestFocusInWindow();
					}
					// else if (textField.getText().trim().toLowerCase().equals("kill")) {
					// exec(new String(new byte[] {3}) +"\n");
					
					// dialog.getInputField().setText("");
					// }
					else {
						// String text = textField.getText() + "\n";
						exec(textField.getText() + "\n");
						// if (textField.getText().contains(" cd ") || textField.getText().contains("cd
						// "))
						// {
						
						// if (commands_compgen != null)
						// if (dirs != null)
						// {
						// if (dirs.addAll(commands_compgen))
						
						// }
						// else
						

						
						
						// }
						((Component) textField).requestFocusInWindow();
						dialog.getInputField().setText("");
					}
				}
				// else if (key == KeyEvent.VK_TAB)
				// {

				// }
				else if ((key == KeyEvent.VK_UP) || (key == KeyEvent.VK_KP_UP)) {
					dialog.getInputField().setText(dialog.getPrevCommand_up());
					((Component) textField).requestFocusInWindow();
				} else if ((key == KeyEvent.VK_DOWN) || (key == KeyEvent.VK_KP_DOWN)) {
					dialog.getInputField().setText(dialog.getPrevCommand_down());
					((Component) textField).requestFocusInWindow();
				}
			}
		};
	}

	public List<String> commands_compgen;

	private String getCompletionList(boolean newCommands, boolean dirchanged) {
		SSHConnectExec ssh = new SSHConnectExec();
		String result = "";
		if (newCommands) {
			// result = ssh.exec(new Empty_Command("compgen -c" ), false, null, true,
			
			result = ssh.exec(new Empty_Command(
					// http://stackoverflow.com/questions/948008/linux-command-to-list-all-available-commands-and-aliases
					SSHCommandFactory.getInstance().str_command_getLinuxCommands), false, null, true, false);
			if (result == null)
				logging.warning(this, "no commands could be found for autocompletion");

			commands_compgen = getList(result);
			logging.debug(this, "getCompletionList commands compgen -c " + result);
		}

		// if (dirchanged)
		// {
		// // String pwd = ssh.exec(new Empty_Command("pwd" ), false, null, true,
		// false).replace("\n", "");
		// try {
		
		// // exec("pwd\n");
		// if (out != null)
		// {
		// out.write("pwd\n".getBytes());
		
		
		// }
		// try{Thread.sleep(50);} catch(Exception ee){}
		// // }
		// // catch (IOException ioe)

		

		// currentDirectory = currentDirectory.replace("\n", "") + "/";
		// String com = "ls -aldU " + currentDirectory + "./*";
		
		// String result_ls = ssh.exec( new Empty_Command(com ),
		
		
		
		// String[] arr_result_dir = result_ls.split("\n");
		// String result_dir = "";

		// for (String l : arr_result_dir)
		// {
		
		
		// String dir = "" + line.split(currentDirectory + "/",2)[1];
		
		// result_dir = result_dir + dir + "\n";
		// }
		// result = result + "\n" + result_dir;
		// }
		// catch (Exception ioe)
		// }
		return result;
	}

	private List<String> getList(String str) {
		if (str.equals(""))
			return null;
		String[] arr = str.split("\n");
		ArrayList<String> result = new ArrayList<>();
		for (String s : arr)
			result.add(s);
		return result;
	}

	@Override
	public void disconnect() {
		logging.info(this, "disconnect");
		if (session != null)
			if (session.isConnected()) {
				logging.info(this, "disconnect session");
				session.disconnect();
				this.session.disconnect();
				session = null;
			}
		if (channel != null)
			if (channel.isConnected()) {
				logging.info(this, "disconnect channel");
				channel.disconnect();
				this.channel.disconnect();
				channel = null;
			}
	}
}
