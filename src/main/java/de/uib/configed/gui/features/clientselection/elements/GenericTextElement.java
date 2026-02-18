/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.SelectData;
import de.uib.configed.gui.features.clientselection.operations.StringEqualsOperation;

public class GenericTextElement extends AbstractSelectElement {
	private List<String> proposedData;

	public GenericTextElement(Set<String> proposedData, String[] name, String... localizedName) {
		super(name, localizedName);
		this.proposedData = new ArrayList<>(proposedData);
	}

	public GenericTextElement(String[] proposedData, String[] name, String... localizedName) {
		super(name, localizedName);
		this.proposedData = Arrays.asList(proposedData);
	}

	public GenericTextElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new ArrayList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}

	public SelectData.DataType dataNeeded(String operation) {
		if ("=".equals(operation)) {
			return SelectData.DataType.TEXT_TYPE;
		}

		throw new IllegalArgumentException(operation + " is no valid operation.");
	}

	@Override
	public List<String> getEnumData() {
		return proposedData;
	}
}
