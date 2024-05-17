/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.DeployClientAgentParameterDialog;

public class SingleCommandDeployClientAgent implements SingleCommand, CommandWithParameters {
	private String command;
	private String baseDir = "/var/lib/opsi/depot";
	private String opsiClientAgentDir;
	private String opsiDeployClientAgent = "opsi-deploy-client-agent";
	private FGeneralDialog dialog;
	private boolean pingIsRequired = true;
	private boolean needParameter = true;
	private int priority = 105;

	private String client = "";
	private String user = "";
	private String passw = "";
	private String finishAction = "";
	private String verbosity = "";

	public SingleCommandDeployClientAgent() {
		command = baseDir;
	}

	@Override
	public String getSecureInfoInCommand() {
		return passw;
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
	public String getId() {
		return "CommandDeployClientAgent";
	}

	@Override
	public String getBasicName() {
		return baseDir + "/" + opsiClientAgentDir + "/" + opsiDeployClientAgent;
	}

	public enum FinalActionType {
		START_OCD, REBOOT, SHUTDOWN
	}

	public void finish(FinalActionType actionType) {
		finishAction = switch (actionType) {
		case START_OCD -> " --start-opsiclientd ";
		case REBOOT -> " --reboot";
		case SHUTDOWN -> " --shutdown";
		default -> "";
		};
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SingleCommandDeployClientAgent.title");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SingleCommandDeployClientAgent.tooltip");
	}

	@Override
	public String getCommand() {
		command = getBasicName() + " " + verbosity + user + passw + finishAction + getPingOption() + client;
		return command;
	}

	@Override
	public void setCommand(String c) {
		command = c;
	}

	public void togglePingIsRequired() {
		pingIsRequired = !pingIsRequired;
	}

	public boolean isPingRequired() {
		return pingIsRequired;
	}

	private String getPingOption() {
		if (pingIsRequired) {
			return " ";
		} else {
			return " --ignore-failed-ping ";
		}
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
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public void startParameterGui(ConfigedMain configedMain) {
		dialog = new DeployClientAgentParameterDialog(configedMain);
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setClient(String c) {
		if (!c.isEmpty()) {
			client = " " + c;
		} else {
			client = "";
		}
	}

	public void setUser(String u) {
		if (!u.isEmpty()) {
			user = " -u " + u;
		} else {
			user = "";
		}
	}

	public void setLoglevel(int vSum) {
		StringBuilder v = new StringBuilder();
		for (int i = 0; i < vSum; i++) {
			v.append("v");
		}

		verbosity = " -" + v + " ";
		if (vSum == 0) {
			verbosity = "";
		}
	}

	public void setPassw(String pw) {
		if (!pw.isEmpty()) {
			passw = " -p " + "\"" + pw + "\"";
		} else {
			passw = "";
		}
	}

	public void setOpsiClientAgentDir(String dir) {
		opsiClientAgentDir = dir == null || dir.isEmpty() ? "" : dir;
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
