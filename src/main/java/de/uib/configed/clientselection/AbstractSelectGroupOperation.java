/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the base class for all operations operating on a group of operations.
 */
public abstract class AbstractSelectGroupOperation extends AbstractSelectOperation {
	private List<AbstractSelectOperation> childOperations;

	protected AbstractSelectGroupOperation() {
		super(null);
		childOperations = new LinkedList<>();
	}

	/** Register an operation as child of this operation. */
	public final void registerChildOperation(AbstractSelectOperation operation) {
		childOperations.add(operation);
	}

	/** Get the registered children. */
	public List<AbstractSelectOperation> getChildOperations() {
		return childOperations;
	}

	@Override
	public SelectData.DataType getDataType() {
		return SelectData.DataType.NONE_TYPE;
	}

	@Override
	public String getOperationString() {
		return "";
	}

	@Override
	public String printOperation(String indent) {
		StringBuilder result = new StringBuilder(indent + getClassName() + " {\n");
		for (AbstractSelectOperation op : childOperations) {
			result.append(op.printOperation(indent + "\t") + "\n");
		}

		return result + indent + "}";
	}
}
