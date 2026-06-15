/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import java.util.List;

import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;

public sealed interface SearchPaneMsg
		permits SearchPaneMsg.FieldChangeMsg, SearchPaneMsg.ActionMsg, SearchPaneMsg.EffectResultMsg {

	/**
	 * User interactions with UI controls.
	 */
	sealed interface FieldChangeMsg extends SearchPaneMsg {
		record ChangeSearchText(String text) implements FieldChangeMsg {
		}

		record ChangeSearchColumn(int columnIndex) implements FieldChangeMsg {
		}

		record ChangeFilterKey(FilterKey filterKey) implements FieldChangeMsg {
		}

		record ChangeSearchColumns(List<Integer> searchColumns) implements FieldChangeMsg {
		}

		record ChangeShowNavPanel(boolean value) implements FieldChangeMsg {
		}

		record ChangeShowFilterMark(boolean value) implements FieldChangeMsg {
		}

		record ToggleRespectCase(boolean value) implements FieldChangeMsg {
		}

		record ToggleRegex(boolean value) implements FieldChangeMsg {
		}

		record ToggleFilterMark(boolean value) implements FieldChangeMsg {
		}
	}

	/**
	 * Actions triggered by buttons, shortcuts, or system events.
	 */
	sealed interface ActionMsg extends SearchPaneMsg {
		record SearchNext() implements ActionMsg {
		}

		record MarkAllAndFilter() implements ActionMsg {
		}

		record NavigateToRow(int row) implements ActionMsg {
		}

		record RestoreFilter() implements ActionMsg {
		}

		record SelectAll() implements ActionMsg {
		}

		record TriggerFilterMark() implements ActionMsg {
		}
	}

	/**
	 * Results returned from Effects to update the model.
	 */
	sealed interface EffectResultMsg extends SearchPaneMsg {
		record SearchCompleted(int foundRow) implements EffectResultMsg {
		}

		record SearchNotFound() implements EffectResultMsg {
		}
	}
}
