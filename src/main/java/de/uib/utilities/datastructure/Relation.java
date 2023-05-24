/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Relation extends ArrayList<StringValuedRelationElement> {
	protected final List<String> attributes;

	private Map<String, Map<String, Relation>> functionByAttribute;

	public Relation(List<String> attributes) {
		super();
		this.attributes = attributes;

		functionByAttribute = new HashMap<>();
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public StringValuedRelationElement integrateRaw(Map<String, Object> map) {
		StringValuedRelationElement rowmap = new StringValuedRelationElement(attributes, map);
		add(rowmap);

		return rowmap;
	}

	// for each attribute:

	// of all entries which have this value
	public Map<String, Relation> getFunctionBy(String attribute) {
		Map<String, Relation> function = functionByAttribute.get(attribute);

		if (function != null) {
			return function;
		}

		function = new HashMap<>();
		functionByAttribute.put(attribute, function);

		for (StringValuedRelationElement element : this) {
			String valueTakenAsKey = element.get(attribute);
			Relation valueList = function.computeIfAbsent(valueTakenAsKey, arg -> new Relation(attributes));

			valueList.add(element);
		}

		return function;
	}
}
