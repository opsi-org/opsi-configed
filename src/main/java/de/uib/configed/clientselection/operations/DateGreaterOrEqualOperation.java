/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.SelectData;

public class DateGreaterOrEqualOperation extends AbstractSelectOperation {
	public DateGreaterOrEqualOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.DATE_TYPE;
	}

	@Override
	public String getOperationString() {
		return ">=";
	}
}
