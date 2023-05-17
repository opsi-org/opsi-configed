package de.uib.opsicommand;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractExecutioner {

	private static AbstractExecutioner nonExecutioner;

	public abstract ConnectionState getConnectionState();

	public abstract boolean doCall(OpsiMethodCall omc);

	public abstract String getErrorFromResponse(Map<String, Object> retrieved);

	public abstract Map<String, Object> getResponses(Map<String, Object> retrieved);

	public abstract Map<String, Object> retrieveResponse(OpsiMethodCall omc);

	public static JSONObject jsonMap(Map<String, ? extends Object> m) {
		return new JSONObject(m);
	}

	public static JSONArray jsonArray(List<?> l) {
		JSONArray result = null;

		if (l == null) {
			result = new JSONArray();
		} else {
			result = new JSONArray(l);
		}

		return result;
	}

	public abstract List<Object> getListResult(OpsiMethodCall omc);

	public abstract List<String> getStringListResult(OpsiMethodCall omc);

	public abstract List<List<String>> getListOfStringLists(OpsiMethodCall omc);

	public abstract Map<String, Object> getMapResult(OpsiMethodCall omc);

	public abstract Map<String, List<String>> getMapOfStringLists(OpsiMethodCall omc);

	public abstract List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc);

	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars, Map<String, String> translateValues);

	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars);

	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key);

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
