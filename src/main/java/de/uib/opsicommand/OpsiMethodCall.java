/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.logging.Logging;

public class OpsiMethodCall {
	public static final boolean BACKGROUND_DEFAULT = true;
	private static final int DEFAULT_JSON_ID = 1;
	private static final List<String> collectedCalls = new ArrayList<>();
	private static int maxCollectSize = -1;

	private String methodname;

	private Object[] parameters;

	private boolean background;

	/**
	 * @param rpcPath    subpath for the rpc call (not including "/rpc/")
	 * @param methodName name of rpc method
	 * @param parameters the parameters for the method
	 * @param background if background then no waiting info is shown
	 */
	public OpsiMethodCall(RPCMethodName methodname, Object[] parameters, boolean background) {
		this.methodname = methodname.toString();
		this.parameters = parameters;
		this.background = background;
		collectCall();
	}

	/**
	 * @param rpcPath    subpath for the rpc call (not including "/rpc/")
	 * @param methodName name of rpc method
	 * @param parameters the parameters for the method
	 */
	public OpsiMethodCall(RPCMethodName methodname, Object[] parameters) {
		this(methodname, parameters, false);
	}

	private void collectCall() {
		// -1 means deactivated; 0 means infinite
		if (maxCollectSize < 0 || (maxCollectSize != 0 && collectedCalls.size() >= maxCollectSize)) {
			return;
		}

		collectedCalls.add(this.getMethodname() + "\n\t" + this.getParameter());
	}

	public static void report() {
		Logging.debug(
				"================================================   collected calls, maxCollectSize " + maxCollectSize);

		for (String c : collectedCalls) {
			Logging.debug(c);
		}
		Logging.debug("================================================");
	}

	public String getMethodname() {
		return methodname;
	}

	public String getParameter() {
		return Arrays.toString(parameters);
	}

	public boolean isBackgroundDefault() {
		return background;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("method=");
		sb.append(methodname);
		sb.append(", ");
		sb.append("params=");
		sb.append("[");
		if (parameters != null && parameters.length > 0) {
			for (Object paramI : parameters) {
				if (paramI instanceof Object[]) {
					sb.append(Arrays.toString((Object[]) paramI));
				} else if (paramI instanceof Map) {
					sb.append(getMapString((Map<?, ?>) paramI));
				} else {
					sb.append("" + paramI);
				}
			}
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	private static String getMapString(Map<?, ?> map) {
		StringBuilder sb = new StringBuilder();

		sb.append("{");
		for (Entry<?, ?> entry : map.entrySet()) {
			sb.append(entry.getKey() + ": ");
			if (entry.getValue() instanceof Object[]) {
				sb.append(Arrays.toString((Object[]) entry.getValue()));
			} else {
				sb.append("" + entry.getValue());
			}
			sb.append(" ");
		}
		sb.append("}");

		return sb.toString();
	}

	public Map<String, Object> getOMCMap() {
		Map<String, Object> map = new HashMap<>();
		List<Object> params = new ArrayList<>();

		for (Object parameter : parameters) {
			if (parameter instanceof Object[]) {
				List<Object> list = Arrays.asList((Object[]) parameter);

				params.add(list);
			} else if (parameter instanceof Map) {
				params.add(parameter);
			} else {
				params.add(parameter);
			}
		}

		map.put("id", DEFAULT_JSON_ID);
		map.put("method", methodname);
		map.put("params", params);

		return map;
	}

	public static int getMaxCollecSize() {
		return maxCollectSize;
	}

	public static void setMaxCollectSize(int maxCollectSize) {
		OpsiMethodCall.maxCollectSize = maxCollectSize;
	}
}
