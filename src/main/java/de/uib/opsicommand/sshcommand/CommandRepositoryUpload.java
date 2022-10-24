package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import de.uib.utilities.logging.*;

public class CommandRepositoryUpload extends CommandSFTPUpload
// implements SSHCommandNeedParameter, SSHSFTPCommand, SSHCommand
{

	public CommandRepositoryUpload(String title)
	{
		super();
		setTitle(title);
		title = "Repo-file Upload";
		baseName = "Repo-file Upload";
		command = "Repo-file Upload (via sftp)";
		description = "# write Repo-file file to opsi-server";
		targetPath = "/etc/opsi/package-updater.repos.d/";
		targetFilename = "";
	}
	public CommandRepositoryUpload()
	{
		this("");
	}

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


	@Override
	public String getId()
	{
		return "CommandRepositoryUpload";
	}


	@Override
	public String getMenuText()
	{
		return configed.getResourceValue("SSHConnection.command.repoupload");
	}


	@Override
	public String getToolTipText()
	{
		return configed.getResourceValue("SSHConnection.command.repoupload.tooltip");
	}

	@Override
	public void startParameterGui()
	{
		//dialog = new SSHRepositoryUploadDialog();
	}
	@Override
	public void startParameterGui(ConfigedMain main)
	{
		// dialog = new SSHRepositoryUploadDialog(main);

		if (main.getOpsiVersion().length() == 0 || main.getOpsiVersion().charAt(0) == '<' || main.getOpsiVersion().compareTo("4.1") < 0){
			logging.error(this, configed.getResourceValue("OpsiConfdVersionError").replace("{0}", "4.1.0"));
		} else dialog = new SSHRepositoryUploadDialog();
	}

	@Override
	public FGeneralDialog  getDialog()
	{
		return dialog;
	}
}