package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	public ConnectionState getConnectionState() {
		return conStat;
	}

	@Override
	public boolean doCall(OpsiMethodCall omc) {
		Map<String, Object> jO = retrieveJSONObject(omc);

		return checkResponse(jO);
	}

	@Override
	public List<List<String>> getListOfStringLists(OpsiMethodCall omc) {
		List<List<String>> result = new ArrayList<>();
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<List<List<String>>>() {
			});
		}

		return result;
	}

	@Override
	public List<Object> getListResult(OpsiMethodCall omc) {
		List<Object> result = new ArrayList<>();
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<List<Object>>() {
			});
		}

		return result;
	}

	@Override
	public List<String> getStringListResult(OpsiMethodCall omc) {
		List<String> result = new ArrayList<>();
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<List<String>>() {
			});
		}

		return result;
	}

	@Override
	public Map<String, Object> getMapResult(OpsiMethodCall omc) {
		// yields possibly JSON objects and arrays as values
		// compare getMap_Object
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<HashMap<String, Object>>() {
			});
		}

		return result;
	}

	@Override
	public Map<String, List<String>> getMapOfStringLists(OpsiMethodCall omc) {
		Map<String, List<String>> result = new HashMap<>();
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<HashMap<String, List<String>>>() {
			});
		}

		return result;
	}

	private static <T> T convertToObject(Object obj, TypeReference<T> typeRef) {
		T result = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

		result = mapper.convertValue(obj, typeRef);

		return result;
	}

	public static String getErrorFromResponse(Map<String, Object> retrieved) {
		String errorMessage = null;

		try {
			if (retrieved.containsKey("error") && retrieved.get("error") != null) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> error = mapper.readValue(mapper.writeValueAsString(retrieved.get("error")),
						new TypeReference<Map<String, Object>>() {
						});

				if (error != null && error.get("class") != null && error.get("message") != null) {
					errorMessage = " [" + error.get("class") + "] " + error.get("message");
				} else {
					errorMessage = " " + retrieved.get("error");
				}
			}
		} catch (JSONException | JsonProcessingException jex) {
			errorMessage = "JSON Error on retrieving result value,  " + jex;
		}

		return errorMessage;
	}

	@Override
	public Map<String, Object> getResponses(Map<String, Object> retrieved) {
		Map<String, Object> result = new HashMap<>();

		try {
			if (retrieved.get("error") == null) {
				List<?> list = (List<?>) retrieved.get("result");
				result.put("result", list);
			} else {
				String str = "" + retrieved.get("error");
				result.put("error", str);
			}
		} catch (Exception ex) {
			Logging.error("JSONReMapper getResponses ", ex);
		}

		Logging.debug("JSONReMapper getResponses  result " + result);

		return result;
	}

	public static boolean checkForNotValidOpsiMethod(Map<String, Object> retrieved) {
		String errorFromResponse = getErrorFromResponse(retrieved);

		if (errorFromResponse != null && errorFromResponse.indexOf("Opsi rpc error: Method") > -1
				&& errorFromResponse.endsWith("is not valid")) {
			Logging.info("JSONReMapper: checkForNotValidOpsiMethod " + getErrorFromResponse(retrieved));
			return false;
		}

		return true;
	}

	public static boolean checkResponse(Map<String, Object> retrieved) {
		boolean responseFound = true;

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
				Logging.debug("Null result in response ");
				responseFound = false;
			}
		}

		return responseFound;
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

		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			resultlist = convertToObject(response.get("result"), new TypeReference<List<Object>>() {
			});
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
				Logging.error(this, "Exception on building string maps  ", ex);
			}
		}

		return result;

	}

	@Override
	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<List<Map<String, Object>>>() {
			});
		}

		return result;
	}

	@Override
	public List<Map<String, List<Map<String, Object>>>> getListOfMapsOfListsOfMaps(OpsiMethodCall omc) {
		List<Map<String, List<Map<String, Object>>>> result = new ArrayList<>();
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"),
					new TypeReference<List<Map<String, List<Map<String, Object>>>>>() {
					});
		}

		return result;
	}

	@Override
	public String getStringResult(OpsiMethodCall omc) {
		String result = "";
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<String>() {
			});
		}

		return result;
	}

	@Override
	public boolean getBooleanResult(OpsiMethodCall omc) {
		Boolean result = null;
		Map<String, Object> response = retrieveJSONObject(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = convertToObject(response.get("result"), new TypeReference<Boolean>() {
			});
		}

		return result;
	}

	@Override
	public Map<String, Object> getMapFromItem(Object s) {
		Map<String, Object> result = null;

		result = convertToObject(s, new TypeReference<Map<String, Object>>() {
		});

		return result;
	}

	@Override
	public List<Object> getListFromItem(String s) {
		List<Object> result = null;

		result = convertToObject(s, new TypeReference<List<Object>>() {
		});

		return result;
	}
}
