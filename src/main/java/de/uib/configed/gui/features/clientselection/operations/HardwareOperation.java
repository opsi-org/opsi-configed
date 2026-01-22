/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.operations;

import de.uib.configed.gui.features.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;

public class HardwareOperation extends AbstractSelectGroupOperation {
	public HardwareOperation(AbstractSelectOperation operation) {
		registerChildOperation(operation);
	}
}
