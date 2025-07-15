/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logpane;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.logpane.view.LogFileParser.LogParsedData;

public class LogPaneUpdate {

	private LogPaneUpdate() {

	}

	public static UpdateResult<LogPaneModel, LogPaneEffect> update(LogPaneMsg msg, LogPaneModel model) {
		return switch (msg) {
		case LogPaneMsg.ParseLogRequest(String text) -> new UpdateResult<>(model.withLogText(text),
				new LogPaneEffect.ParseLog());
		case LogPaneMsg.LogParsed(LogParsedData data, int level) -> new UpdateResult<>(
				model.withTypesList(data.getTypesList()).withMinLevel(data.getMinExistingLevel())
						.withMaxExistingLevel(data.getMaxExistingLevel()).withShowLevel(level),
				new LogPaneEffect.DisplayLog());
		case LogPaneMsg.Search(String query) -> {
			List<String> newHistory = new ArrayList<>(model.getSearchHistory());
			if (!newHistory.contains(query)) {
				newHistory.add(query);
			}
			yield new UpdateResult<>(model.withSearchHistory(newHistory), new LogPaneEffect.Search());
		}
		case LogPaneMsg.SetShowLevel(int level) -> new UpdateResult<>(model.withShowLevel(level),
				new LogPaneEffect.SetLogLevel());
		case LogPaneMsg.SetType(String type) -> new UpdateResult<>(model.withSelectedType(type),
				new LogPaneEffect.SetType());
		case LogPaneMsg.IncreaseFontSize() -> new UpdateResult<>(model, new LogPaneEffect.IncreaseFontSize());
		case LogPaneMsg.DecreaseFontSize() -> new UpdateResult<>(model, new LogPaneEffect.DecreaseFontSize());
		case LogPaneMsg.FontSizeChanged(int fontSize) -> new UpdateResult<>(model.withFontSize(fontSize),
				new LogPaneEffect.None());
		case LogPaneMsg.SetCaretPosition(int position) -> new UpdateResult<>(model.withCaretPosition(position),
				new LogPaneEffect.None());
		case LogPaneMsg.Reload() -> new UpdateResult<>(model, new LogPaneEffect.Reload());
		case LogPaneMsg.Download download -> new UpdateResult<>(model, new LogPaneEffect.Download());
		case LogPaneMsg.DownloadAsZip downloadAsZip -> new UpdateResult<>(model, new LogPaneEffect.DownloadAsZip());
		case LogPaneMsg.DownloadAllAsZip downloadAllAsZip -> new UpdateResult<>(model,
				new LogPaneEffect.DownloadAllAsZip());
		case LogPaneMsg.FloatExternal floatExternal -> new UpdateResult<>(model, new LogPaneEffect.FloatExternal());
		case LogPaneMsg.SetCaseSensitive(boolean caseSensitive) -> new UpdateResult<>(
				model.withCaseSensitive(caseSensitive), new LogPaneEffect.None());
		};
	}

}
