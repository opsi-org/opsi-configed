/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/* 
 * RowMap is the map columnname -> value for some table row 
 *
 * 	uib, www.uib.de, 2012
 * 
 *	author Rupert Röder
 *
 */

package de.uib.utilities.table;

import java.util.Locale;

public class RowMap<K, V> extends java.util.HashMap<K, V> {
	@Override
	public V get(Object key) {
		V result = super.get(key);

		if (!(key instanceof String)) {
			return result;
		}

		if (result == null) {
			result = super.get(((String) key).toUpperCase(Locale.ROOT));
		}

		if (result == null) {
			result = super.get(((String) key).toLowerCase(Locale.ROOT));
		}

		return result;
	}
}
