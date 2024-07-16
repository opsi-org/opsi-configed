/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.PackageManagerInstallParameterDialog;
import de.uib.utils.logging.Logging;

public class SingleCommandOpsiPackageManagerInstall extends SingleCommandOpsiPackageManager
		implements CommandWithParameters {
	private String command;
	private int priority = 8;
	private FGeneralDialog dialog;

	private String opsiproduct = "";
	private String depot = "";
	private String verbosity = "-vvv";
	private String freeInput = "";
	private String property = " -p keep ";

	private String updateInstalled = "";
	private String setupInstalled = "";

	public SingleCommandOpsiPackageManagerInstall() {
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
		return Configed.getResourceValue("SingleCommandOpsiPackageManagerInstall.title");
	}

	@Override
	public String getParentMenuText() {
		return super.getMenuText();
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SingleCommandOpsiPackageManagerInstall.tooltip");
	}

	@Override
	public String getCommand() {
		command = "opsi-package-manager  --force -q " + verbosity + updateInstalled + setupInstalled + property + depot
				+ freeInput + opsiproduct;
		Logging.info(this, "got command ", command);
		return command;
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
		dialog = new PackageManagerInstallParameterDialog(configedMain);
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
		return !opsiproduct.isEmpty();
	}

	public void keepDepotDefaults() {
		property = " -p keep ";
	}

	public void usePackageDefaults() {
		property = " -p package ";
	}

	public String getProperty() {
		return property;
	}

	public void enableUpdateInstalled() {
		updateInstalled = " --update ";
	}

	public void disableUpdateInstalled() {
		updateInstalled = "";
	}

	public void enableSetupInstalled() {
		setupInstalled = " --setup ";
	}

	public void disableSetupInstalled() {
		setupInstalled = "";
	}
}
