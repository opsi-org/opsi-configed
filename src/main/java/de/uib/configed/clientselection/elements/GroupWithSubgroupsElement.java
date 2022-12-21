package de.uib.configed.clientselection.elements;

import de.uib.configed.configed;

public class GroupWithSubgroupsElement
		// extends GroupElement
		extends GenericEnumElement

{
	public GroupWithSubgroupsElement(String[] enumData) {
		super(enumData, new String[] { "GroupWithSubgroups" },
				/* "GroupWithSubgroups" */configed.getResourceValue("ClientSelectionDialog.groupWithSubgroups"));
	}
}