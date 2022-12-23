package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uib.utilities.logging.logging;

/**
 * This class extends the Executioner abstract class in such a way that the data
 * will be retrieved by means of a JSON interface.
 * 
 * @author Rupert Roeder
 */
public abstract class JSONExecutioner extends Executioner {
	protected ConnectionState conStat;

	@Override
	public abstract JSONObject retrieveJSONObject(OpsiMethodCall omc);

	public JSONObject retrieveJSONResult(OpsiMethodCall omc) {
		JSONObject jO = retrieveJSONObject(omc);
		if (checkResponse(jO))
			return jO.optJSONObject("result");

		return null;
	}

	@Override
	public ConnectionState getConnectionState() {
		return conStat;
	}

	@Override
	public void setConnectionState(ConnectionState state) {
		conStat = state;
	}

	public boolean checkResponse(JSONObject retrieved) {
		return JSONReMapper.checkResponse(retrieved);
	}

	@Override
	public Object getValueFromJSONObject(Object o, String key) {
		Object value = null;
		try {
			value = ((JSONObject) o).get(key);
		} catch (JSONException jex) {
			logging.error("json error on getting value,  " + jex.toString());
		} catch (Exception ex) {
			logging.error("error on getting value,  " + ex.toString());
		}

		return value;
	}

	@Override
	public boolean doCall(OpsiMethodCall omc) {
		JSONObject jO = retrieveJSONObject(omc);
		// logging.debug ( " --------------- jO " + jO);
		// logging.debug (" " + checkResponse(jO));
		return checkResponse(jO);
	}

	@Override
	public List<List<String>> getListOfStringLists(OpsiMethodCall omc) {
		return JSONReMapper.getListOfStringLists(retrieveJSONObject(omc));
	}

	@Override
	public List getListResult(OpsiMethodCall omc) {
		return JSONReMapper.getListResult(retrieveJSONObject(omc));
	}

	@Override
	public List<String> getStringListResult(OpsiMethodCall omc) {
		return JSONReMapper.getStringListResult(retrieveJSONObject(omc));
	}

	@Override
	public Map getMapResult(OpsiMethodCall omc)
	// yields possibly JSON objects and arrays as values
	// compare getMap_Object
	{
		return JSONReMapper.getMapResult(retrieveJSONObject(omc));
	}

