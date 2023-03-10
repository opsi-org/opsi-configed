package de.uib.configed.clientselection.operations;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.SelectData;

public class IntLessOrEqualOperation extends AbstractSelectOperation {
	public IntLessOrEqualOperation(AbstractSelectElement element) {
		super(element);
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.IntegerType;
	}

	@Override
	public String getOperationString() {
		return "<=";
	}
}