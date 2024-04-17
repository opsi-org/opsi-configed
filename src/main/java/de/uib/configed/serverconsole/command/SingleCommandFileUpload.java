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

public class SingleCommandFileUpload implements SingleCommand, CommandWithParameters {
	private static final int PRIORITY = 0;

	protected FGeneralDialog dialog;
	private boolean needParameter = true;

	private String baseName = "File Upload";
	private String targetPath = "";
	private String targetFileName = "";
	private String sourcePath = "";
	private String sourceFileName = "";

	protected String command = "";

	public SingleCommandFileUpload() {
		command = "File Upload (via webDAV)";
	}

	public String getFullTargetPath() {
		return getTargetPath() + getTargetFileName();
	}

	public String getTargetPath() {
		return targetPath;
	}

	public String getTargetFileName() {
		return targetFileName;
	}

	public String getFullSourcePath() {
		return getSourcePath() + getSourceFileName();
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setBaseName(String b) {
		baseName = b;
	}

	public void setTargetPath(String p) {
		targetPath = p;
	}

	public void setTargetFileName(String f) {
		targetFileName = f;
	}

	public void setSourcePath(String p) {
		sourcePath = p;
	}

	public void setSourceFileName(String f) {
		sourceFileName = f;
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
		return "cp " + getFullSourcePath() + " " + getFullTargetPath();
	}
}
