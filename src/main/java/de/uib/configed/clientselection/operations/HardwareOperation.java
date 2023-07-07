/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.utilities.logging.Logging;

public class HardwareOperation extends AbstractSelectGroupOperation {
	public HardwareOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}

	public HardwareOperation(List<AbstractSelectOperation> operations) {
		Logging.info(this.getClass(), "created, with operations " + operations);
		registerChildOperation(operations.get(0));
	}
}
