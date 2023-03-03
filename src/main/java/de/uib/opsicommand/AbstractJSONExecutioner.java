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

import de.uib.utilities.logging.Logging;

/**
 * This class extends the Executioner abstract class in such a way that the data
 * will be retrieved by means of a JSON interface.
 * 
 * @author Rupert Roeder
 */
public abstract class AbstractJSONExecutioner extends AbstractExecutioner {
	protected ConnectionState conStat;

	@Override
	public abstract JSONObject retrieveJSONObject(OpsiMethodCall omc);

	public JSONObject retrieveJSONResult(OpsiMethodCall omc) {
		JSONObject jO = retrieveJSONObject(omc);
		if (checkResponse(jO)) {
			return jO.optJSONObject("result");
		}

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
			Logging.error("json error on getting value,  " + jex.toString());
		} catch (Exception ex) {
			Logging.error("error on getting value,  " + ex.toString());
		}

		return value;
	}

	@Override
	public boolean doCall(OpsiMethodCall omc) {
		JSONObject jO = retrieveJSONObject(omc);
		return checkResponse(jO);
	}

	@Override
	public List<List<String>> getListOfStringLists(OpsiMethodCall omc) {
		return JSONReMapper.getListOfStringLists(retrieveJSONObject(omc));
	}

	@Override
	public List<Object> getListResult(OpsiMethodCall omc) {
		return JSONReMapper.getListResult(retrieveJSONObject(omc));
	}

	@Override
	public List<String> getStringListResult(OpsiMethodCall omc) {
		return JSONReMapper.getStringListResult(retrieveJSONObject(omc));
	}

	@Override
	public Map<String, Object> getMapResult(OpsiMethodCall omc)
	// yields possibly JSON objects and arrays as values
	// compare getMap_Object
	{
		return JSONReMapper.getMapResult(retrieveJSONObject(omc));
	}

	public Map<String, List<Object>> getMapOfLists(JSONObject jO) {
		Map<String, List<Object>> result = new HashMap<>();
		try {
			if (jO != null) {
				Iterator<String> iter = jO.keys();
				while (iter.hasNext()) {
					String key = iter.next();

					result.put(key, JSONReMapper.getJsonList(jO, key));
				}
			}
		} catch (Exception ex) {
			Logging.error("Exception on getting Map " + ex.toString());
		}
		return result;
	}

	private Map<String, List<Object>> getMapOfLists(OpsiMethodCall omc, boolean recursive) {
		Map<String, List<Object>> result = new HashMap<>();
		try {
			JSONObject jO = retrieveJSONObject(omc);

			if (recursive && !JSONReMapper.checkForNotValidOpsiMethod(jO)) {
				result = getMapOfLists(omc.activateExtendedRpcPath(), false);
			} else {
				if (checkResponse(jO)) {
					JSONObject jOResult = jO.optJSONObject("result");

					if (jOResult != null) {
						Iterator<String> iter = jOResult.keys();
						while (iter.hasNext()) {
							String key = iter.next();

							result.put(key, JSONReMapper.getJsonList(jOResult, key));
						}
					}
				}
			}
		} catch (Exception ex) {
			Logging.error(this, "Exception on getting Map " + ex.toString());
		}
		return result;
	}

	@Override
	public Map<String, List<Object>> getMapOfLists(OpsiMethodCall omc) {
		return getMapOfLists(omc, true);
	}

	@Override
	public Map<String, Map<String, Object>> getMapOfMaps(OpsiMethodCall omc) {
		Map<String, Map<String, Object>> result = new HashMap<>();
		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");

				if (jOResult != null) {
					Iterator<String> iter = jOResult.keys();
					while (iter.hasNext()) {
						String key = iter.next();
						Map<String, Object> inner = new HashMap<>();
						JSONObject jsonInner = (JSONObject) jOResult.get(key);
						if (jsonInner != null) {
							Iterator<String> iter2 = jsonInner.keys();
							while (iter2.hasNext()) {
								String key2 = iter2.next();
								if (!jsonInner.isNull(key2)) {
									inner.put(key2, jsonInner.get(key2));
								}
							}
						}
						result.put(key, inner);
					}
				}
			}
		} catch (Exception ex) {
			Logging.error(this, "Exception on getting Map " + ex.toString());
		}
		return result;
	}

	@Override
	public Map<String, Object> getMapObject(OpsiMethodCall omc)
	// this method tries to return Java lists in comparison with getMapResult
	{
		return JSONReMapper.getMapObject(retrieveJSONObject(omc));
	}

	@Override
	public Map<String, Map<String, Object>> getMap2Object(OpsiMethodCall omc)
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
					Logging.error(this, "map expected " + jOX);
				} else {
					Logging.debug(this, "map retrieved ");

					Map<String, Object> map0 = jOX.getMap();

					Iterator<String> iter0 = map0.keySet().iterator();
					while (iter0.hasNext()) {
						String key1 = iter0.next();

						JSONObjectX jOX1 = new JSONObjectX((JSONObject) map0.get(key1));

						if (!jOX1.isMap()) {
							Logging.error(this, "map expected in level 2 " + jOX1);
							result = resultNull;
						} else {
							result.put(key1, jOX1.getMap());
						}
					}
				}
			}
		} catch (Exception ex) {
			Logging.error("this, getMap2_Object : " + ex.toString());
		}

		return result;

	}

	@Override
	public Map<String, Map<String, Map<String, Object>>> getMap3Object(OpsiMethodCall omc) {
		return JSONReMapper.getMap3Object(retrieveJSONObject(omc));
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
			String[] sourceVars, String[] targetVars, Map<String, String> translateValues) {

		Map<String, Map<String, String>> result = new TreeMap<>();

		List<Object> resultlist = null;

		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				resultlist = JSONReMapper.getJsonList(jO, "result");
			}

		} catch (Exception ex) {
			Logging.error(this, "Exception on getting list for key \"result\" " + ex.toString());
		}

		// extract key
		if (resultlist != null) {
			try {

				Iterator<Object> iter = resultlist.iterator();

				// loop through list elements
				while (iter.hasNext()) {
					JSONObject jO = (JSONObject) iter.next();

					String keyOfItem = null;

					if (jO.get(key) == null) {
						Logging.error(this, "Missing key " + key + " in output list for " + omc);
						continue;
					}

					keyOfItem = jO.get(key).toString();

					if (translateValues != null && translateValues.get(keyOfItem) != null) {
						keyOfItem = translateValues.get(keyOfItem);
					}

					HashMap<String, String> detailMap = new HashMap<>();

					if (sourceVars == null) {
						// take original keys

						Iterator<String> iterKeys = jO.keys();
						while (iterKeys.hasNext()) {
							String value = iterKeys.next();
							String val = jO.get(value).toString();
							detailMap.put(value, val);
						}
					} else {
						if (targetVars == null) {
							for (int i = 0; i < sourceVars.length; i++) {
								String value = sourceVars[i];
								String val = jO.get(value).toString();

								if (translateValues != null && translateValues.get(val) != null) {
									val = translateValues.get(val);
								}

								detailMap.put(value, val);
							}
						} else {
							if (targetVars.length != sourceVars.length) {
								Logging.warning(this, "getStringMappedObjectsByKey targetVars not assignable");
							}

							for (int i = 0; i < sourceVars.length; i++) {
								String value = sourceVars[i];
								String val = jO.get(value).toString();
								if (i < targetVars.length) {
									value = targetVars[i];

									if (translateValues != null && translateValues.get(val) != null) {
										val = translateValues.get(val);
									}

									detailMap.put(value, val);

								}
							}
						}
					}

					result.put(keyOfItem, detailMap);
				}

			} catch (Exception ex) {
				Logging.error(this, "Exception on building string maps  " + ex.toString());
			}
		}

		return result;

	}

	@Override
	public Map<String, List<Map<String, Object>>> getMapOfListsOfMaps(OpsiMethodCall omc) {
		// TODO: Performance
		Map<String, List<Map<String, Object>>> result = new HashMap<>();
		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {
				JSONObject jOResult = jO.optJSONObject("result");

				if (jOResult != null) {
					Iterator<String> iter = jOResult.keys();
					while (iter.hasNext()) {
						String key = iter.next();

						JSONArray jA = jOResult.optJSONArray(key);
						List<Map<String, Object>> al = new ArrayList<>(jA.length());

						for (int i = 0; i < jA.length(); i++) {
							Map<String, Object> inner = new HashMap<>();
							JSONObject jsonInner = (JSONObject) jA.get(i);
							if (jsonInner != null) {
								Iterator<String> iter2 = jsonInner.keys();
								while (iter2.hasNext()) {
									String key2 = iter2.next();
									if (!jsonInner.isNull(key2)) {
										inner.put(key2, jsonInner.get(key2));
									}
								}
							}
							al.add(inner);
						}
						result.put(key, al);
					}
				}
			}
		} catch (Exception ex) {
			Logging.error(this, "Exception on getting Map " + ex.toString());
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		return JSONReMapper.getListOfMaps(retrieveJSONObject(omc));
	}

	@Override
	public List<Map<String, Object>> getListOfStringMaps(OpsiMethodCall omc) {
		return JSONReMapper.getListOfStringMaps(retrieveJSONObject(omc));
	}

	@Override
	public List<Map<String, List<Map<String, Object>>>> getListOfMapsOfListsOfMaps(OpsiMethodCall omc) {
		// TODO: Performance
		List<Map<String, List<Map<String, Object>>>> result = null;
		try {
			JSONObject jO = retrieveJSONObject(omc);
			if (checkResponse(jO)) {

				JSONArray jA1 = jO.optJSONArray("result");

				if (jA1 != null) {
					result = new ArrayList<>(jA1.length());

					for (int i = 0; i < jA1.length(); i++) {
						Map<String, List<Map<String, Object>>> inner1 = new HashMap<>();
						JSONObject jsonInner1 = (JSONObject) jA1.get(i);
						if (jsonInner1 != null) {
							Iterator<String> iter = jsonInner1.keys();
							while (iter.hasNext()) {
								String key = iter.next();

								JSONArray jA2 = jsonInner1.optJSONArray(key);
								if (jA2 != null) {

									List<Map<String, Object>> al2 = new ArrayList<>(jA2.length());
									for (int j = 0; j < jA2.length(); j++) {
										Map<String, Object> inner2 = new HashMap<>();
										JSONObject jsonInner2 = (JSONObject) jA2.get(j);
										if (jsonInner2 != null) {
											Iterator<String> iter2 = jsonInner2.keys();
											while (iter2.hasNext()) {
												String key2 = iter2.next();
												if (!jsonInner2.isNull(key2)) {
													inner2.put(key2, jsonInner2.get(key2));
												}
											}
										}
										al2.add(inner2);
									}
									inner1.put(key, al2);
								} else {
									// Now we don't have a JSONArray, but a JSONObject.
									// We will put what there is inside into a List of one element
									List<Map<String, Object>> al2 = new ArrayList<>();

									Map<String, Object> inner2 = new HashMap<>();
									JSONObject jsonInner2 = (JSONObject) jsonInner1.get(key);

									if (jsonInner2 != null) {
										Iterator<String> iter2 = jsonInner2.keys();
										while (iter2.hasNext()) {
											String key2 = iter2.next();
											if (!jsonInner2.isNull(key2)) {
												inner2.put(key2, jsonInner2.get(key2));
											}
										}
									}
									al2.add(inner2);
									inner1.put(key, al2);
								}
							}
						}
						result.add(inner1);
					}
				}
			}
		} catch (Exception ex) {
			Logging.error(this, "Exception on getting ListOfMapsOfListsOfMaps " + ex.toString());
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
			} catch (JSONException jsonEx) {
				Logging.warning(this, "Cannot get 'result' from jsonobject in getStringResult", jsonEx);
			}
		}

		return result;
	}

	@Override
	public boolean getBooleanResult(OpsiMethodCall omc) {

		Boolean result = null;

		JSONObject jO = retrieveJSONObject(omc);

		if (!JSONReMapper.checkForNotValidOpsiMethod(jO)) {
			result = getBooleanResult(omc.activateExtendedRpcPath());
		} else {
			if (checkResponse(jO)) {
				try {
					result = (Boolean) jO.get("result");
				} catch (Exception jsonEx) {
					Logging.warning(this, "Cannot get 'result' from jsonobject in getStringResult", jsonEx);
				}

			}
		}

		if (result == null) {
			return false;
		}

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
		if (s == null) {
			return result;
		}

		try {
			JSONObject jO = null;
			boolean wehavejO = true;

			if (s instanceof JSONObject) {
				jO = (JSONObject) s;
			} else {
				try {
					jO = new JSONObject("" + s);
				} catch (Exception ex) {
					Logging.warning("JSONExecutioner.getMapFromItem \"" + s + "\"  "
							+ " cannot be interpreted as a JSON Object, " + ex);
					wehavejO = false;
				}
			}

			// Surely jO does not equal null
			if (wehavejO && jO != JSONObject.NULL) {

				Iterator<String> iter = jO.keys();
				while (iter.hasNext()) {
					String key = iter.next();

					result.put(key, jO.get(key));
				}

			}

			if (!wehavejO || jO == JSONObject.NULL) {
				if (s == JSONObject.NULL) {
					Logging.warning("JSONExecutioner.getMapFromItem \"" + s
							+ "\" is  JSONObject.NULL and cannot be cast to a JSON Object");
				}

				else {

					Logging.warning("JSONExecutioner.getMapFromItem \"" + s + "\" has class " + s.getClass().getName()
							+ " cannot be cast to a JSON Object");
				}
			}
		} catch (Exception ex) {
			Logging.error(this, "Exception on getting map from item  " + s + " : " + ex.toString());
		}

		return result;
	}

	@Override
	public List<Object> getListFromItem(String s) {
		List<Object> result = new ArrayList<>();

		if (s == null || s.equals("null")) {
			return result;
		}

		try {
			JSONArray ar = new JSONArray(s);
			for (int i = 0; i < ar.length(); i++) {
				result.add(ar.get(i));
			}
		} catch (Exception ex) {
			Logging.error(this, "Exception on getting list from item    " + s + " : " + ex.toString());
		}

		return result;
	}
}
