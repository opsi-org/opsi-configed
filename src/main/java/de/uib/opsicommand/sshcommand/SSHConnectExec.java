/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import java.awt.EventQueue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHConnectionOutputDialog;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

/**
 * @inheritDoc Class for executing commands.
 */
public class SSHConnectExec extends SSHConnect {
	private SSHConnectionExecDialog outputDialog;
	protected boolean multiCommand;
	private JButton responseButton;

	protected boolean foundError;

	private boolean interruptChannel;

	public SSHConnectExec() {
		super(null);
	}

	public SSHConnectExec(SSHCommand sshcommand) {
		this(null, sshcommand);
	}

	public SSHConnectExec(SSHCommand sshcommand, JButton responseButton) {
		this(null, sshcommand, responseButton);
	}

	public SSHConnectExec(ConfigedMain configedMain) {
		super(configedMain);
		foundError = false;
		this.configedMain = configedMain;
	}

	public SSHConnectExec(ConfigedMain configedMain, SSHCommand sshcommand) {
		this(configedMain, sshcommand, null);
	}

	public SSHConnectExec(ConfigedMain configedMain, SSHCommand sshcommand, JButton responseButton) {
		super(configedMain);
		foundError = false;
		this.configedMain = configedMain;

		Logging.info(this.getClass(), "SSHConnectExec main " + configedMain);

		Logging.info(this.getClass(), "SSHConnectExec sshcommand " + sshcommand.getSecuredCommand());
		this.responseButton = responseButton;
		if (responseButton != null) {
			responseButton.setEnabled(false);
		}

		starting(sshcommand);
	}

	private void starting(SSHCommand sshcommand) {
		if (!(isConnected())) {
			final SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);
			Logging.error(this, Configed.getResourceValue("SSHConnection.not_connected.message") + " "
					+ factory.getConnectionState());
			return;
		}

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			Logging.info(this, "starting, sshcommand isMultiCommand " + sshcommand.isMultiCommand());

