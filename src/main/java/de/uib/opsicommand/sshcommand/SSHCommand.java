package de.uib.opsicommand.sshcommand;

import java.util.List;

import de.uib.configed.gui.FGeneralDialog;

/**
 * Represent a sshcommand object
 **/
public abstract interface SSHCommand {
	
	/** @return command String to execute **/
	public abstract String getCommand();

	public abstract String getSecuredCommand();

	
	public abstract String getSecureInfoInCommand();

	/** @return raw command String **/
	public abstract String getCommandRaw();

	/** @returnlist of parameter-Ersatz **/
	public abstract List<String> getParameterList();

	/** @return True if command need sudo **/
	public abstract boolean needSudo();

	/** @return command id String **/
	public abstract String getId();

	
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

	
	/** @return the command dialog(parameter) **/
	public abstract FGeneralDialog getDialog();

	public abstract String get_ERROR_TEXT();
}