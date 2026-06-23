/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.List;

public sealed interface GenericTableViewEffect permits GenericTableViewEffect.SaveChanges,
		GenericTableViewEffect.Reload, GenericTableViewEffect.Selection, GenericTableViewEffect.StoreVisibleColulmns {
	record SaveChanges(List<RowData> newRows) implements GenericTableViewEffect {
	}

	record Selection() implements GenericTableViewEffect {
	}

	record Reload() implements GenericTableViewEffect {
	}

	record StoreVisibleColulmns(List<String> visibleColumns) implements GenericTableViewEffect {
	}
}
