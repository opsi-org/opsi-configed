/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneEffect;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneModel;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneMsg;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneUpdate;
import de.uib.configed.gui.features.logviewer.logpane.view.LogFileParser.LogParsedData;

class LogPaneUpdateTest {

	private static LogPaneModel baseModel() {
		return LogPaneModel.builder().logText("old log").withPopup(true).showLevel(1).minLevel(1).maxLevel(5)
				.maxExistingLevel(5).typesList(List.of("INFO")).selectedType("INFO").caretPosition(0).info("").title("")
				.caseSensitive(false).searchHistory(List.of("foo")).fontSize(11).build();
	}

	@Test
	void testParseLogRequest() {
		LogPaneModel model = baseModel();
		String newText = "new log text";
		LogPaneMsg msg = new LogPaneMsg.ParseLogRequest(newText);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newText, result.model().getLogText());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.PARSE_LOG, result.effect().get()));
	}

	@Test
	void testLogParsed() {
		LogParsedData parsedData = new LogParsedData();
		parsedData.setTypesList(List.of("INFO", "WARN"));
		parsedData.setMinExistingLevel(1);
		parsedData.setMaxExistingLevel(5);
		int level = 3;
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.LogParsed(parsedData, level);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(parsedData.getTypesList(), result.model().getTypesList());
		assertEquals(parsedData.getMinExistingLevel(), result.model().getMinLevel());
		assertEquals(parsedData.getMaxExistingLevel(), result.model().getMaxExistingLevel());
		assertEquals(level, result.model().getShowLevel());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.DISPLAY_LOG, result.effect().get()));
	}

	@Test
	void testSearchAddsToHistory() {
		LogPaneModel model = baseModel();
		String query = "bar";
		LogPaneMsg msg = new LogPaneMsg.Search(query);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertTrue(result.model().getSearchHistory().contains(query));
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.SEARCH, result.effect().get()));
	}

	@Test
	void testSearchDoesNotDuplicateHistory() {
		LogPaneModel model = baseModel();
		String query = "foo";
		LogPaneMsg msg = new LogPaneMsg.Search(query);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		// Should not duplicate
		assertEquals(1, result.model().getSearchHistory().stream().filter(q -> q.equals(query)).count());
	}

	@Test
	void testSetShowLevel() {
		LogPaneModel model = baseModel();
		int newLevel = 4;
		LogPaneMsg msg = new LogPaneMsg.SetShowLevel(newLevel);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newLevel, result.model().getShowLevel());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.SET_LOG_LEVEL, result.effect().get()));
	}

	@Test
	void testSetType() {
		LogPaneModel model = baseModel();
		String newType = "WARN";
		LogPaneMsg msg = new LogPaneMsg.SetType(newType);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newType, result.model().getSelectedType());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.SET_TYPE, result.effect().get()));
	}

	@Test
	void testIncreaseFontSize() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = LogPaneMsg.SimpleMsg.INCREASE_FONT_SIZE;

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.INCREASE_FONT_SIZE, result.effect().get()));
	}

	@Test
	void testDecreaseFontSize() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = LogPaneMsg.SimpleMsg.DECREASE_FONT_SIZE;

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.DECREASE_FONT_SIZE, result.effect().get()));
	}

	@Test
	void testFontSizeChanged() {
		LogPaneModel model = baseModel();
		int newFontSize = 15;
		LogPaneMsg msg = new LogPaneMsg.FontSizeChanged(newFontSize);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newFontSize, result.model().getFontSize());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void testSetCaretPosition() {
		LogPaneModel model = baseModel();
		int newPos = 42;
		LogPaneMsg msg = new LogPaneMsg.SetCaretPosition(newPos);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newPos, result.model().getCaretPosition());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void testReload() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = LogPaneMsg.SimpleMsg.RELOAD;

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.RELOAD, result.effect().get()));
	}

	@Test
	void testDownload() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = LogPaneMsg.SimpleMsg.DOWNLOAD;

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.DOWNLOAD, result.effect().get()));
	}

	@Test
	void testDownloadAsZip() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = LogPaneMsg.SimpleMsg.DOWNLOAD_AS_ZIP;

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.DOWNLOAD_AS_ZIP, result.effect().get()));
	}

	@Test
	void testDownloadAllAsZip() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = LogPaneMsg.SimpleMsg.DOWNLOAD_ALL_AS_ZIP;

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.DOWNLOAD_ALL_AS_ZIP, result.effect().get()));
	}

	@Test
	void testFloatExternal() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = LogPaneMsg.SimpleMsg.FLOAT_EXTERNAL;

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(LogPaneEffect.SimpleEffect.FLOAT_EXTERNAL, result.effect().get()));
	}

	@Test
	void testSetCaseSensitive() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.SetCaseSensitive(true);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertTrue(result.model().isCaseSensitive());
		assertFalse(result.effect().isPresent());
	}
}