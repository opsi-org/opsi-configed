/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.util.List;
import java.util.Map;

public interface AbstractExecutioner {
	ConnectionState getConnectionState();

	boolean doCall(OpsiMethodCall omc);

	String getErrorFromResponse(Map<String, Object> retrieved);

	Map<String, Object> getResponses(Map<String, Object> retrieved);

	Map<String, Object> retrieveResponse(OpsiMethodCall omc);

	List<Object> getListResult(OpsiMethodCall omc);

	List<String> getStringListResult(OpsiMethodCall omc);

	List<List<String>> getListOfStringLists(OpsiMethodCall omc);

	Map<String, Object> getMapResult(OpsiMethodCall omc);

	Map<String, List<String>> getMapOfStringLists(OpsiMethodCall omc);

	List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc);

	Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key, String[] sourceVars,
			String[] targetVars);

	List<Map<String, List<Map<String, Object>>>> getListOfMapsOfListsOfMaps(OpsiMethodCall omc);

	String getStringResult(OpsiMethodCall omc);

	boolean getBooleanResult(OpsiMethodCall omc);

	Map<String, Object> getMapFromItem(Object s);

	List<Object> getListFromItem(String s);
}
