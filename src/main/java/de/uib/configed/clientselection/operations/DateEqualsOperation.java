package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class DateEqualsOperation extends AbstractSelectOperation {
	public DateEqualsOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.DateType;
	}

	@Override
	public String getOperationString() {
		return "=";
	}
}