package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class IntGreaterOrEqualOperation extends AbstractSelectOperation {
	public IntGreaterOrEqualOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.IntegerType;
	}

	@Override
	public String getOperationString() {
		return ">=";
	}
}