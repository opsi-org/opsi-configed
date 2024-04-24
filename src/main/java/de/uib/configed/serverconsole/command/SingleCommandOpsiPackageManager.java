/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.gui.FGeneralDialog;

public class SingleCommandOpsiPackageManager implements SingleCommand {
	private FGeneralDialog dialog;
	private boolean needParameter = true;
	private int priority = 100;

	@Override
	public String getSecureInfoInCommand() {
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
	public String getId() {
		return "CommandOpsiPackageManager";
	}

	@Override
	public String getMenuText() {
		return Configed.getResourceValue("SingleCommandOpsiPackageUpdater.title");
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return Configed.getResourceValue("SingleCommandOpsiPackageUpdater.tooltip");
	}

	@Override
	public String getCommand() {
		return "";
	}

	@Override
	public String getCommandRaw() {
		return "";
	}

	@Override
	public void setCommand(String c) {
		/* Not needed in this class */
	}

	@Override
	public boolean needParameter() {
		return needParameter;
	}

	@Override
	public FGeneralDialog getDialog() {
		return dialog;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
