/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.FileUploadDialog;

public class SingleCommandModulesUpload extends SingleCommandFileUpload {
	private static final String ACTUAL_MODULES_DIRECTORY = "/etc/opsi/";
	private static final String DEFAULT_FILENAME = "modules";

	public SingleCommandModulesUpload() {
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
		dialog = new FileUploadDialog(Configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.title"),
				new SingleCommandModulesUpload(), Globals.DIALOG_FRAME_DEFAULT_WIDTH, 430, configedMain) {
			@Override
			protected String doAction1AdditionalSetPath() {
				String modulesServerPath = ACTUAL_MODULES_DIRECTORY;
				command.setTargetPath(ACTUAL_MODULES_DIRECTORY);
				command.setTargetFilename(DEFAULT_FILENAME);
				return modulesServerPath;
			}

			@Override
			protected SingleCommandCurl doAction1AdditionalSetWget(SingleCommandCurl c, String path) {
				c.setDir(path);
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
