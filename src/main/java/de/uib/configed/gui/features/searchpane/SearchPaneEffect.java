/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

public sealed interface SearchPaneEffect permits SearchPaneEffect.UIEffect, SearchPaneEffect.ServiceEffect {
	/**
	 * UI-side effects (dialogs, navigation, state persistence).
	 */
	sealed interface UIEffect extends SearchPaneEffect {
		record NavigateToRow(int row) implements UIEffect {
		}
	}

	/**
	 * Service-side effects (interacting with SearchTargetModel).
	 */
	sealed interface ServiceEffect extends SearchPaneEffect {
		record ApplyFilter(String query, int col, boolean regex, boolean caseSensitive) implements ServiceEffect {
		}

		record SearchNextRow() implements ServiceEffect {
		}

		record MarkAllAndFilter(boolean value) implements ServiceEffect {
		}
	}
}
