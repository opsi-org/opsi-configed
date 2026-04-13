/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole.command;

import java.util.List;

public interface MultiCommand extends CommandMetadata {
	List<SingleCommand> getCommands();

	List<String> getCommandsRaw();

	String getMainName();
}
