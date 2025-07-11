/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

public sealed interface LogPaneEffect permits LogPaneEffect.None, LogPaneEffect.IncreaseFontSize,
		LogPaneEffect.DecreaseFontSize, LogPaneEffect.Reload, LogPaneEffect.Search, LogPaneEffect.SetLogText,
		LogPaneEffect.SetType, LogPaneEffect.SetLogLevel, LogPaneEffect.Download, LogPaneEffect.DownloadAsZip,
		LogPaneEffect.DownloadAllAsZip, LogPaneEffect.FloatExternal {
	final class None implements LogPaneEffect {}

	final class IncreaseFontSize implements LogPaneEffect {}

	final class DecreaseFontSize implements LogPaneEffect {}

	final class Reload implements LogPaneEffect {}

	final class Search implements LogPaneEffect {}

	final class SetLogText implements LogPaneEffect {}

	final class SetType implements LogPaneEffect {}

	final class SetLogLevel implements LogPaneEffect {}

	record Download() implements LogPaneEffect {
	}

	record DownloadAsZip() implements LogPaneEffect {
	}

	record DownloadAllAsZip() implements LogPaneEffect {
	}

	record FloatExternal() implements LogPaneEffect {
	}
}
