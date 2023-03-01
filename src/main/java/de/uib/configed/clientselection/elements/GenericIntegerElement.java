package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.operations.IntEqualsOperation;
import de.uib.configed.clientselection.operations.IntGreaterOrEqualOperation;
import de.uib.configed.clientselection.operations.IntGreaterThanOperation;
import de.uib.configed.clientselection.operations.IntLessOrEqualOperation;
import de.uib.configed.clientselection.operations.IntLessThanOperation;

public class GenericIntegerElement extends AbstractSelectElement {
	public GenericIntegerElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new LinkedList<>();
		result.add(new IntLessThanOperation(this));
		result.add(new IntLessOrEqualOperation(this));
		result.add(new IntEqualsOperation(this));
		result.add(new IntGreaterOrEqualOperation(this));
		result.add(new IntGreaterThanOperation(this));

		return result;
	}
}
