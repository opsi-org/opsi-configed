/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.terminalcommand;

import java.util.List;

import de.uib.configed.gui.FGeneralDialog;

public interface TerminalCommand {
	String getCommand();

	String getSecuredCommand();

	String getSecureInfoInCommand();

	String getCommandRaw();

	List<String> getParameterList();

	String getId();

	String getMenuText();

	String getParentMenuText();

	String getToolTipText();

	int getPriority();

	void setCommand(String c);

	boolean needParameter();

	boolean isMultiCommand();

	FGeneralDialog getDialog();
}
