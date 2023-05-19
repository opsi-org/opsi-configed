/* 
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2013 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

package de.uib.configed.groupaction;

import java.util.Set;

import de.uib.configed.Configed;
import de.uib.configed.HostsStatusInfo;
import de.uib.utilities.logging.Logging;

public class ActivatedGroupModel {
	private String groupName;
	private String groupDescription;

	private Set<String> associatedClients;

	private boolean active;

	private HostsStatusInfo hostsInfo;

	public ActivatedGroupModel(HostsStatusInfo hostsInfo) {
		this.hostsInfo = hostsInfo;
	}

	public void setActive(boolean b) {
		Logging.info(this, "setActive " + b);

		active = b;

		if (b && groupName != null) {
			hostsInfo.setGroupName(groupName);
			hostsInfo.setGroupClientsCount(associatedClients.size());
			Configed.savedStates.setProperty("groupname", groupName);
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setNode(String name) {
		Logging.info(this, "setNode " + name);
		groupName = name;
		hostsInfo.setGroupName(name);
	}

	public void setDescription(String s) {
		groupDescription = s;
	}

	public void setAssociatedClients(Set<String> clients) {
		associatedClients = clients;
		hostsInfo.setGroupClientsCount(clients.size());
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
		return "" + groupName;
	}

	public String getLabel() {
		if (groupName != null && groupDescription != null && !groupDescription.equals(groupName)
				&& !groupDescription.isEmpty()) {
			return groupName + "  (" + groupDescription + ") ";
		}

		return "" + groupName;
	}

	public String getGroupDescription() {
		return "" + groupDescription;
	}

}
