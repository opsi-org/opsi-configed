/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.groupaction;

import java.util.Set;

import de.uib.configed.gui.Configed;
import de.uib.configed.share.logging.Logging;

public class ActivatedGroupModel {
	private String groupName;
	private String groupDescription;

	private Set<String> associatedClients;

	private boolean active;

	public void setActive(boolean b) {
		Logging.info(this, "setActive ", b);

		active = b;

		if (b && groupName != null) {
			Configed.getSavedStates().setProperty("groupname", groupName);
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setNode(String name) {
		Logging.info(this, "setNode ", name);
		groupName = name;
	}

	public void setDescription(String s) {
		groupDescription = s;
	}

	public void setAssociatedClients(Set<String> clients) {
		associatedClients = clients;
	}

	public Set<String> getAssociatedClients() {
		return associatedClients;
	}

	public int getNumberOfClients() {
		if (associatedClients == null) {
			return 0;
		}

		return associatedClients.size();
	}

	public String getGroupName() {
		return groupName;
	}

	public String getLabel() {
		if (groupName != null && groupDescription != null && !groupDescription.equals(groupName)
				&& !groupDescription.isEmpty()) {
			return groupName + "  (" + groupDescription + ") ";
		}

		return "" + groupName;
	}
}
