/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messages;

import de.uib.utilities.logging.Logging;

// a String separated by '='
public class LocaleRepresentation {
	private String value = "";

	public LocaleRepresentation(String name) {
		if (name == null) {
			Logging.error(this, "name must not be null");
		}

		value = name;
	}

	public String getName() {
		int pos = value.indexOf('=');
		if (pos > -1) {
			return value.substring(0, pos);
		}

		return value;
	}

	public String getIconName() {
		int pos = value.indexOf('=');
		if (pos > -1) {
			return value.substring(pos + 1);
		}

		return "";
	}

	@Override
	public String toString() {
		return value;
	}
}
