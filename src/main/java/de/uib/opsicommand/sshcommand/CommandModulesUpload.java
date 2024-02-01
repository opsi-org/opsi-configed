/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHFileUploadDialog;

public class CommandModulesUpload extends CommandSFTPUpload {
	private static final String ACTUAL_MODULES_DIRECTORY = "/etc/opsi/";
	private static final String DEFAULT_FILENAME = "modules";

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
		dialog = new SSHFileUploadDialog(Configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.title"),
				new CommandModulesUpload(), Globals.DIALOG_FRAME_DEFAULT_WIDTH, 430) {
			@Override
			protected String doAction1AdditionalSetPath() {
				String modulesServerPath = CommandModulesUpload.ACTUAL_MODULES_DIRECTORY;
				command.setTargetPath(CommandModulesUpload.ACTUAL_MODULES_DIRECTORY);
				command.setTargetFilename(CommandModulesUpload.DEFAULT_FILENAME);
				return modulesServerPath;
			}

			@Override
			protected CommandWget doAction1AdditionalSetWget(CommandWget c, String path) {
				c.setFileName(path + command.getTargetFilename());
				return c;
			}
		};
		dialog.setVisible(true);
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}
}
