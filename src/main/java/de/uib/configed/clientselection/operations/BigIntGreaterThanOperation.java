package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;

public class BigIntGreaterThanOperation extends SelectOperation {
	public BigIntGreaterThanOperation(SelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.BIT_INTEGER_TYPE;
	}

	@Override
	public String getOperationString() {
		return ">";
	}
}