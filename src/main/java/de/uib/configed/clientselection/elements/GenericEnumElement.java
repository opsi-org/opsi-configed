package de.uib.configed.clientselection.elements;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import de.uib.configed.clientselection.SelectElement;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class GenericEnumElement extends SelectElement {
	protected Vector<String> enumData;

	public GenericEnumElement(String[] enumData, String[] name, String... localizedName) {
		super(name, localizedName);
		this.enumData = new Vector<>(Arrays.asList((String[]) enumData));
		
	}

	@Override
	public List<SelectOperation> supportedOperations() {
		List<SelectOperation> result = new LinkedList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}

	@Override
	public Vector<String> getEnumData() {
		return enumData;
	}

	@Override
	public boolean hasEnumData() {
		return true;
	}

	protected static String[] removeFirst(int n, String[] data) {
		return Arrays.copyOfRange(data, n, data.length);
	}
}