package de.uib.opsicommand.sshcommand;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHRepositoryUploadDialog;
import de.uib.utilities.logging.Logging;

public class CommandRepositoryUpload extends CommandSFTPUpload
// implements SSHCommandNeedParameter, SSHSFTPCommand, SSHCommand
{

	public CommandRepositoryUpload() {
		super.setTitle("Repo-file Upload");
		super.setBaseName("Repo-file Upload");
		command = "Repo-file Upload (via sftp)";
		super.setDescription("# write Repo-file file to opsi-server");
		super.setTargetPath("/etc/opsi/package-updater.repos.d/");
		super.setTargetFilename("");
	}

	@Override
	public String getId() {
		return "CommandRepositoryUpload";
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.repoupload");
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.repoupload.tooltip");
	}

	@Override
	public void startParameterGui(ConfigedMain main) {

		if (main.getOpsiVersion().length() == 0 || main.getOpsiVersion().charAt(0) == '<'
				|| main.getOpsiVersion().compareTo("4.1") < 0) {
			Logging.error(this, Configed.getResourceValue("OpsiConfdVersionError").replace("{0}", "4.1.0"));
		} else {
			dialog = new SSHRepositoryUploadDialog();
		}
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}
}
