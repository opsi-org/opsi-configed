/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.ConfigedMain;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.RPCMethodName;

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
public class UserDataService {
	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;

	public UserDataService(AbstractPOJOExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public boolean usesMultiFactorAuthentication() {
		return cacheManager.getCachedData(CacheIdentifier.MFA_ENABLED, Boolean.class);
	}

	public void checkMultiFactorAuthenticationPD() {
		cacheManager.setCachedData(CacheIdentifier.MFA_ENABLED,
				ServerFacade.isOpsi43() && getOTPSecret(ConfigedMain.getUser()) != null);
	}

	private String getOTPSecret(String userId) {
		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("id", userId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.USER_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> result = exec.getListOfMaps(omc);

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

	public String getOpsiCACert() {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GET_OPSI_CA_CERT, new Object[0]);
		return exec.getStringResult(omc);
	}
}
