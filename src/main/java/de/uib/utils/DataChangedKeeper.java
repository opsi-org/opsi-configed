/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.util.function.Consumer;

import de.uib.utils.logging.Logging;

public class DataChangedKeeper implements DataChangedObserver {
	protected boolean dataChanged;

	private Consumer<Object> actUpon;

	@Override
	public void dataHaveChanged(Object source) {
		Logging.debug(this, "dataHaveChanged " + source);
		dataChanged = true;

		if (actUpon != null) {
			actUpon.accept(source);
		}
	}

	public boolean isDataChanged() {
		return dataChanged;
	}
}
