/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

public interface HostsStatusInfo {
	void setGroupName(String s);

	void setGroupClientsCount(int n);

	void updateValues(Integer clientsCount, Integer selectedClientsCount, String selectedClientNames,
			String involvedDepots);

	String getSelectedClientNames();

	String getInvolvedDepots();

	String getGroupName();
}
