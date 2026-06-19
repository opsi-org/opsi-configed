/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import java.util.List;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.share.table.gui.FilterStateManager;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.TableFilterState;

public final class SearchPaneUpdate {
	private SearchPaneUpdate() {
	}

	static UpdateResult<SearchPaneModel, SearchPaneEffect> update(SearchPaneMsg msg, SearchPaneModel model) {
		return switch (msg) {
		case SearchPaneMsg.FieldChangeMsg m -> handleFieldChange(m, model);
		case SearchPaneMsg.ActionMsg m -> handleAction(m, model);
		case SearchPaneMsg.EffectResultMsg m -> handleEffectResult(m, model);
		};
	}

	@SuppressWarnings("java:S1541")
	private static UpdateResult<SearchPaneModel, SearchPaneEffect> handleFieldChange(SearchPaneMsg.FieldChangeMsg msg,
			SearchPaneModel model) {
		return switch (msg) {
		case SearchPaneMsg.FieldChangeMsg.ChangeSearchText(String text) -> UpdateResult.withEffect(
				model.toBuilder().searchText(text).foundRow(0).build(), new SearchPaneEffect.ServiceEffect.ApplyFilter(
						text, model.getSearchColumnIndex(), model.isRegexActive(), model.isRespectCase()));
		case SearchPaneMsg.FieldChangeMsg.ChangeSearchColumnIndex(int index) -> UpdateResult
				.noEffect(model.withSearchColumnIndex(index));
		case SearchPaneMsg.FieldChangeMsg.ChangeFilterKey(FilterKey filterKey) -> UpdateResult
				.noEffect(model.toBuilder().filterKey(filterKey).enableFilterBySelection(filterKey != null).build());
		case SearchPaneMsg.FieldChangeMsg.ChangeSearchColumns(List<Integer> searchColumns) -> UpdateResult
				.noEffect(model.withSearchColumns(searchColumns));
		case SearchPaneMsg.FieldChangeMsg.ChangeShowNavPanel(boolean value) -> UpdateResult
				.noEffect(model.withShowNavPanel(value));
		case SearchPaneMsg.FieldChangeMsg.ChangeEnableFilterBySelection(boolean value) -> UpdateResult
				.noEffect(model.withEnableFilterBySelection(value));
		case SearchPaneMsg.FieldChangeMsg.ChangeFilterBySelection(boolean value) -> UpdateResult
				.noEffect(model.withFilteredBySelection(value));
		case SearchPaneMsg.FieldChangeMsg.ChangeSelectMode(boolean value) -> UpdateResult
				.noEffect(model.withSelectMode(value));
		case SearchPaneMsg.FieldChangeMsg.ToggleRespectCase(boolean value) -> UpdateResult
				.noEffect(model.withRespectCase(value));
		case SearchPaneMsg.FieldChangeMsg.ToggleRegex(boolean value) -> UpdateResult
				.noEffect(model.withRegexActive(value));
		case SearchPaneMsg.FieldChangeMsg.ToggleFilterMark(boolean value) -> UpdateResult.withEffect(
				model.withFilteredBySelection(value), new SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter(value));
		};
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> handleAction(SearchPaneMsg.ActionMsg msg,
			SearchPaneModel model) {
		return switch (msg) {
		case SearchPaneMsg.ActionMsg.SearchNext() -> UpdateResult.withEffect(model,
				new SearchPaneEffect.ServiceEffect.SearchNextRow());
		case SearchPaneMsg.ActionMsg.MarkAllAndFilter() -> onMarkAllAndFilter(model);
		case SearchPaneMsg.ActionMsg.NavigateToRow(int row) -> {
			row = row < -1 ? -1 : row;
			yield UpdateResult.withEffect(model.withNavigatedToRow(row),
					new SearchPaneEffect.UIEffect.NavigateToRow(row));
		}
		case SearchPaneMsg.ActionMsg.RestoreFilter() -> onRestoreFilter(model);
		case SearchPaneMsg.ActionMsg.TriggerFilterMark() -> UpdateResult.withEffect(model,
				new SearchPaneEffect.UIEffect.FilterMarkTriggered());
		case SearchPaneMsg.ActionMsg.ResetSearch() -> onResetSearch(model);
		};
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> onMarkAllAndFilter(SearchPaneModel model) {
		if (!model.isEnableFilterBySelection() || model.isFilteredBySelection() || model.getSearchText().isEmpty()) {
			return UpdateResult.noEffect(model);
		}

		return UpdateResult.withEffect(model.withFilteredBySelection(true),
				new SearchPaneEffect.ServiceEffect.MarkAllAndFilter());
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> onResetSearch(SearchPaneModel model) {
		if (!model.isFilteredBySelection()) {
			return UpdateResult.noEffect(model);
		}

		return UpdateResult.withEffect(model.withFilteredBySelection(false),
				new SearchPaneEffect.ServiceEffect.ResetSearch());
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> onRestoreFilter(SearchPaneModel model) {
		if (model.getFilterKey() == null) {
			return UpdateResult.noEffect(model);
		}

		TableFilterState filterState = FilterStateManager.getFilterState(model.getFilterKey());
		return filterState != null ? UpdateResult.withEffect(
				model.toBuilder().searchText(filterState.getSearchText())
						.searchColumnIndex(filterState.getSearchColumnIndex())
						.isRegexActive(filterState.isRegexActive()).isRespectCase(filterState.isRespectCase()).build(),
				new SearchPaneEffect.ServiceEffect.ApplyFilter(filterState.getSearchText(),
						filterState.getSearchColumnIndex(), filterState.isRegexActive(), filterState.isRespectCase()))
				: UpdateResult.noEffect(model);
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> handleEffectResult(SearchPaneMsg.EffectResultMsg msg,
			SearchPaneModel model) {
		return switch (msg) {
		case SearchPaneMsg.EffectResultMsg.SearchCompleted(int row) -> UpdateResult.noEffect(model.withFoundRow(row));
		};
	}
}
