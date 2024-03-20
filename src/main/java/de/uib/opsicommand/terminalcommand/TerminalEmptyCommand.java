/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.SSHCommandParameterMethods;
import de.uib.utilities.logging.Logging;

/**
 * This class represent a simple single command
 **/
public class TerminalEmptyCommand implements TerminalCommand {
	public static final String TESTCOMMAND = "pwd";
	private boolean needParameter;
	private boolean isMultiCommand;
	private String id;
	private String menuText;
	private String command;
	private String parentMenuText;
	private String tooltipText = "";
	private int position;
	private String confidentialInformation;

	private String myTmpCommand;

	public TerminalEmptyCommand(String id, String c, String mt) {
		position = TerminalCommandFactory.DEFAULT_POSITION;
		setId(id);
		setCommand(c);
		getParameterList();
		setMenuText(mt);
	}

	public TerminalEmptyCommand(String c) {
		setCommand(c);
		getParameterList();
	}

	public TerminalEmptyCommand() {
	}

	@Override
	public String getSecureInfoInCommand() {
		return confidentialInformation;
	}

	@Override
	public String getSecuredCommand() {
		if (getSecureInfoInCommand() != null && !getSecureInfoInCommand().isBlank()) {
			return getCommand().replace(getSecureInfoInCommand(), TerminalCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	public final void setId(String i) {
		id = i;
	}

	@Override
	public final void setCommand(String c) {
		command = c;
	}

	public final void setMenuText(String mt) {
		menuText = mt;
	}

	public void setParentMenuText(String pmt) {
		parentMenuText = pmt;
	}

	public void setTooltipText(String ttt) {
		tooltipText = ttt;
	}

	public void setPriority(int p) {
		position = p;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getMenuText() {
		return menuText;
	}

	@Override
	public String getParentMenuText() {
		return parentMenuText;
	}

	@Override
	public String getToolTipText() {
		return tooltipText;
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public final List<String> getParameterList() {
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

	private String searchPlaceholder() {
		String temp1 = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_1;
		String temp2 = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_2;

		String splittedText = myTmpCommand.split(temp1, 2)[1].split(temp2, 2)[0];
		Logging.debug(this, "searchPlaceholder found " + temp1 + splittedText + temp2);
		myTmpCommand = myTmpCommand.replace(temp1 + splittedText + temp2, "");
		Logging.debug(this, "searchPlaceholder myCommand_tmp " + myTmpCommand);

		return temp1 + splittedText + temp2;
	}

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
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public int getPriority() {
		return position;
	}

	@Override
	@SuppressWarnings({ "java:S4144" })
	public String getCommand() {
		return command;
	}

	@Override
	public FGeneralDialog getDialog() {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder com = new StringBuilder("{");
		com.append(TerminalCommandFactory.COMMAND_MAP_ID).append(":").append(getId()).append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_PARENT_MENU_TEXT).append(":").append(getParentMenuText())
				.append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_MENU_TEXT).append(":").append(getMenuText()).append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_TOOLTIP_TEXT).append(":").append(getToolTipText()).append(",");
		com.append(TerminalCommandFactory.COMMAND_MAP_POSITION).append(":").append(getPriority()).append(", ");
		com.append("command:").append(getCommand()).append(";");
		com.append("}");
		return com.toString();
	}

	public String commandToString() {
		return getCommand();
	}
}
