package de.uib.opsicommand;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class NONEexecutioner extends Executioner {
	public ConnectionState getConnectionState() {
		return null;
	}

	public void setConnectionState(ConnectionState state) {
	}

	public boolean doCall(OpsiMethodCall omc) {
		return false;
	}

	public JSONObject retrieveJSONObject(OpsiMethodCall omc) {
		return null;
	}

	public List<JSONObject> retrieveJSONObjects(List<OpsiMethodCall> omcList) {
		return null;
	}

	public Object getValueFromJSONObject(Object o, String key) {
		return null;
	}

	public List getListResult(OpsiMethodCall omc) {
		return null;
	}

	public List<String> getStringListResult(OpsiMethodCall omc) {
		return null;
	}

	public List<List<String>> getListOfStringLists(OpsiMethodCall omc) {
		return null;
	}

	public Map getMapResult(OpsiMethodCall omc) {
		return null;
	}

	public Map getMapOfLists(OpsiMethodCall omc) {
		return null;
	}

	public Map getMapOfMaps(OpsiMethodCall omc) {
		return null;
	}

	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		return null;
	}

	public List<Map<String, String>> getListOfStringMaps(OpsiMethodCall omc) {
		return null;
	}

	public Map<String, Object> getMap_Object(OpsiMethodCall omc) {
		return null;
	}

	public Map<String, Map<String, Object>> getMap2_Object(OpsiMethodCall omc) {
		return null;
	}

	public Map<String, Map<String, Map<String, Object>>> getMap3_Object(OpsiMethodCall omc) {
		return null;
	}

	public Map<String, Map<String, String>> getStringMappedObjectsByKey(
			OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars,
			Map<String, String> translateValues) {
		return null;
	}

	public Map<String, Map<String, String>> getStringMappedObjectsByKey(
			OpsiMethodCall omc, String key, String[] sourceVars, String[] targetVars) {
		return null;
	}

	public Map<String, Map<String, String>> getStringMappedObjectsByKey(
			OpsiMethodCall omc, String key) {
		return null;
	}

	public Map getMapOfListsOfMaps(OpsiMethodCall omc) {
		return null;
	}

	public List getListOfMapsOfListsOfMaps(OpsiMethodCall omc) {
		return null;
	}

	public String getStringResult(OpsiMethodCall omc) {
		return null;
	}

	public boolean getBooleanResult(OpsiMethodCall omc) {
		return false;
	}

	public Map getMapFromItem(Object s) {
		return null;
	}

	public List getListFromItem(String s) {
		return null;
	}

	public String getStringValueFromItem(Object s) {
		return null;
	}

}
