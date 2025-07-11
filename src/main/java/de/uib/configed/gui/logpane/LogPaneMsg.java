/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

public sealed interface LogPaneMsg permits LogPaneMsg.Search, LogPaneMsg.SetLogText, LogPaneMsg.SetShowLevel,
		LogPaneMsg.SetType, LogPaneMsg.IncreaseFontSize, LogPaneMsg.DecreaseFontSize, LogPaneMsg.SetCaretPosition,
		LogPaneMsg.Reload, LogPaneMsg.Download, LogPaneMsg.DownloadAsZip, LogPaneMsg.DownloadAllAsZip,
		LogPaneMsg.FloatExternal, LogPaneMsg.SetCaseSensitive {
	record Search(String query) implements LogPaneMsg {
	}

	record SetLogText(String text) implements LogPaneMsg {
	}

	record SetShowLevel(int level) implements LogPaneMsg {
	}

	record SetType(String type) implements LogPaneMsg {
	}

	record IncreaseFontSize() implements LogPaneMsg {
	}

	record DecreaseFontSize() implements LogPaneMsg {
	}

	record SetCaretPosition(int position) implements LogPaneMsg {
	}

	record Reload() implements LogPaneMsg {
	}

	record Download() implements LogPaneMsg {
	}

	record DownloadAsZip() implements LogPaneMsg {
	}

	record DownloadAllAsZip() implements LogPaneMsg {
	}

	record FloatExternal() implements LogPaneMsg {
	}

	record SetCaseSensitive(boolean caseSensitive) implements LogPaneMsg {
	}
}