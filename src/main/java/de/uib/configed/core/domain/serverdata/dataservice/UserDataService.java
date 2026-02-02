/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.RPCMethodName;

/**
 * Provides methods for working with user data on the server.
 * <p>
 * Classes ending in {@code DataService} represent somewhat of a layer between
 * server and the client. It enables to work with specific data, that is saved
 * on the server.
 * <p>
 * {@code DataService} classes only allow to retrieve and update data. Data may
 * be internally cached. The internally cached data is identified by a method
 * name. If a method name ends in {@code PD}, it means that method either
 * retrieves or it updates internally cached data. {@code PD} stands for
 * {@code Persistent Data}.
 */
public class UserDataService extends DataService {
	public UserDataService(DataServices dataServices) {
		super(dataServices);
	}

	public boolean usesMultiFactorAuthentication() {
		return dataServices.cacheManager.getCachedData(CacheIdentifier.MFA_ENABLED, Boolean.class);
	}

	public void checkMultiFactorAuthenticationPD(String user) {
		String otpSecret = getOTPSecret(user);
		dataServices.cacheManager.setCachedData(CacheIdentifier.MFA_ENABLED,
				(otpSecret != null && !otpSecret.isEmpty()));
	}

	private String getOTPSecret(String userId) {
		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("id", userId);
		List<Map<String, Object>> result = dataServices.exec.getListOfMaps(RPCMethodName.USER_GET_OBJECTS,
				callAttributes, callFilter);

		if (result.isEmpty()) {
			return null;
		}

		Map<String, Object> userDetails = result.get(0);
		String otpSecret = null;
		if (userDetails.containsKey("otpSecret")) {
			otpSecret = (String) userDetails.get("otpSecret");
		}

		return otpSecret;
	}

	public String getCACerts() {
		return dataServices.exec.getStringResult(RPCMethodName.GET_CA_CERTS);
	}
}
