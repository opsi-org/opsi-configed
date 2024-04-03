/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;

public class SingleCommandFileUpload implements SingleCommand, SingleCommandNeedParameter {
	private static final int PRIORITY = 0;

	protected FGeneralDialog dialog;
	private boolean needParameter = true;

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

	public SingleCommandFileUpload(String title) {
		this.title = title;
		baseName = "File Upload";
		command = "File Upload (via webDAV)";
	}

	public SingleCommandFileUpload() {
	}

	public boolean isShowOutputDialog() {
		return showOutputDialog;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		if (description.isEmpty()) {
			description = "copy " + sourcePath + sourceFilename + " to " + targetPath + targetFilename
					+ " on connected server";
		}
		return description;
	}

	public String getFullTargetPath() {
		return getTargetPath() + getTargetFilename();
	}

	public String getTargetPath() {
		return targetPath;
	}

	public String getTargetFilename() {
		return targetFilename;
	}

	public String getFullSourcePath() {
		return fullSourcePath;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public String getSourceFilename() {
		return sourceFilename;
	}

	public boolean isOverwriteMode() {
		return overwriteMode;
	}

	public void setTitle(String t) {
		title = t;
	}

	public void setBaseName(String b) {
		baseName = b;
	}

	public void setDescription(String d) {
		description = d;
	}

	public void setTargetPath(String p) {
		targetPath = p;
	}

	public void setTargetFilename(String f) {
		targetFilename = f;
	}

	public void setSourcePath(String p) {
		sourcePath = p;
	}

	public void setSourceFilename(String f) {
		sourceFilename = f;
	}

	public void setFullSourcePath(String f) {
		fullSourcePath = f;
	}

	public void setOverwriteMode(boolean o) {
		overwriteMode = o;
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
			return getCommand().replace(getSecureInfoInCommand(), CommandFactory.CONFIDENTIAL);
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
		return "";
	}

	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public void setCommand(String c) {
		command = c;
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
		/* Not needed */
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
