package de.uib.configed.clientselection.elements;

import de.uib.configed.configed;

public class GroupWithSubgroupsElement
		// extends GroupElement
		extends GenericEnumElement

{
	private String[] enumData;

	public GroupWithSubgroupsElement(String[] enumData) {
		super(enumData, new String[] { "GroupWithSubgroups" },
				/* "GroupWithSubgroups" */configed.getResourceValue("ClientSelectionDialog.groupWithSubgroups"));
	}
}