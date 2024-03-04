/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import de.uib.configed.ConfigedMain;

public interface TerminalCommandNeedParameter {
	void startParameterGui(ConfigedMain configedMain);

	String getBasicName();
}
