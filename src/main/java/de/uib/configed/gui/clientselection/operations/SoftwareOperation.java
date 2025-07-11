/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.clientselection.operations;

import de.uib.configed.gui.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.gui.clientselection.AbstractSelectOperation;

public class SoftwareOperation extends AbstractSelectGroupOperation {
	public SoftwareOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}
}
