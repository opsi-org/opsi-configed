/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntEqualsOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntGreaterOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntGreaterThanOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntLessOrEqualOperation;
import de.uib.configed.gui.features.clientselection.operations.BigIntLessThanOperation;

public class GenericBigIntegerElement extends AbstractSelectElement {
	public GenericBigIntegerElement(String[] name, String... localizedName) {
		super(name, localizedName);
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new LinkedList<>();
		result.add(new BigIntLessThanOperation(this));
		result.add(new BigIntLessOrEqualOperation(this));
		result.add(new BigIntGreaterThanOperation(this));
		result.add(new BigIntGreaterOrEqualOperation(this));
		result.add(new BigIntEqualsOperation(this));
		return result;
	}
}
