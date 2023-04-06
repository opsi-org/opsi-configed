package de.uib.opsicommand.sshcommand;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHConnectionOutputDialog;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.ssh.SSHOutputCollector;

/**
 * @inheritDoc Class for executing commands.
 */
public class SSHConnectExec extends SSHConnect {
	protected SSHConnectionExecDialog outputDialog;
	protected boolean multiCommand;
	protected ActionListener killProcessListener;
	protected JButton responseButton;

	private int supwRetriedTimes;

	protected boolean foundError;

	protected boolean interruptChannel;

	public SSHConnectExec() {
		super(null);
		connect();
	}

	public SSHConnectExec(SSHCommand sshcommand) {
		this(null, sshcommand);
	}

	public SSHConnectExec(SSHCommand sshcommand, JButton responseButton) {
		this(null, sshcommand, responseButton);
	}

	public SSHConnectExec(ConfigedMain m) {
		super(m);
		foundError = false;
		main = m;

	}

	public SSHConnectExec(ConfigedMain m, SSHCommand sshcommand) {
		this(m, sshcommand, null);
	}

	public SSHConnectExec(ConfigedMain m, SSHCommand sshcommand, JButton responseButton) {
		super(m);
		foundError = false;
		main = m;

		Logging.info(this, "SSHConnectExec main " + main);

		Logging.info(this, "SSHConnectExec sshcommand " + sshcommand.getSecuredCommand());
		this.responseButton = responseButton;
		if (responseButton != null) {
			responseButton.setEnabled(false);
		}

		starting(sshcommand);
	}

	private void starting(SSHCommand sshcommand) {

		if (!(isConnected())) {
			final SSHCommandFactory factory = SSHCommandFactory.getInstance(main);
			Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
					+ factory.getConnectionState());
			return;
		}

