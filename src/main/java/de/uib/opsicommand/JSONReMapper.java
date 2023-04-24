package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uib.utilities.logging.Logging;

/**
 * This class contains utility methods for remapping JSON data
 *
 * @author Rupert Roeder
 */

public final class JSONReMapper {

	private static final String CLASSNAME = JSONReMapper.class.getName();

	public static final String NULL_REPRESENTER = "null";

	// private constructor to hide the implicit public one
	private JSONReMapper() {
	}

	public static String getErrorFromResponse(JSONObject retrieved) {
		String errorMessage = null;

		try {
			if (!retrieved.isNull("error")) {

				JSONObject jOError = retrieved.optJSONObject("error");

				if ((jOError != null) && (!jOError.isNull("class")) && (!jOError.isNull("message"))) {
					errorMessage = " [" + jOError.get("class") + "] " + jOError.get("message");
				} else {
					errorMessage = " " + retrieved.get("error");
				}
			}
		} catch (JSONException jex) {
			errorMessage = "JSON Error on retrieving result value,  " + jex;
		}

		return errorMessage;
	}

	public static Map<String, Object> getResponses(JSONObject retrieved) {
		Map<String, Object> result = new HashMap<>();

		Map<String, Object> result0 = getMapResult(retrieved);

		try {

			for (Entry<String, Object> result0Entry : result0.entrySet()) {
				JSONObject jO = (JSONObject) (result0Entry.getValue());
				HashMapX<Object> response = new HashMapX<>(jO, true);

				if (response.get("error") == null) {
					List<?> list = (List<?>) response.get("result");

					result.put(result0Entry.getKey(), list);
				} else {

					String str = "" + response.get("error");
					result.put(result0Entry.getKey(), str);
				}
			}
		} catch (Exception ex) {
			Logging.error("JSONReMapper getResponses ", ex);
		}

		Logging.debug("JSONReMapper getResponses  result " + result);

		return result;
	}

	public static boolean checkForNotValidOpsiMethod(JSONObject retrieved) {
		String errorFromResponse = getErrorFromResponse(retrieved);

		if (errorFromResponse != null && errorFromResponse.indexOf("Opsi rpc error: Method") > -1
				&& errorFromResponse.endsWith("is not valid")) {
			Logging.info("JSONReMapper: checkForNotValidOpsiMethod " + getErrorFromResponse(retrieved));
			return false;
		}

		return true;
	}

	public static boolean checkResponse(JSONObject retrieved) {
		boolean responseFound = true;

		Logging.debug(CLASSNAME + ".checkResponse " + Logging.LEVEL_DEBUG);

		if (retrieved == null) {
			responseFound = false;
		} else {
			Object resultValue = null;

			try {

				String errorMessage = getErrorFromResponse(retrieved);

				if (errorMessage != null) {

					String logMessage = "Opsi service error: " + errorMessage;
					Logging.error(logMessage);
				} else {
					resultValue = retrieved.get("result");

				}
			} catch (JSONException jex) {
				Logging.error("JSON Error on retrieving result value,  ", jex);
			}

			if (resultValue == null) {
				Logging.debug(CLASSNAME + ": " + " checkResponse " + Logging.LEVEL_DEBUG, "Null result in response ");
				responseFound = false;
			}
		}

		return responseFound;
	}

