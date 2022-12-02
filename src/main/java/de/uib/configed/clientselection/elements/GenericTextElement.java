package de.uib.configed.clientselection.elements;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class GenericTextElement extends SelectElement {

	protected Vector<String> proposedData;

	public GenericTextElement(Set<String> proposedData, String[] name, String... localizedName) {
		super(name, localizedName);
		this.proposedData = new Vector<String>(proposedData);
	}

	public GenericTextElement(String[] proposedData, String[] name, String... localizedName) {
		super(name, localizedName);
		this.proposedData = new Vector<String>(Arrays.asList((String[]) proposedData));
	}

	public GenericTextElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		result.add(new StringEqualsOperation(this));
		return result;
	}

	public SelectData.DataType dataNeeded(String operation) {
		if (operation.equals("="))
			return SelectData.DataType.TextType;
		throw new IllegalArgumentException(operation + " is no valid operation.");
	}

	@Override
	public Vector<String> getEnumData() {
		return proposedData;
	}

	@Override
	public boolean hasEnumData() {
		return true;
	}
}