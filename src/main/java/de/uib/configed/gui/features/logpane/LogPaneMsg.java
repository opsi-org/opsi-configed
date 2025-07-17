/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logpane;

import de.uib.configed.gui.features.logpane.view.LogFileParser.LogParsedData;

public sealed interface LogPaneMsg permits LogPaneMsg.SimpleMsg, LogPaneMsg.Search, LogPaneMsg.ParseLogRequest,
		LogPaneMsg.LogParsed, LogPaneMsg.SetShowLevel, LogPaneMsg.SetType, LogPaneMsg.SetTitle, LogPaneMsg.SetInfo,
		LogPaneMsg.FontSizeChanged, LogPaneMsg.SetCaretPosition, LogPaneMsg.SetCaseSensitive {

	enum SimpleMsg implements LogPaneMsg {
		INCREASE_FONT_SIZE, DECREASE_FONT_SIZE, RELOAD, DOWNLOAD, DOWNLOAD_AS_ZIP, DOWNLOAD_ALL_AS_ZIP, FLOAT_EXTERNAL,
	}

	record Search(String query) implements LogPaneMsg {
	}

	record ParseLogRequest(String text) implements LogPaneMsg {
	}

	record LogParsed(LogParsedData data, int level) implements LogPaneMsg {

	}

	record SetShowLevel(int level) implements LogPaneMsg {
	}

	record SetType(String type) implements LogPaneMsg {
	}

	record SetTitle(String title) implements LogPaneMsg {
	}

	record SetInfo(String info) implements LogPaneMsg {
	}

	record FontSizeChanged(int fontSize) implements LogPaneMsg {
	}

	record SetCaretPosition(int position) implements LogPaneMsg {
	}

	record SetCaseSensitive(boolean caseSensitive) implements LogPaneMsg {
	}
}