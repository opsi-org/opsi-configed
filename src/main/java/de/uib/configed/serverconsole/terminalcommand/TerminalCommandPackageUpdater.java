/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.terminalcommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.PackageUpdaterDialog;

public class TerminalCommandPackageUpdater implements TerminalSingleCommand, TerminalCommandNeedParameter {
	private static final int PRIORITY = 105;

	private String command;
	private String baseName = "opsi-package-updater";
	private FGeneralDialog dialog;
	private boolean needParameter = true;

	private String action = "list --repos";
	private String repo = "";
	private Set<String> actionSet = new HashSet<>();
	private Map<String, String> actionhash = new HashMap<>();
	private Map<String, String> repohash = new HashMap<>();
	private String verbosity = "-v";

	public TerminalCommandPackageUpdater() {
		command = baseName;
		actionSet.add("list");
		actionhash.put(Configed.getResourceValue("SSHConnection.command.opsipackageupdater.action.list"), "list");
		actionSet.add("install");
		actionhash.put(Configed.getResourceValue("SSHConnection.command.opsipackageupdater.action.install"), "install");
		actionSet.add("update");
		actionhash.put(Configed.getResourceValue("SSHConnection.command.opsipackageupdater.action.update"), "update");
	}

	@Override
	public String getSecureInfoInCommand() {
		return "";
	}

	@Override
	public String getSecuredCommand() {
		if (getSecureInfoInCommand() != null && !getSecureInfoInCommand().isBlank()) {
			return getCommand().replace(getSecureInfoInCommand(), TerminalCommandFactory.CONFIDENTIAL);
		} else {
			return getCommand();
		}
	}

	@Override
	public String getId() {
		return "CommandPackageUpdater";
	}

	@Override
	public String getBasicName() {
		return baseName;
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackageupdater");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.opsipackageupdater.tooltip");
	}

	@Override
	public String getCommand() {
		if ("list".equals(action)) {
			action = "list --repos";
			repo = "";
		}
		setCommand(baseName + " " + verbosity + " " + repo + " " + action);
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
		dialog = new PackageUpdaterDialog(configedMain);
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setVerbosity(int vSum) {
		StringBuilder v = new StringBuilder();
		for (int i = 0; i < vSum; i++) {
			v.append("v");
		}

		verbosity = "-" + v + "";
		if (vSum == 0) {
			verbosity = "";
		}
	}

	public void setRepos(Map<String, String> r) {
		repohash = r;
	}

	public void setRepo(String r) {
		if (r == null) {
			repo = "";
		} else {
			repo = "--repo " + r;
		}
	}

	public Map<String, String> getRepos() {
		return repohash;
	}

	public void setAction(String a) {
		if (actionSet.contains(a)) {
			action = a;
		} else {
			action = "";
		}
	}

	public String getAction(String text) {
		return actionhash.get(text);
	}

	public String[] getActionsText() {
		return actionhash.keySet().toArray(new String[0]);
	}

	public boolean checkCommand() {
		return !action.isEmpty();
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
