package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;

import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHDeployClientAgentParameterDialog;

public class CommandDeployClientAgent implements SSHCommand, SSHCommandNeedParameter {
	private String command;
	private String baseName = "/var/lib/opsi/depot/opsi-client-agent/opsi-deploy-client-agent";
	protected FGeneralDialog dialog = null;
	private boolean needingSudo = true;
	private boolean pingIsRequired = true;
	private boolean needParameter = true;
	private boolean isMultiCommand = false;
	private int priority = 105;

	private String client = "";
	private String user = "";
	private String passw = "";
	private String finishAction = "";
	private String keepClientOnFailure = "";
	private String verbosity = "";

	public CommandDeployClientAgent() {
		command = baseName;
	}

	@Override
	public String getSecureInfoInCommand() {
		return passw;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.getInstance().confidential);
		else
			return getCommand();
	}

	@Override
	/**
	 * Sets the command specific error text
	 **/
	public String get_ERROR_TEXT() {
		return "ERROR";
	}

	@Override
	public String getId() {
		return "CommandDeployClientAgent";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	public enum FinalActionType {
		START_OCD, REBOOT, SHUTDOWN
	};

	public void finish(FinalActionType actionType) {
		switch (actionType) {
		case START_OCD:
			finishAction = " --start-opsiclientd ";
			break;
		case REBOOT:
			finishAction = " --reboot";
			break;
		case SHUTDOWN:
			finishAction = " --shutdown";
			break;
		default:
			finishAction = "";
		}
	}

	public void finish(String action) {
		switch (action) {
		case "startocd":
			finishAction = " --start-opsiclientd ";
			break;
		case "reboot":
			finishAction = " --reboot";
			break;
		case "shutdown":
			finishAction = " --shutdown";
			break;
		default:
			finishAction = "";
		}
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getMenuText() {
		return configed.getResourceValue("SSHConnection.command.deploy-clientagent");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return configed.getResourceValue("SSHConnection.command.deploy-clientagent.tooltip");
	}

	@Override
	public String getCommand() {
		command = baseName + " " + verbosity + user + passw + finishAction + keepClientOnFailure + getPingOption()
				+ client;
		if (needSudo())
			return SSHCommandFactory.sudo_text + " " + command + " 2>&1";
		return command + " 2>&1";
	}

	/**
	 * Sets the given command
	 * 
	 * @param c (command): String
	 **/
	@Override
	public void setCommand(String c) {
		command = c;
	}

	public void setNeedingSudo(boolean b) {
		needingSudo = b;
	}

	public void togglePingIsRequired() {
		pingIsRequired = !pingIsRequired;
	}

	public boolean isPingRequired() {
		return pingIsRequired;
	}

	private String getPingOption() {
		if (pingIsRequired)
			return " ";
		else
			return " --ignore-failed-ping ";
	}

	@Override
	public boolean needSudo() {
		return needingSudo;
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public void startParameterGui() {
		dialog = new SSHDeployClientAgentParameterDialog();
	}

	@Override
	public void startParameterGui(ConfigedMain main) {
		dialog = new SSHDeployClientAgentParameterDialog(main);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		SSHCommand command = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec(command
		/*
		 * new SSHConnectionExecDialog(
		 * configed.getResourceValue("SSHConnection.Exec.title") +
		 * " \""+command.getCommand() + "\" ",
		 * command)
		 */
		);
		// exec.exec(command, true, new SSHConnectionExecDialog(command,
		// configed.getResourceValue("SSHConnection.Exec.title") + "
		// \""+command.getCommand() + "\" "));
		return (SSHConnectionExecDialog) exec.getDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}
	// @Override
	// public int getHelpColumns()
	// {
	// return helpColumns;
	// }

	public void setClient(String c) {
		if (c != "")
			client = " " + c;
		else
			client = "";
	}

	public void setUser(String u) {
		if (u != "")
			user = " -u " + u;
		else
			user = "";
	}

	public void setVerbosity(int v_sum) {
		String v = "";
		for (int i = 0; i < v_sum; i++)
			v = v + "v";
		verbosity = " -" + v + " ";
		if (v_sum == 0)
			verbosity = "";
	}

	public void setPassw(String pw) {
		if (pw != "")
			passw = " -p " + pw;
		else
			passw = "";
	}

	public void setKeepClient(boolean kc) {
		if (kc)
			keepClientOnFailure = " --keep-client-on-failure ";
		else
			keepClientOnFailure = "";
	}

	public boolean checkCommand() {
		if (client == "")
			return false;
		if (passw == "")
			return false;
		return true;
	}

	@Override
	public ArrayList<String> getParameterList() {
		return null;
	}

}