			if (sshcommand instanceof SSHCommandTemplate) {
				Logging.info(this, "exec_template " + sshcommand + ": " + sshcommand.getSecuredCommand());
				execTemplate((SSHCommandTemplate) sshcommand);
			} else {
				if (sshcommand.isMultiCommand()) {
					Logging.info(this, "exec_list " + sshcommand + ": " + sshcommand.getSecuredCommand());
					execList((SSHMultiCommand) sshcommand);
				} else {
					Logging.info(this, "exec " + sshcommand + ": " + sshcommand.getSecuredCommand());
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

	public void execTemplate(SSHCommandTemplate command, boolean sequential) {
		execList(command, true, null, sequential, true);
	}

	public void execList(SSHMultiCommand commands) {
		execList(commands, true, null, false, true);
	}

	public void execList(final SSHMultiCommand commands, final boolean withGui, SSHConnectionExecDialog dialog,
			final boolean sequential, final boolean rememberPw) {
		Logging.info(this, "exec_list commands[" + ((SSHCommand) commands).getId() + "] withGui[" + withGui
				+ "] sequential[" + sequential + "] dialog[" + dialog + "]");
		if (!isConnectionAllowed()) {
			Logging.warning(this, "connection forbidden.");
			return;
		}

		multiCommand = true;
		interruptChannel = false;
		commandInfoName = commands.getMainName();
		SSHConnectionExecDialog multiDialog = null;
		if (dialog != null) {
			Logging.info(this, "exec_list, take given dialog");
			multiDialog = dialog;
		} else {
			Logging.info(this, "exec_list, create SSHConnectionExecDialog");
			multiDialog = new SSHConnectionExecDialog();
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

		finalDialog.appendLater("\n\n\n" + LocalDate.now() + " " + LocalTime.now());
		finalDialog.appendLater("\n[" + Configed.getResourceValue("SSHConnection.Exec.dialog.commandlist").trim()
				+ "]\n" + defaultCommandsString + "\n\n");
		if (SSHCommandFactory.alwaysExecInBackground()) {
			multiDialog.setVisible(false);
			finalDialog.setVisible(false);
		}

		final SSHMultiCommand commandToExec = commands;
		Logging.info(this, "exec_list command " + commands);
		Logging.info(this, "exec_list commandToExec " + commandToExec);
		final SSHCommandParameterMethods pmethodHandler = SSHCommandFactory.getInstance(configedMain)
				.getParameterHandler();
		final SSHConnectExec caller = this;
		foundError = false;

		if (!SSHCommandFactory.alwaysExecInBackground()) {
			finalDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
			finalDialog.setVisible(true);
		}

		pmethodHandler.setCanceled(false);
		boolean foundErrorInCommandList = false;
		List<SSHCommand> commandList = commandToExec.getCommands();
		for (SSHCommand co : commandToExec.getCommands()) {
			co = pmethodHandler.parseParameter(co, caller);
			if (!pmethodHandler.isCanceled()) {
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
		if (foundErrorInCommandList) {
			finalDialog.appendLater("[" + Configed.getResourceValue("SSHConnection.Exec.dialog.commandlist") + "]     "
					+ "" + Configed.getResourceValue("SSHConnection.Exec.exitClosed"));
		}

		Logging.info(this, "exec_list command after starting " + commands);
		Logging.info(this, "exec_list commandToExec " + commandToExec);
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

	@SuppressWarnings({ "java:S2301" })
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
			displayOutputDialog(dialog);
		} else {
			outputDialog = null;
		}

		String result = null;

		try {
			Logging.info(this, "exec isConnected " + isConnected());
			SSHCommandWorker task = new SSHCommandWorker(this, command, outputDialog, withGui, rememberPw,
					interruptChannel);
			task.setMaxCommandNumber(maxcommandnumber);
			task.setCommandNumber(commandnumber);
			task.execute();
			Logging.info(this, "execute was called with task for command " + command.getSecuredCommand());

			if (sequential) {
				result = task.get();
			} else {
				if (SSHCommandFactory.alwaysExecInBackground() && withGui && outputDialog != null) {
					outputDialog.setVisible(false);
				}

				if (withGui) {
					result = "finish";
				} else {
					result = task.get();
				}
			}
		} catch (InterruptedException e) {
			Logging.error(this, "exec InterruptedException", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			Logging.error(this, "exec ExecutionException", e);
		}
		return result;
	}

	private void displayOutputDialog(SSHConnectionExecDialog dialog) {
		if (dialog != null) {
			outputDialog = dialog;
			if (!EventQueue.isDispatchThread()) {
				SwingUtilities.invokeLater(() -> outputDialog.setVisible(true));
			} else {
				outputDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
				outputDialog.setVisible(true);
			}
		} else {
			outputDialog = new SSHConnectionExecDialog();
		}

		outputDialog.setTitle(Configed.getResourceValue("SSHConnection.Exec.title") + " "
				+ Configed.getResourceValue("SSHConnection.Exec.dialog.commandoutput") + "  ("
				+ SSHConnectionInfo.getInstance().getUser() + "@" + SSHConnectionInfo.getInstance().getHost() + ")");
		outputDialog.setVisible(!SSHCommandFactory.alwaysExecInBackground());
	}

	public boolean isMultiCommand() {
		return multiCommand;
	}

	public void enableResponseButton() {
		if (responseButton != null) {
			responseButton.setEnabled(true);
		}
	}

	public String getCommandInfoName() {
		return commandInfoName;
	}

	public void foundError() {
		foundError = true;
	}

	public void interruptChannel() {
		interruptChannel = true;
	}

	public boolean isChannelInterrupted() {
		return interruptChannel;
	}

	protected String setAsInfoString(String s) {
		if (outputDialog != null && s.length() > 0 && !"\n".equals(s)) {
			return SSHConnectionOutputDialog.ANSI_CODE_INFO + s;
		}
		return s;
	}
}
