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
import de.uib.configed.serverconsole.CurlParameterDialog;
import de.uib.utils.logging.Logging;

public class SingleCommandCurl implements SingleCommand, CommandWithParameters {
	private static final String BASE_NAME = "curl";
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

	public SingleCommandCurl() {
	}

	public SingleCommandCurl(String d, String u, String au, String auth) {
		this(d, u, au);
		setAuthentication(auth);
	}

	public SingleCommandCurl(String d, String u, String au) {
		this(d, u);
		additionalURL = "-O " + au;
	}

	public SingleCommandCurl(String d, String u) {
		setLoglevel(4);
		setDir(d);
		setUrl(u);

		if (d.charAt(d.length() - 1) != '/') {
			d = d + "/";
		}
		setProduct(d + getFilenameFromUrl(url));
		Logging.debug(this.getClass(), "SingleCommandCurl dir " + dir);
		Logging.debug(this.getClass(), "SingleCommandCurl url " + url);
		Logging.debug(this.getClass(), "SingleCommandCurl product " + getProduct());
		needParameter = false;
	}

	public void setFileName(String newFilename) {
		if (newFilename != null && !newFilename.isBlank()) {
			fileName = "--output-document=" + newFilename;
		}
	}

	public final void setAuthentication(String a) {
		if (a != null) {
			authentication = a;
		}
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
		return Configed.getResourceValue("SingleCommandCurl.title");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SingleCommandCurl.tooltip");
	}

	@Override
	public String getCommand() {
		if (!freeInput.isEmpty()) {
			command = BASE_NAME + " " + authentication + " " + fileName + " " + freeInput + " " + verbosity + " " + dir
					+ " " + url + " " + additionalURL;
		} else {
			command = BASE_NAME + " " + authentication + " " + fileName + " " + verbosity + " " + dir + " " + url + " "
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
			return getCommand().replace(getSecureInfoInCommand(), CommandFactory.CONFIDENTIAL);
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

	public final void setDir(String d) {
		if (!d.isEmpty()) {
			dir = "--output-dir " + d;
		} else {
			dir = "";
		}
	}

	public final void setUrl(String u) {
		if (!u.isEmpty()) {
			url = "-O " + u;
		} else {
			url = "";
		}
	}

	public final void setLoglevel(int vSum) {
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

	public final void setFreeInput(String fI) {
		freeInput = fI;
	}

	public final void setProduct(String pr) {
		product = pr;
	}

	public final String getProduct() {
		return product;
	}

	@Override
	public void setCommand(String c) {
		command = c;
	}

	public final boolean checkCommand() {
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