	public Map getMapOfLists(JSONObject jO) {
		HashMap result = new HashMap<>();
		try {
			if (jO != null) {
				Iterator iter = jO.keys();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					// logging.debug(this, "getMapOfLists, key " + key + " list value " +
					// JSONReMapper.getJsonList(jOResult, key));
					result.put(key, JSONReMapper.getJsonList(jO, key));
				}
			}
		} catch (Exception ex) {
			logging.error("Exception on getting Map " + ex.toString());
		}
		return result;
	}

	private Map getMapOfLists(OpsiMethodCall omc, boolean recursive) {
		Map result = new HashMap<>();
		try {
			JSONObject jO = retrieveJSONObject(omc);

			if (recursive && !JSONReMapper.checkForNotValidOpsiMethod(jO)) {
				result = getMapOfLists(omc.activateExtendedRpcPath(), false);
			} else {
				if (checkResponse(jO)) {
					JSONObject jOResult = jO.optJSONObject("result");

					if (jOResult != null) {
						Iterator iter = jOResult.keys();
						while (iter.hasNext()) {
							String key = (String) iter.next();
							// logging.debug(this, "getMapOfLists, key " + key + " list value " +
							// JSONReMapper.getJsonList(jOResult, key));
							result.put(key, JSONReMapper.getJsonList(jOResult, key));
						}
					}
				}
			}
		} catch (Exception ex) {
			logging.error(this, "Exception on getting Map " + ex.toString());
		}
		return result;
	}

	@Override
	public Map getMapOfLists(OpsiMethodCall omc) {
		return getMapOfLists(omc, true);
	}

	@Override
	public Map getMapOfMaps(OpsiMethodCall omc) {
		HashMap result = new HashMap<>();
		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");

				if (jOResult != null) {
					Iterator iter = jOResult.keys();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						HashMap inner = new HashMap<>();
						JSONObject jsonInner = (JSONObject) jOResult.get(key);
						if (jsonInner != null) {
							Iterator iter2 = jsonInner.keys();
							while (iter2.hasNext()) {
								String key2 = (String) iter2.next();
								if (!jsonInner.isNull(key2))
									inner.put(key2, jsonInner.get(key2));
							}
						}
						result.put(key, inner);
					}
				}
			}
		} catch (Exception ex) {
			logging.error(this, "Exception on getting Map " + ex.toString());
		}
		return result;
	}

	@Override
	public Map<String, Object> getMap_Object(OpsiMethodCall omc)
	// this method tries to return Java lists in comparison with getMapResult
	{
		return JSONReMapper.getMap_Object(retrieveJSONObject(omc));
	}

	@Override
	public Map<String, Map<String, Object>> getMap2_Object(OpsiMethodCall omc)
	// including a conversion of json objects to a standard java object

	{
		HashMap<String, Map<String, Object>> result = new HashMap<>();
		HashMap<String, Map<String, Object>> resultNull = new HashMap<>();

		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");
				JSONObjectX jOX = new JSONObjectX(jOResult);

				if (!jOX.isMap()) {
					logging.error(this, "map expected " + jOX);
				} else {
					logging.debug(this, "map retrieved ");
					// + jOX.getMap());
					Map map0 = jOX.getMap();

					Iterator iter0 = map0.keySet().iterator();
					while (iter0.hasNext()) {
						String key1 = (String) iter0.next();

						JSONObjectX jOX1 = new JSONObjectX((JSONObject) map0.get(key1));

						if (!jOX1.isMap()) {
							logging.error(this, "map expected in level 2 " + jOX1);
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

	/*
	 * public Map<String, Map<String, Map<String, Object>>> getMap3_Object(Object
	 * retrieved)
	 * {
	 * HashMap<String, Map<String, Map<String, Object>>> result = new
	 * HashMap<String, Map<String, Map<String, Object>>>();
	 * try
	 * {
	 * JSONObject jO = (JSONObject) retrieved;
	 * if (checkResponse(jO))
	 * {
	 * JSONObject jOResult = jO.optJSONObject ("result");
	 * 
	 * HashMap<String, JSONObject> map0 = new HashMapX<String,
	 * JSONObject>(jOResult);
	 * //logging.debug(this, " map0: " + map0);
	 * 
	 * Iterator iter0 = map0.keySet().iterator();
	 * while (iter0.hasNext())
	 * {
	 * String key1 = (String) iter0.next(); //e.g. client
	 * HashMap<String, JSONObject> map1 =new HashMapX<>(
	 * (JSONObject) map0.get(key1) ); //e.g. map of 1 client values
	 * //logging.debug(this, " key1 " + key1 + " value " + map1);
	 * HashMap<String, Map<String, Object>> map1R = new HashMap<String, Map<String,
	 * Object>>(); // to produce
	 * 
	 * Iterator iter1 = map1.keySet().iterator();
	 * while (iter1.hasNext())
	 * {
	 * String key2 = (String) iter1.next(); //e.g. product
	 * HashMap<String, Object> map2 = new HashMapX<>((JSONObject)
	 * map1.get(key2), true); //e.g. product values;
	 * //logging.debug(this, " key2 " + key2 + " value " + map2);
	 * map1R.put(key2, map2);
	 * //logging.debug(this, " map1R.get(key2) " + map1R.get(key2));
	 * }
	 * 
	 * result.put(key1, map1R);
	 * }
	 * }
	 * }
	 * catch (Exception ex)
	 * {
	 * logging.debug("getMap3_String: " + ex.toString());
	 * }
	 * 
	 * return result;
	 * 
	 * }
	 */

	@Override
	public Map<String, Map<String, Map<String, Object>>> getMap3_Object(OpsiMethodCall omc) {
		return JSONReMapper.getMap3_Object(retrieveJSONObject(omc));
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key) {
		return getStringMappedObjectsByKey(omc, key, null, null);
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars) {
		return getStringMappedObjectsByKey(omc, key, sourceVars, targetVars, null);
	}

	@Override
	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars, Map<String, String> translateValues)

	{
		Map<String, Map<String, String>> result = new TreeMap<>();

		List<JSONObject> resultlist = null;

		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				resultlist = JSONReMapper.getJsonList(jO, "result");
			}

		} catch (Exception ex) {
			logging.error(this, "Exception on getting list for key \"result\" " + ex.toString());
		}

		// extract key
		if (resultlist != null) {
			try {

				Iterator iter = resultlist.iterator();
				while (iter.hasNext()) {
					JSONObject jO = (JSONObject) iter.next();
					// logging.debug("jO " + jO);

					String keyOfItem = null;

					if (jO.get(key) == null) {
						logging.error(this, "Missing key " + key + " in output list for " + omc);
						continue;
					}

					keyOfItem = jO.get(key).toString();

					if (translateValues != null && translateValues.get(keyOfItem) != null)
						keyOfItem = translateValues.get(keyOfItem);

					HashMap<String, String> detailMap = new HashMap<>();

					if (sourceVars == null) {
						// take original keys

						Iterator iterKeys = jO.keys();
						while (iterKeys.hasNext()) {
							String var = iterKeys.next().toString();
							String val = jO.get(var).toString();
							detailMap.put(var, val);
						}
					} else {
						if (targetVars == null) {
							for (int i = 0; i < sourceVars.length; i++) {
								String var = sourceVars[i];
								String val = jO.get(var).toString();

								if (translateValues != null && translateValues.get(val) != null)
									val = translateValues.get(val);

								detailMap.put(var, val);
							}
						} else {
							if (targetVars.length != sourceVars.length)
								logging.warning(this, "getStringMappedObjectsByKey targetVars not assignable");

							for (int i = 0; i < sourceVars.length; i++) {
								String var = sourceVars[i];
								String val = jO.get(var).toString();
								if (i < targetVars.length) {
									var = targetVars[i];

									if (translateValues != null && translateValues.get(val) != null)
										val = translateValues.get(val);

									detailMap.put(var, val);

								}
							}
						}
					}

					result.put(keyOfItem, detailMap);
				} // loop through list elements

			} catch (Exception ex) {
				logging.error(this, "Exception on building string maps  " + ex.toString());
			}
		}

		// logging.info (this, " getStringMappedObjectsByKey result:" + result);
		return result;

	}

	@Override
	public Map getMapOfListsOfMaps(OpsiMethodCall omc) {
		// TODO: Performance
		HashMap result = new HashMap<>();
		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");

				if (jOResult != null) {
					Iterator iter = jOResult.keys();
					while (iter.hasNext()) {
						String key = (String) iter.next();

						JSONArray jA = jOResult.optJSONArray(key);
						ArrayList al = new ArrayList<>(jA.length());

						for (int i = 0; i < jA.length(); i++) {
							HashMap inner = new HashMap<>();
							JSONObject jsonInner = (JSONObject) jA.get(i);
							if (jsonInner != null) {
								Iterator iter2 = jsonInner.keys();
								while (iter2.hasNext()) {
									String key2 = (String) iter2.next();
									if (!jsonInner.isNull(key2))
										inner.put(key2, jsonInner.get(key2));
								}
							}
							al.add(inner);
						}
						result.put(key, al);
					}
				}
			}
		} catch (Exception ex) {
			logging.error(this, "Exception on getting Map " + ex.toString());
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		return JSONReMapper.getListOfMaps(retrieveJSONObject(omc));
	}

	@Override
	public List<Map<String, String>> getListOfStringMaps(OpsiMethodCall omc) {
		return JSONReMapper.getListOfStringMaps(retrieveJSONObject(omc));
	}

	@Override
	public List getListOfMapsOfListsOfMaps(OpsiMethodCall omc) {
		// TODO: Performance
		ArrayList result = null;
		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				// JSONObject jOResult = jO.optJSONObject ("result");
				JSONArray jA1 = jO.optJSONArray("result");

				if (jA1 != null) {
					result = new ArrayList<>(jA1.length());

					for (int i = 0; i < jA1.length(); i++) {
						HashMap inner1 = new HashMap<>();
						JSONObject jsonInner1 = (JSONObject) jA1.get(i);
						if (jsonInner1 != null) {
							Iterator iter = jsonInner1.keys();
							while (iter.hasNext()) {
								String key = (String) iter.next();
								try {
									JSONArray jA2 = jsonInner1.optJSONArray(key);
									ArrayList al2 = new ArrayList<>(jA2.length());
									for (int j = 0; j < jA2.length(); j++) {
										HashMap inner2 = new HashMap<>();
										JSONObject jsonInner2 = (JSONObject) jA2.get(j);
										if (jsonInner2 != null) {
											Iterator iter2 = jsonInner2.keys();
											while (iter2.hasNext()) {
												String key2 = (String) iter2.next();
												if (!jsonInner2.isNull(key2))
													inner2.put(key2, jsonInner2.get(key2));
											}
										}
										al2.add(inner2);
									}
									inner1.put(key, al2);
								} catch (Exception e) {
									HashMap inner2 = new HashMap<>();
									JSONObject jsonInner2 = (JSONObject) jsonInner1.get(key);
									if (jsonInner2 != null) {
										Iterator iter2 = jsonInner2.keys();
										while (iter2.hasNext()) {
											String key2 = (String) iter2.next();
											if (!jsonInner2.isNull(key2))
												inner2.put(key2, jsonInner2.get(key2));
										}
									}
									inner1.put(key, inner2);
								}
							}
						}
						result.add(inner1);
					}
				}
			}
		} catch (Exception ex) {
			logging.error(this, "Exception on getting ListOfMapsOfListsOfMaps " + ex.toString());
		}
		return result;
	}

	@Override
	public String getStringResult(OpsiMethodCall omc) {
		String result = null;

		JSONObject jO = retrieveJSONObject(omc);

		if (checkResponse(jO)) {
			try {
				result = (String) jO.get("result");
			} catch (Exception jsonEx) {
			}

			// logging.debug (" ------------- getStringResult() " + result);
		}

		return result;
	}

	@Override
	public boolean getBooleanResult(OpsiMethodCall omc) {
		// logging.info(this, "getBooleanResult " + omc);
		Boolean result = null;

		JSONObject jO = retrieveJSONObject(omc);

		// logging.info(this, "getBooleanResult " + jO);

		if (!JSONReMapper.checkForNotValidOpsiMethod(jO)) {
			result = getBooleanResult(omc.activateExtendedRpcPath());
		} else {
			if (checkResponse(jO)) {
				try {
					result = (Boolean) jO.get("result");
				} catch (Exception jsonEx) {
				}
				// logging.debug (" ------------- getBooleanResult() " + result);
			}
		}

		if (result == null)
			return false;

		return result.booleanValue();
	}

	@Override
	public String getStringValueFromItem(Object s) {
		if (s instanceof String) {
			return (String) s;
		}
		return null;
	}

	@Override
	public Map<String, Object> getMapFromItem(Object s) {
		HashMap<String, Object> result = new HashMap<>();
		if (s == null)
			return result;

		try {
			JSONObject jO = null;
			boolean wehavejO = true;

			if (s instanceof JSONObject) {
				jO = (JSONObject) s;
			} else {
				try {
					jO = new JSONObject("" + s);
				} catch (Exception ex) {
					logging.warning("JSONExecutioner.getMapFromItem \"" + s + "\"  "
							+ " cannot be interpreted as a JSON Object, " + ex);
					wehavejO = false;
				}
			}

			if (wehavejO) {
				// logging.debug (" +++++++++++++++ JSONObject jO " + jO.toString());
				if (jO != null && jO != JSONObject.NULL) {
					// logging.debug ("JO keys");
					Iterator iter = jO.keys();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						// logging.debug (key);
						result.put(key, jO.get(key));
					}
				}
			}

			if (!wehavejO || jO == JSONObject.NULL) {
				if (s == JSONObject.NULL) {
					logging.warning("JSONExecutioner.getMapFromItem \"" + s
							+ "\" is  JSONObject.NULL and cannot be cast to a JSON Object");
				}

				else {

					logging.warning("JSONExecutioner.getMapFromItem \"" + s + "\" has class " + s.getClass().getName()
							+ " cannot be cast to a JSON Object");
				}
			}
		} catch (Exception ex) {
			logging.error(this, "Exception on getting map from item  " + s + " : " + ex.toString());
		}

		return result;
	}

	@Override
	public List getListFromItem(String s) {
		ArrayList result = new ArrayList<>();

		if (s == null || s.equals("null"))
			return result;

		try {
			JSONArray ar = new JSONArray(s);
			for (int i = 0; i < ar.length(); i++) {
				result.add(ar.get(i));
			}
		} catch (Exception ex) {
			logging.error(this, "Exception on getting list from item    " + s + " : " + ex.toString());
		}

		return result;

	}

}
