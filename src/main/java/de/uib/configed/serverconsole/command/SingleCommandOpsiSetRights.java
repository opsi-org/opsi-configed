/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.OpsiSetRightsParameterDialog;
import de.uib.opsicommand.sshcommand.SSHCommandParameterMethods;
import de.uib.utils.logging.Logging;

public class SingleCommandOpsiSetRights implements SingleCommand, CommandWithParameters {
	private static final int PRIORITY = 110;

	private static final String BASE_NAME = "opsi-set-rights ";

	private String command = BASE_NAME;

	private FGeneralDialog dialog;
	private boolean needParameter = true;
	private String dir;
	private String myTmpCommand;

	public SingleCommandOpsiSetRights() {
		command = "opsi-set-rights " + Configed.getResourceValue("SSHConnection.command.opsisetrights.additionalPath")
				+ " ";
	}

	public SingleCommandOpsiSetRights(String d) {
		setDir(d);
		command = BASE_NAME + dir;
		if (d.length() > 0 && d.charAt(d.length() - 1) != '/') {
			d = d + "/";
		}

		Logging.info(this.getClass(), "CommandOpsiSetRights dir " + dir);
	}

	@Override
	public String getId() {
		return "CommandOpsiSetRights";
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
		if (getSecureInfoInCommand() != null && !getSecureInfoInCommand().isBlank()) {
			return getCommand().replace(getSecureInfoInCommand(), CommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	@Override
	public String getCommand() {
		if (dir != null) {
			command = "opsi-set-rights" + dir;
		}

		return command;
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public void setCommand(String c) {
		command = c;
	}

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
	public void startParameterGui(ConfigedMain configedMain) {
		dialog = new OpsiSetRightsParameterDialog(configedMain, this);
		dialog.setVisible(true);
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

	@Override
	public String getBasicName() {
		return BASE_NAME;
	}
}