		try {
			if (!(Globals.isGlobalReadOnly())) {
				Logging.info(this, "starting, sshcommand isMultiCommand " + sshcommand.isMultiCommand());

				if (sshcommand instanceof SSHCommandTemplate) {
					Logging.info(this, "exec_template " + sshcommand + ": " + sshcommand.getCommand());
					execTemplate((SSHCommandTemplate) sshcommand);
				} else {
					if (sshcommand.isMultiCommand()) {
						Logging.info(this, "exec_list " + sshcommand + ": " + sshcommand.getCommand());
						execList((SSHMultiCommand) sshcommand);
					} else {
						Logging.info(this, "exec " + sshcommand + ": " + sshcommand.getCommand());
						exec(sshcommand);
					}
				}
			} else {
				Logging.warning(this, Configed.getResourceValue("SSHConnection.Exec.cannotAsReadonly"));
				if (outputDialog != null) {
					outputDialog.appendLater("[" + sshcommand.getId() + "] \t"
							+ Configed.getResourceValue("SSHConnection.Exec.cannotAsReadonly"));
				}
			}
		} catch (Exception e) {
			Logging.error(this, "SSHConnectExec Exception", e);
		}
	}

	public SSHConnectionExecDialog getDialog() {
		return outputDialog;
	}

	public void setDialog(SSHConnectionExecDialog dia) {
		outputDialog = dia;
	}

	public void execTemplate(SSHCommandTemplate command) {
		execList(command, true, null, true, true);
	}

	public void execTemplate(SSHCommandTemplate command, SSHConnectionExecDialog dia, boolean sequential) {
		execList(command, true, dia, sequential, true);
	}

	public void execTemplate(SSHCommandTemplate command, boolean sequential) {
		execList(command, true, null, sequential, true);
	}

	public void execList(SSHMultiCommand commands) {
		execList(commands, true, null, false, true);
	}

	public void execList(SSHMultiCommand commands, boolean sequential) {
		execList(commands, true, null, sequential, true);
	}

	public void execList(SSHMultiCommand commands, SSHConnectionExecDialog dialog, boolean sequential) {
		execList(commands, true, dialog, sequential, true);
	}

	public void execList(final SSHMultiCommand commands, final boolean withGui, SSHConnectionExecDialog dialog,
			final boolean sequential, final boolean rememberPw) {
		Logging.info(this, "exec_list commands[" + ((SSHCommand) commands).getId() + "] withGui[" + withGui
				+ "] sequential[" + sequential + "] dialog[" + dialog + "]");
		if (!isConnectionAllowed()) {
			Logging.warning(this, "connection forbidden.");

		} else {

			multiCommand = true;
			interruptChannel = false;
			commandInfoName = commands.getMainName();
			SSHConnectionExecDialog multiDialog = null;
			if (dialog != null) {
				Logging.info(this, "exec_list, take given dialog");
				multiDialog = dialog;
			} else {
				Logging.info(this, "exec_list, create SSHConnectionExecDialog");
				multiDialog = SSHConnectionExecDialog.getInstance();
			}
			outputDialog = multiDialog;
			final SSHConnectionExecDialog finalDialog = multiDialog;

			StringBuilder defaultCommandsString = new StringBuilder();
			int anzahlCommands = ((SSHCommandTemplate) commands).getOriginalCommands().size();
			Logging.info(this, "exec_list, anzahlCommands " + anzahlCommands);

			for (int i = 0; i < anzahlCommands; i++) {
				String com = ((SSHCommandTemplate) commands).getOriginalCommands().get(i).getCommandRaw();
				com = "(" + (i + 1) + ")  " + com;

				defaultCommandsString.append(com + "   \n");
			}
			try {

				finalDialog.appendLater("\n\n\n" + LocalDate.now() + " " + LocalTime.now());
				finalDialog
						.appendLater("\n[" + Configed.getResourceValue("SSHConnection.Exec.dialog.commandlist").trim()
								+ "]\n" + defaultCommandsString + "\n\n");
				if (SSHCommandFactory.sshAlwaysExecInBackground) {
					multiDialog.setVisible(false);
					finalDialog.setVisible(false);
				}

				final SSHMultiCommand commandToExec = commands;
				Logging.info(this, "exec_list command " + commands);
				Logging.info(this, "exec_list commandToExec " + commandToExec);
				final SSHCommandParameterMethods pmethodHandler = SSHCommandFactory.getInstance(main)
						.getParameterHandler();
				final SSHConnectExec caller = this;
				foundError = false;

				if (!SSHCommandFactory.sshAlwaysExecInBackground) {
					finalDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
					finalDialog.setVisible(true);
				}

				pmethodHandler.canceled = false;
				boolean foundErrorInCommandList = false;
				List<SSHCommand> commandList = commandToExec.getCommands();
				for (SSHCommand co : commandToExec.getCommands()) {
					if (!foundErrorInCommandList) {

						// ???????? sollte hier eigentlich stehen?!
						// # nein! co wird vom phander verändert
						co = pmethodHandler.parseParameter(co, caller);
						if (!pmethodHandler.canceled) {
							if (co instanceof SSHSFTPCommand) {
								SSHConnectSCP sftp = new SSHConnectSCP(commandInfoName);
								sftp.exec(co, withGui, finalDialog, sequential, rememberPw, commandList.indexOf(co) + 1,
										commandList.size());
							} else {
								exec(co, withGui, finalDialog, sequential, rememberPw, commandList.indexOf(co) + 1,
										commandList.size());
							}
						} else {
							foundErrorInCommandList = true;
						}
					}
				}
				if (foundErrorInCommandList) {
					finalDialog.appendLater("[" + Configed.getResourceValue("SSHConnection.Exec.dialog.commandlist")
							+ "]     " + "" + Configed.getResourceValue("SSHConnection.Exec.exitClosed"));
				}

				Logging.info(this, "exec_list command after starting " + commands);
				Logging.info(this, "exec_list commandToExec " + commandToExec);

			} catch (Exception e) {
				Logging.warning("exception: " + e);
			}
		}
	}

	public String exec(SSHCommand command) {
		return exec(command, true, null, false, false, 1, 1);
	}

	public String exec(SSHCommand command, boolean withGui) {
		foundError = false;
		return exec(command, withGui, null, false, false, 1, 1);
	}

	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog) {
		return exec(command, withGui, dialog, false, false, 1, 1);
	}

	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog, boolean sequential,
			boolean rememberPw, int commandnumber, int maxcommandnumber) {
		if (!isConnectionAllowed()) {
			Logging.error(this, "connection forbidden.");
			return null;
		}

		if (foundError) {
			Logging.warning(this, "exec found error.");
			return command.getErrorText();
		}

		Logging.info(this, "exec command " + command.getSecuredCommand());
		Logging.info(this, "exec withGui " + withGui);
		Logging.info(this, "exec dialog " + dialog);
		Logging.info(this, "exec isConnected " + isConnected());

		if (!(isConnected())) {
			connect(command);
		}

		if (withGui) {
			Logging.info(this, "exec given dialog " + dialog);

			if (dialog != null) {
				outputDialog = dialog;
				if (!EventQueue.isDispatchThread()) {
					// does this really occur anywhere?

					SwingUtilities.invokeLater(() -> dialog.setVisible(true));
				} else {
					dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
					dialog.setVisible(true);
				}

			} else {
				outputDialog = SSHConnectionExecDialog.getInstance();
			}

			if (SSHCommandFactory.sshAlwaysExecInBackground) {
				outputDialog.setVisible(false);
			}

			outputDialog.setTitle(Configed.getResourceValue("SSHConnection.Exec.title") + " "
					+ Configed.getResourceValue("SSHConnection.Exec.dialog.commandoutput") + "  ("
					+ SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost()
					+ ")");

		} else {
			outputDialog = null;
		}

		try {

			Logging.info(this, "exec isConnected " + isConnected());
			SshCommandWorker task = new SshCommandWorker(command, outputDialog, withGui, rememberPw);
			task.setMaxCommandNumber(maxcommandnumber);
			task.setCommandNumber(commandnumber);
			task.execute();
			Logging.info(this, "execute was called with task for command " + command.getSecuredCommand());

			if (sequential) {
				return task.get();
			}

			if (SSHCommandFactory.sshAlwaysExecInBackground && withGui && outputDialog != null) {
				outputDialog.setVisible(false);
			}

			if (withGui) {
				return "finish";
			} else {
				return task.get();
			}
		} catch (InterruptedException e) {
			Logging.error(this, "exec InterruptedException", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			Logging.error(this, "exec ExecutionException", e);
		}
		return null;
	}

	protected String setAsInfoString(String s) {
		if (outputDialog != null && s.length() > 0 && !"\n".equals(s)) {
			return SSHConnectionOutputDialog.ANSI_CODE_INFO + s;
		}
		return s;
	}

	// first parameter class is return type of doInBackground
	// second is element type of the list which is used by process
	private class SshCommandWorker extends SwingWorker<String, String> {
		SSHCommand command;
		SSHConnectionExecDialog outputDialog;
		SSHConnectExec caller;

		boolean withGui;
		boolean rememberPw;
		boolean interruptChannelWorker;
		int retriedTimes = 1;
		int commandNumber = -1;
		int maxCommandNumber = -1;

		SshCommandWorker(SSHCommand command, SSHConnectionExecDialog outputDialog, boolean withGui,
				boolean rememberPw) {
			this(null, command, outputDialog, withGui, rememberPw);
		}

		SshCommandWorker(SSHConnectExec caller, SSHCommand command, SSHConnectionExecDialog outputDialog,
				boolean withGui, boolean rememberPw) {
			this(caller, command, outputDialog, withGui, rememberPw, false);
		}

		SshCommandWorker(SSHConnectExec caller, SSHCommand command, SSHConnectionExecDialog outputDialog,
				boolean withGui, boolean rememberPw, boolean interruptChannel) {
			super();
			if (caller != null) {
				this.caller = caller;
			}
			this.command = command;
			this.outputDialog = outputDialog;
			this.withGui = withGui;
			if (!withGui) {
				this.outputDialog = null;
			}

			this.rememberPw = rememberPw;
			this.interruptChannelWorker = interruptChannel;
			retriedTimes = 1;

		}

		public void setMaxCommandNumber(int mc) {
			this.maxCommandNumber = mc;
		}

		public void setCommandNumber(int cn) {
			this.commandNumber = cn;
		}

		private void checkExitCode(int exitCode, boolean withGui, Channel channel) {
			String s = "checkExitCode " + exitCode;
			Logging.debug(this, "publish " + s);
			publishInfo(
					"---------------------------------------------------------------------------------------------------------------------------------------------------");
			if (this.commandNumber != -1 && this.maxCommandNumber != -1) {
				publishInfo(Configed.getResourceValue("SSHConnection.Exec.commandcountertext")
						.replace("xX0Xx", Integer.toString(this.commandNumber))
						.replace("xX1Xx", Integer.toString(this.maxCommandNumber)));
			}

			publishInfo(s);
			if (exitCode == 127) {
				Logging.info(this, "exec exit code 127 (command does not exists).");
				Logging.debug(Configed.getResourceValue("SSHConnection.Exec.exit127"));
				if (withGui) {
					publishError(Configed.getResourceValue("SSHConnection.Exec.exit127"));
					Logging.info(this, "2. publish");
				}
			} else if (exitCode != 0) {
				foundError = true;
				Logging.info(this, "exec exit code " + exitCode + ".");
				Logging.debug(this, Configed.getResourceValue("SSHConnection.Exec.exitError") + " "
						+ Configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
				if (withGui) {
					publishError(Configed.getResourceValue("SSHConnection.Exec.exitError") + " "
							+ Configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
				}
			} else {
				foundError = true;
				Logging.debug(this, Configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
				Logging.debug(this, Configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				if (withGui) {
					publishError(Configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
					publishError(Configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				}
			}

			if (interruptChannelWorker && caller != null) {
				interruptChannel(channel);
				disconnect();
				interruptChannel = true;
				interruptChannelWorker = true;
				Globals.threadSleep(this, 50);
			}

		}

		@Override
		public String doInBackground() throws java.net.SocketException {
			StringBuilder buf = new StringBuilder();
			try {
				Logging.info(this, "doInBackground getSession " + getSession());

				if (!(isConnected())) {
					connect();
				}
				final Channel channel = getSession().openChannel("exec");

				((ChannelExec) channel).setErrStream(System.err);
				((ChannelExec) channel).setCommand(command.getCommand());
				final OutputStream out = channel.getOutputStream();
				final InputStream in = channel.getInputStream();
				channel.connect();
				killProcessListener = (ActionEvent actionEvent) -> {
					interruptChannel(channel);
					disconnect();
					interruptChannel = true;
					interruptChannelWorker = true;
					Globals.threadSleep(this, 50);
				};

				Logging.info(this, "doInBackground start waiting for answer");
				int size = 1024 * 1024;

				byte[] tmp = new byte[size];
				int progress = 0;
				if (outputDialog != null) {
					outputDialog.removeKillProcessListener(killProcessListener);
					outputDialog.addKillProcessListener(killProcessListener);

				}
				supwRetriedTimes = 0;
				while (true) {
					while (in.available() > 0) {

						int i = in.read(tmp, 0, size);
						Logging.info(this, "doInBackground i " + i);

						int timeStepMillis = 1000;
						Globals.threadSleep(this, timeStepMillis);

						if (i < 0) {
							break;
						}

						String str = new String(tmp, 0, i, StandardCharsets.UTF_8);

						if ((command.needSudo()) && (str.contains(SSHCommandFactory.SUDO_FAILED_TEXT))) {
							String pw = "";
							if (supwRetriedTimes >= 1) {
								pw = getSudoPass(outputDialog);
							} else {
								pw = getSudoPass(outputDialog, rememberPw);
							}

							if (pw == null) {
								Logging.info(this, "exec ready (1)");
								foundError = true;
								publish(Configed.getResourceValue("SSHConnection.Exec.exitClosed"));
								if (outputDialog != null) {
									outputDialog.setStatusFinish();
								}
								return null;
							} else {
								out.write((pw + "\n").getBytes());
								out.flush();
								supwRetriedTimes += 1;
							}
						}
						if (withGui) {
							for (String line : str.split("\n")) {

								Logging.debug(this, " doInBackground publish " + progress + ": " + line);
								publish(line);
								progress++;
								Globals.threadSleep(this, timeStepMillis);
							}
						} else {

							for (String line : str.split("\n")) {
								Logging.debug(this, "line: " + line);
							}
						}
						buf.append(str);
					}

					if (channel.isClosed() || interruptChannel || interruptChannelWorker) {
						if ((in.available() > 0) && (!interruptChannel)) {
							continue;
						}
						checkExitCode(channel.getExitStatus(), withGui, channel);
						if (channel.getExitStatus() != 0) {
							Logging.info(this, "exec ready (2)");
							foundError = true;
							if (outputDialog != null) {
								outputDialog.setStatusFinish();
							}
							return null;
						}
						break;
					}
				}
				Globals.threadSleep(this, 1000);

				if (outputDialog != null) {
					setDialog(outputDialog);
				}

				Logging.info(this, "exec ready (0)");
			} catch (JSchException jschex) {
				if (retriedTimes >= 3) {
					retriedTimes = 1;
					Logging.warning(this, "jsch exception", jschex);
					publishError(jschex.toString());
					return "";
				} else {
					Logging.warning(this, "jsch exception", jschex);
					retriedTimes = retriedTimes + 1;
					connect();
					doInBackground();
				}
			} catch (IOException ex) {
				Logging.warning(this, "SSH IOException", ex);
				publishError(ex.toString());
			}

			if (outputDialog != null && !multiCommand) {
				outputDialog.setStatusFinish();
			}

			return buf.toString();
		}

		@Override
		protected void process(List<String> chunks) {
			Logging.info(this, "chunks " + chunks.size());

			if (outputDialog != null) {
				outputDialog.setStartAnsi(Globals.SSH_CONNECTION_SET_START_ANSI);

				for (String line : chunks) {
					Logging.debug(this, "process " + line);
					SSHOutputCollector.appendValue(line);
					outputDialog.append(getCommandName(), line + "\n");

				}

			}
		}

		protected void publishInfo(String s) {
			if (outputDialog != null) {

				outputDialog.setStartAnsi(Globals.SSH_CONNECTION_SET_START_ANSI);
			}
		}

		protected void publishError(String s) {
			// TODO what to do if publishError?
		}

		@Override
		protected void done() {
			Logging.info(this, "done");
			if (outputDialog != null) {
				outputDialog.append(getCommandName(), "\n... READY  \n\n");
			}
			if (responseButton != null) {
				responseButton.setEnabled(true);
			}
		}

		private String getCommandName() {
			String commandinfo = "[" + this.command.getMenuText() + "]";
			if (this.commandNumber != -1 && this.maxCommandNumber != -1) {
				if ((commandInfoName != null) && (!commandInfoName.isEmpty())) {
					commandinfo = "[" + commandInfoName + "(" + Integer.toString(this.commandNumber) + "/"
							+ Integer.toString(this.maxCommandNumber) + ")]";
				} else {
					commandinfo = "[" + this.command.getMenuText() + "(" + Integer.toString(this.commandNumber) + "/"
							+ Integer.toString(this.maxCommandNumber) + ")]";
				}
			}

			return commandinfo;
		}
	}
}
