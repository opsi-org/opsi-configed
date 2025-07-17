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
		case LogPaneMsg.ParseLogRequest(String text) -> UpdateResult.withEffect(model.withLogText(text),
				LogPaneEffect.SimpleEffect.PARSE_LOG);
		case LogPaneMsg.LogParsed(LogParsedData data, int level) -> UpdateResult.withEffect(
				model.withTypesList(data.getTypesList()).withMinLevel(data.getMinExistingLevel())
						.withMaxExistingLevel(data.getMaxExistingLevel()).withShowLevel(level),
				LogPaneEffect.SimpleEffect.DISPLAY_LOG);
		case LogPaneMsg.Search(String query) -> {
			List<String> newHistory = new ArrayList<>(model.getSearchHistory());
			if (!newHistory.contains(query)) {
				newHistory.add(query);
			}
			yield UpdateResult.withEffect(model.withSearchHistory(newHistory), LogPaneEffect.SimpleEffect.SEARCH);
		}
		case LogPaneMsg.SetShowLevel(int level) -> UpdateResult.withEffect(model.withShowLevel(level),
				LogPaneEffect.SimpleEffect.SET_LOG_LEVEL);
		case LogPaneMsg.SetType(String type) -> UpdateResult.withEffect(model.withSelectedType(type),
				LogPaneEffect.SimpleEffect.SET_TYPE);
		case LogPaneMsg.SetTitle(String title) -> UpdateResult.noEffect(model.withTitle(title));
		case LogPaneMsg.SetInfo(String info) -> UpdateResult.noEffect(model.withInfo(info));

		case LogPaneMsg.FontSizeChanged(int fontSize) -> UpdateResult.noEffect(model.withFontSize(fontSize));
		case LogPaneMsg.SetCaretPosition(int position) -> UpdateResult.noEffect(model.withCaretPosition(position));
		case LogPaneMsg.SetCaseSensitive(boolean caseSensitive) -> UpdateResult
				.noEffect(model.withCaseSensitive(caseSensitive));
		};
	}

	private static UpdateResult<LogPaneModel, LogPaneEffect> handleSimpleMsg(LogPaneMsg.SimpleMsg msg,
			LogPaneModel model) {
		return switch (msg) {
		case RELOAD -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.RELOAD);
		case DOWNLOAD -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.DOWNLOAD);
		case DOWNLOAD_AS_ZIP -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.DOWNLOAD_AS_ZIP);
		case DOWNLOAD_ALL_AS_ZIP -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.DOWNLOAD_ALL_AS_ZIP);
		case FLOAT_EXTERNAL -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.FLOAT_EXTERNAL);
		case INCREASE_FONT_SIZE -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.INCREASE_FONT_SIZE);
		case DECREASE_FONT_SIZE -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.DECREASE_FONT_SIZE);
		};
	}

}
