/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole.command;

import de.uib.configed.gui.ConfigedMain;

public interface CommandWithParameters {
	void startParameterGui(ConfigedMain configedMain);

	String getBasicName();
}
