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
		case LogPaneMsg.SimpleMsg m -> handleSimpleMsg(m, model);
		case LogPaneMsg.ParseLogRequest(String text) -> new UpdateResult<>(model.withLogText(text),
				LogPaneEffect.SimpleEffect.PARSE_LOG);
		case LogPaneMsg.LogParsed(LogParsedData data, int level) -> new UpdateResult<>(
				model.withTypesList(data.getTypesList()).withMinLevel(data.getMinExistingLevel())
						.withMaxExistingLevel(data.getMaxExistingLevel()).withShowLevel(level),
				LogPaneEffect.SimpleEffect.DISPLAY_LOG);
		case LogPaneMsg.Search(String query) -> {
			List<String> newHistory = new ArrayList<>(model.getSearchHistory());
			if (!newHistory.contains(query)) {
				newHistory.add(query);
			}
			yield new UpdateResult<>(model.withSearchHistory(newHistory), LogPaneEffect.SimpleEffect.SEARCH);
		}
		case LogPaneMsg.SetShowLevel(int level) -> new UpdateResult<>(model.withShowLevel(level),
				LogPaneEffect.SimpleEffect.SET_LOG_LEVEL);
		case LogPaneMsg.SetType(String type) -> new UpdateResult<>(model.withSelectedType(type),
				LogPaneEffect.SimpleEffect.SET_TYPE);
		case LogPaneMsg.SetTitle(String title) -> new UpdateResult<>(model.withTitle(title), new LogPaneEffect.None());
		case LogPaneMsg.SetInfo(String info) -> new UpdateResult<>(model.withInfo(info), new LogPaneEffect.None());

		case LogPaneMsg.FontSizeChanged(int fontSize) -> new UpdateResult<>(model.withFontSize(fontSize),
				new LogPaneEffect.None());
		case LogPaneMsg.SetCaretPosition(int position) -> new UpdateResult<>(model.withCaretPosition(position),
				new LogPaneEffect.None());
		case LogPaneMsg.SetCaseSensitive(boolean caseSensitive) -> new UpdateResult<>(
				model.withCaseSensitive(caseSensitive), new LogPaneEffect.None());
		};
	}

	private static UpdateResult<LogPaneModel, LogPaneEffect> handleSimpleMsg(LogPaneMsg.SimpleMsg msg,
			LogPaneModel model) {
		return switch (msg) {
		case RELOAD -> new UpdateResult<>(model, LogPaneEffect.SimpleEffect.RELOAD);
		case DOWNLOAD -> new UpdateResult<>(model, LogPaneEffect.SimpleEffect.DOWNLOAD);
		case DOWNLOAD_AS_ZIP -> new UpdateResult<>(model, LogPaneEffect.SimpleEffect.DOWNLOAD_AS_ZIP);
		case DOWNLOAD_ALL_AS_ZIP -> new UpdateResult<>(model, LogPaneEffect.SimpleEffect.DOWNLOAD_ALL_AS_ZIP);
		case FLOAT_EXTERNAL -> new UpdateResult<>(model, LogPaneEffect.SimpleEffect.FLOAT_EXTERNAL);
		case INCREASE_FONT_SIZE -> new UpdateResult<>(model, LogPaneEffect.SimpleEffect.INCREASE_FONT_SIZE);
		case DECREASE_FONT_SIZE -> new UpdateResult<>(model, LogPaneEffect.SimpleEffect.DECREASE_FONT_SIZE);
		};
	}

}
