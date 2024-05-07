/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

public interface CommandMetadata {
	String getId();

	String getMenuText();

	String getParentMenuText();

	String getToolTipText();

	int getPriority();
}
