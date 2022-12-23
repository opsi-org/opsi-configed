package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;

public class StringEqualsOperation extends SelectOperation {
	public StringEqualsOperation(SelectElement element) {
		super(element);
		
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.TextType;
	}

	@Override
	public String getOperationString() {
		return "=";
	}
}