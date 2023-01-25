package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;

public class IntLessThanOperation extends SelectOperation {
	public IntLessThanOperation(SelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.IntegerType;
	}

	@Override
	public String getOperationString() {
		return "<";
	}
}