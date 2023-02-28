package de.uib.configed.clientselection.elements;

import de.uib.configed.Configed;

public class GroupWithSubgroupsElement
		// extends GroupElement
		extends GenericEnumElement

{

	public GroupWithSubgroupsElement(String[] enumData) {
		super(enumData, new String[] { "GroupWithSubgroups" },
				/* "GroupWithSubgroups" */Configed.getResourceValue("ClientSelectionDialog.groupWithSubgroups"));
	}
}
