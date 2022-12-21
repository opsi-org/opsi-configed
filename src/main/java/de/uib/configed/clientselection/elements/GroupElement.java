package de.uib.configed.clientselection.elements;

import de.uib.configed.configed;

public class GroupElement extends GenericEnumElement {
	private String[] enumData;

	public GroupElement(String[] enumData) {
		super(enumData, new String[] { "Group" },
				/* "Group" */configed.getResourceValue("ClientSelectionDialog.group"));
	}

	public GroupElement(String[] enumData, String[] name, String... localizedName) {
		super(enumData, name, localizedName);
	}

}