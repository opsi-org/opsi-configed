/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logpane;

public sealed interface LogPaneEffect permits LogPaneEffect.None, LogPaneEffect.SimpleEffect {
	record None() implements LogPaneEffect {
	}

	enum SimpleEffect implements LogPaneEffect {
		INCREASE_FONT_SIZE, DECREASE_FONT_SIZE, RELOAD, SEARCH, PARSE_LOG, DISPLAY_LOG, SET_TYPE, SET_LOG_LEVEL,
		DOWNLOAD, DOWNLOAD_AS_ZIP, DOWNLOAD_ALL_AS_ZIP, FLOAT_EXTERNAL
	}
}
