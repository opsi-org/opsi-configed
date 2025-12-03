/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

import de.uib.configed.gui.features.logviewer.logpane.view.LogFileParser.LogParsedData;

public sealed interface LogPaneMsg permits LogPaneMsg.SimpleMsg, LogPaneMsg.Search, LogPaneMsg.ParseLogRequested,
		LogPaneMsg.LogParsed, LogPaneMsg.ChangeLogLevel, LogPaneMsg.ChangeEventType, LogPaneMsg.ChangeTitle,
		LogPaneMsg.ChangeInfo, LogPaneMsg.ChangeCaretPosition, LogPaneMsg.ToggleCaseSensitivity {

	enum SimpleMsg implements LogPaneMsg {
		INCREASE_FONT_SIZE, DECREASE_FONT_SIZE, RELOAD_LOG, COPY_CONTENTS, DOWNLOAD_LOG, DOWNLOAD_LOG_AS_ZIP,
		DOWNLOAD_ALL_AS_ZIP, FLOAT_EXTERNAL,
	}

	record Search(String query) implements LogPaneMsg {
	}

	record ParseLogRequested(String text, boolean resetCaret) implements LogPaneMsg {
		public ParseLogRequested(String text) {
			this(text, false);
		}
	}

	record LogParsed(LogParsedData data, int level) implements LogPaneMsg {
	}

	record ChangeLogLevel(int level) implements LogPaneMsg {
	}

	record ChangeEventType(String type) implements LogPaneMsg {
	}

	record ChangeTitle(String title) implements LogPaneMsg {
	}

	record ChangeInfo(String info) implements LogPaneMsg {
	}

	record ChangeCaretPosition(int position) implements LogPaneMsg {
	}

	record ToggleCaseSensitivity(boolean caseSensitive) implements LogPaneMsg {
	}
}
