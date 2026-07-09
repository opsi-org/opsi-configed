/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.productstate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.dataservice.ProductDataService;

public final class ProductState {
	private static Map<String, String> defaultProductState;

	// directly taken values
	public static final String KEY_LAST_STATE_CHANGE = "modificationTime";
	public static final String KEY_PRODUCT_VERSION = "productVersion";
	public static final String KEY_PACKAGE_VERSION = "packageVersion";
	public static final String KEY_LAST_ACTION = LastAction.KEY;
	public static final String KEY_INSTALLATION_STATUS = InstallationStatus.KEY;
	public static final String KEY_ACTION_REQUEST = ActionRequest.KEY;
	public static final String KEY_ACTION_PROGRESS = "actionProgress";
	public static final String KEY_ACTION_RESULT = ActionResult.KEY;
	public static final String KEY_PRODUCT_ID = "productId";

	public static final String KEY_PRODUCT_PRIORITY = "priority";
	public static final String KEY_ACTION_SEQUENCE = "actionSequence";

	// transformed info
	public static final String KEY_VERSION_INFO = "versionInfo";

	// additional values
	public static final String KEY_PRODUCT_NAME = "productName";

	public static final List<String> KEYS = List.of(KEY_PRODUCT_ID, KEY_PRODUCT_NAME, KEY_INSTALLATION_STATUS,
			KEY_ACTION_RESULT, KEY_ACTION_PROGRESS, KEY_LAST_ACTION, KEY_PRODUCT_PRIORITY, KEY_ACTION_SEQUENCE,
			KEY_ACTION_REQUEST, KEY_VERSION_INFO, KEY_PRODUCT_VERSION, KEY_PACKAGE_VERSION, KEY_LAST_STATE_CHANGE);

	// Empty constructor to prevent instantiation
	private ProductState() {
	}

	public static Map<String, String> createDefaultProductState() {
		Map<String, String> productState = new HashMap<>();
		productState.put(KEY_PRODUCT_ID, "");
		productState.put(KEY_PRODUCT_NAME, "");

		productState.put(KEY_INSTALLATION_STATUS, InstallationStatus.NOT_INSTALLED.getLabel());

		productState.put(KEY_ACTION_RESULT, ActionResult.NONE.getLabel());
		productState.put(KEY_ACTION_PROGRESS, "");
		productState.put(KEY_LAST_ACTION, LastAction.NONE.getLabel());

		productState.put(KEY_ACTION_REQUEST, ActionRequest.NONE.getLabel());

		productState.put(KEY_PRODUCT_PRIORITY, "");
		productState.put(KEY_ACTION_SEQUENCE, "");

		productState.put(KEY_PRODUCT_VERSION, "");
		productState.put(KEY_PACKAGE_VERSION, "");

		productState.put(KEY_LAST_STATE_CHANGE, "");

		calculateProductVersion(productState);

		return productState;
	}

	public static Map<String, String> calculateProductVersion(Map<String, String> productState) {
		String versionInfo = "";

		if (!productState.get(KEY_PRODUCT_VERSION).isEmpty()) {
			versionInfo = productState.get(KEY_PRODUCT_VERSION) + ProductDataService.FOR_DISPLAY
					+ productState.get(KEY_PACKAGE_VERSION);
		}

		productState.put(KEY_VERSION_INFO, versionInfo);

		return productState;
	}

	public static Map<String, String> getDefaultProductState() {
		if (defaultProductState == null) {
			defaultProductState = createDefaultProductState();
		}

		return defaultProductState;
	}
}
