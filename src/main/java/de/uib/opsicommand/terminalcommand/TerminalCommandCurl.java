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
import de.uib.configed.terminal.CurlParameterDialog;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.utilities.logging.Logging;

public class TerminalCommandCurl implements TerminalCommand, TerminalCommandNeedParameter {
	private static final String BASE_NAME = "curl";
	private static final boolean IS_MULTI_COMMAND = false;
	private static final int PRIORITY = 110;

	private String command = "curl ";
	private boolean needParameter = true;

	private String url = "";
	private String authentication = "";
	private String additionalURL = "";
	private String dir = "";
	private String product = "";
	private String fileName = "";
	private String verbosity = "";
	private String freeInput = "";

	private FGeneralDialog dialog;

	public TerminalCommandCurl() {
	}

	public TerminalCommandCurl(String d, String u, String au, String auth) {
		this(d, u, au);
		setAuthentication(auth);
	}

	public TerminalCommandCurl(String d, String u, String au) {
		this(d, u);
		additionalURL = au;
	}

	public TerminalCommandCurl(String d, String u) {
		setVerbosity(1);
		setDir(d);
		setUrl(u);

		if (d.charAt(d.length() - 1) != '/') {
			d = d + "/";
		}
		setProduct(d + getFilenameFromUrl(url));
		Logging.debug(this.getClass(), "CommandCurl dir " + dir);
		Logging.debug(this.getClass(), "CommandCurl url " + url);
		Logging.debug(this.getClass(), "CommandCurl product " + getProduct());
		needParameter = false;
	}

	public void setFileName(String newFilename) {
		if (newFilename != null && !newFilename.isBlank()) {
			fileName = "--output-document=" + newFilename;
		}
	}

	public void setAuthentication(String a) {
		if (a != null) {
			authentication = a;
		}
	}

	@Override
	public boolean isMultiCommand() {
		return IS_MULTI_COMMAND;
	}

	@Override
	public String getId() {
		return "CommandWget";
	}

	@Override
	public String getBasicName() {
		return BASE_NAME;
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SSHConnection.command.wget");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SSHConnection.command.wget.tooltip");
	}

	@Override
	public String getCommand() {
		if (!freeInput.isEmpty()) {
			command = BASE_NAME + " " + authentication + " " + fileName + " " + freeInput + " " + verbosity + " " + dir
					+ " -O " + url + " -O " + additionalURL;
		} else {
			command = BASE_NAME + " " + authentication + " " + fileName + " " + verbosity + dir + " -O " + url + " -O "
					+ additionalURL;
		}
		return command;
	}

	@Override
	public String getSecureInfoInCommand() {
		return authentication;
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
		dialog = new CurlParameterDialog(configedMain);
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	public void setDir(String d) {
		if (!d.isEmpty()) {
			dir = "--output-dir " + d;
		} else {
			dir = "";
		}
	}

	public void setUrl(String u) {
		if (!u.isEmpty()) {
			url = u;
		} else {
			url = "";
		}
	}

	public void setVerbosity(int vSum) {
		StringBuilder v = new StringBuilder();
		for (int i = 0; i < vSum; i++) {
			v.append("v");
		}

		verbosity = "-" + v;
		if (vSum == 0) {
			verbosity = "";
		}
	}

	public void setFreeInput(String fI) {
		freeInput = fI;
	}

	public void setProduct(String pr) {
		product = pr;
	}

	public String getProduct() {
		return product;
	}

	@Override
	public void setCommand(String c) {
		command = c;
	}

	public boolean checkCommand() {
		return !dir.isEmpty() && !url.isEmpty();
	}

	private static String getFilenameFromUrl(String url) {
		int p = url.lastIndexOf("/");
		return url.substring(p + 1);
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
