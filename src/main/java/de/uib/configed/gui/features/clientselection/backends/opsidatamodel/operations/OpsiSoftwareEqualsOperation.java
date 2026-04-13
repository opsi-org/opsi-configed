/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.share.logging.Logging;

public class OpsiSoftwareEqualsOperation extends OpsiDataStringEqualsOperation {
	public OpsiSoftwareEqualsOperation(String key, String data, AbstractSelectElement element) {
		super(OpsiDataClient.SOFTWARE_MAP, key, data, element);
		Logging.debug(this, "created  for key, data, element ", key, ", ", data, ", ", element);
	}
}