	public static Map<String, Map<String, Object>> getMap2Object(Object retrieved) {
		HashMap<String, Map<String, Object>> result = new HashMap<>();
		HashMap<String, Map<String, Object>> resultNull = new HashMap<>();

		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");
				JSONObjectX jOX = new JSONObjectX(jOResult);

				if (!jOX.isMap()) {
					Logging.error(CLASSNAME + "map expected " + jOX);
				} else {
					Logging.debug(CLASSNAME + "map retrieved ");

					Map<String, Object> map0 = jOX.getMap();

					Iterator<String> iter0 = map0.keySet().iterator();
					while (iter0.hasNext()) {
						String key1 = iter0.next();

						JSONObjectX jOX1 = new JSONObjectX((JSONObject) map0.get(key1));

						if (!jOX1.isMap()) {
							Logging.error(CLASSNAME + "map expected in level 2 " + jOX1);
							result = resultNull;
						} else {
							result.put(key1, jOX1.getMap());
						}
					}
				}
			}
		} catch (Exception ex) {
			Logging.error("this, getMap2_Object : ", ex);
		}

		return result;

	}

	public static Map<String, Map<String, Map<String, Object>>> getMap3Objects(Object retrieved) {
		HashMap<String, Map<String, Map<String, Object>>> result = new HashMap<>();
		try {
			JSONObject jO = (JSONObject) retrieved;
			HashMap<String, JSONObject> map0 = new HashMapX<>(jO);

			Iterator<String> iter0 = map0.keySet().iterator();
			while (iter0.hasNext()) {
				// e.g. client
				String key1 = iter0.next();

				// e.g. map of 1 client values
				HashMap<String, JSONObject> map1 = new HashMapX<>(map0.get(key1));

				// to produce
				HashMap<String, Map<String, Object>> map1R = new HashMap<>();

				Iterator<String> iter1 = map1.keySet().iterator();
				while (iter1.hasNext()) {
					// e.g. product
					String key2 = iter1.next();
					HashMap<String, Object> map2 = new HashMapX<>(map1.get(key2), true);

					map1R.put(key2, map2);

				}

				result.put(key1, map1R);
			}
		} catch (Exception ex) {
			Logging.debug(CLASSNAME + ".getMap3_String: " + ex);
		}

		return result;

	}

	public static Map<String, Map<String, Map<String, Object>>> getMap3Object(Object retrieved) {
		HashMap<String, Map<String, Map<String, Object>>> result = new HashMap<>();
		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");

				HashMap<String, JSONObject> map0 = new HashMapX<>(jOResult);

				Iterator<String> iter0 = map0.keySet().iterator();
				while (iter0.hasNext()) {

					// e.g. client
					String key1 = iter0.next();
					HashMap<String, JSONObject> map1 = new HashMapX<>(map0.get(key1)); // e.g.
																						// map
																						// of
																						// 1
																						// client
																						// values

					HashMap<String, Map<String, Object>> map1R = new HashMap<>(); // to
																					// produce

					Iterator<String> iter1 = map1.keySet().iterator();
					while (iter1.hasNext()) {
						// e.g. product
						String key2 = iter1.next();
						HashMap<String, Object> map2 = new HashMapX<>(map1.get(key2), true); // e.g.
																								// product

						map1R.put(key2, map2);

					}

					result.put(key1, map1R);
				}
			}
		} catch (Exception ex) {
			Logging.debug(CLASSNAME + ".getMap3_String: " + ex);
		}

		return result;

	}

	public static List<Map<String, Object>> getListOfStringMaps(Object retrieved) {
		List<Object> jsonList = null;
		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				jsonList = getJsonList(jO, "result");
			}
		} catch (Exception ex) {
			Logging.error("JSONReMapper: Exception on getting list for key \"result\" " + retrieved, ex);
		}

		if (jsonList == null) {
			Logging.error("JSONReMapper: Error on getting list for key \"result\": jsonList is null " + retrieved);
			return new ArrayList<>();
		}

		JSONObject item = null;

		List<Map<String, Object>> result = new ArrayList<>();
		try {
			Iterator<Object> iter = jsonList.iterator();

			while (iter.hasNext()) {
				item = (JSONObject) iter.next();
				Map<String, Object> mapItem = JSONReMapper.deriveStandard(item);
				result.add(mapItem);

			}
			if (jsonList.size() != result.size()) {
				Logging.warning(" getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped "
						+ result.size());
			}
		} catch (Exception ex) {
			Logging.error("JSONReMapper: Exception on reproducing  " + item, ex);
		}

		return result;
	}

	public static List<Map<String, Object>> getListOfMaps(JSONArray retrieved) {
		List<Map<String, Object>> result = new ArrayList<>();
		List<Object> jsonList = null;

		JSONArray jA = retrieved;
		jsonList = new ArrayList<>();

		try {
			if (jA != null) {
				for (int i = 0; i < jA.length(); i++) {

					jsonList.add(jA.get(i));
				}
			}

		} catch (JSONException jex) {
			Logging.error("JSONReMapper: Exception on getting list ", jex);
		}

		JSONObject item = null;

		try {
			Iterator<Object> iter = jsonList.iterator();

			while (iter.hasNext()) {
				item = (JSONObject) iter.next();
				Map<String, Object> mapItem = JSONReMapper.deriveStandard(item);
				result.add(mapItem);

			}
			if (jsonList.size() != result.size()) {
				Logging.warning(" getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped "
						+ result.size());
			}
		} catch (JSONException ex) {
			Logging.error("JSONReMapper: Exception on reproducing  " + item + ", ", ex);
		}

		return result;
	}

	@SuppressWarnings("java:S1168")
	public static List<Map<String, Object>> getListOfMaps(Object retrieved) {
		List<Object> jsonList = null;

		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				jsonList = getJsonList(jO, "result");
			}
		} catch (Exception ex) {
			Logging.error("JSONReMapper: Exception on getting list for key \"result\" ", ex);
		}

		if (jsonList == null) {
			Logging.error("JSONReMapper: Error on getting list for key \"result\": jsonList is null");
			return null;
		}

		JSONObject item = null;

		List<Map<String, Object>> result = new ArrayList<>();

		try {

			Iterator<Object> iter = jsonList.iterator();

			while (iter.hasNext()) {
				item = (JSONObject) iter.next();
				Map<String, Object> mapItem = JSONReMapper.deriveStandard(item);
				result.add(mapItem);

			}
			if (jsonList.size() != result.size()) {
				Logging.warning(" getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped "
						+ result.size());
			}
		} catch (Exception ex) {
			Logging.error("JSONReMapper: Exception on reproducing  " + item, ex);
		}

		return result;
	}

	public static List<List<String>> getListOfListsOfStrings(JSONObject jO, String key) {
		List<List<String>> list0 = new ArrayList<>();
		JSONArray jA = jO.optJSONArray(key);

		int size0 = jA.length();
		try {
			for (int i = 0; i < size0; i++) {
				JSONArray element0 = jA.getJSONArray(i);
				int size1 = element0.length();

				List<String> list1 = new ArrayList<>();

				for (int j = 0; j < size1; j++) {

					list1.add("" + element0.get(j));
				}

				list0.add(list1);
			}
		} catch (JSONException jex) {
			Logging.error("JSONReMapper: Exception on getting list of lists of JSONObjects ", jex);
		}

		return list0;
	}

	public static List<List<String>> getJsonListOfStringLists(JSONObject jO, String key) {
		ArrayList<List<String>> result = new ArrayList<>();

		List<Object> list1 = getJsonList(jO, key);

		try {
			for (Object ob1 : list1) {
				JSONArray jA = (JSONArray) ob1;

				List<String> row = null;

				if (jA != null) {
					row = new ArrayList<>(jA.length());
					for (int i = 0; i < jA.length(); i++) {
						if (isNull(jA.get(i))) {
							row.add("");
						} else {
							row.add("" + jA.get(i));
						}
					}

					result.add(row);
				}
			}
		} catch (JSONException jex) {
			Logging.error("JSONReMapper: Exception on getting list of stringlists ", jex);
		}

		return result;
	}

	public static List<Object> getJsonList(JSONObject jO, String key) {
		List<Object> result = new ArrayList<>();
		try {
			JSONArray jA = jO.optJSONArray(key);

			if (jA != null) {
				result = new ArrayList<>(jA.length());
				for (int i = 0; i < jA.length(); i++) {

					result.add(jA.get(i));
				}
			}

		} catch (JSONException jex) {
			Logging.error("JSONReMapper: Exception on getting list ", jex);
		}

		return result;
	}

	public static List<String> getJsonStringList(JSONObject jO, String key) {
		List<String> result = new ArrayList<>();
		try {
			JSONArray jA = jO.optJSONArray(key);

			if (jA != null) {
				result = new ArrayList<>(jA.length());
				for (int i = 0; i < jA.length(); i++) {
					result.add("" + jA.get(i));
				}
			}

		} catch (JSONException jex) {
			Logging.error("JSONReMapper: Exception on getting list ", jex);
		}

		return result;
	}

	public static List<Object> getListResult(JSONObject jO) {
		List<Object> result = new ArrayList<>();
		try {
			if (checkResponse(jO)) {
				result = JSONReMapper.getJsonList(jO, "result");
			}
		} catch (Exception ex) {
			Logging.error(CLASSNAME + "Exception on getting list for key \"result\" ", ex);
		}

		return result;
	}

	// this method tries to return Java lists in comparison with getMapResult
	public static Map<String, Object> getMapObject(JSONObject jo) {
		Map<String, Object> result = new HashMap<>();

		try {
			JSONObjectX jOX = new JSONObjectX(jo);

			if (!jOX.isMap()) {
				Logging.error("JSONReMapper map expected " + jOX);
			} else {
				result = jOX.getMap();
			}

		} catch (Exception ex) {
			Logging.error("JSONReMapper  getMap_Object : ", ex);
		}

		return result;
	}

	public static Map<String, Object> getMapResult(JSONObject jO) {
		// yields possibly JSON objects and arrays as values
		// compare getMap_Object

		Map<String, Object> result = new HashMap<>();
		try {
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");
				if (jOResult != null) {
					Iterator<String> iter = jOResult.keys();
					while (iter.hasNext()) {
						String key = iter.next();
						result.put(key, jOResult.get(key));
					}
				}
			}
		} catch (JSONException jex) {
			Logging.error(CLASSNAME + "Exception on getting Map ", jex);
		}
		return result;
	}

	public static List<String> getStringListResult(JSONObject jO) {
		List<String> result = new ArrayList<>();
		try {
			if (checkResponse(jO)) {
				result = JSONReMapper.getJsonStringList(jO, "result");
			}

		} catch (Exception ex) {
			Logging.error(CLASSNAME + "Exception on getting list for key \"result\" ", ex);
		}

		return result;
	}

	public static List<List<String>> getListOfStringLists(JSONObject jO) {
		List<List<String>> result = new ArrayList<>();
		try {
			if (checkResponse(jO)) {
				result = JSONReMapper.getJsonListOfStringLists(jO, "result");
			}

		} catch (Exception ex) {
			Logging.error(CLASSNAME + "Exception on getting list for key \"result\" ", ex);
		}

		return result;
	}

	private static class HashMapX<V> extends HashMap<String, V> {
		HashMapX(JSONObject jO) {
			this(jO, false);
		}

		HashMapX(JSONObject jO, boolean derive) {
			super();

			try {
				if (jO != null) {
					Iterator<String> iter = jO.keys();
					while (iter.hasNext()) {
						String key = iter.next();

						if (!jO.isNull(key)) {
							if (derive) {
								super.put(key, (V) (deriveStandard(jO.get(key))));
							} else {
								super.put(key, (V) (jO.get(key)));
							}
						}
					}
				}
			} catch (Exception ex) {
				Logging.error(CLASSNAME + "json transform exception: ", ex);
			}
		}
	}

	public static Object deriveStandard(Object ob) {
		if (ob == null) {
			return null;
		} else if (ob instanceof String) {
			return ob;
		} else if (ob instanceof JSONArray) {
			return ((JSONArray) ob).toList();
			// to do: make recursive
		} else if (ob instanceof JSONObject) {
			return deriveStandard((JSONObject) ob);

		} else {

			return ob;
		}
	}

	public static Map<String, Object> deriveStandard(JSONObject ob) {
		Map<String, Object> map = new HashMap<>();

		Iterator<String> iter = ob.keys();

		while (iter.hasNext()) {
			String key = null;
			Object value = null;

			try {
				key = iter.next();

				if (ob.isNull(key)) {
					map.put(key, null);
				} else {
					value = ob.get(key);
					map.put(key, value);
				}
				// make recursive
			} catch (Exception ex) {
				Logging.error("deriveStandard, key " + key + ", value " + value, ex);
			}
		}
		return map;
	}

	public static boolean isNull(Object ob) {
		return ob == null || (ob instanceof String && "null".equalsIgnoreCase((String) ob))
				|| ((ob instanceof JSONObject) && (JSONObject.NULL.equals(ob)));
	}

	public static boolean equalsNull(String ob) {
		return ob == null || "null".equalsIgnoreCase(ob);
	}

	public static String giveEmptyForNullString(String ob) {
		if (ob == null || "null".equalsIgnoreCase(ob)) {
			return "";
		} else {
			return ob;
		}
	}

	public static Map<String, String> giveEmptyForNullString(Map<String, String> m) {
		for (Entry<String, String> entry : m.entrySet()) {
			if (isNull(entry.getValue())) {
				m.put(entry.getKey(), "");
			}
		}

		return m;
	}

	public static Map<String, String> giveEmptyForNull(Map<String, Object> m) {

		HashMap<String, String> result = new HashMap<>();
		for (Entry<String, Object> entry : m.entrySet()) {
			if (isNull(entry.getValue())) {
				result.put(entry.getKey(), "");
			} else {
				result.put(entry.getKey(), "" + entry.getValue());
			}
		}

		return result;
	}

}
