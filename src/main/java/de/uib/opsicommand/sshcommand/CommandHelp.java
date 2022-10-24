package de.uib.opsicommand.sshcommand;

import java.util.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;

public class CommandHelp /*extends*/implements SSHCommand //, SSHCommandNeedParameter

{
	private String command;
	// private String baseName = "";
	// private LinkedList<CommandHelp_Row> helpLinesSplitted = null;
	private boolean needSudo = false;
	// private boolean needRoot = false;
	private boolean needParameter = false;
	private SSHCommand basicCommand;
	protected FGeneralDialog dialog = null;
	public CommandHelp( SSHCommand basicCommand)
	{
		this.basicCommand = basicCommand;
		// command = this.basicCommand.getBasicName() + " --help";
		this.dialog = this.basicCommand.getDialog();
	}
	
	private boolean isMultiCommand = false;
	
	@Override
	public boolean isMultiCommand()
	{
		return isMultiCommand;
	}
	@Override
	public String getId()
	{
		return basicCommand.getId();
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
	/** 
	* Sets the command specific error text
	**/
	public String get_ERROR_TEXT()
	{
		return "ERROR";
	}

	// @Override
	// public String getBasicName()
	// {
	// 	return basicCommand.getBasicName();
	// }
	
	@Override
	public String getMenuText()
	{
		return null;
	}
	@Override
	public String getParentMenuText()
	{
		return null;
	}
	@Override
	public String getToolTipText()
	{
		return null;
	}
	@Override
	public String getCommand()
	{
		return ((SSHCommandNeedParameter) this.basicCommand).getBasicName() + " --help";
	}
	
	/** 
	* Sets the given command
	* @param c (command): String
	**/
	public void setCommand(String c)
	{ command = c; }

	@Override
	public String getCommandRaw()
	{
		return basicCommand.getCommandRaw();
	}
	@Override
	public boolean needSudo()
	{
		return needSudo;
	}

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
		return 0;
	}


	@Override
	public ArrayList<String> getParameterList()
	{
		return null;
	}
// public void startParameterGui();
// 	public void startParameterGui(ConfigedMain main);
// 	public SSHConnectionExecDialog startHelpDialog();
	// public String getBasicName()

	// public LinkedList<CommandHelp_Row> getHelpLines()
	// {
	// 	return helpLinesSplitted;
	// }
	// public void setHelpLines(LinkedList<CommandHelp_Row> lines)
	// {
	// 	helpLinesSplitted = lines;
	// }
	// @Override
	// public int getHelpColumns()
	// {
	// 	// if (packageManager) 
	// 	// 	return ((CommandOpsiPackageManager)basicCommand).getHelpColumns;
	// 	// return 0;
	// 	return basicCommand.getHelpColumns();
	// }
	// public String[] getRegexes()
	// {
	// 	final String[] regexes = new String[2];
	// 	regexes[0] = "(-[\\w*],[ ])|(--[\\w*]*-[\\w*]*-[\\w*]*)|(--[\\w*]*-[\\w*]*)|(--[\\w*]*)";
	// 	regexes[1] = "((<[\\w*]*>)|(<[\\w*]*-[\\w*]*>)|(<[\\w*]*-[\\w*]*-[\\w*]*>)) | ((<[\\w*]*>\\s\\.{3})|(<[\\w*]*-[\\w*]*>\\s\\.{3})|(<[\\w*]*-[\\w*]*-[\\w*]*>\\s\\.{3}))";
	// 	return regexes;
	// }
	
}