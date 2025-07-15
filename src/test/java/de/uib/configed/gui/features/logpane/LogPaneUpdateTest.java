/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logpane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.TeaComponent.UpdateResult;
import de.uib.configed.gui.features.logpane.view.LogFileParser.LogParsedData;

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
		assertTrue(result.effect() instanceof LogPaneEffect.ParseLog);
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
		assertTrue(result.effect() instanceof LogPaneEffect.DisplayLog);
	}

	@Test
	void testSearchAddsToHistory() {
		LogPaneModel model = baseModel();
		String query = "bar";
		LogPaneMsg msg = new LogPaneMsg.Search(query);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertTrue(result.model().getSearchHistory().contains(query));
		assertTrue(result.effect() instanceof LogPaneEffect.Search);
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
		assertTrue(result.effect() instanceof LogPaneEffect.SetLogLevel);
	}

	@Test
	void testSetType() {
		LogPaneModel model = baseModel();
		String newType = "WARN";
		LogPaneMsg msg = new LogPaneMsg.SetType(newType);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newType, result.model().getSelectedType());
		assertTrue(result.effect() instanceof LogPaneEffect.SetType);
	}

	@Test
	void testIncreaseFontSize() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.IncreaseFontSize();

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertTrue(result.effect() instanceof LogPaneEffect.IncreaseFontSize);
	}

	@Test
	void testDecreaseFontSize() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.DecreaseFontSize();

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertTrue(result.effect() instanceof LogPaneEffect.DecreaseFontSize);
	}

	@Test
	void testFontSizeChanged() {
		LogPaneModel model = baseModel();
		int newFontSize = 15;
		LogPaneMsg msg = new LogPaneMsg.FontSizeChanged(newFontSize);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newFontSize, result.model().getFontSize());
		assertTrue(result.effect() instanceof LogPaneEffect.None);
	}

	@Test
	void testSetCaretPosition() {
		LogPaneModel model = baseModel();
		int newPos = 42;
		LogPaneMsg msg = new LogPaneMsg.SetCaretPosition(newPos);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertEquals(newPos, result.model().getCaretPosition());
		assertTrue(result.effect() instanceof LogPaneEffect.None);
	}

	@Test
	void testReload() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.Reload();

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertTrue(result.effect() instanceof LogPaneEffect.Reload);
	}

	@Test
	void testDownload() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.Download();

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertTrue(result.effect() instanceof LogPaneEffect.Download);
	}

	@Test
	void testDownloadAsZip() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.DownloadAsZip();

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertTrue(result.effect() instanceof LogPaneEffect.DownloadAsZip);
	}

	@Test
	void testDownloadAllAsZip() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.DownloadAllAsZip();

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertTrue(result.effect() instanceof LogPaneEffect.DownloadAllAsZip);
	}

	@Test
	void testFloatExternal() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.FloatExternal();

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertSame(model, result.model());
		assertTrue(result.effect() instanceof LogPaneEffect.FloatExternal);
	}

	@Test
	void testSetCaseSensitive() {
		LogPaneModel model = baseModel();
		LogPaneMsg msg = new LogPaneMsg.SetCaseSensitive(true);

		UpdateResult<LogPaneModel, LogPaneEffect> result = LogPaneUpdate.update(msg, model);

		assertTrue(result.model().isCaseSensitive());
		assertTrue(result.effect() instanceof LogPaneEffect.None);
	}
}