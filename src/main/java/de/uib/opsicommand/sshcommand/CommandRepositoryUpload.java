/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHRepositoryUploadDialog;

public class CommandRepositoryUpload extends CommandSFTPUpload {

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
	public void startParameterGui(ConfigedMain configedMain) {

		dialog = new SSHRepositoryUploadDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}
}
