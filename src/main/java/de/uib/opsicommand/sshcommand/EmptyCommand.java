package de.uib.opsicommand.sshcommand;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2015 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */
import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.FGeneralDialog;
import de.uib.utilities.logging.Logging;

/**
 * This class represent a simple single command
 **/
public class EmptyCommand implements SSHCommand {
	public static final String TESTCOMMAND = "pwd";
	/** boolean needParameter = false **/
	private boolean needParameter = false;
	/** boolean isMultiCommand = true **/
	private boolean isMultiCommand = false;
	/** String unique command id **/
	private String id;
	/** String unique menu text **/
	private String menuText;
	/** String command **/
	private String command;
	/** boolean needSudo state **/
	private boolean needSudo = false;
	/** String parent menu text **/
	private String parentMenuText = null;
	/** String tooltip text **/
	private String tooltipText = "";
	/** integer position **/
	private int position;
	private String confidentialInformation = null;

	private SSHCommandFactory factory = SSHCommandFactory.getInstance();

	@Override
	/**
	 * Sets the command specific error text
	 **/
	public String get_ERROR_TEXT() {
		return "ERROR";
	}

	@Override
	public String getSecureInfoInCommand() {
		return confidentialInformation;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.getInstance().CONFIDENTIAL);
		else
			return getCommand();
	}

	/**
	 * Creates an empty SSHCommand_Template instance
	 * 
	 * @return SSHCommand_Template instance
	 */
	public EmptyCommand(String id, String c, String mt, boolean ns) {
		position = factory.POSITION_DEFAULT;
		setId(id);
		setCommand(c);
		getParameterList();
		setMenuText(mt);
		setNeedSudo(ns);
	}

	public EmptyCommand(String c) {

		setCommand(c);
		getParameterList();

	}

	/**
	 * Empty constuctor for creatiing empty instances
	 */
	public EmptyCommand() {
	}

	/**
	 * Sets the Id
	 * 
	 * @param i (id): String
	 **/
	public void setId(String i) {
		id = i;
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
	 * Sets the menu text
	 * 
	 * @param mt (menu text): String
	 **/
	public void setMenuText(String mt) {
		menuText = mt;
	}

	/**
	 * Sets the parent menu text
	 * 
	 * @param pmt (parent menu text): String
	 **/
	public void setParentMenuText(String pmt) {
		parentMenuText = pmt;
	}

	/**
	 * Sets the tooltip text
	 * 
	 * @param ttt (tooltip text): String
	 **/
	public void setTooltipText(String ttt) {
		tooltipText = ttt;
	}

	/**
	 * Sets the need sudo state
	 * 
	 * @param ns (needSudo): boolean
	 **/
	public void setNeedSudo(boolean ns) {
		needSudo = ns;
	}

	/**
	 * Sets the position
	 * 
	 * @param p (position): int
	 **/
	public void setPriority(int p) {
		position = p;
	}

	/**
	 * Get the command id
	 * 
	 * @return id
	 **/
	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	/**
	 * Get the command menutext
	 * 
	 * @return menuText
	 **/
	@Override
	public String getMenuText() {
		return menuText;
	}

	/**
	 * Get the command parent menutext
	 * 
	 * @return parentMenuText
	 **/
	@Override
	public String getParentMenuText() {
		return parentMenuText;
	}

	/**
	 * Get the command tooltiptext
	 * 
	 * @return tooltipText
	 **/
	@Override
	public String getToolTipText() {
		return tooltipText;
	}

	/**
	 * Get the command without parameter (e.g. sudo text)
	 * 
	 * @return command
	 **/
	@Override
	public String getCommandRaw() {
		return command;
	}

	private String myTmpCommand;

	/**
	 * Searches placeholders like <<<sth>>>
	 * 
	 * @return List with placeholdern for parameter
	 */
	@Override
	public List<String> getParameterList() {
		List<String> paramlist = new ArrayList<>();
		String temp1 = SSHCommandParameterMethods.replacement_default_1;
		String temp2 = SSHCommandParameterMethods.replacement_default_2;
		if (command != null && command.contains(temp1) && command.contains(temp2)) {
			myTmpCommand = getCommandRaw();
			Logging.debug(this, "getParameterList myCommand_tmp " + myTmpCommand);
			for (int i = 0; i < counterString(getCommandRaw(), temp1); i++) {
				String plHolder = searchPlaceholder();
				if (!paramlist.contains(plHolder))
					paramlist.add(plHolder);
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
		String temp1 = SSHCommandParameterMethods.replacement_default_1;
		String temp2 = SSHCommandParameterMethods.replacement_default_2;

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

	/**
	 * @return True if the commands needs sudo
	 **/
	@Override
	public boolean needSudo() {
		return needSudo;
	}

	/**
	 * @return False if the commands does not need Parameter
	 **/
	@Override
	public boolean needParameter() {
		return needParameter;
	}

	/**
	 * @return the position
	 */
	@Override
	public int getPriority() {
		return position;
	}

	/**
	 * Get the whole command with e.g. sudo text
	 * 
	 * @return command
	 **/
	@Override
	public String getCommand() {
		String result = "";

		if (needSudo()) {
			if (command.contains("2>&1"))
				result = SSHCommandFactory.SUDO_TEXT + " " + command;
			else
				result = SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		} else {
			if (command.contains("2>&1"))
				result = command;
			else
				result = command + "  2>&1";
		}

		return result;

	}

	/**
	 * @return null (SSHCommand_Template does not have a parameter dialog)
	 **/
	@Override
	public FGeneralDialog getDialog() {
		return null;
	}

	/**
	 * @return a string representation of this command
	 */
	@Override
	public String toString() {
		StringBuilder com = new StringBuilder("{");
		com.append(factory.COMMAND_MAP_ID).append(":").append(getId()).append(",");
		com.append(factory.COMMAND_MAP_PARENT_MENU_TEXT).append(":").append(getParentMenuText()).append(",");
		com.append(factory.COMMAND_MAP_MENU_TEXT).append(":").append(getMenuText()).append(",");
		com.append(factory.COMMAND_MAP_TOOLTIP_TEXT).append(":").append(getToolTipText()).append(",");
		com.append(factory.COMMAND_MAP_NEED_SUDO).append(":").append(needSudo()).append(",");
		com.append(factory.COMMAND_MAP_POSITION).append(":").append(getPriority()).append(", ");
		com.append("command:").append(getCommand()).append(";");
		com.append("}");
		return com.toString();
	}

	public String commandToString() {
		return getCommand();
	}
}