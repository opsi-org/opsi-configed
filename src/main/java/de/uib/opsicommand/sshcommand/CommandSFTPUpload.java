/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;

public class CommandSFTPUpload implements SSHCommandNeedParameter, SSHSFTPCommand, SSHCommand {
	private static final int PRIORITY = 0;

	protected FGeneralDialog dialog;
	private boolean needParameter = true;
	private boolean isMultiCommand;
	private boolean needSudo;

	private String title = "File Upload";
	private String baseName = "File Upload";
	private String description = "# write file to opsi-server";
	private String targetPath = "";
	private String targetFilename = "";
	private String sourcePath = "";
	private String fullSourcePath = "";
	private String sourceFilename = "";
	private boolean overwriteMode = true;

	private boolean showOutputDialog = true;

	protected String command = "";

	public CommandSFTPUpload(String title) {
		setTitle(title);
		title = "File Upload";
		baseName = "File Upload";
		command = "File Upload (via sftp)";
	}

	public CommandSFTPUpload() {
	}

	@Override
	public boolean isShowOutputDialog() {
		return showOutputDialog;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		if (description.isEmpty()) {
			description = "copy " + sourcePath + sourceFilename + " to " + targetPath + targetFilename
					+ " on connected server";
		}
		return description;
	}

	@Override
	public String getFullTargetPath() {
		return getTargetPath() + getTargetFilename();
	}

	@Override
	public String getTargetPath() {
		return targetPath;
	}

	@Override
	public String getTargetFilename() {
		return targetFilename;
	}

	@Override
	public String getFullSourcePath() {
		return fullSourcePath;
	}

	@Override
	public String getSourcePath() {
		return sourcePath;
	}

	@Override
	public String getSourceFilename() {
		return sourceFilename;
	}

	@Override
	public boolean isOverwriteMode() {
		return overwriteMode;
	}

	@Override
	public void setTitle(String t) {
		title = t;
	}

	public void setBaseName(String b) {
		baseName = b;
	}

	@Override
	public void setDescription(String d) {
		description = d;
	}

	@Override
	public void setTargetPath(String p) {
		targetPath = p;
	}

	@Override
	public void setTargetFilename(String f) {
		targetFilename = f;
	}

	@Override
	public void setSourcePath(String p) {
		sourcePath = p;
	}

	@Override
	public void setSourceFilename(String f) {
		sourceFilename = f;
	}

	@Override
	public void setFullSourcePath(String f) {
		fullSourcePath = f;
	}

	@Override
	public void setOverwriteMode(boolean o) {
		overwriteMode = o;
	}

	@Override
	/**
	 * Sets the command specific error text
	 **/
	public String getErrorText() {
		return "ERROR";
	}

	@Override
	public String getId() {
		return "CommandFilesUpload";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getMenuText() {
		return "File Upload";
	}

	@Override
	public String getSecureInfoInCommand() {
		// maybe null maybe sth
		return null;
	}

	@Override
	public String getSecuredCommand() {
		if (getSecureInfoInCommand() != null && !getSecureInfoInCommand().isBlank()) {
			return getCommand().replace(getSecureInfoInCommand(), SSHCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		// return

		return "";
	}

	@Override
	public String getCommand() {
		return "# this is no usually command";
	}

	@Override
	public void setCommand(String c) {
		command = c;
	}

	@Override
	public boolean needSudo() {
		return needSudo;
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public void startParameterGui(ConfigedMain configedMain) {
		/* Not needed */}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		return null;
	}

	@Override
	public FGeneralDialog getDialog() {
		return null;
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}

	@Override
	public String toString() {
		return "cp " + getSourcePath() + getSourceFilename() + " " + getTargetPath() + getTargetFilename();
	}
}
