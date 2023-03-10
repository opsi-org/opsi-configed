package de.uib.configed.gui.ssh;

import de.uib.configed.Configed;
import de.uib.opsicommand.sshcommand.CommandRepositoryUpload;
import de.uib.utilities.logging.Logging;

public class SSHRepositoryUploadDialog extends SSHFileUploadDialog {
	private static String title = Configed.getResourceValue("SSHConnection.ParameterDialog.repoupload.title");

	public SSHRepositoryUploadDialog() {
		super(title, new CommandRepositoryUpload());
		this.setVisible(true);
		Logging.info(this, "SSHRepositoryUploadDialog build");
		height = 400;
		showDialog();
	}

	@Override
	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(jFileChooserLocal.getSelectedFile().getName());
		return modulesServerPath;
	}
}