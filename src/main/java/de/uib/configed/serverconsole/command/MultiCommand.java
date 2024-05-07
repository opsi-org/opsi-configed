/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.util.List;

public interface MultiCommand extends CommandMetadata {
	List<SingleCommand> getCommands();

	List<String> getCommandsRaw();

	String getMainName();
}
