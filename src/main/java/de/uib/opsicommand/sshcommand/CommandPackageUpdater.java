package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHPackageUpdaterDialog;
import de.uib.utilities.logging.Logging;

public class CommandPackageUpdater implements SSHCommand, SSHCommandNeedParameter {
	private String command;
	private String baseName = "opsi-package-updater";
	protected FGeneralDialog dialog = null;
	public boolean needSudo = false;
	private boolean needParameter = true;
	private boolean isMultiCommand = false;
	private int priority = 105;

	private String action = " list --repos";
	private String repo = "";
	private List<String> actionlist = new ArrayList<>();
	private Map<String, String> actionhash = new HashMap<>();
	private Map<String, String> repohash = new HashMap<>();
	private String verbosity = " -v ";

	public CommandPackageUpdater() {
		command = baseName;
		actionlist.add("list");
		actionhash.put(Configed.getResourceValue("SSHConnection.command.opsipackageupdater.action.list"), "list");
		actionlist.add("install");
		actionhash.put(Configed.getResourceValue("SSHConnection.command.opsipackageupdater.action.install"), "install");
		actionlist.add("update");
		actionhash.put(Configed.getResourceValue("SSHConnection.command.opsipackageupdater.action.update"), "update");
	}

	@Override
	public String getSecureInfoInCommand() {
		return "";
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
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
		return "CommandPackageUpdater";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackageupdater");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackageupdater.tooltip");
	}

	@Override
	public String getCommand() {
		if (action.equals("list")) {
			action = " list --repos ";
			repo = " ";
		}
		setCommand(baseName + verbosity + repo + action);
		if (needSudo() && !action.equals("list"))
			return SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		return command + " 2>&1"; // the output redirection semms not to produce a jsch input
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

	@Override
	public boolean needSudo() {
		return needSudo;
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

	}

	@Override
	public void startParameterGui(ConfigedMain main) {

		if (main.getOpsiVersion().length() == 0 || main.getOpsiVersion().charAt(0) == '<'
				|| main.getOpsiVersion().compareTo("4.1") < 0) {
			Logging.error(this, Configed.getResourceValue("OpsiConfdVersionError").replace("{0}", "4.1.0"));
		} else
			dialog = new SSHPackageUpdaterDialog();

	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		SSHCommand command = new CommandHelp(this);
		SSHConnectExec exec = new SSHConnectExec(command

		);
		return exec.getDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setVerbosity(int vSum) {
		String v = "";
		for (int i = 0; i < vSum; i++)
			v = v + "v";
		verbosity = " -" + v + " ";
		if (vSum == 0)
			verbosity = "";
	}

	public void setRepos(Map<String, String> r) {
		repohash = r;
	}

	public void setRepo(String r) {
		if (r == null)
			repo = " ";
		else
			repo = " --repo " + r + " ";
	}

	public Map<String, String> getRepos() {
		return repohash;
	}

	public void setAction(String a) {
		if (actionlist.contains(a))
			action = a;
		else
			action = "";
	}

	public String getAction(String text) {
		return actionhash.get(text);
	}

	public String[] getActionsText() {
		return actionhash.keySet().toArray(new String[0]);
	}

	public boolean checkCommand() {
		return !action.equals("");
	}

	@Override
	public List<String> getParameterList() {
		return null;
	}
}