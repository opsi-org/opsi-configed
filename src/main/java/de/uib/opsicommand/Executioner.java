package de.uib.opsicommand;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Executioner {
	public abstract ConnectionState getConnectionState();

	public abstract void setConnectionState(ConnectionState state);

	public abstract boolean doCall(OpsiMethodCall omc);

	public abstract JSONObject retrieveJSONObject(OpsiMethodCall omc);

	public static JSONObject jsonMap(Map m) {
		return new JSONObject(m);
	}

	public abstract Object getValueFromJSONObject(Object o, String key);

	public static Object jsonArray(List l) {
		Object result = null;

		if (l == null)
			result = new JSONArray();
		else
			result = new JSONArray(l);

		return result;
	}

	public abstract List getListResult(OpsiMethodCall omc);

	public abstract List<String> getStringListResult(OpsiMethodCall omc);

	public abstract List<List<String>> getListOfStringLists(OpsiMethodCall omc);

	public abstract Map<String, Object> getMapResult(OpsiMethodCall omc);

	public abstract Map getMapOfLists(OpsiMethodCall omc);

	public abstract Map getMapOfMaps(OpsiMethodCall omc);

	public abstract List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc);

	public abstract List<Map<String, String>> getListOfStringMaps(OpsiMethodCall omc);

	public abstract Map<String, Object> getMapObject(OpsiMethodCall omc);

	public abstract Map<String, Map<String, Object>> getMap2Object(OpsiMethodCall omc);

	public abstract Map<String, Map<String, Map<String, Object>>> getMap3Object(OpsiMethodCall omc);

	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars, Map<String, String> translateValues);

	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars);

	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key);

	public abstract Map getMapOfListsOfMaps(OpsiMethodCall omc);

	public abstract List getListOfMapsOfListsOfMaps(OpsiMethodCall omc);

	public abstract String getStringResult(OpsiMethodCall omc);

	public abstract boolean getBooleanResult(OpsiMethodCall omc);

	public abstract Map<String, Object> getMapFromItem(Object s);

	public abstract List getListFromItem(String s);

	public abstract String getStringValueFromItem(Object s);

	public static final Executioner NONE = new NONEexecutioner();
}
