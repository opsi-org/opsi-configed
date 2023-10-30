/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.datastructure;

import java.util.ArrayList;
import java.util.List;

public class Relation extends ArrayList<StringValuedRelationElement> {
	protected final List<String> attributes;

	public Relation(List<String> attributes) {
		super();
		this.attributes = attributes;
	}

	public List<String> getAttributes() {
		return attributes;
	}
}
