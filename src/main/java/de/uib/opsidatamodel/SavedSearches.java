/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel;

import java.util.HashMap;

import de.uib.configed.type.SavedSearch;
import de.uib.utils.logging.Logging;

public class SavedSearches extends HashMap<String, SavedSearch> {
	@SuppressWarnings({ "java:S103" })
	public static final String SEARCH_FAILED_PRODUCT = "{ \"version\" : \"2\", \"data\" : { \"element\" : null, \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : null, \"elementPath\" : null, \"operation\" : \"AndOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : \"SoftwareNameElement\", \"elementPath\" : [ \"Product\", \"Name\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"%s\", \"children\" : null }, { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] } ] } }";
	@SuppressWarnings({ "java:S103" })
	public static final String SEARCH_FAILED_BY_TIMES = "{ \"version\" : \"2\", \"data\" : { \"element\" : null, \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : null, \"elementPath\" : null, \"operation\" : \"AndOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null }, { \"element\" : \"SoftwareModificationTimeElement\", \"elementPath\" : [ \"Product\", \"Modification Time\" ], \"operation\" : \"DateGreaterOrEqualOperation\", \"dataType\" : DateType, \"data\" : \"%s\", \"children\" : null } ] } ] } }";
	@SuppressWarnings({ "java:S103" })
	public static final String SEARCH_FAILED_AT_ANY_TIME = "{ \"version\" : \"2\", \"data\" : { \"element\" : null, \"elementPath\" : null, \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] } }";

	public void checkIn(String key, String value) {
		if (!key.startsWith(SavedSearch.CONFIG_KEY)) {
			return;
		}

		String rcPartOfKey = key.substring(SavedSearch.CONFIG_KEY.length());

		if (rcPartOfKey.length() < 2 || rcPartOfKey.charAt(0) != '.') {
			Logging.error("No name key given after '" + SavedSearch.CONFIG_KEY + "'");
		} else {
			rcPartOfKey = rcPartOfKey.substring(1);

			int i = nextPartAt(rcPartOfKey);

			// first level key

			if (i == -1) {
				SavedSearch rc = retrieveRC(rcPartOfKey);
				if (rc.getSerialization().isEmpty()) {
					rc.setSerialization(value);
				}

				// if serialized command is specified by an explicit command key, leave it
			} else {
				// second level key

				String name = rcPartOfKey.substring(0, i);

				SavedSearch rc = retrieveRC(name);

				String remainder = rcPartOfKey.substring(i + 1);

				i = nextPartAt(remainder);

				if (i != -1) {
					// there are no 3rd level keys
					Logging.error("Remote control key has too many parts");
				} else if (remainder.equals(SavedSearch.DESCRIPTION_KEY)) {
					rc.setDescription(value);
				} else {
					Logging.warning(this, "Unexpected remainer " + remainder);
				}
			}
		}
	}

	private static int nextPartAt(String remainder) {
		int posDot = remainder.indexOf(".");
		if (posDot == -1 || remainder.length() == posDot + 1) {
			return -1;
		} else {
			return posDot;
		}
	}

	private SavedSearch retrieveRC(String name) {
		if (get(name) != null) {
			return get(name);
		} else {
			SavedSearch rc = new SavedSearch();
			rc.setName(name);
			put(name, rc);
			return rc;
		}
	}
}
