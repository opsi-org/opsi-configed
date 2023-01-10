package de.uib.opsicommand.sshcommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHConnectionOutputDialog;
import de.uib.utilities.logging.logging;

/***
if more then one command have to be executed (e.g. also a set-rights) use SSHConnectExec. 
This class those the Right SwingWorker to execute it.
***/

/**
 * @inheritDoc Class for executing commands.
 */
public class SSHConnectSCP extends SSHConnectExec {
	SSHConnectionExecDialog outputDialog;

	public SSHConnectSCP() {
	}

	public SSHConnectSCP(String commandInfo) {
		commandInfoName = commandInfo;
	}

	public SSHConnectSCP(SSHSFTPCommand command) {
		this(command, null);
	}

	public SSHConnectSCP(SSHSFTPCommand command, SSHConnectionExecDialog outDia) {
		connect();
		start(command, outDia);
	}

	public String start(SSHSFTPCommand command) {
		return start(command, null);
	}

	public String start(SSHSFTPCommand command, SSHConnectionExecDialog outDia) {
		logging.debug(this, "starting, create SSHConnectionExecDialog");
		logging.info(this, "execsftp command " + command.getDescription());
		logging.debug(this, "execsftp withGui " + command.getShowOutputDialog());
		logging.debug(this, "execsftp dialog " + outDia);
		logging.debug(this, "execsftp isConnected " + isConnected());

		if (!(isConnected()))
			connect();

		if (command.getShowOutputDialog()) {
			if (outDia != null)
				setDialog(outDia);
			else
				setDialog(SSHConnectionExecDialog.getInstance());
			outputDialog = getDialog();

			if (SSHCommandFactory.sshAlwaysExecInBackground) {
				outputDialog.setVisible(false);
			}
		}

		try {
			logging.info(this, "exec isConnected " + isConnected());
			SshSFTPCommandWorker task = new SshSFTPCommandWorker(command, outputDialog, command.getShowOutputDialog());
			task.execute();
			logging.info(this, "execute was called");

			if (SSHCommandFactory.sshAlwaysExecInBackground)
				outputDialog.setVisible(command.getShowOutputDialog());

			return task.get();
		} catch (ExecutionException e) {
			logging.error(this, "exec Exception", e);
		} catch (InterruptedException e) {
			logging.error(this, "interrupted Exception", e);
			Thread.currentThread().interrupt();
		}
		return null;
	}

	@Override
	public SSHConnectionExecDialog getDialog() {
		return outputDialog;
	}

	@Override
	public void setDialog(SSHConnectionExecDialog dia) {
		outputDialog = dia;
	}

	@Override
	public String exec(SSHCommand com, boolean withGui, final SSHConnectionExecDialog dialog, boolean sequential,
			boolean rememberPw, int commandnumber, int maxcommandnumber) {
		try {
			SSHSFTPCommand command = (SSHSFTPCommand) com;
			logging.info(this, "exec isConnected " + isConnected());
			SshSFTPCommandWorker task = new SshSFTPCommandWorker(command, dialog, withGui);
			task.setMaxCommandNumber(maxcommandnumber);
			task.setCommandNumber(commandnumber);
			task.execute();
			logging.info(this, "execute was called");

			if (sequential)
				return task.get();

			if (SSHCommandFactory.sshAlwaysExecInBackground)
				dialog.setVisible(withGui);

			return task.get();
		} catch (java.lang.NullPointerException npe) {
			logging.error(this, "exec NullPointerException", npe);
		} catch (ExecutionException e) {
			logging.error(this, "exec Exception", e);
		} catch (InterruptedException e) {
			logging.error(this, "interrupted Exception", e);
			Thread.currentThread().interrupt();
		}
		return "end of method";
	}

