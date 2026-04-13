/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.gui.Configed;

public class ConnectionElement extends GenericEnumElement {
	public ConnectionElement() {
		super(new String[] { Configed.getResourceValue("connected"), Configed.getResourceValue("notConnected") },
				new String[] { "Connection" }, Configed.getResourceValue("connection"));
	}
}
