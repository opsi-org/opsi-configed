/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.List;

import de.uib.configed.gui.FGeneralDialog;

public interface SingleCommand extends CommandMetadata {
	String getCommand();

	String getSecuredCommand();

	String getSecureInfoInCommand();

	String getCommandRaw();

	List<String> getParameterList();

	void setCommand(String c);

	boolean needParameter();

	FGeneralDialog getDialog();
}
