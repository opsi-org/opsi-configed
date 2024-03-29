/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;
import de.uib.configed.gui.ssh.SSHPackageManagerInstallParameterDialog;
import de.uib.utilities.logging.Logging;

public class CommandOpsiPackageManagerInstall extends CommandOpsiPackageManager implements SSHCommandNeedParameter {
	private String command;
	private int priority = 8;
	private FGeneralDialog dialog;
	private boolean isMultiCommand;

	private String opsiproduct = "";
	private String depot = "";
	private String verbosity = "-vvv";
	private String freeInput = "";
	private String property = " -p keep ";

	private String updateInstalled = "";
	private String setupInstalled = "";

	public CommandOpsiPackageManagerInstall() {
		command = "opsi-package-manager";
	}

	@Override
	public String getId() {
		return "CommandOpsiPackageManagerInstall";
	}

	@Override
	public String getBasicName() {
		return "opsi-package-manager";
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackagemanager_install");
	}

	@Override
	public String getParentMenuText() {
		return super.getMenuText();
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackagemanager_install.tooltip");
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getCommand() {
		command = "opsi-package-manager  --force -q " + verbosity + updateInstalled + setupInstalled + property + depot
				+ freeInput + opsiproduct;
		Logging.info(this, "got command " + command);
		if (needSudo()) {
			return SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		}
		return command + " 2>&1";
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void startParameterGui(ConfigedMain configedMain) {
		dialog = new SSHPackageManagerInstallParameterDialog(configedMain);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		// Never needed

		return null;
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setOpsiproduct(String prod) {
		if (prod != null && !prod.isEmpty()) {
			opsiproduct = " -i " + prod;
		} else {
			opsiproduct = "";
		}
	}

	public void setDepotForPInstall(String dep) {
		if (!dep.isEmpty()) {
			depot = " -d " + dep;
		} else {
			depot = "";
		}
	}

	public void setVerbosity(int vSum) {
		StringBuilder v = new StringBuilder("v");
		for (int i = 0; i < vSum; i++) {
			v.append("v");
		}
		verbosity = " -" + v + " ";
	}

	public void setFreeInput(String fI) {
		freeInput = " " + fI;
	}

	public boolean checkCommand() {
		return !opsiproduct.isEmpty();
	}

	public void setProperty(boolean keepDepotDefaults) {
		if (keepDepotDefaults) {
			property = " -p keep ";
		} else {
			property = " -p package ";
		}
	}

	public String getProperty() {
		return property;
	}

	public void setUpdateInstalled(boolean u) {
		if (u) {
			updateInstalled = " --update ";
		} else {
			updateInstalled = "";
		}
	}

	public void setSetupInstalled(boolean s) {
		if (s) {
			setupInstalled = " --setup ";
		} else {
			setupInstalled = "";
		}
	}
}
