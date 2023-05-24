/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import java.util.List;

import de.uib.configed.gui.FGeneralDialog;

/**
 * Represent a sshcommand object
 **/
public interface SSHCommand {

	/** @return command String to execute **/
	String getCommand();

	String getSecuredCommand();

	String getSecureInfoInCommand();

	/** @return raw command String **/
	String getCommandRaw();

	/** @returnlist of parameter-Ersatz **/
	List<String> getParameterList();

	/** @return True if command need sudo **/
	boolean needSudo();

	/** @return command id String **/
	String getId();

	/** @return command menu text String **/
	String getMenuText();

	/** @return command parent menu text String **/
	String getParentMenuText();

	/** @return command tooltip text String **/
	String getToolTipText();

	/** @return command priority int **/
	int getPriority();

	void setCommand(String c);

	/** @return True if command needs an parameter gui **/
	boolean needParameter();

	/** @return True if command is a multicommand **/
	boolean isMultiCommand();

	/** @return the command dialog(parameter) **/
	FGeneralDialog getDialog();

	String getErrorText();
}
