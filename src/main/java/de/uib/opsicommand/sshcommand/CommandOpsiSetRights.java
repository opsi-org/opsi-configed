package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHOpsiSetRightsParameterDialog;
import de.uib.utilities.logging.Logging;

public class CommandOpsiSetRights extends SSHCommandTemplate implements SSHCommandNeedParameter {
	private static final int PRIORITY = 110;

	private static final String BASE_NAME = "opsi-set-rights ";

	private String command = BASE_NAME;

	private FGeneralDialog dialog;
	private boolean needSudo = true;
	private boolean needParameter = true;
	private boolean isMultiCommand = true;
	private List<SSHCommand> sshCommand = new LinkedList<>();
	private List<SSHCommand> sshCommandOriginal = new LinkedList<>();
	private String mainName = "";
	private String dir;
	private String myTmpCommand;

	public CommandOpsiSetRights() {
		command = "opsi-set-rights " + Configed.getResourceValue("SSHConnection.command.opsisetrights.additionalPath")
				+ " ";
		sshCommand.add(this);
	}

	public CommandOpsiSetRights(String d) {
		setDir(d);
		command = BASE_NAME + dir;
		if (d.length() > 0 && d.charAt(d.length() - 1) != '/') {
			d = d + "/";
		}

		Logging.info(this, "CommandOpsiSetRights dir " + dir);
		sshCommand.add(this);
		sshCommandOriginal.add(this);
	}

	@Override
	public String getMainName() {
		return mainName;
	}

	@Override
	public void setMainName(String n) {
		mainName = n;
	}

	@Override
	public String getId() {
		return "CommandOpsiSetRights";
	}

	@Override
	public String getBasicName() {
		return BASE_NAME;
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.opsisetrights");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.opsisetrights.tooltip");
	}

	@Override
	public String getSecureInfoInCommand() {
		return null;
	}

	@Override
	public String getSecuredCommand() {
		if (getSecureInfoInCommand() != null && !getSecureInfoInCommand().trim().isEmpty()) {
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	@Override
	public String getCommand() {
		if (dir != null) {
			command = "opsi-set-rights " + dir;
		}

		if (needSudo()) {
			return SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		}

		return command + " 2>&1";
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public LinkedList<String> getCommandsRaw() {
		LinkedList<String> commandsStringList = new LinkedList<>();
		for (SSHCommand c : sshCommand) {
			String comstr = c.getCommandRaw();
			if (!(comstr == null || comstr.trim().isEmpty())) {
				commandsStringList.add(c.getCommandRaw());
			}
		}
		return commandsStringList;
	}

	@Override
	public List<SSHCommand> getOriginalCommands() {
		return sshCommandOriginal;
	}

	@Override
	public List<SSHCommand> getCommands() {
		return sshCommand;
	}

	@Override
	public boolean needSudo() {
		return needSudo;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
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
	@Override
	public void setCommand(String c) {
		command = c;
	}

	/**
	 * Searches placeholders like <<<sth>>>
	 * 
	 * @return List with placeholdern for parameter
	 */
	@Override
	public List<String> getParameterList() {
		List<String> paramlist = new ArrayList<>();
		String temp1 = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_1;
		String temp2 = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_2;
		if (command != null && command.contains(temp1) && command.contains(temp2)) {
			myTmpCommand = getCommandRaw();
			Logging.debug(this, "getParameterList myCommand_tmp " + myTmpCommand);
			for (int i = 0; i < counterString(getCommandRaw(), temp1); i++) {
				String plHolder = searchPlaceholder();
				if (!paramlist.contains(plHolder)) {
					paramlist.add(plHolder);
				}
			}
		}
		Logging.debug(this, "getParameterList command " + command + " placeholders " + paramlist);
		return paramlist;
	}

	/**
	 * Searches placeholder like <<<sth>>>
	 * 
	 * @return String with and between "<<<" and ">>>"
	 */
	private String searchPlaceholder() {
		String temp1 = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_1;
		String temp2 = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_2;

		String splittedText = myTmpCommand.split(temp1, 2)[1].split(temp2, 2)[0];
		Logging.debug(this, "searchPlaceholder found " + temp1 + splittedText + temp2);
		myTmpCommand = myTmpCommand.replace(temp1 + splittedText + temp2, "");
		Logging.debug(this, "searchPlaceholder myCommand_tmp " + myTmpCommand);

		return temp1 + splittedText + temp2;
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
		Logging.debug(this, "counterString placeholders count  " + times);
		return times;
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

	public final void setDir(String d) {
		if (!d.isEmpty()) {
			dir = " " + d;
		} else {
			dir = "";
		}
	}

	public String getDir() {
		return dir;
	}
}
