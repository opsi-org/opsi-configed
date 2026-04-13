/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.elements;

import de.uib.configed.gui.Configed;

public class GroupElement extends GenericEnumElement {
	public GroupElement(String[] enumData) {
		super(enumData, new String[] { "Group" },
				/* "Group" */Configed.getResourceValue("ClientSelectionDialog.group"));
	}

	public GroupElement(String[] enumData, String[] name, String... localizedName) {
		super(enumData, name, localizedName);
	}
}
