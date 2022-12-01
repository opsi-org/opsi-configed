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

import de.uib.configed.gui.FGeneralDialog;
import de.uib.utilities.logging.logging;

/**
 * This class represent a simple single command
 **/
public class Empty_Command implements SSHCommand {
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
	private String confidential_information = null;

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
		return confidential_information;
	}

	@Override
	public String getSecuredCommand() {
		if ((getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.getInstance().confidential);
		else
			return getCommand();
	}

	/**
	 * Creates an empty SSHCommand_Template instance
	 * 
	 * @return SSHCommand_Template instance
	 */
	public Empty_Command(String id, String c, String mt, boolean ns) {
		position = factory.position_default;
		setId(id);
		setCommand(c);
		getParameterList();
		setMenuText(mt);
		setNeedSudo(ns);
	}

	public Empty_Command(String c) {
		// position = factory.position_default;
		// setId(id);
		setCommand(c);
		getParameterList();
		// setMenuText(mt);
		// setNeedSudo(ns);
	}

	/**
	 * Empty constuctor for creatiing empty instances
	 */
	public Empty_Command() {
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
				result = SSHCommandFactory.getInstance().sudo_text + " " + command;
			else
				result = SSHCommandFactory.getInstance().sudo_text + " " + command + " 2>&1";
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
		StringBuffer com = new StringBuffer("{");
		com.append(factory.command_map_id).append(":").append(getId()).append(",");
		com.append(factory.command_map_parentMenuText).append(":").append(getParentMenuText()).append(",");
		com.append(factory.command_map_menuText).append(":").append(getMenuText()).append(",");
		com.append(factory.command_map_tooltipText).append(":").append(getToolTipText()).append(",");
		com.append(factory.command_map_needSudo).append(":").append(needSudo()).append(",");
		com.append(factory.command_map_position).append(":").append(getPriority()).append(", ");
		com.append("command:").append(getCommand()).append(";");
		com.append("}");
		return com.toString();
	}

	public String commandToString() {
		return getCommand();
	}
}