/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.gui.Configed;

public class GroupWithSubgroupsElement extends GenericEnumElement {
	public GroupWithSubgroupsElement(String[] enumData) {
		super(enumData, new String[] { "GroupWithSubgroups" },
				/* "GroupWithSubgroups" */Configed.getResourceValue("ClientSelectionDialog.groupWithSubgroups"));
	}
}
