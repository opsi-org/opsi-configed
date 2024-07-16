/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.util.List;

import de.uib.utils.logging.Logging;

public class TableModelFilter {
	private TableModelFilterCondition condition;
	protected boolean inverted;
	protected boolean inUse;

	public TableModelFilter(TableModelFilterCondition condition) {
		this(condition, false, true);
	}

	public TableModelFilter(TableModelFilterCondition condition, boolean inverted, boolean used) {
		this.condition = condition;
		this.inverted = inverted;
		this.inUse = used;

		Logging.info(this.getClass(), "TableModelFilter constructed : ", this);
	}

	public TableModelFilter() {
		this(null, false, true);
	}

	public TableModelFilterCondition getCondition() {
		return condition;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean b) {
		inUse = b;
	}

	public boolean test(List<Object> row) {
		if (!inUse || condition == null) {
			return true;
		}

		boolean testresult = condition.test(row);

		return inverted ? !testresult : testresult;
	}

	@Override
	public String toString() {
		return getClass().getName() + " in use " + inUse + ", inverted " + inverted + " condition " + condition;
	}
}
