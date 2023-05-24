/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import java.util.List;

public interface SSHMultiCommand {
	List<SSHCommand> getCommands();

	List<String> getCommandsRaw();

	String getMainName();
}
