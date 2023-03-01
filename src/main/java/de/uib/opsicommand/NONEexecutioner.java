package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class NONEexecutioner extends AbstractExecutioner {
	@Override
	public ConnectionState getConnectionState() {
		return null;
	}

	@Override
	public void setConnectionState(ConnectionState state) {
		/* Do nothing, because this Executioner should do nothing */}

	@Override
	public boolean doCall(OpsiMethodCall omc) {
		return false;
	}

	@Override
	public JSONObject retrieveJSONObject(OpsiMethodCall omc) {
		return null;
	}

	@Override
	public Object getValueFromJSONObject(Object o, String key) {
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
	public Map<String, List<Object>> getMapOfLists(OpsiMethodCall omc) {
		return new HashMap<>();
	}

	@Override
	public Map<String, Map<String, Object>> getMapOfMaps(OpsiMethodCall omc) {
		return new HashMap<>();
	}

	@Override
	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		return new ArrayList<>();
	}

	@Override
	public List<Map<String, Object>> getListOfStringMaps(OpsiMethodCall omc) {
		return new ArrayList<>();
	}

	@Override
	public Map<String, Object> getMapObject(OpsiMethodCall omc) {
		return new HashMap<>();
	}

	@Override
	public Map<String, Map<String, Object>> getMap2Object(OpsiMethodCall omc) {
		return new HashMap<>();
	}

	@Override
	public Map<String, Map<String, Map<String, Object>>> getMap3Object(OpsiMethodCall omc) {
		return new HashMap<>();
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
	public Map<String, List<Map<String, Object>>> getMapOfListsOfMaps(OpsiMethodCall omc) {
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

	@Override
	public String getStringValueFromItem(Object s) {
		return null;
	}

}
