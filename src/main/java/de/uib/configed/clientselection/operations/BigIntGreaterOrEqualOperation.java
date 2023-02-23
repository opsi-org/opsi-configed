package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.SelectData;

public class BigIntGreaterOrEqualOperation extends AbstractSelectOperation {
	public BigIntGreaterOrEqualOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.BIG_INTEGER_TYPE;
	}

	@Override
	public String getOperationString() {
		return ">=";
	}
}