package de.uib.opsicommand.sshcommand;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHConnectionOutputDialog;
import de.uib.utilities.logging.logging;
import de.uib.utilities.ssh.SSHOutputCollector;

/**
 * @inheritDoc Class for executing commands.
 */
public class SSHConnectExec extends SSHConnect {
	protected SSHConnectionExecDialog outputDialog = null;
	protected boolean multiCommand = false;
	protected ActionListener killProcessListener;
	protected JButton responseButton;

	public SSHConnectExec(SSHCommand sshcommand) {
		this(null, sshcommand);
	}

	public SSHConnectExec(SSHCommand sshcommand, JButton responseButton) {
		this(null, sshcommand, responseButton);
	}

	public SSHConnectExec(ConfigedMain m) {
		super(m);
		FOUND_ERROR = false;
		main = m;

	}

	public SSHConnectExec(ConfigedMain m, SSHCommand sshcommand) {
		this(m, sshcommand, null);
	}

	public SSHConnectExec(ConfigedMain m, SSHCommand sshcommand, JButton responseButton) {
		super(m);
		FOUND_ERROR = false;
		main = m;

		logging.info(this, "SSHConnectExec main " + main);

		logging.info(this, "SSHConnectExec sshcommand " + sshcommand.getSecuredCommand());
		this.responseButton = responseButton;
		if (responseButton != null)
			responseButton.setEnabled(false);

		starting(sshcommand);
	}

	public void starting(SSHCommand sshcommand) {

		if (!(isConnected())) {
			final SSHCommandFactory factory = SSHCommandFactory.getInstance(main);
			logging.error(this, configed.getResourceValue("SSHConnection.not_connected.message") + " "
					+ factory.getConnectionState());
			return;
		}

		try {
			if (!(Globals.isGlobalReadOnly())) {
				logging.info(this, "starting, sshcommand isMultiCommand " + sshcommand.isMultiCommand());

				if (sshcommand instanceof SSHCommand_Template) {
					logging.info(this, "exec_template " + sshcommand + ": " + sshcommand.getCommand());
					execTemplate((SSHCommand_Template) sshcommand);
				} else {
					if (sshcommand.isMultiCommand()) {
						logging.info(this, "exec_list " + sshcommand + ": " + sshcommand.getCommand());
						execList((SSHMultiCommand) sshcommand);
					} else {
						logging.info(this, "exec " + sshcommand + ": " + sshcommand.getCommand());
						exec(sshcommand);
					}
				}
			} else {
				logging.warning(this, configed.getResourceValue("SSHConnection.Exec.cannotAsReadonly"));
				if (outputDialog != null)
					outputDialog.appendLater("[" + sshcommand.getId() + "] \t"
							+ configed.getResourceValue("SSHConnection.Exec.cannotAsReadonly"));
			}
		} catch (Exception e) {
			logging.error(this, "SSHConnectExec Exception", e);
		}
	}

	public SSHConnectExec() {
		super(null);
		connect();
	}

	public SSHConnectionExecDialog getDialog() {
		if (outputDialog != null)
			return outputDialog;
		return null;
	}

	public void setDialog(SSHConnectionExecDialog dia) {
		outputDialog = dia;
	}

	public void execTemplate(SSHCommand_Template command) {
		execList(command, true, null, true, true);
	}

	public void execTemplate(SSHCommand_Template command, SSHConnectionExecDialog dia, boolean sequential) {
		execList(command, true, dia, sequential, true);
	}

	public void execTemplate(SSHCommand_Template command, boolean sequential) {
		execList(command, true, null, sequential, true);
	}

