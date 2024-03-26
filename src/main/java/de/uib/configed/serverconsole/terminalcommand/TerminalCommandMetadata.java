/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.terminalcommand;

public interface TerminalCommandMetadata {
	String getId();

	String getMenuText();

	String getParentMenuText();

	String getToolTipText();

	int getPriority();
}
