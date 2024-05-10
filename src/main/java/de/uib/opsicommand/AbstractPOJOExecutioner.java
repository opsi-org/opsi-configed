/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.utils.logging.Logging;

/**
 * This class extends the Executioner abstract class in such a way that the data
 * will be retrieved in POJO.
 */
public abstract class AbstractPOJOExecutioner {
	protected ConnectionState conStat;

	public abstract Map<String, Object> retrieveResponse(OpsiMethodCall omc);

	public ConnectionState getConnectionState() {
		return conStat;
	}

	public boolean doCall(OpsiMethodCall omc) {
		Map<String, Object> jO = retrieveResponse(omc);

		return checkResponse(jO);
	}

	public List<List<String>> getListOfStringLists(OpsiMethodCall omc) {
		List<List<String>> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"), new TypeReference<List<List<String>>>() {
			});
		}

		return result;
	}

	public List<Object> getListResult(OpsiMethodCall omc) {
		List<Object> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"), new TypeReference<List<Object>>() {
			});
		}

		return result;
	}

	public List<String> getStringListResult(OpsiMethodCall omc) {
		List<String> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"), new TypeReference<List<String>>() {
			});
		}

		return result;
	}

	public Map<String, Object> getMapResult(OpsiMethodCall omc) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"), new TypeReference<HashMap<String, Object>>() {
			});
		}

		return result;
	}

	public String getErrorFromResponse(Map<String, Object> retrieved) {
		String errorMessage = null;

		if (retrieved.containsKey("error") && retrieved.get("error") != null) {
			if (retrieved.get("error") instanceof Map) {
				Map<String, Object> error = POJOReMapper.remap(retrieved.get("error"),
						new TypeReference<Map<String, Object>>() {
						});

				if (error != null && error.get("class") != null && error.get("message") != null) {
					errorMessage = " [" + error.get("class") + "] " + error.get("message");
				}
			} else {
				errorMessage = " " + retrieved.get("error");
			}
		}

		return errorMessage;
	}

	public Map<String, Object> getResponses(Map<String, Object> retrieved) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> responses = POJOReMapper.remap(retrieved.get("result"),
				new TypeReference<Map<String, Object>>() {
				});

		for (Entry<String, Object> entry : responses.entrySet()) {
			Map<String, Object> response = POJOReMapper.remap(entry.getValue(),
					new TypeReference<Map<String, Object>>() {
					});

			if (response.get("error") == null) {
				List<Object> list = POJOReMapper.remap(response.get("result"), new TypeReference<List<Object>>() {
				});
				result.put(entry.getKey(), list);
			} else {
				String str = "" + response.get("error");
				result.put(entry.getKey(), str);
			}
		}

		Logging.debug(this, "getResponses  result " + result);

		return result;
	}

	// returns false if the "error" key does not exist or is null
	// Otherwise returns true which means call was successful
	private boolean checkResponse(Map<String, Object> retrieved) {
		if (retrieved == null) {
			return false;
		} else {
			String errorMessage = getErrorFromResponse(retrieved);

			if (errorMessage != null) {
				Logging.error("Opsi service error: " + errorMessage);

				return false;
			} else {
				Object resultValue = retrieved.get("result");
				Logging.debug(this, "got result " + resultValue);

				return true;
			}
		}
	}

	public Map<String, Map<String, String>> getStringMappedObjectsByKey(OpsiMethodCall omc, String key,
			String[] sourceVars, String[] targetVars) {
		List<Object> resultlist = getListResult(omc);

		if (resultlist == null) {
			return new TreeMap<>();
		}

		return generateStringMappedObjectsByKeyResult(resultlist, key, sourceVars, targetVars);
	}

	public static Map<String, Map<String, String>> generateStringMappedObjectsByKeyResult(Iterable<Object> objects,
			String key, String[] sourceVars, String[] targetVars) {
		Map<String, Map<String, String>> result = new TreeMap<>();

		for (Object object : objects) {
			Map<String, String> originalMap = POJOReMapper.remap(object, new TypeReference<Map<String, String>>() {
			});

			if (originalMap.get(key) == null) {
				Logging.error(AbstractPOJOExecutioner.class, "Missing key " + key + " in output list");
				continue;
			}

			String keyOfItem = originalMap.get(key);

			Map<String, String> detailMap = new HashMap<>();

			if (sourceVars == null) {
				detailMap.putAll(originalMap);
			} else if (targetVars == null) {
				detailMap = generateDetailMapBasedOnKeys(originalMap, sourceVars);
			} else {
				detailMap = generateDetailMapBasedOnKeys(originalMap, sourceVars, targetVars);
			}

			result.put(keyOfItem, detailMap);
		}

		return result;
	}

	private static Map<String, String> generateDetailMapBasedOnKeys(Map<String, String> originalMap,
			String[] sourceVars) {
		Map<String, String> detailMap = new HashMap<>();

		for (String value : sourceVars) {
			String val = String.valueOf(originalMap.get(value));

			detailMap.put(value, val);
		}

		return detailMap;
	}

	private static Map<String, String> generateDetailMapBasedOnKeys(Map<String, String> originalMap,
			String[] sourceVars, String[] targetVars) {
		Map<String, String> detailMap = new HashMap<>();

		if (targetVars.length != sourceVars.length) {
			Logging.warning(AbstractPOJOExecutioner.class, "generateDetailMapBasedOnKeys targetVars not assignable");
		}

		for (int i = 0; i < sourceVars.length; i++) {
			String value = sourceVars[i];
			String val = String.valueOf(originalMap.get(value));

			if (i < targetVars.length) {
				value = targetVars[i];

				detailMap.put(value, val);
			}
		}

		return detailMap;
	}

	public List<Map<String, Object>> getListOfMaps(OpsiMethodCall omc) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"), new TypeReference<List<Map<String, Object>>>() {
			});
		}

		return result;
	}

	public List<Map<String, List<Map<String, Object>>>> getListOfMapsOfListsOfMaps(OpsiMethodCall omc) {
		List<Map<String, List<Map<String, Object>>>> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"),
					new TypeReference<List<Map<String, List<Map<String, Object>>>>>() {
					});
		}

		return result;
	}

	public String getStringResult(OpsiMethodCall omc) {
		String result = "";
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"), new TypeReference<String>() {
			});
		}

		return result;
	}

	public boolean getBooleanResult(OpsiMethodCall omc) {
		Boolean result = null;
		Map<String, Object> response = retrieveResponse(omc);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"), new TypeReference<Boolean>() {
			});
		}

		return Boolean.TRUE.equals(result);
	}

	public Map<String, Object> getMapFromItem(Object item) {
		return POJOReMapper.remap(item, new TypeReference<Map<String, Object>>() {
		});
	}

	public List<Object> getListFromItem(String item) {
		return POJOReMapper.remap(item, new TypeReference<List<Object>>() {
		});
	}
}
