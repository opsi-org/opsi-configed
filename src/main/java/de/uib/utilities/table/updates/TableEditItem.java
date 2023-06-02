/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.updates;

public class TableEditItem {
	protected Object source;
	protected int keyCol = -1;

	public Object getSource() {
		return source;
	}

	public void setSource(Object o) {
		source = o;
	}

	public boolean keyChanged() {
		return true;
	}
}
