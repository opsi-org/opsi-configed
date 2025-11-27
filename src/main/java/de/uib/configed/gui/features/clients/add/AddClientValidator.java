/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.regex.Pattern;

final class AddClientValidator {
	static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");

	private AddClientValidator() {
	}

	static boolean isBoolean(String bool) {
		return bool.isEmpty() || bool.equalsIgnoreCase(Boolean.TRUE.toString())
				|| bool.equalsIgnoreCase(Boolean.FALSE.toString());
	}

	static boolean areValuesValid(String hostname, String selectedDomain) {
		if (hostname == null || hostname.isEmpty()) {
			return false;
		}
		return selectedDomain != null && !selectedDomain.isEmpty();
	}

	static boolean requiresNetbiosConfirmation(String hostname) {
		return hostname.length() > 15 || NUMERIC_PATTERN.matcher(hostname).matches();
	}
}
