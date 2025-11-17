/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

public sealed interface LogPaneEffect permits LogPaneEffect.SimpleEffect {
	enum SimpleEffect implements LogPaneEffect {
		INCREASE_FONT_SIZE, DECREASE_FONT_SIZE, RELOAD, COPY_CONTENTS, SEARCH, PARSE_LOG, DISPLAY_LOG,
		CHANGE_EVENT_TYPE, CHANGE_LOG_LEVEL, DOWNLOAD, DOWNLOAD_AS_ZIP, DOWNLOAD_ALL_AS_ZIP, FLOAT_EXTERNAL
	}
}
