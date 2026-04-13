/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.operations;

import de.uib.configed.gui.features.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;

public class SoftwareOperation extends AbstractSelectGroupOperation {
	public SoftwareOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}
}
