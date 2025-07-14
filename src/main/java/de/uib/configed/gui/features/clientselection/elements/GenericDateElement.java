/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.operations.DateEqualsOperation;
import de.uib.configed.gui.features.clientselection.operations.DateGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.DateGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.operations.DateLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.DateLessThanOperation;

public class GenericDateElement extends AbstractSelectElement {
	public GenericDateElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new LinkedList<>();

		result.add(new DateGreaterThanOperation(this));
		result.add(new DateGreaterOrEqualOperation(this));
		result.add(new DateEqualsOperation(this));
		result.add(new DateLessOrEqualOperation(this));
		result.add(new DateLessThanOperation(this));

		return result;
	}
}
