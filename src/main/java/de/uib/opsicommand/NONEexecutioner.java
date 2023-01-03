package de.uib.opsicommand;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class NONEexecutioner extends Executioner {
	@Override
	public ConnectionState getConnectionState() {
		return null;
	}

	@Override
	public void setConnectionState(ConnectionState state) {
	}

	@Override
	public boolean doCall(OpsiMethodCall omc) {
		return false;
	}

	@Override
	public JSONObject retrieveJSONObject(OpsiMethodCall omc) {
		return null;
	}

	public List<JSONObject> retrieveJSONObjects(List<OpsiMethodCall> omcList) {
		return null;
	}

	@Override
	public Object getValueFromJSONObject(Object o, String key) {
		return null;
	}

	@Override
	public List getListResult(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public List<String> getStringListResult(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public List<List<String>> getListOfStringLists(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Map getMapResult(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Map getMapOfLists(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Map getMapOfMaps(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public List<Map<String, String>> getListOfStringMaps(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Map<String, Object> getMap_Object(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Map<String, Map<String, Object>> getMap2_Object(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Map<String, Map<String, Map<String, Object>>> getMap3_Object(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars, Map<String, String> translateValues) {
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars) {
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key) {
		return null;
	}

	@Override
	public Map getMapOfListsOfMaps(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public List getListOfMapsOfListsOfMaps(OpsiMethodCall omc) {
		return null;
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
	public Map getMapFromItem(Object s) {
		return null;
	}

	@Override
	public List getListFromItem(String s) {
		return null;
	}

	@Override
	public String getStringValueFromItem(Object s) {
		return null;
	}

}
