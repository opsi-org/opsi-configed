package de.uib.configed.clientselection;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the base class for all operations operating on a group of operations.
 */
public abstract class SelectGroupOperation extends SelectOperation {
	private List<SelectOperation> childOperations;

	protected SelectGroupOperation() {
		super(null);
		childOperations = new LinkedList<>();
	}

	/** Register an operation as child of this operation. */
	public final void registerChildOperation(SelectOperation operation) {
		childOperations.add(operation);
	}

	/** Get the registered children. */
	public List<SelectOperation> getChildOperations() {
		return childOperations;
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.NoneType;
	}

	@Override
	public String getOperationString() {
		return "";
	}

	@Override
	public String printOperation(String indent) {
		StringBuilder result = new StringBuilder(indent + getClassName() + " {\n");
		for (SelectOperation op : childOperations)
			result.append(op.printOperation(indent + "\t") + "\n");
		return result + indent + "}";
	}
}