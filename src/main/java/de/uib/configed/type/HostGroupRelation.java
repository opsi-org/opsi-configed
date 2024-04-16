/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.ArrayList;

import de.uib.utils.datastructure.Relation;

public class HostGroupRelation extends Relation {
	public HostGroupRelation() {
		super(new ArrayList<>());
		attributes.add("groupId");
		attributes.add("description");
		attributes.add("notes");
		attributes.add("parentGroupId");
	}
}
