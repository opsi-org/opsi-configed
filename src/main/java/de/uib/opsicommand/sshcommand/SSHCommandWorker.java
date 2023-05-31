/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.SwingWorker;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.ssh.SSHOutputCollector;

// first parameter class is return type of doInBackground
// second is element type of the list which is used by process
public class SSHCommandWorker extends SwingWorker<String, String> {
	private SSHCommand command;
	private SSHConnectionExecDialog outputDialog;
	private SSHConnectExec caller;

	private boolean withGui;
	private boolean rememberPw;
	private boolean interruptChannelWorker;
	private int retriedTimes = 1;
	private int commandNumber = -1;
	private int maxCommandNumber = -1;

	SSHCommandWorker(SSHConnectExec caller, SSHCommand command, SSHConnectionExecDialog outputDialog, boolean withGui,
			boolean rememberPw, boolean interruptChannel) {
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
		publishInfo(SSHConnectExec.SEPARATING_LINE);
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
			caller.foundError();
			Logging.info(this, "exec exit code " + exitCode + ".");
			Logging.debug(this, Configed.getResourceValue("SSHConnection.Exec.exitError") + " "
					+ Configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
			if (withGui) {
				publishError(Configed.getResourceValue("SSHConnection.Exec.exitError") + " "
						+ Configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
			}
		} else {
			caller.foundError();
			Logging.debug(this, Configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
			Logging.debug(this, Configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
			if (withGui) {
				publishError(Configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
				publishError(Configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
			}
		}

		if (interruptChannelWorker && caller != null) {
			caller.interruptChannel(channel);
			caller.disconnect();
			caller.interruptChannel();
			interruptChannelWorker = true;
			Globals.threadSleep(this, 50);
		}

	}

	@SuppressWarnings("java:S106")
	@Override
	public String doInBackground() throws java.net.SocketException {
		StringBuilder buf = new StringBuilder();
		try {
			Logging.info(this, "doInBackground getSession " + caller.getSession());

			if (!(caller.isConnected())) {
				caller.connect();
			}
			final Channel channel = caller.getSession().openChannel("exec");

			((ChannelExec) channel).setErrStream(System.err);
			((ChannelExec) channel).setCommand(command.getCommand());
			final OutputStream out = channel.getOutputStream();
			final InputStream in = channel.getInputStream();
			channel.connect();
			ActionListener killProcessListener = (ActionEvent actionEvent) -> {
				caller.interruptChannel(channel);
				caller.disconnect();
				caller.interruptChannel();
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

			int supwRetriedTimes = 0;

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

					if (command.needSudo() && str.contains(SSHCommandFactory.SUDO_FAILED_TEXT)) {
						String pw = "";
						if (supwRetriedTimes >= 1) {
							pw = caller.getSudoPass(outputDialog);
						} else {
							pw = caller.getSudoPass(outputDialog, rememberPw);
						}

						if (pw == null) {
							Logging.info(this, "exec ready (1)");
							caller.foundError();
							publish(Configed.getResourceValue("SSHConnection.Exec.exitClosed"));
							if (outputDialog != null) {
								outputDialog.setStatusFinish();
							}
							return null;
						} else {
							out.write((pw + "\n").getBytes(StandardCharsets.UTF_8));
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

				if (channel.isClosed() || caller.isChannelInterrupted() || interruptChannelWorker) {
					if (in.available() > 0 && !caller.isChannelInterrupted()) {
						continue;
					}
					checkExitCode(channel.getExitStatus(), withGui, channel);
					if (channel.getExitStatus() != 0) {
						Logging.info(this, "exec ready (2)");
						caller.foundError();
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
				caller.setDialog(outputDialog);
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
				caller.connect();
				doInBackground();
			}
		} catch (IOException ex) {
			Logging.warning(this, "SSH IOException", ex);
			publishError(ex.toString());
		}

		if (outputDialog != null && !caller.isMultiCommand()) {
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

	private void publishInfo(String s) {
		if (outputDialog != null) {

			outputDialog.setStartAnsi(Globals.SSH_CONNECTION_SET_START_ANSI);
		}
	}

	private void publishError(String s) {
		// TODO what to do if publishError?
	}

	@Override
	protected void done() {
		Logging.info(this, "done");
		if (outputDialog != null) {
			outputDialog.append(getCommandName(), "\n... READY  \n\n");
		}

		caller.enableResponseButton();
	}

	private String getCommandName() {
		String commandinfo = "[" + this.command.getMenuText() + "]";
		if (this.commandNumber != -1 && this.maxCommandNumber != -1) {
			if (caller.getCommandInfoName() != null && !caller.getCommandInfoName().isEmpty()) {
				commandinfo = "[" + caller.getCommandInfoName() + "(" + Integer.toString(this.commandNumber) + "/"
						+ Integer.toString(this.maxCommandNumber) + ")]";
			} else {
				commandinfo = "[" + this.command.getMenuText() + "(" + Integer.toString(this.commandNumber) + "/"
						+ Integer.toString(this.maxCommandNumber) + ")]";
			}
		}

		return commandinfo;
	}
}
