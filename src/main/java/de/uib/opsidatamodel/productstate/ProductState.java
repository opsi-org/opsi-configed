/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.productstate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;
import de.uib.utils.logging.Logging;

public class ProductState extends HashMap<String, String> {
	private static ProductState defaultProductState;

	private static final List<String> SERVICE_KEYS = List.of("modificationTime", "productId", "productVersion",
			"packageVersion", "targetConfiguration", "lastAction", "installationStatus", "actionRequest",
			"actionProgress", "actionResult", "priority", "actionSequence");

	// directly taken values
	public static final String KEY_LAST_STATE_CHANGE = "modificationTime";
	public static final String KEY_PRODUCT_VERSION = "productVersion";
	public static final String KEY_PACKAGE_VERSION = "packageVersion";
	public static final String KEY_TARGET_CONFIGURATION = TargetConfiguration.KEY;
	public static final String KEY_LAST_ACTION = LastAction.KEY;
	public static final String KEY_INSTALLATION_STATUS = InstallationStatus.KEY;
	public static final String KEY_ACTION_REQUEST = ActionRequest.KEY;
	public static final String KEY_ACTION_PROGRESS = ActionProgress.KEY;
	public static final String KEY_ACTION_RESULT = ActionResult.KEY;
	public static final String KEY_PRODUCT_ID = "productId";

	public static final String KEY_PRODUCT_PRIORITY = "priority";
	public static final String KEY_ACTION_SEQUENCE = "actionSequence";

	// transformed values
	public static final String KEY_INSTALLATION_INFO = "installationInfo";
	public static final String KEY_VERSION_INFO = "versionInfo";

	// additional values
	public static final String KEY_PRODUCT_NAME = "productName";

	public static final List<String> KEYS = List.of(KEY_PRODUCT_ID, KEY_PRODUCT_NAME, KEY_TARGET_CONFIGURATION,
			KEY_INSTALLATION_STATUS, KEY_INSTALLATION_INFO, KEY_ACTION_RESULT, KEY_ACTION_PROGRESS, KEY_LAST_ACTION,
			KEY_PRODUCT_PRIORITY, KEY_ACTION_SEQUENCE, KEY_ACTION_REQUEST, KEY_VERSION_INFO, KEY_PRODUCT_VERSION,
			KEY_PACKAGE_VERSION, KEY_LAST_STATE_CHANGE);

	private final Map<String, String> retrieved;

	public ProductState(Map<String, String> retrievedState, boolean transform) {
		super();
		this.retrieved = retrievedState;
		if (retrieved == null) {
			setDefaultValues();
		} else {
			readRetrieved();
		}

		if (transform) {
			setTransforms();
		}
	}

	public ProductState(Map<String, String> retrievedState) {
		this(retrievedState, true);
	}

	public static ProductState getDefaultProductState() {
		if (defaultProductState == null) {
			defaultProductState = new ProductState(null);
		}

		return defaultProductState;
	}

	private void readRetrieved() {
		put(KEY_PRODUCT_ID, getRetrievedValue(KEY_PRODUCT_ID));

		put(KEY_TARGET_CONFIGURATION, getRetrievedValue(KEY_TARGET_CONFIGURATION));
		put(KEY_INSTALLATION_STATUS, getRetrievedValue(KEY_INSTALLATION_STATUS));

		put(KEY_ACTION_RESULT, getRetrievedValue(KEY_ACTION_RESULT));
		put(KEY_ACTION_PROGRESS, getRetrievedValue(KEY_ACTION_PROGRESS));
		put(KEY_LAST_ACTION, getRetrievedValue(KEY_LAST_ACTION));

		put(KEY_ACTION_REQUEST, getRetrievedValue(KEY_ACTION_REQUEST));

		put(KEY_PRODUCT_PRIORITY, getRetrievedValue(KEY_PRODUCT_PRIORITY));
		put(KEY_ACTION_SEQUENCE, getRetrievedValue(KEY_ACTION_SEQUENCE));

		put(KEY_PRODUCT_VERSION, getRetrievedValue(KEY_PRODUCT_VERSION));
		put(KEY_PACKAGE_VERSION, getRetrievedValue(KEY_PACKAGE_VERSION));

		put(KEY_LAST_STATE_CHANGE, getRetrievedValue(KEY_LAST_STATE_CHANGE));
	}

	@Override
	public String put(String key, String value) {
		if (KEYS.indexOf(key) < 0) {
			Logging.error(this, "key ", key, " not known, value was ", value, " , ", KEYS);
			return null;
		} else {
			return super.put(key, value);
		}
	}

	private void setTransforms() {
		// transformed values
		StringBuilder installationInfo = new StringBuilder();
		// the reverse will be found in in setInstallationInfo in
		// InstallationStateTableModel

		LastAction lastAction = LastAction.produceFromLabel(get(KEY_LAST_ACTION));

		if (!get(KEY_ACTION_PROGRESS).isEmpty()) {
			ActionResult result = ActionResult.produceFromLabel(get(KEY_ACTION_RESULT));
			if (result.getVal() == ActionResult.FAILED) {
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(": ");
			}

			installationInfo.append(get(KEY_ACTION_PROGRESS));
			installationInfo.append(" ( ");
			if (lastAction.getVal() > 0) {
				installationInfo.append(ActionRequest.getDisplayLabel(lastAction.getVal()));
			}

			installationInfo.append(" ) ");

			if (result.getVal() == ActionResult.FAILED) {
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(" ");
			}
		} else {
			ActionResult result = ActionResult.produceFromLabel(get(KEY_ACTION_RESULT));
			if (result.getVal() == ActionResult.SUCCESSFUL || result.getVal() == ActionResult.FAILED) {
				installationInfo.append("");
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
			}
			// else

			if (lastAction.getVal() > 0) {
				installationInfo.append(" (");
				installationInfo.append(ActionRequest.getDisplayLabel(lastAction.getVal()));
				installationInfo.append(")");
			}
		}

		put(KEY_INSTALLATION_INFO, installationInfo.toString());

		String versionInfo = "";

		if (!get(KEY_PRODUCT_VERSION).isEmpty()) {
			versionInfo = get(KEY_PRODUCT_VERSION) + ProductDataService.FOR_DISPLAY + get(KEY_PACKAGE_VERSION);
		}

		put(KEY_VERSION_INFO, versionInfo);
	}

	private void setDefaultValues() {
		put(KEY_PRODUCT_ID, "");
		put(KEY_PRODUCT_NAME, "");

		put(KEY_TARGET_CONFIGURATION, "undefined");
		put(KEY_INSTALLATION_STATUS, InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED));

		put(KEY_ACTION_RESULT, LastAction.getLabel(ActionResult.NONE));
		put(KEY_ACTION_PROGRESS, "");
		put(KEY_LAST_ACTION, LastAction.getLabel(LastAction.NONE));

		put(KEY_ACTION_REQUEST, ActionRequest.getLabel(ActionRequest.NONE));

		put(KEY_PRODUCT_PRIORITY, "");
		put(KEY_ACTION_SEQUENCE, "");

		put(KEY_PRODUCT_VERSION, "");
		put(KEY_PACKAGE_VERSION, "");

		put(KEY_LAST_STATE_CHANGE, "");
	}

	private String getRetrievedValue(String key) {
		if (SERVICE_KEYS.indexOf(key) < 0) {
			Logging.warning("service key ", key, " not known");
			return "";
		}

		if (retrieved.get(key) == null || (retrieved.get(key) instanceof String && "null".equals(retrieved.get(key)))) {
			return "";
		}

		return retrieved.get(key);
	}
}
