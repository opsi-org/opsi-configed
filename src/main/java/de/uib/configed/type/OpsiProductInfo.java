/**
 *   OpsiProductInfo
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 *    
 *  copyright:  2014
 *  organization: uib.de
 *  @author  R. Roeder 
 */

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.logging.Logging;

//data source product table
public class OpsiProductInfo extends OpsiPackage {
	public static final int NO_PRIORITY = -101;

	public static final String SERVICE_KEY_PRIORITY = "priority";
	public static final String SERVICE_KEY_USER_LOGIN_SCRIPT = "userLoginScript";
	public static final String SERVICE_KEY_PRODUCT_ADVICE = "advice";
	public static final String SERVICE_KEY_PRODUCT_DESCRIPTION = "description";
	public static final String SERVICE_KEY_PRODUCT_NAME = "name";

	private List<String> possibleActions;
	private String productName;
	private String description;
	private String advice;
	private boolean hasUserLoginScript;
	private Integer priority;

	public OpsiProductInfo(Map<String, Object> m) {
		super(m);

		possibleActions = new ArrayList<>();
		possibleActions.add(ActionRequest.getState2Label().get(ActionRequest.NONE));
		// keys are the possible script types
		for (String scriptKey : ActionRequest.getScriptKeys()) {
			if (m.get(scriptKey) != null && !("" + m.get(scriptKey)).isEmpty()) {
				possibleActions.add(ActionRequest.getScriptKey2Label().get(scriptKey));
			}
		}

		productName = "";
		if (m.get(SERVICE_KEY_PRODUCT_NAME) != null) {
			productName = "" + m.get(SERVICE_KEY_PRODUCT_NAME);
		}

		description = "";
		if (m.get(SERVICE_KEY_PRODUCT_DESCRIPTION) != null) {
			description = "" + m.get(SERVICE_KEY_PRODUCT_DESCRIPTION);
		}

		advice = "";
		if (m.get(SERVICE_KEY_PRODUCT_ADVICE) != null) {
			advice = "" + m.get(SERVICE_KEY_PRODUCT_ADVICE);
		}

		try {
			priority = Integer.valueOf("" + m.get(SERVICE_KEY_PRIORITY));
		} catch (NumberFormatException ex) {
			Logging.info(this, "no priority " + m.get(SERVICE_KEY_PRIORITY));
		}

		hasUserLoginScript = m.get(SERVICE_KEY_USER_LOGIN_SCRIPT) != null
				&& !("" + m.get(SERVICE_KEY_USER_LOGIN_SCRIPT)).isEmpty();

		Logging.debug(this, "created with description " + description + "\n," + " possible actions " + possibleActions
				+ ", hasUserLoginScript " + hasUserLoginScript);

	}

	public List<String> getPossibleActions() {
		return possibleActions;
	}

	public boolean hasLoginScript() {
		return hasUserLoginScript;
	}

	public String getProductName() {
		return productName;
	}

	public String getDescription() {
		return description;
	}

	public String getAdvice() {
		return advice;
	}

	public int getPriority() {
		if (priority == null) {
			return NO_PRIORITY;
		} else {
			return priority;
		}
	}

	@Override
	public List<Object> appendValues(List<Object> row) {
		row.add(getProductName());
		return row;
	}

	@Override
	public String toString() {
		return getClass().getName() + "{productId=" + productId + ";productName=" + productName + ";description="
				+ description + ";possibleActions" + possibleActions + ";hasUserLoginScript=" + hasUserLoginScript
				+ ";priority=" + priority + ";description=" + description + ";advice=" + advice + "}";
	}
}
