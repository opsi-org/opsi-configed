package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.SelectData;

public class IntGreaterThanOperation extends AbstractSelectOperation {
	public IntGreaterThanOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.INTEGER_TYPE;
	}

	@Override
	public String getOperationString() {
		return ">";
	}
}
