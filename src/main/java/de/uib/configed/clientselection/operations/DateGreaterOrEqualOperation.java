package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;

public class DateGreaterOrEqualOperation extends SelectOperation {
	public DateGreaterOrEqualOperation(SelectElement element) {
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