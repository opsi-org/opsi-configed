/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import java.util.List;

public record LogPaneModel(String logText, boolean withPopup, int showLevel, int maxExistingLevel,
		List<String> typesList, String selectedType, int caretPosition, String info, String title,
		boolean caseSensitive, List<String> searchHistory) {
	public LogPaneModel withLogText(String logText) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withPopup(boolean withPopup) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withShowLevel(int showLevel) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withMaxExistingLevel(int maxExistingLevel) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withTypesList(List<String> typesList) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withSelectedType(String selectedType) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withCaretPosition(int caretPosition) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withInfo(String info) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withTitle(String title) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withCaseSensitive(boolean caseSensitive) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel withSearchHistory(List<String> searchHistory) {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

	public LogPaneModel copy() {
		return new LogPaneModel(logText, withPopup, showLevel, maxExistingLevel, typesList, selectedType, caretPosition,
				info, title, caseSensitive, searchHistory);
	}

}