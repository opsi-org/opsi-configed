/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import de.uib.configed.Configed;
import de.uib.opsicommand.sshcommand.CommandRepositoryUpload;
import de.uib.utilities.logging.Logging;

public class SSHRepositoryUploadDialog extends SSHFileUploadDialog {
	private static final int DEFAULT_HEIGHT = 400;

	public SSHRepositoryUploadDialog() {
		super(Configed.getResourceValue("SSHConnection.ParameterDialog.repoupload.title"),
				new CommandRepositoryUpload());
		super.setVisible(true);

		Logging.info(this.getClass(), "SSHRepositoryUploadDialog build");
		frameHeight = DEFAULT_HEIGHT;
		super.showDialog();
	}

	@Override
	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(jFileChooserLocal.getSelectedFile().getName());
		return modulesServerPath;
	}
}
