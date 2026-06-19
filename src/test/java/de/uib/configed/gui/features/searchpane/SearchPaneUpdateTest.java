/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.share.table.gui.FilterStateManager;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.TableFilterState;

class SearchPaneUpdateTest {

	private static final FilterKey TEST_FILTER_KEY = FilterKey.CLIENT_TABLE;

	private static SearchPaneModel baseModel() {
		return SearchPaneModel.builder().searchText("").searchColumnIndex(-1).isRegexActive(false).isRespectCase(false)
				.selectMode(false).foundRow(-1).showNavPanel(false).extraOptionsVisible(false).isNarrow(false)
				.isFilteredBySelection(false).filterKey(TEST_FILTER_KEY).isDirty(false).build();
	}

	@Test
	void shouldApplyFilterEffect_whenChangeSearchText() {
		SearchPaneModel model = baseModel().withSearchColumnIndex(2).withRegexActive(true).withRespectCase(false);
		String searchText = "test query";

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ChangeSearchText(searchText), model);

		assertEquals(searchText, result.model().getSearchText());
		assertEquals(0, result.model().getFoundRow());
		assertTrue(result.effect().isPresent());
		assertInstanceOf(SearchPaneEffect.ServiceEffect.ApplyFilter.class, result.effect().get());

		SearchPaneEffect.ServiceEffect.ApplyFilter effect = (SearchPaneEffect.ServiceEffect.ApplyFilter) result.effect()
				.get();
		assertEquals(searchText, effect.query());
		assertEquals(2, effect.col());
		assertTrue(effect.regex());
		assertFalse(effect.caseSensitive());
	}

	@Test
	void shouldUpdateSearchColumnIndex_whenChangeSearchColumnIndex() {
		SearchPaneModel model = baseModel();
		int newColumnIndex = 5;

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ChangeSearchColumnIndex(newColumnIndex), model);

		assertEquals(newColumnIndex, result.model().getSearchColumnIndex());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldToggleRespectCase_whenToggleRespectCase() {
		SearchPaneModel model = baseModel().withRespectCase(false);

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ToggleRespectCase(true), model);

		assertTrue(result.model().isRespectCase());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldToggleRegex_whenToggleRegex() {
		SearchPaneModel model = baseModel().withRegexActive(false);

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ToggleRegex(true), model);

		assertTrue(result.model().isRegexActive());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldMarkAndFilter_whenToggleFilterMarkToTrue() {
		SearchPaneModel model = baseModel().withFilteredBySelection(false);

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ToggleFilterMark(true), model);

		assertTrue(result.model().isFilteredBySelection());
		assertTrue(result.effect().isPresent());
		assertInstanceOf(SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter.class, result.effect().get());

		SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter effect = (SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter) result
				.effect().get();
		assertTrue(effect.value());
	}

	@Test
	void shouldUnmarkAndFilter_whenToggleFilterMarkToFalse() {
		SearchPaneModel model = baseModel().withFilteredBySelection(true);

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ToggleFilterMark(false), model);

		assertFalse(result.model().isFilteredBySelection());
		assertTrue(result.effect().isPresent());
		assertInstanceOf(SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter.class, result.effect().get());

		SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter effect = (SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter) result
				.effect().get();
		assertFalse(effect.value());
	}

	@Test
	void shouldTriggerSearchNextRow_whenSearchNextRequested() {
		SearchPaneModel model = baseModel();

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.SearchNext(), model);

		assertEquals(model, result.model());
		assertTrue(result.effect().isPresent());
		assertInstanceOf(SearchPaneEffect.ServiceEffect.SearchNextRow.class, result.effect().get());
	}

	@Test
	void shouldTriggerMarkAllAndFilterAndActivateFiltered_whenMarkAllAndFilter() {
		SearchPaneModel model = baseModel().toBuilder().enableFilterBySelection(true).isFilteredBySelection(false)
				.searchText("test").build();

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.MarkAllAndFilter(), model);

		assertNotEquals(model, result.model());
		assertTrue(result.effect().isPresent());
		assertTrue(result.model().isEnableFilterBySelection());
		assertTrue(result.model().isFilteredBySelection());
		assertInstanceOf(SearchPaneEffect.ServiceEffect.MarkAllAndFilter.class, result.effect().get());
	}

	@Test
	void shouldReturnUnchangedModelAndTriggerNoEffect_whenMarkAllAndFilterWithActiveFilter() {
		SearchPaneModel model = baseModel().toBuilder().enableFilterBySelection(true).isFilteredBySelection(true)
				.searchText("test").build();

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.MarkAllAndFilter(), model);

		assertEquals(model, result.model());
		assertFalse(result.effect().isPresent());
		assertTrue(result.model().isEnableFilterBySelection());
		assertTrue(result.model().isFilteredBySelection());
		assertEquals("test", result.model().getSearchText());
	}

	@Test
	void shouldReturnUnchangedModelAndTriggerNoEffect_whenMarkAllAndFilterWithEmptySearchText() {
		SearchPaneModel model = baseModel().toBuilder().enableFilterBySelection(true).isFilteredBySelection(false)
				.searchText("").build();

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.MarkAllAndFilter(), model);

		assertEquals(model, result.model());
		assertFalse(result.effect().isPresent());
		assertTrue(result.model().isEnableFilterBySelection());
		assertFalse(result.model().isFilteredBySelection());
		assertEquals("", result.model().getSearchText());
	}

	@Test
	void shouldReturnUnchangedModelAndTriggerNoEffect_whenMarkAllAndFilterWithNotEnabledFilterBySelection() {
		SearchPaneModel model = baseModel().toBuilder().enableFilterBySelection(false).isFilteredBySelection(false)
				.searchText("test").build();

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.MarkAllAndFilter(), model);

		assertEquals(model, result.model());
		assertFalse(result.effect().isPresent());
		assertFalse(result.model().isEnableFilterBySelection());
		assertFalse(result.model().isFilteredBySelection());
		assertEquals("test", result.model().getSearchText());
	}

	@Test
	void shouldTriggerResetSearchAndDeactivateFiltered_whenResetSearch() {
		SearchPaneModel model = baseModel().withFilteredBySelection(true);

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.ResetSearch(), model);

		assertNotEquals(model, result.model());
		assertTrue(result.effect().isPresent());
		assertFalse(result.model().isFilteredBySelection());
		assertInstanceOf(SearchPaneEffect.ServiceEffect.ResetSearch.class, result.effect().get());
	}

	@Test
	void shouldReturnUnchangedModelAndTriggerNoEffect_whenResetSearchWithNoFilterActivated() {
		SearchPaneModel model = baseModel().withFilteredBySelection(false);

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.ResetSearch(), model);

		assertEquals(model, result.model());
		assertFalse(result.effect().isPresent());
		assertFalse(result.model().isFilteredBySelection());
	}

	@Test
	void shouldNavigateToRow_whenNavigateToRow() {
		SearchPaneModel model = baseModel();
		int targetRow = 42;

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.NavigateToRow(targetRow), model);

		assertEquals(targetRow, result.model().getNavigatedToRow());
		assertTrue(result.effect().isPresent());
		assertInstanceOf(SearchPaneEffect.UIEffect.NavigateToRow.class, result.effect().get());

		SearchPaneEffect.UIEffect.NavigateToRow effect = (SearchPaneEffect.UIEffect.NavigateToRow) result.effect()
				.get();
		assertEquals(targetRow, effect.row());
	}

	@Test
	void shouldNormalizeNegativeRow_whenNavigateToRowWithLargeNegativeValue() {
		SearchPaneModel model = baseModel();
		int invalidRow = -50;

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.NavigateToRow(invalidRow), model);

		assertEquals(-1, result.model().getNavigatedToRow());
		assertTrue(result.effect().isPresent());
		assertInstanceOf(SearchPaneEffect.UIEffect.NavigateToRow.class, result.effect().get());

		SearchPaneEffect.UIEffect.NavigateToRow effect = (SearchPaneEffect.UIEffect.NavigateToRow) result.effect()
				.get();
		assertEquals(-1, effect.row());
	}

	@Test
	void shouldRestoreFilterState_whenRestoreFilterAction() {
		SearchPaneModel model = baseModel();

		FilterStateManager.saveFilterState(TEST_FILTER_KEY, new TableFilterState("test", 2, true, false));

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.RestoreFilter(), model);

		if (result.effect().isEmpty()) {
			assertEquals(model, result.model());
		} else {
			assertEquals("test", result.model().getSearchText());
			assertEquals(2, result.model().getSearchColumnIndex());
			assertTrue(result.model().isRegexActive());
			assertFalse(result.model().isRespectCase());
			assertAll(() -> assertTrue(result.effect().isPresent()),
					() -> assertInstanceOf(SearchPaneEffect.ServiceEffect.ApplyFilter.class, result.effect().get()));
		}
	}

	@Test
	void shouldUpdateFoundRow_whenSearchCompleted() {
		SearchPaneModel model = baseModel();
		int foundRow = 10;

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.EffectResultMsg.SearchCompleted(foundRow), model);

		assertEquals(foundRow, result.model().getFoundRow());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldTriggerFilterMarkTriggered_whenTriggerFilterMark() {
		SearchPaneModel model = baseModel();

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.ActionMsg.TriggerFilterMark(), model);

		assertEquals(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(SearchPaneEffect.UIEffect.FilterMarkTriggered.class, result.effect().get()));
	}

	@Test
	void shouldChangeSelectMode_whenChangeSelectMode() {
		SearchPaneModel model = baseModel();

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ChangeSelectMode(false), model);

		assertFalse(result.model().isSelectMode());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldChangeFilterBySelection_whenChangeFilterBySelection() {
		SearchPaneModel model = baseModel().withFilteredBySelection(false);

		UpdateResult<SearchPaneModel, SearchPaneEffect> result = SearchPaneUpdate
				.update(new SearchPaneMsg.FieldChangeMsg.ChangeFilterBySelection(true), model);

		assertNotEquals(model, result.model());
		assertTrue(result.model().isFilteredBySelection());
		assertFalse(result.effect().isPresent());
	}
}