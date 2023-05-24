/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.ssh.SSHConnectionExecDialog;

public interface SSHCommandNeedParameter {

	void startParameterGui(ConfigedMain main);

	SSHConnectionExecDialog startHelpDialog();

	String getBasicName();
}
