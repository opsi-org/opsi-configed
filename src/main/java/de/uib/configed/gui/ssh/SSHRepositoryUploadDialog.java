package de.uib.configed.gui.ssh;

import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.CommandRepositoryUpload;
import de.uib.utilities.logging.logging;

public class SSHRepositoryUploadDialog extends SSHFileUploadDialog {
	private static String title = configed.getResourceValue("SSHConnection.ParameterDialog.repoupload.title");

	public SSHRepositoryUploadDialog() {
		this(null);
	}

	public SSHRepositoryUploadDialog(CommandRepositoryUpload com) {
		super(title, new CommandRepositoryUpload());
		this.setVisible(true);
		logging.info(this, "SSHRepositoryUploadDialog build");
		height = 400;
		showDialog();
	}

	@Override
	protected String doAction1_additional_setPath() {
		String modules_server_path = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(filechooser_local.getSelectedFile().getName());
		return modules_server_path;
	}
}