/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NONEexecutioner extends AbstractExecutioner {
	@Override
	public ConnectionState getConnectionState() {
		return null;
	}

	@Override
	public boolean doCall(OpsiMethodCall omc) {
		return false;
	}

	@Override
	public String getErrorFromResponse(Map<String, Object> retrieved) {
		return null;
	}

	@Override
	public Map<String, Object> getResponses(Map<String, Object> retrieved) {
		return null;
	}

	@Override
	public Map<String, Object> retrieveResponse(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public List<Object> getListResult(OpsiMethodCall omc) {
		return new ArrayList<>();
	}

	@Override
	public List<String> getStringListResult(OpsiMethodCall omc) {
		return new ArrayList<>();
	}

	@Override
	public List<List<String>> getListOfStringLists(OpsiMethodCall omc) {
		return new ArrayList<>();
	}

	@Override
	public Map<String, Object> getMapResult(OpsiMethodCall omc) {
		return new HashMap<>();
	}

	@Override
	public Map<String, List<String>> getMapOfStringLists(OpsiMethodCall omc) {
		return new HashMap<>();
	}

	@Override
	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		return new ArrayList<>();
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars, Map<String, String> translateValues) {
		return new HashMap<>();
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars) {
		return new HashMap<>();
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key) {
		return new HashMap<>();
	}

	@Override
	public List<Map<String, List<Map<String, Object>>>> getListOfMapsOfListsOfMaps(OpsiMethodCall omc) {
		return new ArrayList<>();
	}

	@Override
	public String getStringResult(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public boolean getBooleanResult(OpsiMethodCall omc) {
		return false;
	}

	@Override
	public Map<String, Object> getMapFromItem(Object s) {
		return new HashMap<>();
	}

	@Override
	public List<Object> getListFromItem(String s) {
		return new ArrayList<>();
	}
}
