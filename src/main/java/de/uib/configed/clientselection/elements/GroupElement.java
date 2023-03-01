package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class GroupElement extends GenericEnumElement {

	public GroupElement(String[] enumData) {
		super(enumData, new String[] { "Group" },
				/* "Group" */Configed.getResourceValue("ClientSelectionDialog.group"));
	}

	public GroupElement(String[] enumData, String[] name, String... localizedName) {
		super(enumData, name, localizedName);
	}

}
