package de.uib.configed.type;

import java.util.ArrayList;

import de.uib.utilities.datastructure.Relation;

public class HostGroupRelation extends Relation {
	public HostGroupRelation() {
		super(new ArrayList<>());
		attributes.add("groupId");
		attributes.add("description");
		attributes.add("notes");
		attributes.add("parentGroupId");
	}
}
