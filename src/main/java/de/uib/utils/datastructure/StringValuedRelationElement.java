/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.datastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.utils.logging.Logging;

//very similar to TableEntry
public class StringValuedRelationElement extends HashMap<String, String> {
	public static final String NULLDATE = "0000-00-00";

	private List<String> allowedAttributes;

	public StringValuedRelationElement() {
		super();
	}

	@Override
	public String get(Object key) {
		if (allowedAttributes != null && allowedAttributes.indexOf(key) < 0) {
			Logging.error(this, "key " + key + " not allowed");
			return null;
		} else {
			return super.get(key);
		}
	}

	public void setAllowedAttributes(List<String> allowedAttributes) {
		this.allowedAttributes = allowedAttributes;
	}

	protected void produceFrom(Map<String, ? extends Object> map) {
		for (String attribute : allowedAttributes) {
			if (map.get(attribute) != null && !"null".equals(map.get(attribute))) {
				if (map.get(attribute) instanceof String) {
					put(attribute, (String) map.get(attribute));
				} else {
					// create String object by toString() method
					put(attribute, "" + map.get(attribute));
				}

				if (get(attribute).startsWith(NULLDATE)) {
					put(attribute, "");
				}
			} else {
				put(attribute, null);
			}
		}
	}
}
