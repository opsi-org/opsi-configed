/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.share.logging.Logging;

/**
 * This class extends the Executioner abstract class in such a way that the data
 * will be retrieved in POJO.
 */
// We allow the use of varargs here for the parameters of the rpc calls
@SuppressWarnings("java:S923")
public abstract class AbstractPOJOExecutioner {
	private final AtomicReference<ConnectionState> conStat = new AtomicReference<>();

	public abstract Map<String, Object> retrieveResponse(RPCMethodName methodname, Object[] parameters);

	public ConnectionState getConnectionState() {
		return conStat.get();
	}

	public void setConnectionState(ConnectionState newState) {
		conStat.set(newState);
	}

	public boolean doCall(RPCMethodName methodname, Object... parameters) {
		Map<String, Object> jO = retrieveResponse(methodname, parameters);

		return checkResponse(jO);
	}

	public List<Object> getListResult(RPCMethodName methodname, Object... parameters) {
		List<Object> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(methodname, parameters);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"));
		}

		return result;
	}

	public Map<String, Map<String, Object>> getMapOfMaps(RPCMethodName methodname, Object... parameters) {
		Map<String, Map<String, Object>> result = new HashMap<>();
		Map<String, Object> response = retrieveResponse(methodname, parameters);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"));
		}

		return result;
	}

	public List<String> getStringListResult(RPCMethodName methodname, Object... parameters) {
		List<String> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(methodname, parameters);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"));
		}

		return result;
	}

	public Map<String, Object> getMapResult(RPCMethodName methodname, Object... parameters) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> response = retrieveResponse(methodname, parameters);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"));
		}

		return result;
	}

	public String getErrorFromResponse(Map<String, Object> retrieved) {
		String errorMessage = null;

		if (retrieved.containsKey("error") && retrieved.get("error") != null) {
			if (retrieved.get("error") instanceof Map) {
				Map<String, Object> error = POJOReMapper.remap(retrieved.get("error"));

				if (error != null && error.get("class") != null && error.get("message") != null) {
					errorMessage = " [" + error.get("class") + "] " + error.get("message");
				}
			} else {
				errorMessage = " " + retrieved.get("error");
			}
		}

		return errorMessage;
	}

	// returns false if the "error" key does not exist or is null
	// Otherwise returns true which means call was successful
	private boolean checkResponse(Map<String, Object> retrieved) {
		if (retrieved == null) {
			return false;
		} else {
			String errorMessage = getErrorFromResponse(retrieved);

			if (errorMessage != null) {
				Logging.error("Opsi service error: ", errorMessage);

				return false;
			} else {
				Object resultValue = retrieved.get("result");
				Logging.debug(this, "got result ", resultValue);

				return true;
			}
		}
	}

	public Map<String, Map<String, String>> getStringMappedObjectsByKey(RPCMethodName methodname, Object[] parameters,
			String key, String[] sourceVars, String[] targetVars) {
		List<Object> resultlist = getListResult(methodname, parameters);

		if (resultlist == null) {
			return new TreeMap<>();
		}

		return generateStringMappedObjectsByKeyResult(resultlist, key, sourceVars, targetVars);
	}

	public static Map<String, Map<String, String>> generateStringMappedObjectsByKeyResult(Iterable<Object> objects,
			String key, String[] sourceVars, String[] targetVars) {
		Map<String, Map<String, String>> result = new TreeMap<>();

		for (Object object : objects) {
			Map<String, String> originalMap = POJOReMapper.remap(object);

			if (originalMap.get(key) == null) {
				Logging.error(AbstractPOJOExecutioner.class, "Missing key ", key, " in output list");
				continue;
			}

			String keyOfItem = originalMap.get(key);

			Map<String, String> detailMap = generateDetailMapBasedOnKeys(originalMap, sourceVars, targetVars);

			result.put(keyOfItem, detailMap);
		}

		return result;
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

	public List<Map<String, Object>> getListOfMaps(RPCMethodName methodname, Object... parameters) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> response = retrieveResponse(methodname, parameters);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = POJOReMapper.remap(response.get("result"));
		}

		return result;
	}

	public String getStringResult(RPCMethodName methodname, Object... parameters) {
		String result = "";
		Map<String, Object> response = retrieveResponse(methodname, parameters);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = (String) response.get("result");
		}

		return result;
	}

	public boolean getBooleanResult(RPCMethodName methodname, Object... parameters) {
		Boolean result = null;
		Map<String, Object> response = retrieveResponse(methodname, parameters);

		if (checkResponse(response) && response.containsKey("result") && response.get("result") != null) {
			result = (Boolean) response.get("result");
		}

		return Boolean.TRUE.equals(result);
	}
}
