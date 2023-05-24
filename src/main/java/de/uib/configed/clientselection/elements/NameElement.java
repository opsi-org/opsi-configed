/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.elements;

import java.util.LinkedList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.operations.StringEqualsOperation;

public class NameElement extends AbstractSelectElement {

	public NameElement(String displayLabel) {
		super(new String[] { "Name" }, displayLabel);
	}

	public NameElement() {
		super(new String[] { "Name" }, Configed.getResourceValue("PanelSWInfo.tableheader_displayName"));
	}

	@Override
	public List<AbstractSelectOperation> supportedOperations() {
		List<AbstractSelectOperation> result = new LinkedList<>();
		result.add(new StringEqualsOperation(this));
		return result;
	}
}
