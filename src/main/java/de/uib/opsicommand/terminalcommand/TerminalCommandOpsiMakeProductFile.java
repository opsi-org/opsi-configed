/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.terminal.MakeProductFileDialog;
import de.uib.utilities.logging.Logging;

public class TerminalCommandOpsiMakeProductFile implements TerminalCommand, TerminalCommandNeedParameter {
	private static final int PRIORITY = 110;

	private String baseName = "opsi-makepackage";
	private String commandName = "opsi-makepackage";

	private FGeneralDialog dialog;
	private boolean needParameter = true;
	private boolean isMultiCommand;

	private String dir = "";
	private String keepVersions = "";
	private String packageVersion = "";
	private String productVersion = "";
	private String md5sum = "-m";
	private String zsync = "-z";

	public TerminalCommandOpsiMakeProductFile(String d, String pav, String prv, boolean m, boolean z) {
		setDir(d);
		setPackageVersion(pav);
		setProductVersion(prv);
		setMd5sum(m);
		setZsync(z);
		Logging.info(this.getClass(), "CommandOpsimakeproductfile dir " + dir);
		Logging.info(this.getClass(), "CommandOpsimakeproductfile packageVersion " + packageVersion);
		Logging.info(this.getClass(), "CommandOpsimakeproductfile productVersion " + productVersion);
	}

	public TerminalCommandOpsiMakeProductFile(String d, String pav, String prv) {
		this(d, pav, prv, false, false);
	}

	public TerminalCommandOpsiMakeProductFile(String d, boolean o, boolean m, boolean z) {
		setDir(d);
		setKeepVersions(o);
		setMd5sum(m);
		setZsync(z);
	}

	public TerminalCommandOpsiMakeProductFile() {
	}

	@Override
	public String getSecureInfoInCommand() {
		return null;
	}

	@Override
	public String getSecuredCommand() {
		if (getSecureInfoInCommand() != null && !getSecureInfoInCommand().isBlank()) {
			return getCommand().replace(getSecureInfoInCommand(), TerminalCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	private void setDir(String d) {
		if (d != null) {
			dir = d;
		}
	}

	public String getDir() {
		return dir;
	}

	private void setKeepVersions(boolean o) {
		if (o) {
			keepVersions = "--keep-versions";
		} else {
			keepVersions = "";
		}
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getId() {
		return "CommandMakeproductfile";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.tooltip");
	}

	@Override
	public String getCommand() {
		if (!packageVersion.isEmpty() || !productVersion.isEmpty()) {
			keepVersions = "--keep-versions";
		}

		commandName = "cd " + dir + " && " + baseName + " " + keepVersions + " " + packageVersion + " " + productVersion
				+ " " + md5sum + " " + zsync + " ";
		return commandName;
	}

	@Override
	public void setCommand(String c) {
		commandName = c;
	}

	@Override
	public String getCommandRaw() {
		return commandName;
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
		dialog = new MakeProductFileDialog(configedMain);
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	private void setMd5sum(boolean m) {
		if (m) {
			md5sum = "-m";
		} else {
			md5sum = "";
		}
	}

	private void setZsync(boolean z) {
		if (z) {
			zsync = "-z";
		} else {
			zsync = "";
		}
	}

	private void setPackageVersion(String pav) {
		if (!pav.isEmpty()) {
			packageVersion = "--package-version " + pav;
		} else {
			packageVersion = "";
		}
	}

	private void setProductVersion(String prv) {
		if (!prv.isEmpty()) {
			productVersion = "--product-version " + prv;
		} else {
			productVersion = "";
		}
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
