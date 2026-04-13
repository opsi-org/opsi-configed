/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.operations.StringEqualsOperation;

public class IPElement extends AbstractSelectElement {
	public IPElement() {
		super(new String[] { "IP Address" }, Configed.getResourceValue("ipAddress"));
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new ArrayList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}
