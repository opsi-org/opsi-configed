/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.sshcommand;

/**
 * This interface handles SSHCommands.
 **/
public interface SSHCommandParameterInterface {

	String[] getSelectedClientNames();

	String[] getSelectedDepotNames();

	String getConfigServerName();

	String getConfigSSHServerName();
}
