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
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog ;

/** 
* Represent a sshcommand object
**/
public abstract interface SSHCommand
{
	// private SSHCommand instance;
	/** @return command String to execute **/
	public abstract String getCommand();
	public abstract String getSecuredCommand();
	// public abstract boolean hasSecureInfoInCommand();
	public abstract String getSecureInfoInCommand();

	/** @return raw command String **/
	public abstract String getCommandRaw();
	/** @returnlist of parameter-Ersatz **/
	public abstract ArrayList<String> getParameterList();
	/** @return True if command need sudo **/
	public abstract boolean needSudo();
	/** @return command id String **/
	public abstract String getId();
	// public abstract String getBasicName();
	/** @return command menu text String **/
	public abstract String getMenuText();
	/** @return command parent menu text String **/
	public abstract String getParentMenuText();
	/** @return command tooltip text String **/
	public abstract String getToolTipText();
	/** @return command priority int **/
	public abstract int getPriority();
	public void setCommand(String c);


	/** @return True if command needs an parameter gui **/
	public abstract boolean needParameter();
	/** @return True if command is a multicommand **/
	public abstract boolean isMultiCommand();
	// public abstract int getHelpColumns();
	/** @return the command dialog(parameter)**/
	public abstract FGeneralDialog getDialog();
	
	public abstract String get_ERROR_TEXT(); 
}