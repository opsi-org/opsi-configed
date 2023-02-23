package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;

public class IntLessThanOperation extends AbstractSelectOperation {
	public IntLessThanOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.INTEGER_TYPE;
	}

	@Override
	public String getOperationString() {
		return "<";
	}
}