package de.uib.opsicommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uib.utilities.logging.Logging;

public class OpsiMethodCall {

	public static final boolean BACKGROUND_DEFAULT = true;
	protected static final int DEFAULT_JSON_ID = 1;
	private static String extendRpcPath = "extend/configed";
	private static final List<String> collectedCalls = new ArrayList<>();
	public static int maxCollectSize = -1;

	private Map<String, Object> theCall;
	private String methodname;

	private Object[] parameters;

	private boolean background;

	private String rpcPath = "";

	/**
	 * @param rpcPath    subpath for the rpc call (not including "/rpc/")
	 * @param methodName name of rpc method
	 * @param parameters the parameters for the method
	 * @param background if background then no waiting info is shown
	 */
	public OpsiMethodCall(String methodname, Object[] parameters, boolean background) {
		this.methodname = methodname;
		this.parameters = parameters;
		theCall = new HashMap<>();
		theCall.put("method", methodname);
		theCall.put("params", parameters);
		theCall.put("rpcpath", rpcPath);
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
		if ((maxCollectSize < 0) || (maxCollectSize != 0 && collectedCalls.size() >= maxCollectSize)) {
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

	public String getRpcPath() {
		return rpcPath;
	}

	public OpsiMethodCall activateExtendedRpcPath() {
		Logging.info(this, "activateExtendedRpcPath");
		rpcPath = extendRpcPath;
		return this;
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
		sb.append("rpcpath=");
		sb.append(rpcPath);
		sb.append(", ");
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

	public String getCommandLineString() {
		StringBuilder sb = new StringBuilder();
		sb.append(methodname);
		sb.append("(");
		if (parameters.length > 0) {
			sb.append(parameters[0].toString());
			for (int i = 1; i < parameters.length; i++) {
				sb.append("&");

				sb.append(parameters[i].toString());
			}
		}
		sb.append(")");

		return sb.toString();
	}

	public String getJsonString() {
		String result = "";
		try {
			JSONObject jO = new JSONObject();

			JSONArray joParams = new JSONArray();

			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] instanceof Object[]) {
					Object[] obs = (Object[]) parameters[i];
					JSONArray arr = new JSONArray();
					for (int j = 0; j < obs.length; j++) {
						arr.put(obs[j]);
					}

					joParams.put(arr);
				} else if (parameters[i] instanceof Map) {
					JSONObject job = new JSONObject((Map<?, ?>) parameters[i]);
					joParams.put(job);
				} else {
					joParams.put(parameters[i]);
				}

			}

			jO.put("id", DEFAULT_JSON_ID);
			jO.put("method", methodname);
			jO.put("params", joParams);
			result = jO.toString();

		} catch (JSONException jex) {
			Logging.error(this, "Exception while producing a JSONObject, " + jex.toString());
		}

		return result;
	}
}
