/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

public sealed interface LogPaneEffect permits LogPaneEffect.SimpleEffect {
	enum SimpleEffect implements LogPaneEffect {
		RELOAD_LOG, COPY_CONTENTS, PERFORM_SEARCH, PARSE_LOG, DOWNLOAD_LOG, DOWNLOAD_LOG_AS_ZIP, DOWNLOAD_ALL_AS_ZIP,
		FLOAT_EXTERNAL
	}
}