	protected boolean interruptChannel = false;

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
		logging.info(this, "exec_list commands[" + ((SSHCommand) commands).getId() + "] withGui[" + withGui
				+ "] sequential[" + sequential + "] dialog[" + dialog + "]");
		if (!isConnectionAllowed()) {
			logging.warning(this, "connection forbidden.");

		} else {

			multiCommand = true;
			interruptChannel = false;
			commandInfoName = commands.getMainName();
			SSHConnectionExecDialog multiDialog = null;
			if (dialog != null) {
				logging.info(this, "exec_list, take given dialog");
				multiDialog = dialog;
			} else {
				logging.info(this, "exec_list, create SSHConnectionExecDialog");
				multiDialog = SSHConnectionExecDialog.getInstance();
			}
			outputDialog = multiDialog;
			final SSHConnectionExecDialog final_dia = multiDialog;

			String defaultCommandsString = "";
			int anzahlCommands = ((SSHCommand_Template) commands).getOriginalCommands().size();
			logging.info(this, "exec_list, anzahlCommands " + anzahlCommands);

			for (int i = 0; i < anzahlCommands; i++) {
				String com = ((SSHCommand_Template) commands).getOriginalCommands().get(i).getCommandRaw();
				com = "(" + (i + 1) + ")  " + com;

				// else

				defaultCommandsString = defaultCommandsString + com + "   \n";

			}
			try {

				final_dia.appendLater("\n\n\n" + new Date());
				final_dia.appendLater("\n[" + configed.getResourceValue("SSHConnection.Exec.dialog.commandlist").trim()
						+ "]\n" + defaultCommandsString + "\n\n");
				if (SSHCommandFactory.ssh_always_exec_in_background) {
					multiDialog.setVisible(false);
					final_dia.setVisible(false);
				}

				final SSHMultiCommand commandToExec = commands;
				logging.info(this, "exec_list command " + commands);
				logging.info(this, "exec_list commandToExec " + commandToExec);
				final SSHCommandParameterMethods pmethodHandler = SSHCommandFactory.getInstance(main)
						.getParameterHandler();
				final SSHConnectExec caller = this;
				FOUND_ERROR = false;

				if (!SSHCommandFactory.ssh_always_exec_in_background) {
					final_dia.centerOn(Globals.mainFrame);
					final_dia.setVisible(true);
				}

				pmethodHandler.canceled = false;
				boolean foundError = false;
				LinkedList<SSHCommand> commandList = commandToExec.getCommands();
				for (SSHCommand co : commandToExec.getCommands()) {
					if (!foundError) {

						co = pmethodHandler.parseParameter(co, caller); // ???????? sollte hier eigentlich stehen?!
																		// # nein! co wird vom phander verändert
						if (!pmethodHandler.canceled) {
							if (co instanceof SSHSFTPCommand) {
								SSHConnectSCP sftp = new SSHConnectSCP(commandInfoName);
								sftp.exec(co, withGui, final_dia, sequential, rememberPw, commandList.indexOf(co) + 1,
										commandList.size());
							} else
								exec(co, withGui, final_dia, sequential, rememberPw, commandList.indexOf(co) + 1,
										commandList.size());
						} else
							foundError = true;
					}
				}
				if (foundError) {
					final_dia.appendLater("[" + configed.getResourceValue("SSHConnection.Exec.dialog.commandlist")
							+ "]     " + "" + configed.getResourceValue("SSHConnection.Exec.exitClosed"));
				}

				logging.info(this, "exec_list command after starting " + commands);
				logging.info(this, "exec_list commandToExec " + commandToExec);

			} catch (Exception e) {
				logging.warning("exception: " + e);
			}
		}
	}

	protected boolean FOUND_ERROR = false;

	public String exec(SSHCommand command) {
		return exec(command, true, null, false, false);
	}

	public String exec(SSHCommand command, boolean withGui) {
		FOUND_ERROR = false;
		return exec(command, withGui, null, false, false);
	}

	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog) {
		return exec(command, withGui, dialog, false, false);
	}

	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog, boolean sequential,
			boolean rememberPw) {
		return exec(command, withGui, dialog, false, false, 1, 1);
	}

	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog, boolean sequential,
			boolean rememberPw, int commandnumber, int maxcommandnumber) {
		if (!isConnectionAllowed()) {
			logging.error(this, "connection forbidden.");
			return null;
		}

		if (FOUND_ERROR) {
			logging.warning(this, "exec found error.");
			return command.get_ERROR_TEXT();
		}

		logging.info(this, "exec command " + command.getSecuredCommand());
		logging.info(this, "exec withGui " + withGui);
		logging.info(this, "exec dialog " + dialog);
		logging.info(this, "exec isConnected " + isConnected());

		if (!(isConnected()))
			connect(command);

		if (withGui) {
			logging.info(this, "exec given dialog " + dialog);

			if (dialog != null) {
				outputDialog = dialog;
				if (!EventQueue.isDispatchThread())
				// does this really occur anywhere?
				{
					SwingUtilities.invokeLater(() -> dialog.setVisible(true));
				} else {
					dialog.centerOn(Globals.mainFrame);
					dialog.setVisible(true);
				}

			} else {
				outputDialog = SSHConnectionExecDialog.getInstance();
			}

			if (SSHCommandFactory.ssh_always_exec_in_background)
				outputDialog.setVisible(false);

			outputDialog.setTitle(configed.getResourceValue("SSHConnection.Exec.title") + " "
					+ configed.getResourceValue("SSHConnection.Exec.dialog.commandoutput") + "  ("
					+ SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost()
					+ ")");

		} else
			outputDialog = null;

		try {

			logging.info(this, "exec isConnected " + isConnected());
			SshCommandWorker task = new SshCommandWorker(command, outputDialog, withGui, rememberPw);
			task.setMaxCommandNumber(maxcommandnumber);
			task.setCommandNumber(commandnumber);
			task.execute();
			logging.info(this, "execute was called with task for command " + command.getSecuredCommand());

			if (sequential)
				return task.get();

			if (SSHCommandFactory.ssh_always_exec_in_background && withGui)
				outputDialog.setVisible(false);

			if (withGui)
				return "finish";
			else
				return task.get();
		} catch (java.lang.NullPointerException npe) {
			logging.error(this, "exec NullPointerException", npe);
		} catch (InterruptedException e) {
			logging.error(this, "exec InterruptedException", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			logging.error(this, "exec ExecutionException", e);
		}
		return null;
	}

	protected String setAsInfoString(String s) {
		if (outputDialog != null && s.length() > 0 && !s.equals("\n")) {
			return SSHConnectionOutputDialog.ANSI_CODE_INFO + s;
		}
		return s;
	}

	private class SshCommandWorker extends SwingWorker<String, String>
	// first parameter class is return type of doInBackground
	// second is element type of the list which is used by process
	{
		SSHCommand command;
		SSHConnectionExecDialog outputDialog;
		SSHConnectExec caller;

		boolean withGui;
		boolean rememberPw;
		boolean interruptChannelWorker = false;
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
			if (caller != null)
				this.caller = caller;
			this.command = command;
			this.outputDialog = outputDialog;
			this.withGui = withGui;
			if (!withGui)
				this.outputDialog = null;

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
			logging.debug(this, "publish " + s);
			publishInfo(
					"---------------------------------------------------------------------------------------------------------------------------------------------------");
			if (this.commandNumber != -1 && this.maxCommandNumber != -1)
				publishInfo(configed.getResourceValue("SSHConnection.Exec.commandcountertext")
						.replace("xX0Xx", Integer.toString(this.commandNumber))
						.replace("xX1Xx", Integer.toString(this.maxCommandNumber)));
			publishInfo(s);
			if (exitCode == 127) {
				logging.info(this, "exec exit code 127 (command does not exists).");
				logging.debug(configed.getResourceValue("SSHConnection.Exec.exit127"));
				if (withGui) {
					publishError(configed.getResourceValue("SSHConnection.Exec.exit127"));
					logging.info(this, "2. publish");
				}
			} else if (exitCode != 0) {
				FOUND_ERROR = true;
				logging.info(this, "exec exit code " + exitCode + ".");
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitError") + " "
						+ configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
				if (withGui) {
					publishError(configed.getResourceValue("SSHConnection.Exec.exitError") + " "
							+ configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
				}
			} else {
				FOUND_ERROR = true;
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				if (withGui) {
					publishError(configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
					publishError(configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				}
			}

			if (interruptChannelWorker && caller != null) {
				interruptChannel(channel);
				disconnect();
				interruptChannel = true;
				interruptChannelWorker = true;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

		}

		int supwRetriedTimes = 0;

		@Override
		public String doInBackground() throws java.net.SocketException {
			StringBuilder buf = new StringBuilder();
			try {
				logging.info(this, "doInBackground getSession " + getSession());

				if (!(isConnected()))
					connect();
				final Channel channel = getSession().openChannel("exec");

				((ChannelExec) channel).setErrStream(System.err);
				((ChannelExec) channel).setCommand(command.getCommand());
				final OutputStream out = channel.getOutputStream();
				final InputStream in = channel.getInputStream();
				channel.connect();
				killProcessListener = actionEvent -> {
					interruptChannel(channel);
					disconnect();
					interruptChannel = true;
					interruptChannelWorker = true;
					try {
						Thread.sleep(50);
					} catch (Exception ee) {
						Thread.currentThread().interrupt();
					}
				};

				logging.info(this, "doInBackground start waiting for answer");
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
						logging.info(this, "doInBackground i " + i);

						int timeStepMillis = 1000;
						try {
							Thread.sleep(timeStepMillis);
						} catch (InterruptedException ignore) {
							logging.info(this, "InterruptedException");
							Thread.currentThread().interrupt();
						}

						if (i < 0)
							break;
						String str = new String(tmp, 0, i, "UTF-8");

						if ((command.needSudo()) && (str.contains(SSHCommandFactory.sudo_failed_text))) {
							String pw = "";
							if (supwRetriedTimes >= 1)
								pw = getSudoPass(outputDialog);
							else
								pw = getSudoPass(outputDialog, rememberPw);

							if (pw == null) {
								logging.info(this, "exec ready (1)");
								FOUND_ERROR = true;
								publish(configed.getResourceValue("SSHConnection.Exec.exitClosed"));
								if (outputDialog != null)
									outputDialog.setStatusFinish();
								return null;
							} else {
								out.write((pw + "\n").getBytes());
								out.flush();
								supwRetriedTimes += 1;
							}
						}
						if (withGui) {
							for (String line : str.split("\n")) {

								logging.debug(this, " doInBackground publish " + progress + ": " + line);
								publish(new String(line));
								progress++;
								try {
									Thread.sleep(50);
								} catch (InterruptedException ee) {
									Thread.currentThread().interrupt();
								}
							}
						} else {

							for (String line : str.split("\n"))
								logging.debug(this, "line: " + line);
						}
						buf.append(str);
					}

					if (channel.isClosed() || interruptChannel || interruptChannelWorker) {
						if ((in.available() > 0) && (!interruptChannel))
							continue;
						checkExitCode(channel.getExitStatus(), withGui, channel);
						if (channel.getExitStatus() != 0) {
							logging.info(this, "exec ready (2)");
							FOUND_ERROR = true;
							if (outputDialog != null)
								outputDialog.setStatusFinish();
							return null;
						}
						break;
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ee) {
					Thread.currentThread().interrupt();
				}
				if (outputDialog != null)
					setDialog(outputDialog);
				logging.info(this, "exec ready (0)");
			}

			catch (JSchException jschex) {
				if (retriedTimes >= 3) {
					retriedTimes = 1;
					logging.warning(this, "jsch exception", jschex);
					publishError(jschex.toString());
					return "";
				} else {
					logging.warning(this, "jsch exception", jschex);
					retriedTimes = retriedTimes + 1;
					connect();
					doInBackground();
				}
			} catch (IOException ex) {
				logging.warning(this, "SSH IOException", ex);
				publishError(ex.toString());
			} catch (Exception e) {
				logging.warning(this, "SSH Exception", e);
				publishError(e.getMessage());
			}
			if (outputDialog != null && !multiCommand)
				outputDialog.setStatusFinish();

			return buf.toString();
		}

		@Override
		protected void process(List<String> chunks) {
			logging.info(this, "chunks " + chunks.size());
			final SSHOutputCollector sshOutputCollector = SSHOutputCollector.getInstance();

			if (outputDialog != null) {
				outputDialog.setStartAnsi(Globals.SSH_CONNECTION_SET_START_ANSI);

				for (String line : chunks) {
					logging.debug(this, "process " + line);
					sshOutputCollector.appendValue(line);
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
			logging.info(this, "done");
			if (outputDialog != null)
				outputDialog.append(getCommandName(), "\n... READY  \n\n");
			if (responseButton != null)
				responseButton.setEnabled(true);

		}

		private String getCommandName() {
			String commandinfo = "[" + this.command.getMenuText() + "]";
			if (this.commandNumber != -1 && this.maxCommandNumber != -1) {
				if ((commandInfoName != null) && (!commandInfoName.equals("")))
					commandinfo = "[" + commandInfoName + "(" + Integer.toString(this.commandNumber) + "/"
							+ Integer.toString(this.maxCommandNumber) + ")]";
				else
					commandinfo = "[" + this.command.getMenuText() + "(" + Integer.toString(this.commandNumber) + "/"
							+ Integer.toString(this.maxCommandNumber) + ")]";
			}

			return commandinfo;
		}
	}
}
