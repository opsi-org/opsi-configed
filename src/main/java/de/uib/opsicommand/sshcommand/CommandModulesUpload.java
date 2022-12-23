package de.uib.opsicommand.sshcommand;

import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHModulesUploadDialog;

public class CommandModulesUpload extends CommandSFTPUpload
// implements SSHCommandNeedParameter, SSHSFTPCommand, SSHCommand
{
	public String actually_modules_directory = "/etc/opsi/";
	public String unofficial_modules_directory = "/etc/opsi/modules.d/";
	public static String DEFAULT_FILENAME = "modules";

	public CommandModulesUpload(String title) {
		setTitle(title);
		title = "Modules Upload";
		baseName = "Modules Upload";
		command = "Modules Upload (via sftp)";
		description = "# write modules file to opsi-server";
		targetPath = "/etc/opsi/";
		targetFilename = DEFAULT_FILENAME;
	}

	public CommandModulesUpload() {
		this("");
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		if (description.equals(""))
			description = "copy " + sourcePath + sourceFilename + " to " + targetPath + targetFilename
					+ " on connected server";
		return description;
	}

	@Override
	public String getId() {
		return "CommandModulesUpload";
	}

	@Override
	public String getMenuText() {
		return configed.getResourceValue("SSHConnection.command.modulesupload");
	}

	@Override
	public String getToolTipText() {
		return configed.getResourceValue("SSHConnection.command.modulesupload.tooltip");
	}

	@Override
	public void startParameterGui() {
		dialog = new SSHModulesUploadDialog();
	}

	@Override
	public void startParameterGui(ConfigedMain main) {
		// dialog = new SSHModulesUploadDialog(main);
		dialog = new SSHModulesUploadDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}
}