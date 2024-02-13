/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.util.List;
import java.util.Map;

public abstract class AbstractExecutioner {
	private static AbstractExecutioner nonExecutioner;

	public abstract ConnectionState getConnectionState();

	public abstract boolean doCall(OpsiMethodCall omc);

	public abstract String getErrorFromResponse(Map<String, Object> retrieved);

	public abstract Map<String, Object> getResponses(Map<String, Object> retrieved);

	public abstract Map<String, Object> retrieveResponse(OpsiMethodCall omc);

	public abstract List<Object> getListResult(OpsiMethodCall omc);

	public abstract List<String> getStringListResult(OpsiMethodCall omc);

	public abstract List<List<String>> getListOfStringLists(OpsiMethodCall omc);

	public abstract Map<String, Object> getMapResult(OpsiMethodCall omc);

	public abstract Map<String, List<String>> getMapOfStringLists(OpsiMethodCall omc);

	public abstract List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc);

	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars);

	public abstract List<Map<String, List<Map<String, Object>>>> getListOfMapsOfListsOfMaps(OpsiMethodCall omc);

	public abstract String getStringResult(OpsiMethodCall omc);

	public abstract boolean getBooleanResult(OpsiMethodCall omc);

	public abstract Map<String, Object> getMapFromItem(Object s);

	public abstract List<Object> getListFromItem(String s);

	public static AbstractExecutioner getNoneExecutioner() {
		if (nonExecutioner == null) {
			nonExecutioner = new NONEexecutioner();
		}

		return nonExecutioner;
	}
}
