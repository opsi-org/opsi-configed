package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;

public class BigIntGreaterOrEqualOperation extends SelectOperation {
	public BigIntGreaterOrEqualOperation(SelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.BIT_INTEGER_TYPE;
	}

	@Override
	public String getOperationString() {
		return ">=";
	}
}