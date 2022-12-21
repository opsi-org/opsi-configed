package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.LinkedList;

import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHOpsiSetRightsParameterDialog;
import de.uib.utilities.logging.logging;

public class CommandOpsiSetRights extends SSHCommand_Template
		implements SSHCommand, SSHMultiCommand, SSHCommandNeedParameter {
	private String baseName = "opsi-set-rights ";
	private String command = "opsi-set-rights ";
	protected FGeneralDialog dialog = null;
	private boolean needSudo = true;
	private boolean needParameter = true;
	private boolean isMultiCommand = true;
	private LinkedList<SSHCommand> ssh_command = new LinkedList<SSHCommand>();
	private LinkedList<SSHCommand> ssh_command_original = new LinkedList<SSHCommand>();
	private int priority = 110;
	private String mainName = "";
	private String dir = null;
	private String myTmpCommand;

	public CommandOpsiSetRights() {
		command = "opsi-set-rights " + configed.getResourceValue("SSHConnection.command.opsisetrights.additionalPath")
				+ " ";
		ssh_command.add((SSHCommand) this);
	}

	public CommandOpsiSetRights(String d) {
		setDir(d);
		command = baseName + dir;
		if (d.length() > 0)
			if (d.charAt(d.length() - 1) != '/')
				d = d + "/";

		logging.info(this, "CommandOpsiSetRights dir " + dir);
		ssh_command.add((SSHCommand) this);
		ssh_command_original.add((SSHCommand) this);
	}

	@Override
	public String getMainName() {
		return mainName;
	}

	public void setMainName(String n) {
		mainName = n;
	}

	@Override
	public String getId() {
		return "CommandOpsiSetRights";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	@Override
	public String getMenuText() {
		return configed.getResourceValue("SSHConnection.command.opsisetrights");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return configed.getResourceValue("SSHConnection.command.opsisetrights.tooltip");
	}

	@Override
	public String getSecureInfoInCommand() {
		return null;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.getInstance().confidential);
		else
			return getCommand();
	}

	@Override
	public String getCommand() {
		if (dir != null)
			command = "opsi-set-rights " + dir;
		// command = "opsisetrights <<<Enter path (if needed)>>> ";
		if (needSudo())
			return SSHCommandFactory.getInstance().sudo_text + " " + command + " 2>&1";
		return command + " 2>&1";
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public LinkedList<String> getCommandsRaw() {
		LinkedList<String> commands_string_list = new LinkedList<String>();
		for (SSHCommand c : ssh_command) {
			String comstr = c.getCommandRaw();
			if (!((comstr == null) || (comstr.trim().equals(""))))
				commands_string_list.add(c.getCommandRaw());
		}
		return commands_string_list;
	}

	public LinkedList<SSHCommand> getOriginalCommands() {
		return ssh_command_original;
	}

	@Override
	public LinkedList<SSHCommand> getCommands() {
		return ssh_command;
	}

	@Override
	public boolean needSudo() {
		return needSudo;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	/**
	 * Sets the given command
	 * 
	 * @param c (command): String
	 **/
	public void setCommand(String c) {
		command = c;
	}

	/**
	 * Searches placeholders like <<<sth>>>
	 * 
	 * @return ArrayList with placeholdern for parameter
	 */
	@Override
	public ArrayList<String> getParameterList() {
		java.util.ArrayList<String> paramlist = new ArrayList<String>();
		String tmp_1 = SSHCommandFactory.getInstance().getParameterHandler().replacement_default_1;
		String tmp_2 = SSHCommandFactory.getInstance().getParameterHandler().replacement_default_2;
		if (command != null)
			if ((command.contains(tmp_1)) && (command.contains(tmp_2))) {
				myTmpCommand = getCommandRaw();
				logging.debug(this, "getParameterList myCommand_tmp " + myTmpCommand);
				for (int i = 0; i < counterString(getCommandRaw(), tmp_1); i++) {
					String plHolder = searchPlaceholder();
					if (!paramlist.contains(plHolder))
						paramlist.add(plHolder);
				}
			}
		logging.debug(this, "getParameterList command " + command + " placeholders " + paramlist);
		return paramlist;
	}

	/**
	 * Searches placeholder like <<<sth>>>
	 * 
	 * @return String with and between "<<<" and ">>>"
	 */
	private String searchPlaceholder() {
		String tmp_1 = SSHCommandFactory.getInstance().getParameterHandler().replacement_default_1;
		String tmp_2 = SSHCommandFactory.getInstance().getParameterHandler().replacement_default_2;

		String splitted_text = myTmpCommand.split(tmp_1, 2)[1].split(tmp_2, 2)[0];
		logging.debug(this, "searchPlaceholder found " + tmp_1 + splitted_text + tmp_2);
		myTmpCommand = myTmpCommand.replace(tmp_1 + splitted_text + tmp_2, "");
		logging.debug(this, "searchPlaceholder myCommand_tmp " + myTmpCommand);
		// logging.debug("my com now: : " + myTmpCommand);
		return tmp_1 + splitted_text + tmp_2;
	}

	/**
	 * @return the placeholder count
	 */
	private int counterString(String s, String search) {
		int times = 0;
		int index = s.indexOf(search, 0);
		while (index > 0) {
			index = s.indexOf(search, index + 1);
			++times;
		}
		logging.debug(this, "counterString placeholders count  " + times);
		return times;
	}

	@Override
	public void startParameterGui() {
		dialog = new SSHOpsiSetRightsParameterDialog(this);
	}

	@Override
	public void startParameterGui(ConfigedMain main) {
		dialog = new SSHOpsiSetRightsParameterDialog(this);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		return null;
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setDir(String d) {
		if (d != "")
			dir = " " + d;
		else
			dir = "";
	}

	public String getDir() {
		return dir;
	}
}
