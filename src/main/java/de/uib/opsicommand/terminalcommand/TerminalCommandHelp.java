/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.FGeneralDialog;

public class TerminalCommandHelp implements TerminalCommand {
	private boolean needParameter;
	private TerminalCommand basicCommand;
	private FGeneralDialog dialog;

	private boolean isMultiCommand;

	public TerminalCommandHelp(TerminalCommand basicCommand) {
		this.basicCommand = basicCommand;

		this.dialog = this.basicCommand.getDialog();
	}

	@Override
	public boolean isMultiCommand() {
		return isMultiCommand;
	}

	@Override
	public String getId() {
		return basicCommand.getId();
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

	@Override
	public String getMenuText() {
		return null;
	}

	@Override
	public String getParentMenuText() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public String getCommand() {
		return ((TerminalCommandNeedParameter) this.basicCommand).getBasicName() + " --help";
	}

	@Override
	public void setCommand(String c) {
		// Leave empty, never used actually and never needed
	}

	@Override
	public String getCommandRaw() {
		return basicCommand.getCommandRaw();
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
		return 0;
	}

	@Override
	public List<String> getParameterList() {
		return new ArrayList<>();
	}
}
