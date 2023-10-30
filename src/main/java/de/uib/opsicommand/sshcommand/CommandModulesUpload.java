/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHModulesUploadDialog;

public class CommandModulesUpload extends CommandSFTPUpload {
	public static final String ACTUAL_MODULES_DIRECTORY = "/etc/opsi/";
	public static final String UNOFFICIAL_MODULES_DIRECTORY = "/etc/opsi/modules.d/";
	public static final String DEFAULT_FILENAME = "modules";

	public CommandModulesUpload() {
		super.setTitle("Modules Upload");
		super.setBaseName("Modules Upload");
		command = "Modules Upload (via sftp)";
		super.setDescription("# write modules file to opsi-server");
		super.setTargetPath("/etc/opsi/");
		super.setTargetFilename(DEFAULT_FILENAME);
	}

	@Override
	public String getId() {
		return "CommandModulesUpload";
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.modulesupload");
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.modulesupload.tooltip");
	}

	@Override
	public void startParameterGui(ConfigedMain configedMain) {

		dialog = new SSHModulesUploadDialog();
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}
}
