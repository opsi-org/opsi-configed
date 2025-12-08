/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.logviewer.logpane.view.LogFileParser.LogParsedData;

public final class LogPaneUpdate {

	private LogPaneUpdate() {

	}

	@SuppressWarnings("java:S1541")
	public static UpdateResult<LogPaneModel, LogPaneEffect> update(LogPaneMsg msg, LogPaneModel model) {
		return switch (msg) {
		case LogPaneMsg.SimpleMsg m -> handleSimpleMsg(m, model);
		case LogPaneMsg.ParseLogRequested(String text, boolean resetCaret) -> handlePraseLogRequestedMsg(model, text,
				resetCaret);
		case LogPaneMsg.LogParsed(LogParsedData data, int level) -> UpdateResult
				.noEffect(model.toBuilder().typesList(data.getTypesList()).minLevel(data.getMinExistingLevel())
						.maxExistingLevel(data.getMaxExistingLevel()).showLevel(level).needsRebuild(true).build());
		case LogPaneMsg.Search(String query) -> handleSearchMsg(model, query);
		case LogPaneMsg.ChangeLogLevel(int level) -> handleChangelogLevelMsg(model, level);
		case LogPaneMsg.ChangeEventType(String type) -> handleChangeEventTypeMsg(model, type);
		case LogPaneMsg.ChangeTitle(String title) -> UpdateResult.noEffect(model.withTitle(title));
		case LogPaneMsg.ChangeInfo(String info) -> UpdateResult.noEffect(model.withInfo(info));
		case LogPaneMsg.ChangeCaretPosition(int position) -> UpdateResult
				.noEffect(model.toBuilder().caretPosition(position).needsRebuild(false).build());
		case LogPaneMsg.ToggleCaseSensitivity(boolean caseSensitive) -> UpdateResult
				.noEffect(model.withCaseSensitive(caseSensitive));
		};
	}

	private static UpdateResult<LogPaneModel, LogPaneEffect> handleSearchMsg(LogPaneModel model, String query) {
		List<String> newHistory = new ArrayList<>(model.getSearchHistory());
		if (!newHistory.contains(query)) {
			newHistory.add(query);
		}
		return UpdateResult.withEffect(model.withSearchHistory(newHistory), LogPaneEffect.SimpleEffect.PERFORM_SEARCH);
	}

	private static UpdateResult<LogPaneModel, LogPaneEffect> handleChangelogLevelMsg(LogPaneModel model, int level) {
		int oldShowLevel = model.getShowLevel();
		boolean needsRebuild = oldShowLevel != level;
		return UpdateResult.noEffect(model.toBuilder().showLevel(level).needsRebuild(needsRebuild).build());
	}

	private static UpdateResult<LogPaneModel, LogPaneEffect> handleChangeEventTypeMsg(LogPaneModel model, String type) {
		String oldSelectedType = model.getSelectedType();
		boolean needsRebuild = !type.equals(oldSelectedType);
		return UpdateResult.noEffect(model.toBuilder().selectedType(type).needsRebuild(needsRebuild).build());
	}

	private static UpdateResult<LogPaneModel, LogPaneEffect> handlePraseLogRequestedMsg(LogPaneModel model, String text,
			boolean resetCaret) {
		int caretPos = resetCaret ? model.getCaretPosition() : 0;
		return UpdateResult.withEffect(model.toBuilder().logText(text).caretPosition(caretPos).build(),
				LogPaneEffect.SimpleEffect.PARSE_LOG);

	}

	private static UpdateResult<LogPaneModel, LogPaneEffect> handleSimpleMsg(LogPaneMsg.SimpleMsg msg,
			LogPaneModel model) {
		return switch (msg) {
		case RELOAD_LOG -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.RELOAD_LOG);
		case COPY_CONTENTS -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.COPY_CONTENTS);
		case DOWNLOAD_LOG -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.DOWNLOAD_LOG);
		case DOWNLOAD_LOG_AS_ZIP -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.DOWNLOAD_LOG_AS_ZIP);
		case DOWNLOAD_ALL_AS_ZIP -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.DOWNLOAD_ALL_AS_ZIP);
		case FLOAT_EXTERNAL -> UpdateResult.withEffect(model, LogPaneEffect.SimpleEffect.FLOAT_EXTERNAL);
		case INCREASE_FONT_SIZE -> UpdateResult.noEffect(model.withFontSize((int) (model.getFontSize() * 1.1)));
		case DECREASE_FONT_SIZE -> UpdateResult
				.noEffect(model.withFontSize((int) Math.max((model.getFontSize() + 1) / 1.1, 10)));
		};
	}

}