	private class SshSFTPCommandWorker extends SwingWorker<String, String>
	// first parameter class is return type of doInBackground
	// second is element type of the list which is used by process
	{
		SSHSFTPCommand command;
		SSHConnectionExecDialog outputDialog;
		SSHConnectExec caller;

		boolean withGui;
		boolean interruptChannel = false;
		int retriedTimes = 1;
		int commandNumber = -1;
		int maxCommandNumber = -1;

		SshSFTPCommandWorker(SSHSFTPCommand command, SSHConnectionExecDialog outputDialog, boolean withGui) {
			super();
			this.command = command;
			this.outputDialog = outputDialog;
			this.withGui = withGui;
			retriedTimes = 1;
			if ((this.command.getDescription() != null) && (!this.command.getDescription().equals(""))) {
				publishInfo("exec:  " + this.command.getDescription() + "");
			}
			publishInfo(
					"---------------------------------------------------------------------------------------------------------------------------------------------------");
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
			publishInfo(s.replace("-1", "0"));
			if (exitCode == 127) {
				logging.info(this, "exec exit code 127 (command does not exists).");
				logging.debug(configed.getResourceValue("SSHConnection.Exec.exit127"));
				if (withGui) {
					publishError(configed.getResourceValue("SSHConnection.Exec.exit127"));
					logging.info(this, "2. publish");
				}
			} else if ((exitCode != 0) && (exitCode != -1)) {
				FOUND_ERROR = true;
				logging.info(this, "exec exit code " + exitCode + ".");
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitError")
						+ configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
				if (withGui) {
					publishError(configed.getResourceValue("SSHConnection.Exec.exitError")
							+ configed.getResourceValue("SSHConnection.Exec.exitCode") + " " + exitCode);
				}
			} else if ((exitCode == 0) || (exitCode == -1)) {
				logging.info(this, "exec exit code 0");
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitNoError"));
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				if (withGui) {
					publishInfo(configed.getResourceValue("SSHConnection.Exec.exitNoError"));
					publishInfo(configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
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
			if (interruptChannel && caller != null) {
				interruptChannel(channel);
				disconnect();
				interruptChannel = true;
				Globals.threadSleep(this, 50);
			}
			publishInfo(
					"---------------------------------------------------------------------------------------------------------------------------------------------------");
		}

		@Override
		public String doInBackground() throws java.net.SocketException {
			StringBuilder buf = new StringBuilder();
			File sourcefile = new File(command.getFullSourcePath());
			try (FileInputStream fis = new FileInputStream(sourcefile)) {
				logging.info(this, "doInBackground getSession " + getSession());

				if (!(isConnected()))
					connect();

				final Channel channel = getSession().openChannel("sftp");
				logging.info(this, "doInBackground channel openchannel " + channel);
				channel.connect();

				logging.info(this, "doInBackground channel connect " + channel);
				final ChannelSftp channelsftp = (ChannelSftp) channel;

				channelsftp.cd(command.getTargetPath());
				publish("Set target directory … " + command.getTargetPath());

				if (command.getOverwriteMode())
					channelsftp.put(fis, command.getTargetFilename(), ChannelSftp.OVERWRITE);
				else
					channelsftp.put(fis, command.getTargetFilename());
				publish("Set target filename … " + command.getTargetFilename());
				publish("Set overwrite mode … " + command.getOverwriteMode());
				publish(" ");
				Globals.threadSleep(this, 2000);

				publish("Copying finish ");
				channel.disconnect();
				session.disconnect();

				checkExitCode(channel.getExitStatus(), withGui, channel);
				if ((channel.getExitStatus() != 0) && (channel.getExitStatus() != -1)) {
					logging.info(this, "exec ready (2)");
					FOUND_ERROR = true;
					if (outputDialog != null)
						outputDialog.setStatusFinish();
					return null;
				}

				setDialog(outputDialog);
				logging.info(this, "exec ready (0)");
			}

			catch (JSchException jschex) {
				if (retriedTimes >= 3) {
					retriedTimes = 1;
					logging.warning(this, "jsch exception ", jschex);
					publishError(jschex.toString());
					FOUND_ERROR = true;
					return "";
				} else {
					logging.warning(this, "jsch exception ", jschex);
					retriedTimes = retriedTimes + 1;
					connect();
					doInBackground();
				}
			} catch (IOException ex) {
				logging.warning(this, "SSH IOException", ex);
				FOUND_ERROR = true;
				publishError(ex.toString());
			} catch (Exception e) {
				logging.warning(this, "SSH Exception", e);
				FOUND_ERROR = true;
				publishError(e.getMessage());
				Thread.currentThread().interrupt();
			}

			if (outputDialog != null && !multiCommand) {
				outputDialog.setStatusFinish();
				disconnect();
			}
			return buf.toString();
		}

		@Override
		protected void process(List<String> chunks) {
			logging.debug(this, "chunks " + chunks.size());
			if (outputDialog != null) {

				for (String line : chunks) {
					logging.debug(this, "process " + line);
					outputDialog.append(getCommandName(), line + "\n");
				}
			}
		}

		protected void publishInfo(String s) {
			if (outputDialog != null)
				publish(setAsInfoString(s));
		}

		protected void publishError(String s) {
			if (outputDialog != null && s.length() > 0 && !s.equals("\n"))
				s = SSHConnectionOutputDialog.ANSI_CODE_ERROR + s;
			publish(s);
		}

		@Override
		protected void done() {
			logging.info(this, "done");
		}

		private String getCommandName() {

			String counterInfo = "";
			if (this.commandNumber != -1 && this.maxCommandNumber != -1)
				counterInfo = "(" + Integer.toString(this.commandNumber) + "/" + Integer.toString(this.maxCommandNumber)
						+ ")";

			String commandinfo = "[" + this.command.getId() + counterInfo + "]";
			if ((commandInfoName != null) && (!commandInfoName.equals("")))
				commandinfo = "[" + commandInfoName + counterInfo + "]";

			return commandinfo;
		}
	}
}