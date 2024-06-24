/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.PackageManagerUninstallParameterDialog;
import de.uib.utils.logging.Logging;

public class SingleCommandOpsiPackageManagerUninstall extends SingleCommandOpsiPackageManager
		implements CommandWithParameters {
	private FGeneralDialog dialog;
	private String command;
	private int priority = 10;

	private String opsiproduct;
	private String depots;
	private String verbosity = " -vvv";
	private String keepFiles = "";
	private String freeInput = "";

	public SingleCommandOpsiPackageManagerUninstall() {
		command = "opsi-package-manager";
	}

	@Override
	public String getId() {
		return "CommandOpsiPackageManagerUninstall";
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SingleCommandOpsiPackageManagerUninstall.title");
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
		return Configed.getResourceValue("SingleCommandOpsiPackageManagerUninstall.tooltip");
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	@Override
	public void startParameterGui(ConfigedMain configedMain) {
		dialog = new PackageManagerUninstallParameterDialog(configedMain);
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getCommand() {
		command = "opsi-package-manager -q" + verbosity + keepFiles + depots + freeInput + opsiproduct;
		return command;
	}

	@Override
	public String getCommandRaw() {
		return command;
	}

	public void enableKeepingFiles() {
		keepFiles = " --keep-files";
	}

	public void disableKeepingFiels() {
		keepFiles = "";
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
			depots = " -d " + depotlist.replace(" ", "");
		} else {
			depots = "";
		}
	}

	public void setLoglevel(int vSum) {
		if (vSum <= 3) {
			verbosity = "";
		} else {
			StringBuilder v = new StringBuilder("-");
			for (int i = 3; i < vSum; i++) {
				v.append("v");
			}
			verbosity = v.toString();
		}
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
