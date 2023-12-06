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
import de.uib.configed.gui.ssh.SSHPackageManagerUninstallParameterDialog;
import de.uib.utilities.logging.Logging;

public class CommandOpsiPackageManagerUninstall extends CommandOpsiPackageManager implements SSHCommandNeedParameter {
	private FGeneralDialog dialog;
	private String command;
	private int priority = 10;

	private String opsiproduct;
	private String depots;
	private String verbosity = " -vvv ";
	private String keepFiles = " ";
	private String freeInput = " ";

	public CommandOpsiPackageManagerUninstall() {
		command = "opsi-package-manager";
	}

	@Override
	public String getId() {
		return "CommandOpsiPackageManagerUninstall";
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackagemanager_uninstall");
	}

	@Override
	public String getParentMenuText() {
		return super.getMenuText();
	}

	@Override
	public String getBasicName() {
		return "opsi-package-manager";
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackagemanager_uninstall.tooltip");
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	@Override
	public void startParameterGui(ConfigedMain configedMain) {
		dialog = new SSHPackageManagerUninstallParameterDialog(configedMain);
	}

	@Override
	public SSHConnectionExecDialog startHelpDialog() {
		// Never needed

		return null;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getCommand() {
		command = "opsi-package-manager -q " + verbosity + keepFiles + depots + freeInput + opsiproduct;
		if (needSudo()) {
			return SSHCommandFactory.SUDO_TEXT + " " + command + " 2>&1";
		}
		return command + " 2>&1";
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	public void setKeepFiles(boolean kF) {
		if (kF) {
			keepFiles = "  --keep-files ";
		} else {
			keepFiles = "";
		}
	}

	public void setOpsiproduct(String prod) {
		if (prod != null && !prod.isEmpty()) {
			opsiproduct = " -r " + prod;
		} else {
			opsiproduct = " ";
		}
	}

	public void setDepot(String depotlist) {
		if (depotlist != null && !depotlist.isEmpty()) {
			depots = " -d " + depotlist;
		} else {
			depots = " ";
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
		if (opsiproduct == null || opsiproduct.isBlank()) {
			Logging.info(this, "no product given");
			return false;
		}
		return true;
	}
}
