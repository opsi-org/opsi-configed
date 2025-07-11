/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.clientselection.operations;

import de.uib.configed.gui.clientselection.AbstractSelectElement;
import de.uib.configed.gui.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.clientselection.SelectData;

public class BigIntLessOrEqualOperation extends AbstractSelectOperation {
	public BigIntLessOrEqualOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.BIG_INTEGER_TYPE;
	}

	@Override
	public String getOperationString() {
		return "<=";
	}
}
