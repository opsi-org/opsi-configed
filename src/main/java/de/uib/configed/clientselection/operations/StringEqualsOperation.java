package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.SelectData;

public class StringEqualsOperation extends AbstractSelectOperation {
	public StringEqualsOperation(AbstractSelectElement element) {
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