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

import de.uib.utilities.logging.Logging;

public class OpsiMethodCall {

	public static final boolean BACKGROUND_DEFAULT = true;
	private static final int DEFAULT_JSON_ID = 1;
	private static final List<String> collectedCalls = new ArrayList<>();
	public static int maxCollectSize = -1;

	private String methodname;

	private Object[] parameters;

	private boolean background;

	/**
	 * @param rpcPath    subpath for the rpc call (not including "/rpc/")
	 * @param methodName name of rpc method
	 * @param parameters the parameters for the method
	 * @param background if background then no waiting info is shown
	 */
	public OpsiMethodCall(String methodname, Object[] parameters, boolean background) {
		this.methodname = methodname;
		this.parameters = parameters;
		this.background = background;
		collectCall();
	}

	/**
	 * @param rpcPath    subpath for the rpc call (not including "/rpc/")
	 * @param methodName name of rpc method
	 * @param parameters the parameters for the method
	 */
	public OpsiMethodCall(String methodname, Object[] parameters) {

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
			for (int i = 0; i < parameters.length; i++) {
				Object paramI = parameters[i];

				if (paramI instanceof Object[]) {
					sb.append(Arrays.toString((Object[]) paramI));
				} else if (paramI instanceof Map) {
					sb.append("{");

					for (Object key : ((Map<?, ?>) paramI).keySet()) {
						sb.append("" + key + ": ");
						if (((Map<?, ?>) paramI).get(key) instanceof Object[]) {
							sb.append(Arrays.toString((Object[]) ((Map<?, ?>) paramI).get(key)));
						} else {
							sb.append("" + ((Map<?, ?>) paramI).get(key));
						}
						sb.append(" ");
					}
					sb.append("}");
				} else {
					sb.append("" + paramI);
				}
			}
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();
	}

	public Map<String, Object> getOMCMap() {
		Map<String, Object> map = new HashMap<>();
		List<Object> params = new ArrayList<>();

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] instanceof Object[]) {
				List<Object> list = Arrays.asList((Object[]) parameters[i]);

				params.add(list);
			} else if (parameters[i] instanceof Map) {
				params.add(parameters[i]);
			} else {
				params.add(parameters[i]);
			}
		}

		map.put("id", DEFAULT_JSON_ID);
		map.put("method", methodname);
		map.put("params", params);

		return map;
	}
}
