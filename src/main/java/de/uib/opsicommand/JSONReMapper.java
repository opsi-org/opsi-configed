package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uib.utilities.logging.logging;

/**
 * This class contains utility methods for remapping JSON data
 * 
 * @author Rupert Roeder
 */

public class JSONReMapper {
	private static final String CLASSNAME = JSONReMapper.class.getName();

	public static final String NullRepresenter = "null";

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
			errorMessage = "JSON Error on retrieving result value,  " + jex.toString();
		}

		return errorMessage;
	}

	public static Map<String, Object> getResponses(JSONObject retrieved) {
		Map<String, Object> result = new HashMap<>();

		Map<String, Object> result0 = getMapResult(retrieved);

		try {

			for (String key : result0.keySet()) {
				JSONObject jO = (JSONObject) (result0.get(key));
				HashMapX response = new HashMapX(jO, true);
				// String value = "";

				if (response.get("error") == null) {
					List list = (List) response.get("result");

					
					// value = "" + list;

					result.put(key, list);
				} else {
					// value = (String) response.get("error");
					String str = "" + response.get("error");
					result.put(key, str);
				}
				
			}
		}

		catch (Exception ex) {
			logging.error("JSONReMapper getResponses " + ex);
		}

		logging.debug("JSONReMapper getResponses  result " + result);

		return result;
	}

	public static boolean checkForNotValidOpsiMethod(JSONObject retrieved) {
		
		
		if (retrieved != null && getErrorFromResponse(retrieved) != null
				&& getErrorFromResponse(retrieved).indexOf("Opsi rpc error: Method") > -1
				&& getErrorFromResponse(retrieved).endsWith("is not valid")) {
			logging.info("JSONReMapper: checkForNotValidOpsiMethod " + getErrorFromResponse(retrieved));
			return false;
		}

		return true;
	}

	public static boolean checkResponse(JSONObject retrieved) {
		boolean responseFound = true;

		logging.debug(CLASSNAME + ".checkResponse " + logging.LEVEL_DEBUG);
		// "retrieved JSONObject " + retrieved);

		if (retrieved == null) {
			responseFound = false;
		} else {
			Object resultValue = null;

			try {
				/*
				 * if ( ! retrieved.isNull("error") )
				 * {
				 * String logMessage = "Opsi service error: ";
				 * JSONObject jOError = retrieved.optJSONObject("error");
				 * 
				 * if ( (jOError != null) && ( ! jOError.isNull("class") ) && ( !
				 * jOError.isNull("message") ) )
				 * {
				 * 
				 * logMessage = logMessage + " [" + jOError.get("class") + "] " +
				 * jOError.get("message");
				 * }
				 * else
				 * {
				 * logMessage = logMessage + " " + retrieved.get("error");
				 * }
				 * logging.error(logMessage);
				 * responseFound = false;
				 * }
				 */

				String errorMessage = getErrorFromResponse(retrieved);

				if (errorMessage != null) {
					// responseFound = false;
					String logMessage = "Opsi service error: " + errorMessage;
					logging.error(logMessage);
				} else {
					resultValue = retrieved.get("result");
					
					
				}
			} catch (JSONException jex) {
				logging.error("JSON Error on retrieving result value,  " + jex.toString());
			}

			if (resultValue == null) {
				logging.debug(CLASSNAME + ": " + " checkResponse " + logging.LEVEL_DEBUG, "Null result in response ");
				responseFound = false;
			}
		}

		return responseFound;
	}

	public static Map<String, Map<String, Object>> getMap2_Object(Object retrieved) {
		HashMap<String, Map<String, Object>> result = new HashMap<>();
		HashMap<String, Map<String, Object>> resultNull = new HashMap<>();

		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");
				JSONObjectX jOX = new JSONObjectX(jOResult);

				if (!jOX.isMap()) {
					logging.error(CLASSNAME + "map expected " + jOX);
				} else {
					logging.debug(CLASSNAME + "map retrieved ");
					// + jOX.getMap());
					Map map0 = jOX.getMap();

					Iterator iter0 = map0.keySet().iterator();
					while (iter0.hasNext()) {
						String key1 = (String) iter0.next();

						JSONObjectX jOX1 = new JSONObjectX((JSONObject) map0.get(key1));

						if (!jOX1.isMap()) {
							logging.error(CLASSNAME + "map expected in level 2 " + jOX1);
							result = resultNull;
						} else {
							result.put(key1, jOX1.getMap());
						}
					}
				}
			}
		} catch (Exception ex) {
			logging.error("this, getMap2_Object : " + ex.toString());
		}

		return result;

	}

	public static Map<String, Map<String, Map<String, Object>>> getMap3_Objects(Object retrieved) {
		HashMap<String, Map<String, Map<String, Object>>> result = new HashMap<>();
		try {
			JSONObject jO = (JSONObject) retrieved;
			HashMap<String, JSONObject> map0 = new HashMapX<>(jO);
			

			Iterator iter0 = map0.keySet().iterator();
			while (iter0.hasNext()) {
				String key1 = (String) iter0.next(); // e.g. client
				HashMap<String, JSONObject> map1 = new HashMapX<>((JSONObject) map0.get(key1)); // e.g.
																								// map
																								// of
																								// 1
																								// client
																								// values
																								
				HashMap<String, Map<String, Object>> map1R = new HashMap<>(); // to produce

				Iterator iter1 = map1.keySet().iterator();
				while (iter1.hasNext()) {
					String key2 = (String) iter1.next(); // e.g. product
					HashMap<String, Object> map2 = new HashMapX<>((JSONObject) map1.get(key2), true); // e.g.
																										// product
																										
																										
					map1R.put(key2, map2);
					
				}

				result.put(key1, map1R);
			}
		} catch (Exception ex) {
			logging.debug(CLASSNAME + ".getMap3_String: " + ex.toString());
		}

		return result;

	}

	public static Map<String, Map<String, Map<String, Object>>> getMap3_Object(Object retrieved) {
		HashMap<String, Map<String, Map<String, Object>>> result = new HashMap<>();
		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");

				HashMap<String, JSONObject> map0 = new HashMapX<>(jOResult);
				

				Iterator iter0 = map0.keySet().iterator();
				while (iter0.hasNext()) {
					String key1 = (String) iter0.next(); // e.g. client
					HashMap<String, JSONObject> map1 = new HashMapX<>((JSONObject) map0.get(key1)); // e.g.
																									// map
																									// of
																									// 1
																									// client
																									// values
																									
					HashMap<String, Map<String, Object>> map1R = new HashMap<>(); // to
																					// produce

					Iterator iter1 = map1.keySet().iterator();
					while (iter1.hasNext()) {
						String key2 = (String) iter1.next(); // e.g. product
						HashMap<String, Object> map2 = new HashMapX<>((JSONObject) map1.get(key2), true); // e.g.
																											// product
																											
																											
						map1R.put(key2, map2);
						
					}

					result.put(key1, map1R);
				}
			}
		} catch (Exception ex) {
			logging.debug(CLASSNAME + ".getMap3_String: " + ex.toString());
		}

		return result;

	}

	public static List<Map<String, String>> getListOfStringMaps(Object retrieved) {
		List<Map<String, String>> result = new ArrayList<>();
		List jsonList = null;
		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				jsonList = getJsonList(jO, "result");
			}
		} catch (Exception ex) {
			logging.error("JSONReMapper: Exception on getting list for key \"result\" " + ex.toString());
		}

		JSONObject item = null;

		try {

			Iterator iter = jsonList.iterator();

			while (iter.hasNext()) {
				item = (JSONObject) iter.next();
				Map<String, String> mapItem = (Map<String, String>) JSONReMapper.deriveStandard(item);
				result.add(mapItem);

			}
			assert jsonList.size() == result.size()
					: " getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped " + result.size();
		} catch (Exception ex) {
			logging.error("JSONReMapper: Exception on reproducing  " + item + ", " + ex);
		}

		return result;
	}

	public static List<Map<String, Object>> getListOfMaps(JSONArray retrieved) {
		List<Map<String, Object>> result = new ArrayList<>();
		List jsonList = null;

		try {
			JSONArray jA = retrieved;
			jsonList = new ArrayList<>();

			if (jA != null) {
				for (int i = 0; i < jA.length(); i++) {
					
					jsonList.add(jA.get(i));
				}
			}

			
			
		} catch (JSONException jex) {
			logging.error("JSONReMapper: Exception on getting list " + jex.toString());
		}

		JSONObject item = null;

		try {

			Iterator iter = jsonList.iterator();

			while (iter.hasNext()) {
				item = (JSONObject) iter.next();
				Map<String, Object> mapItem = (Map<String, Object>) JSONReMapper.deriveStandard(item);
				result.add(mapItem);

			}
			assert jsonList.size() == result.size()
					: " getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped " + result.size();
		} catch (Exception ex) {
			logging.error("JSONReMapper: Exception on reproducing  " + item + ", " + ex);
		}

		return result;
	}

	public static List<Map<String, Object>> getListOfMaps(Object retrieved) {
		List<Map<String, Object>> result = new ArrayList<>();
		List jsonList = null;

		try {
			JSONObject jO = (JSONObject) retrieved;
			if (checkResponse(jO)) {
				jsonList = getJsonList(jO, "result");
			}
		} catch (Exception ex) {
			logging.error("JSONReMapper: Exception on getting list for key \"result\" " + ex.toString());
		}

		JSONObject item = null;

		try {

			Iterator iter = jsonList.iterator();

			while (iter.hasNext()) {
				item = (JSONObject) iter.next();
				Map<String, Object> mapItem = (Map<String, Object>) JSONReMapper.deriveStandard(item);
				result.add(mapItem);

			}
			assert jsonList.size() == result.size()
					: " getListOfMaps did not work, jsonList.size " + jsonList.size() + ", remapped " + result.size();
		} catch (Exception ex) {
			logging.error("JSONReMapper: Exception on reproducing  " + item + ", " + ex);
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
			logging.error("JSONReMapper: Exception on getting list of lists of JSONObjects " + jex.toString());
		}

		return list0;
	}

	public static List<List<String>> getJsonListOfStringLists(JSONObject jO, String key) {
		ArrayList<List<String>> result = new ArrayList<>();

		List<Object> list1 = getJsonList(jO, key);

		try {
			for (Object ob1 : list1) {
				JSONArray jA = (JSONArray) ob1;

				ArrayList<String> row = null;

				if (jA != null) {
					row = new ArrayList<>(jA.length());
					for (int i = 0; i < jA.length(); i++) {
						

						if (isNull(jA.get(i)))
							row.add("");

						else
							row.add("" + jA.get(i));
					}

					result.add(row);
				}
			}
		} catch (JSONException jex) {
			logging.error("JSONReMapper: Exception on getting list of stringlists " + jex.toString());
		}

		
		// System.exit(0);

		return result;
	}

	public static List getJsonList(JSONObject jO, String key) {
		ArrayList result = new ArrayList<>();
		try {
			JSONArray jA = jO.optJSONArray(key);

			if (jA != null) {
				result = new ArrayList<>(jA.length());
				for (int i = 0; i < jA.length(); i++) {
					
					result.add(jA.get(i));
				}
			}

			
			
		} catch (JSONException jex) {
			logging.error("JSONReMapper: Exception on getting list " + jex.toString());
		}

		return result;
	}

	public static List<String> getJsonStringList(JSONObject jO, String key) {
		ArrayList<String> result = new ArrayList<>();
		try {
			JSONArray jA = jO.optJSONArray(key);

			
			if (jA != null) {
				result = new ArrayList<>(jA.length());
				for (int i = 0; i < jA.length(); i++) {
					result.add("" + jA.get(i));
				}
			}

			
		} catch (JSONException jex) {
			logging.error("JSONReMapper: Exception on getting list " + jex.toString());
		}

		return result;
	}

	public static List getListResult(JSONObject jO) {
		List result = new ArrayList<>();
		try {
			if (checkResponse(jO)) {
				result = JSONReMapper.getJsonList(jO, "result");
			}
		} catch (Exception ex) {
			logging.error(CLASSNAME + "Exception on getting list for key \"result\" " + ex.toString());
		}

		return result;
	}

	public static Map<String, Object> getMap_Object(JSONObject jo)
	// this method tries to return Java lists in comparison with getMapResult
	{
		Map<String, Object> result = new HashMap<>();

		try {
			JSONObjectX jOX = new JSONObjectX(jo);

			if (!jOX.isMap()) {
				logging.error("JSONReMapper map expected " + jOX);
			} else
				result = jOX.getMap();

		} catch (Exception ex) {
			logging.error("JSONReMapper  getMap_Object : " + ex.toString());
		}

		return result;
	}

	public static Map getMapResult(JSONObject jO)
	// yields possibly JSON objects and arrays as values
	// compare getMap_Object
	{
		HashMap result = new HashMap<>();
		try {
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");
				if (jOResult != null) {
					Iterator iter = jOResult.keys();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						result.put(key, jOResult.get(key));
					}
				}
			}
		} catch (JSONException jex) {
			logging.error(CLASSNAME + "Exception on getting Map " + jex.toString());
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
			logging.error(CLASSNAME + "Exception on getting list for key \"result\" " + ex.toString());
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
			logging.error(CLASSNAME + "Exception on getting list for key \"result\" " + ex.toString());
		}

		return result;
	}

	private static class HashMapX<String, V> extends HashMap<String, V> {
		HashMapX(JSONObject jO) {
			this(jO, false);
		}

		HashMapX(JSONObject jO, boolean derive) {
			super();
			
			try {
				if (jO != null) {
					Iterator iter = jO.keys();
					while (iter.hasNext()) {
						Object key = iter.next();
						
						if (!jO.isNull((java.lang.String) key)) {
							if (derive)
								put((String) key, (V) (deriveStandard(jO.get((java.lang.String) key))));
							else
								put((String) key, (V) (jO.get((java.lang.String) key)));
						}
						/*
						 * else
						 * put((String) key, null);
						 */
					}
				}
			} catch (Exception ex) {
				logging.error(CLASSNAME + "json transform exception: " + ex);
			}
		}
	}

	public static Object deriveStandard(Object ob) {
		if (ob == null)
			return null;

		else if (ob instanceof String)
			return ob;

		else if (ob instanceof JSONArray) {
			return ((JSONArray) ob).toList();
			// to do: make recursive
		}

		else if (ob instanceof JSONObject) {
			Map<String, Object> map = new HashMap<>();

			Iterator iter = ((JSONObject) ob).keys();

			while (iter.hasNext()) {
				String key = null;
				Object value = null;;

				try {
					key = (String) iter.next();

					if (((JSONObject) ob).isNull(key))
						map.put(key, null);

					else {
						value = ((JSONObject) ob).get(key);
						map.put(key, value);
					}
					// make recursive
				} catch (Exception ex) {
					logging.error("deriveStandard, key " + key + ", value " + value + ", " + ex);
				}
			}
			return map;
		} else
			return ob;
	}

	public static boolean isNull(Object ob) {
		if (ob == null)
			return true;

		if (ob instanceof String && ((String) ob).equalsIgnoreCase("null"))
			return true;

		if ((ob instanceof JSONObject) && (JSONObject.NULL.equals((JSONObject) ob)))
			return true;

		return false;
	}

	public static boolean equalsNull(String ob) {
		return ob == null || ob.equalsIgnoreCase("null");
	}

	public static String giveEmptyForNullString(String ob) {
		if (ob == null || ob.equalsIgnoreCase("null"))
			return "";
		else
			return ob;
	}

	public static Map<String, String> giveEmptyForNullString(Map<String, String> m) {
		for (String key : m.keySet()) {
			if (isNull(m.get(key)))
				m.put(key, "");
		}

		return m;
	}

	public static Map<String, String> giveEmptyForNull(Map<String, Object> m) {
		
		HashMap<String, String> result = new HashMap<>();
		for (String key : m.keySet()) {
			if (isNull(m.get(key)))
				result.put(key, "");
			else
				result.put(key, "" + m.get(key));
		}
		
		return result;
	}

}
