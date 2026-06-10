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

final class TableSearchPaneUpdate {

	private TableSearchPaneUpdate() {
	}

	static UpdateResult<SearchPaneModel, SearchPaneEffect> update(SearchPaneMsg msg, SearchPaneModel model) {
		return switch (msg) {
		case SearchPaneMsg.FieldChangeMsg m -> handleFieldChange(m, model);
		case SearchPaneMsg.ActionMsg m -> handleAction(m, model);
		case SearchPaneMsg.EffectResultMsg m -> handleEffectResult(m, model);
		};
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> handleFieldChange(SearchPaneMsg.FieldChangeMsg msg,
			SearchPaneModel model) {
		return switch (msg) {
		case SearchPaneMsg.FieldChangeMsg.ChangeSearchText(String text) -> UpdateResult
				.withEffect(model.withSearchText(text), new SearchPaneEffect.ServiceEffect.ApplyFilter(text,
						model.getSearchColumnIndex(), model.isRegexActive(), model.isRespectCase()));
		case SearchPaneMsg.FieldChangeMsg.ChangeSearchColumn(int index) -> UpdateResult
				.noEffect(model.withSearchColumnIndex(index));
		case SearchPaneMsg.FieldChangeMsg.ChangeFilterKey(FilterKey filterKey) -> UpdateResult
				.noEffect(model.toBuilder().filterKey(filterKey).showFilterMark(filterKey != null).build());
		case SearchPaneMsg.FieldChangeMsg.ChangeSearchColumns(List<Integer> searchColumns) -> UpdateResult
				.noEffect(model.withSearchColumns(searchColumns));
		case SearchPaneMsg.FieldChangeMsg.ChangeShowNavPanel(boolean value) -> UpdateResult
				.noEffect(model.withShowNavPanel(value));
		case SearchPaneMsg.FieldChangeMsg.ChangeShowFilterMark(boolean value) -> UpdateResult
				.noEffect(model.withShowFilterMark(value));
		case SearchPaneMsg.FieldChangeMsg.ToggleRespectCase(boolean val) -> UpdateResult
				.noEffect(model.withRespectCase(val));
		case SearchPaneMsg.FieldChangeMsg.ToggleRegex(boolean val) -> UpdateResult.noEffect(model.withRegexActive(val));
		case SearchPaneMsg.FieldChangeMsg.ToggleFilterMark(boolean val) -> UpdateResult
				.withEffect(model.withFiltered(val), new SearchPaneEffect.ServiceEffect.MarkAllAndFilter(val));
		};
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> handleAction(SearchPaneMsg.ActionMsg msg,
			SearchPaneModel model) {
		return switch (msg) {
		case SearchPaneMsg.ActionMsg.SearchNext() -> UpdateResult.withEffect(model,
				new SearchPaneEffect.ServiceEffect.SearchNextRow());
		case SearchPaneMsg.ActionMsg.MarkAllAndFilter() -> UpdateResult.withEffect(model,
				new SearchPaneEffect.ServiceEffect.MarkAllAndFilter(model.isFiltered()));
		case SearchPaneMsg.ActionMsg.NavigateToRow(int row) -> {
			row = row < -1 ? -1 : row;
			yield UpdateResult.withEffect(model.withFoundRow(row), new SearchPaneEffect.UIEffect.NavigateToRow(row));
		}
		case SearchPaneMsg.ActionMsg.RestoreFilter() -> onRestoreFilter(model);
		};
	}

	private static UpdateResult<SearchPaneModel, SearchPaneEffect> onRestoreFilter(SearchPaneModel model) {
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
		case SearchPaneMsg.EffectResultMsg.SearchNotFound() -> UpdateResult.noEffect(model.withFoundRow(-1));
		};
	}
}
