package de.uib.opsicommand.sshcommand;

import java.util.*;
import de.uib.configed.configed;
import de.uib.configed.gui.ssh.SSHPackageManagerParameterDialog;
import de.uib.configed.gui.*;

public class CommandOpsiPackageManager implements SSHCommand
{
	// private String baseName = "opsi-package-manager";
	protected LinkedList<Object> helpLinesSplitted;
	protected FGeneralDialog dialog = null;
	protected boolean needSudo = false;
	protected boolean needRoot = false;
	protected boolean needParameter = true;
	private boolean isMultiCommand = false;
	protected int helpColumns = 3;
	protected int priority = 100;
	public CommandOpsiPackageManager()
	{
	}
	
	@Override 
	/** 
	* Sets the command specific error text
	**/
	public String get_ERROR_TEXT()
	{
		return "ERROR";
	}
	
	@Override
	public String getSecureInfoInCommand()
	{
		return null;
	}
	@Override 
	public String getSecuredCommand()
	{
		if ( (getSecureInfoInCommand() != null) && (!getSecureInfoInCommand().trim().equals("")))
			return 	getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.getInstance().confidential);
		else return getCommand();
	}
	@Override
	public String getId()
	{
		return "CommandOpsiPackageManager";
	}
	// @Override
	// public String getBasicName()
	// {
	// 	return "opsi-package-manager";
	// }
	@Override
	public String getMenuText()
	{
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager");
	}

	@Override
	public String getParentMenuText()
	{
		return null;
	}
	@Override
	public String getToolTipText()
	{
		return configed.getResourceValue("SSHConnection.command.opsipackagemanager.tooltip");
	}
	@Override
	public boolean isMultiCommand()
	{
		return isMultiCommand;
	}
	@Override
	public String getCommand()
	{
		return "";
	}
	@Override
	public String getCommandRaw()
	{
		return "";
	}
	@Override
	public boolean needSudo()
	{
		return needSudo;
	}
	/** 
	* Sets the given command
	* @param c (command): String
	**/
	public void setCommand(String c)
	{  }
	
	
	@Override
	public boolean needParameter()
	{
		return needParameter;
	}
	@Override
	public FGeneralDialog  getDialog()
	{
		return dialog;
	}
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	@Override
	public ArrayList<String> getParameterList()
	{
		return null;
	}
}