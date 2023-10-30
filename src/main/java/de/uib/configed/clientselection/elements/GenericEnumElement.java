/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class GenericEnumElement extends AbstractSelectElement {
	private List<String> enumData;

	public GenericEnumElement(String[] enumData, String[] name, String... localizedName) {
		super(name, localizedName);
		this.enumData = new ArrayList<>(Arrays.asList(enumData));
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new LinkedList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}

	@Override
	public List<String> getEnumData() {
		return enumData;
	}

	protected static String[] removeFirst(int n, String[] data) {
		return Arrays.copyOfRange(data, n, data.length);
	}
}
