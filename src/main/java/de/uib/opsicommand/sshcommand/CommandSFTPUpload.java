package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;

public class CommandSFTPUpload
implements SSHCommandNeedParameter, SSHSFTPCommand, SSHCommand
{
	protected FGeneralDialog dialog;
	private boolean needParameter = true;
	private boolean isMultiCommand = false;
	private boolean needSudo = false;
	String command = "";
	int priority = 0;

	public String title = "File Upload";
	public String baseName = "File Upload";
	public String description = "# write file to opsi-server";
	public String targetPath = "";
	public String targetFilename = "";
	public String sourcePath = "";
	public String fullSourcePath = "";
	public String sourceFilename = "";
	public boolean overwriteMode = true;
	boolean showOutputDialog = true;


	public CommandSFTPUpload(String title)
	{
		setTitle(title);
		title = "File Upload";
		baseName = "File Upload";
		command = "File Upload (via sftp)";
	}
	public CommandSFTPUpload()
	{
	}

	public boolean getShowOutputDialog()
	{return showOutputDialog;}

	public String getTitle()
	{return title;}
	public String getDescription()
	{
		if (description.equals(""))
			description = "copy " + sourcePath  + sourceFilename
					+ " to " + targetPath  + targetFilename
					+ " on connected server";
		return description;
	}
	public String getFullTargetPath()
	{return getTargetPath() + getTargetFilename();}

	public String getTargetPath()
	{return targetPath;}
	public String getTargetFilename()
	{return targetFilename;}
	public String getFullSourcePath()
	{return fullSourcePath;}
	public String getSourcePath()
	{return sourcePath;}
	public String getSourceFilename()
	{return sourceFilename;}
	public boolean getOverwriteMode()
	{return overwriteMode;}

	public void setTitle(String t)
	{title = t;}
	public void setDescription(String d)
	{description = d;}
	public void setTargetPath(String p)
	{targetPath = p;}
	public void setTargetFilename(String f)
	{targetFilename = f;}
	public void setSourcePath(String p)
	{sourcePath = p;}
	public void setSourceFilename(String f)
	{sourceFilename = f;}
	public void setFullSourcePath(String f)
	{fullSourcePath = f;}
	public void setOverwriteMode(boolean o)
	{overwriteMode=o;}


	@Override
	/**
	* Sets the command specific error text
	**/
	public String get_ERROR_TEXT()
	{
		return "ERROR";
	}

	@Override
	public String getId()
	{
		return "CommandFilesUpload";
	}

	@Override
	public String getBasicName()
	{
		return baseName;
	}

	@Override
	public boolean isMultiCommand()
	{
		return isMultiCommand;
	}
	@Override
	public String getMenuText()
	{
		// return configed.getResourceValue("SSHConnection.command.modulesupload");
		return "File Upload";
	}
	@Override
	public String getSecureInfoInCommand()
	{
		// maybe null maybe sth
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
	public String getParentMenuText()
	{
		return null;
	}
	@Override
	public String getToolTipText()
	{
		// return configed.getResourceValue("SSHConnection.command.modulesupload.tooltip");
		return "";
	}
	@Override
	public String getCommand()
	{
		return "# this is no usually command";
	}
	@Override
	public void setCommand(String c)
	{ command = c; }


	@Override
	public boolean needSudo()
	{
		return needSudo;
	}
	@Override
	public String getCommandRaw()
	{
		return command;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}
	@Override
	public boolean needParameter()
	{
		return needParameter;
	}
	@Override
	public void startParameterGui()
	{
		// dialog = new SSHModulesUploadDialog();
	}
	@Override
	public void startParameterGui(ConfigedMain main)
	{
		// dialog = new SSHModulesUploadDialog(main);
		// dialog = new SSHModulesUploadDialog();
	}
	@Override
	public SSHConnectionExecDialog startHelpDialog()
	{
		return null;
	}

	@Override
	public FGeneralDialog  getDialog()
	{
		return null;
	}

	@Override
	public ArrayList<String> getParameterList()
	{
		return null;
	}

	public String toString(){
		return "cp " + getSourcePath() + getSourceFilename() + " " + getTargetPath() + getTargetFilename();
	}
}