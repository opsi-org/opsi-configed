/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import de.uib.configed.ConfigedMain;

public interface CommandWithParameters {
	void startParameterGui(ConfigedMain configedMain);

	String getBasicName();
}
