/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.tabbedpane;

import de.uib.configed.ConfigedMain.LicencesTabStatus;

public interface TabController {
	LicencesTabStatus getStartTabState();

	LicencesTabStatus reactToStateChangeRequest(LicencesTabStatus newState);

	void addClient(LicencesTabStatus state, TabClient client);

	TabClient getClient(LicencesTabStatus state);
}
