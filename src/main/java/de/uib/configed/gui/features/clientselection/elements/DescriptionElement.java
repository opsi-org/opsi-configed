/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.operations.StringEqualsOperation;

public class DescriptionElement extends AbstractSelectElement {
	public DescriptionElement() {
		super(new String[] { "Description" }, Configed.getResourceValue("description"));
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new LinkedList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}
